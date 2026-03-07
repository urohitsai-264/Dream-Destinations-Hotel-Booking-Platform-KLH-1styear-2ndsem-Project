public class Models {
    private Models() {
    }

    public static class User {
        public final int userId;
        public final String name;
        public final String email;
        public final String password;

        public User(int userId, String name, String email, String password) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.password = password;
        }

        public String toFile() {
            return userId + "," + name + "," + email + "," + password;
        }
    }

    public static class Destination {
        public final int destinationId;
        public final String destinationName;

        public Destination(int destinationId, String destinationName) {
            this.destinationId = destinationId;
            this.destinationName = destinationName;
        }

        public String toFile() {
            return destinationId + "," + destinationName;
        }
    }

    public static class Hotel {
        public final int hotelId;
        public final int destinationId;
        public final String hotelName;
        public final double price;
        public int availableRooms;

        public Hotel(int hotelId, int destinationId, String hotelName, double price, int availableRooms) {
            this.hotelId = hotelId;
            this.destinationId = destinationId;
            this.hotelName = hotelName;
            this.price = price;
            this.availableRooms = availableRooms;
        }

        public String toFile() {
            return hotelId + "," + destinationId + "," + hotelName + "," + price + "," + availableRooms;
        }
    }

    public static class Booking {
        public final int bookingId;
        public final int userId;
        public final int hotelId;
        public final int roomsBooked;
        public final String guestName;
        public final String guestEmail;
        public final String checkInDate;
        public final String checkOutDate;
        public final double totalAmount;
        public String status;

        public Booking(int bookingId, int userId, int hotelId, int roomsBooked, String status) {
            this(bookingId, userId, hotelId, roomsBooked, "", "", "", "", 0.0, status);
        }

        public Booking(int bookingId, int userId, int hotelId, int roomsBooked, String guestName, String guestEmail,
                String checkInDate, String checkOutDate, double totalAmount, String status) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.hotelId = hotelId;
            this.roomsBooked = roomsBooked;
            this.guestName = guestName;
            this.guestEmail = guestEmail;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.totalAmount = totalAmount;
            this.status = status;
        }

        public String toFile() {
            return bookingId + "," + userId + "," + hotelId + "," + roomsBooked + "," + guestName + "," + guestEmail + ","
                    + checkInDate + "," + checkOutDate + "," + totalAmount + "," + status;
        }
    }

    public static class WaitingEntry {
        public final int bookingId;
        public final int userId;
        public final int hotelId;
        public final int priority;

        public WaitingEntry(int bookingId, int userId, int hotelId, int priority) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.hotelId = hotelId;
            this.priority = priority;
        }

        public String toFile() {
            return bookingId + "," + userId + "," + hotelId + "," + priority;
        }
    }
}
