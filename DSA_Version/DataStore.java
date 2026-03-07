import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static final String USERS = "users.txt";
    private static final String DESTINATIONS = "destinations.txt";
    private static final String HOTELS = "hotels.txt";
    private static final String BOOKINGS = "bookings.txt";
    private static final String WAITING = "waitinglist.txt";

    public void initializeFiles() {
        create(USERS);
        create(DESTINATIONS);
        create(HOTELS);
        create(BOOKINGS);
        create(WAITING);
    }

    private void create(String path) {
        File f = new File(path);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                System.out.println("Could not create " + path);
            }
        }
    }

    public List<Models.User> readUsers() {
        List<Models.User> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length >= 4) {
                    list.add(new Models.User(toInt(p[0]), p[1].trim(), p[2].trim(), p[3].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users.");
        }
        return list;
    }

    public void writeUsers(List<Models.User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS))) {
            for (Models.User u : users) {
                bw.write(u.toFile());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing users.");
        }
    }

    public List<Models.Destination> readDestinations() {
        List<Models.Destination> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DESTINATIONS))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length >= 2) {
                    list.add(new Models.Destination(toInt(p[0]), p[1].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading destinations.");
        }
        return list;
    }

    public List<Models.Hotel> readHotels() {
        List<Models.Hotel> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(HOTELS))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length >= 5) {
                    list.add(new Models.Hotel(toInt(p[0]), toInt(p[1]), p[2].trim(), toDouble(p[3]), toInt(p[4])));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading hotels.");
        }
        return list;
    }

    public void writeHotels(List<Models.Hotel> hotels) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HOTELS))) {
            for (Models.Hotel h : hotels) {
                bw.write(h.toFile());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing hotels.");
        }
    }

    public List<Models.Booking> readBookings() {
        List<Models.Booking> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKINGS))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length >= 10) {
                    list.add(new Models.Booking(toInt(p[0]), toInt(p[1]), toInt(p[2]), toInt(p[3]), p[4].trim(),
                            p[5].trim(), p[6].trim(), p[7].trim(), toDouble(p[8]), p[9].trim()));
                } else if (p.length >= 5) {
                    list.add(new Models.Booking(toInt(p[0]), toInt(p[1]), toInt(p[2]), toInt(p[3]), "", "", "", "",
                            0.0, p[4].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading bookings.");
        }
        return list;
    }

    public void writeBookings(List<Models.Booking> bookings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKINGS))) {
            for (Models.Booking b : bookings) {
                bw.write(b.toFile());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing bookings.");
        }
    }

    public List<Models.WaitingEntry> readWaitingList() {
        List<Models.WaitingEntry> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(WAITING))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length >= 4) {
                    list.add(new Models.WaitingEntry(toInt(p[0]), toInt(p[1]), toInt(p[2]), toInt(p[3])));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading waiting list.");
        }
        return list;
    }

    public void writeWaitingList(List<Models.WaitingEntry> waiting) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(WAITING))) {
            for (Models.WaitingEntry w : waiting) {
                bw.write(w.toFile());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing waiting list.");
        }
    }

    public int nextUserId() {
        int max = 0;
        for (Models.User u : readUsers()) {
            max = Math.max(max, u.userId);
        }
        return max + 1;
    }

    public int nextBookingId() {
        int max = 0;
        for (Models.Booking b : readBookings()) {
            max = Math.max(max, b.bookingId);
        }
        return max + 1;
    }

    private int toInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double toDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
