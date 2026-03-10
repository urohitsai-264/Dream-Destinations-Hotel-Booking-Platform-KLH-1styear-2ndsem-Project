import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookingSystem {
    private static final int HOTEL_TOTAL_ROOMS = 5;
    private final Scanner scanner = new Scanner(System.in);
    private final DataStore store = new DataStore();
    private final DSA.BookingQueue bookingQueue = new DSA.BookingQueue();
    private final DSA.BookingStack bookingStack = new DSA.BookingStack();

    public void start() {
        store.initializeFiles();
        processWaitingList();
        refreshHotelAvailability();
        System.out.println("===== Dream Destinations Hotel Booking System =====");

        while (true) {
            processWaitingList();
            refreshHotelAvailability();
            System.out.println("\n1. Login");
            System.out.println("2. Signup");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = readInt();

            if (choice == 1) {
                Models.User user = login();
                if (user != null) {
                    bookingJourney(user);
                }
            } else if (choice == 2) {
                signup();
            } else if (choice == 3) {
                System.out.println("Thank you for using the system.");
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void signup() {
        List<Models.User> users = store.readUsers();
        DSA.UserHashTable table = new DSA.UserHashTable(50);
        for (Models.User u : users) {
            table.put(u.email, u);
        }

        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (table.containsKey(email)) {
            System.out.println("Email already exists.");
            return;
        }

        Models.User user = new Models.User(store.nextUserId(), name, email, password);
        users.add(user);
        store.writeUsers(users);
        System.out.println("Signup successful.");
    }

    private Models.User login() {
        List<Models.User> users = store.readUsers();
        DSA.UserHashTable table = new DSA.UserHashTable(50);
        for (Models.User u : users) {
            table.put(u.email, u);
        }

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        Models.User user = table.get(email);
        if (user == null || !user.password.equals(password)) {
            System.out.println("Invalid credentials.");
            return null;
        }
        System.out.println("Login successful. Welcome, " + user.name + ".");
        return user;
    }

    private void bookingJourney(Models.User user) {
        System.out.println("\n===== BOOKING FLOW =====");
        Models.Destination destination = chooseDestination();
        if (destination == null) {
            return;
        }

        Models.Hotel hotel = chooseHotel(destination.destinationId);
        if (hotel == null) {
            return;
        }

        int rooms = readPositiveInt("Enter rooms to book: ");
        LocalDate checkIn = readDate("Enter check-in date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Enter check-out date (YYYY-MM-DD): ");
        while (!checkOut.isAfter(checkIn)) {
            System.out.println("Check-out must be after check-in.");
            checkOut = readDate("Enter check-out date (YYYY-MM-DD): ");
        }

        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        double totalAmount = hotel.price * rooms * nights;
        List<Models.Booking> allBookings = store.readBookings();
        if (!canAllocateRooms(hotel.hotelId, rooms, checkIn, checkOut, allBookings)) {
            LocalDate nextAvailableDate = findEarliestAvailableCheckIn(
                    hotel.hotelId, rooms, nights, checkIn, allBookings);
            if (nextAvailableDate == null) {
                System.out.println("No rooms are available in this hotel for the next 1 year.");
                return;
            }

            System.out.println("All 5 rooms are already booked for selected dates.");
            System.out.println("Next available check-in date for this hotel: " + nextAvailableDate);
            String choice = readNonEmpty("Do you want to join waiting list for this date? (yes/no): ");
            if (!choice.equalsIgnoreCase("yes")) {
                return;
            }

            LocalDate requestedWaitDate = readDate("Enter check-in date to join waiting list (YYYY-MM-DD): ");
            if (!requestedWaitDate.equals(nextAvailableDate)) {
                System.out.println("Waiting list is allowed only for suggested date: " + nextAvailableDate);
                return;
            }

            String guestName = readNonEmpty("Enter name: ");
            String guestEmail = readNonEmpty("Enter email: ");
            LocalDate waitCheckOut = requestedWaitDate.plusDays(nights);
            Models.Booking waitingBooking = new Models.Booking(
                    store.nextBookingId(),
                    user.userId,
                    hotel.hotelId,
                    rooms,
                    guestName,
                    guestEmail,
                    requestedWaitDate.toString(),
                    waitCheckOut.toString(),
                    totalAmount,
                    "WAITING");

            allBookings.add(waitingBooking);
            List<Models.WaitingEntry> waitingEntries = store.readWaitingList();
            int nextPriority = waitingEntries.size() + 1;
            waitingEntries.add(new Models.WaitingEntry(
                    waitingBooking.bookingId, user.userId, hotel.hotelId, nextPriority));

            store.writeBookings(allBookings);
            store.writeWaitingList(waitingEntries);
            System.out.println("Added to waiting list. Booking ID: " + waitingBooking.bookingId);
            return;
        }

        String guestName = readNonEmpty("Enter name: ");
        String guestEmail = readNonEmpty("Enter email: ");
        System.out.println("\n===== PAYMENT =====");
        readNonEmpty("Card holder name: ");
        readCardNumber();
        readExpiryMonth();
        readCvv();

        Models.Booking request = new Models.Booking(
                store.nextBookingId(),
                user.userId,
                hotel.hotelId,
                rooms,
                guestName,
                guestEmail,
                checkIn.toString(),
                checkOut.toString(),
                totalAmount,
                "PENDING");
        bookingQueue.enqueue(request);
        Models.Booking current = bookingQueue.dequeue();

        allBookings = store.readBookings();
        if (!canAllocateRooms(hotel.hotelId, rooms, checkIn, checkOut, allBookings)) {
            System.out.println("Rooms became unavailable. Please try again.");
            return;
        }

        current.status = "CONFIRMED";

        allBookings.add(current);
        bookingStack.push(current);
        store.writeBookings(allBookings);
        refreshHotelAvailability();

        printConfirmation(current, destination.destinationName, hotel.hotelName, rooms, nights);
        System.out.println("Returning to Home Page...");
    }

    private Models.Destination chooseDestination() {
        List<Models.Destination> destinations = store.readDestinations();
        if (destinations.isEmpty()) {
            System.out.println("No destinations available.");
            return null;
        }

        System.out.println("\nAvailable Destinations:");
        DSA.DestinationLinkedList linkedList = DSA.DestinationLinkedList.fromList(destinations);
        for (Models.Destination d : linkedList.toList()) {
            System.out.println(d.destinationId + ". " + d.destinationName);
        }

        System.out.print("Choose destination ID: ");
        int destinationId = readInt();
        for (Models.Destination d : destinations) {
            if (d.destinationId == destinationId) {
                return d;
            }
        }
        System.out.println("Invalid destination.");
        return null;
    }

    private Models.Hotel chooseHotel(int destinationId) {
        List<Models.Hotel> hotels = filterHotelsByDestination(store.readHotels(), destinationId);
        if (hotels.isEmpty()) {
            System.out.println("No hotels found.");
            return null;
        }
        System.out.println("\nHotels in selected destination:");
        printHotels(hotels);

        System.out.print("Choose hotel ID: ");
        int hotelId = readInt();
        for (Models.Hotel h : hotels) {
            if (h.hotelId == hotelId) {
                return h;
            }
        }
        System.out.println("Invalid hotel.");
        return null;
    }

    private List<Models.Hotel> filterHotelsByDestination(List<Models.Hotel> hotels, int destinationId) {
        List<Models.Hotel> filtered = new java.util.ArrayList<>();
        for (Models.Hotel h : hotels) {
            if (h.destinationId == destinationId) {
                filtered.add(h);
            }
        }
        return filtered;
    }

    private void printHotels(List<Models.Hotel> hotels) {
        List<Models.Booking> bookings = store.readBookings();
        List<Models.Hotel> sortedByPrice = DSA.bubbleSortHotelsByPrice(hotels);
        for (Models.Hotel h : sortedByPrice) {
            printHotel(h, bookings);
        }
    }

    private void printHotel(Models.Hotel h, List<Models.Booking> bookings) {
        LocalDate nextVacancyDate = findNextVacancyDate(h.hotelId, bookings, LocalDate.now());
        int vacantRooms = 0;
        String vacancyDateText = "N/A";
        if (nextVacancyDate != null) {
            vacantRooms = vacantRoomsOnDate(h.hotelId, nextVacancyDate, bookings);
            vacancyDateText = nextVacancyDate.toString();
        }

        System.out.println("HotelID: " + h.hotelId + ", DestinationID: " + h.destinationId
                + ", Name: " + h.hotelName + ", Price: " + h.price + ", Available Rooms: " + h.availableRooms
                + ", Vacant Rooms: " + vacantRooms + " (" + vacancyDateText + ")");
    }

    private Models.Hotel findHotelById(List<Models.Hotel> hotels, int hotelId) {
        for (Models.Hotel h : hotels) {
            if (h.hotelId == hotelId) {
                return h;
            }
        }
        return null;
    }

    private void refreshHotelAvailability() {
        List<Models.Hotel> hotels = store.readHotels();
        List<Models.Booking> bookings = store.readBookings();
        LocalDate today = LocalDate.now();

        for (Models.Hotel hotel : hotels) {
            int occupied = 0;
            for (Models.Booking booking : bookings) {
                if (!"CONFIRMED".equalsIgnoreCase(booking.status) || booking.hotelId != hotel.hotelId) {
                    continue;
                }
                LocalDate checkIn = safeParseDate(booking.checkInDate);
                LocalDate checkOut = safeParseDate(booking.checkOutDate);
                if (checkIn == null || checkOut == null) {
                    continue;
                }
                if (!today.isBefore(checkIn) && today.isBefore(checkOut)) {
                    occupied += booking.roomsBooked;
                }
            }
            hotel.availableRooms = Math.max(0, HOTEL_TOTAL_ROOMS - occupied);
        }
        store.writeHotels(hotels);
    }

    private void processWaitingList() {
        List<Models.WaitingEntry> waiting = store.readWaitingList();
        if (waiting.isEmpty()) {
            return;
        }

        List<Models.Booking> bookings = store.readBookings();
        LocalDate today = LocalDate.now();
        boolean changed = false;
        List<Models.WaitingEntry> remaining = new ArrayList<>();

        waiting.sort(java.util.Comparator.comparingInt(e -> e.priority));
        for (Models.WaitingEntry entry : waiting) {
            Models.Booking waitingBooking = findBookingById(bookings, entry.bookingId);
            if (waitingBooking == null || !"WAITING".equalsIgnoreCase(waitingBooking.status)) {
                changed = true;
                continue;
            }

            LocalDate checkIn = safeParseDate(waitingBooking.checkInDate);
            LocalDate checkOut = safeParseDate(waitingBooking.checkOutDate);
            if (checkIn == null || checkOut == null) {
                remaining.add(entry);
                continue;
            }

            if (!checkIn.isAfter(today)
                    && canAllocateRooms(waitingBooking.hotelId, waitingBooking.roomsBooked, checkIn, checkOut, bookings)) {
                waitingBooking.status = "CONFIRMED";
                changed = true;
            } else {
                remaining.add(entry);
            }
        }

        if (changed) {
            store.writeBookings(bookings);
            store.writeWaitingList(remaining);
        }
    }

    private Models.Booking findBookingById(List<Models.Booking> bookings, int bookingId) {
        for (Models.Booking booking : bookings) {
            if (booking.bookingId == bookingId) {
                return booking;
            }
        }
        return null;
    }

    private boolean canAllocateRooms(int hotelId, int rooms, LocalDate checkIn, LocalDate checkOut, List<Models.Booking> bookings) {
        LocalDate day = checkIn;
        while (day.isBefore(checkOut)) {
            int booked = roomsBookedOnDate(hotelId, day, bookings);
            if (booked + rooms > HOTEL_TOTAL_ROOMS) {
                return false;
            }
            day = day.plusDays(1);
        }
        return true;
    }

    private int roomsBookedOnDate(int hotelId, LocalDate date, List<Models.Booking> bookings) {
        int total = 0;
        for (Models.Booking booking : bookings) {
            if (!"CONFIRMED".equalsIgnoreCase(booking.status) || booking.hotelId != hotelId) {
                continue;
            }
            LocalDate checkIn = safeParseDate(booking.checkInDate);
            LocalDate checkOut = safeParseDate(booking.checkOutDate);
            if (checkIn == null || checkOut == null) {
                continue;
            }
            if (!date.isBefore(checkIn) && date.isBefore(checkOut)) {
                total += booking.roomsBooked;
            }
        }
        return total;
    }

    private int vacantRoomsOnDate(int hotelId, LocalDate date, List<Models.Booking> bookings) {
        return Math.max(0, HOTEL_TOTAL_ROOMS - roomsBookedOnDate(hotelId, date, bookings));
    }

    private LocalDate findNextVacancyDate(int hotelId, List<Models.Booking> bookings, LocalDate fromDate) {
        LocalDate date = fromDate;
        LocalDate limit = fromDate.plusDays(365);

        while (!date.isAfter(limit)) {
            if (vacantRoomsOnDate(hotelId, date, bookings) > 0) {
                return date;
            }
            date = date.plusDays(1);
        }
        return null;
    }

    private LocalDate findEarliestAvailableCheckIn(
            int hotelId, int rooms, long nights, LocalDate fromDate, List<Models.Booking> bookings) {
        LocalDate cursor = fromDate;
        LocalDate limit = fromDate.plusDays(365);

        while (!cursor.isAfter(limit)) {
            LocalDate candidateCheckOut = cursor.plusDays(nights);
            if (canAllocateRooms(hotelId, rooms, cursor, candidateCheckOut, bookings)) {
                return cursor;
            }
            cursor = cursor.plusDays(1);
        }
        return null;
    }

    private LocalDate safeParseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field is required.");
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            int value = readInt();
            if (value > 0) {
                return value;
            }
            System.out.println("Enter a value greater than 0.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String input = readNonEmpty(prompt);
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    private void readCardNumber() {
        while (true) {
            String cardNumber = readNonEmpty("Card number: ");
            if (cardNumber.matches("\\d{12,19}")) {
                return;
            }
            System.out.println("Card number must be 12 to 19 digits.");
        }
    }

    private void readExpiryMonth() {
        while (true) {
            String expiry = readNonEmpty("Expiry (MM/YY): ");
            if (expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                return;
            }
            System.out.println("Use MM/YY format.");
        }
    }

    private void readCvv() {
        while (true) {
            String cvv = readNonEmpty("CVV: ");
            if (cvv.matches("\\d{3,4}")) {
                return;
            }
            System.out.println("CVV must be 3 or 4 digits.");
        }
    }

    private void printConfirmation(Models.Booking booking, String placeName, String hotelName, int rooms, long nights) {
        System.out.println("\n===== BOOKING CONFIRMATION =====");
        System.out.println("Booking ID: " + booking.bookingId);
        System.out.println("Booked By: " + booking.guestName);
        System.out.println("Email: " + booking.guestEmail);
        System.out.println("Place: " + placeName);
        System.out.println("Hotel: " + hotelName);
        System.out.println("Check-in Date: " + booking.checkInDate);
        System.out.println("Check-out Date: " + booking.checkOutDate);
        System.out.println("Rooms: " + rooms);
        System.out.println("Nights: " + nights);
        System.out.println("Total Money: " + booking.totalAmount);
        System.out.println("Status: " + booking.status);
    }

    private int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
