import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DSA {
    private DSA() {
    }

    // Linked list for destinations.
    public static class DestinationLinkedList {
        private static class Node {
            Models.Destination data;
            Node next;

            Node(Models.Destination data) {
                this.data = data;
            }
        }

        private Node head;

        public void add(Models.Destination destination) {
            Node node = new Node(destination);
            if (head == null) {
                head = node;
                return;
            }
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = node;
        }

        public List<Models.Destination> toList() {
            List<Models.Destination> out = new ArrayList<>();
            Node current = head;
            while (current != null) {
                out.add(current.data);
                current = current.next;
            }
            return out;
        }

        public static DestinationLinkedList fromList(List<Models.Destination> destinations) {
            DestinationLinkedList list = new DestinationLinkedList();
            for (Models.Destination d : destinations) {
                list.add(d);
            }
            return list;
        }
    }

    // Queue for FCFS booking requests.
    public static class BookingQueue {
        private static class Node {
            Models.Booking booking;
            Node next;

            Node(Models.Booking booking) {
                this.booking = booking;
            }
        }

        private Node front;
        private Node rear;

        public void enqueue(Models.Booking booking) {
            Node node = new Node(booking);
            if (rear == null) {
                front = rear = node;
            } else {
                rear.next = node;
                rear = node;
            }
        }

        public Models.Booking dequeue() {
            if (front == null) {
                return null;
            }
            Models.Booking b = front.booking;
            front = front.next;
            if (front == null) {
                rear = null;
            }
            return b;
        }
    }

    // Stack for recent booking undo.
    public static class BookingStack {
        private static class Node {
            Models.Booking booking;
            Node next;

            Node(Models.Booking booking) {
                this.booking = booking;
            }
        }

        private Node top;

        public void push(Models.Booking booking) {
            Node n = new Node(booking);
            n.next = top;
            top = n;
        }

        public Models.Booking pop() {
            if (top == null) {
                return null;
            }
            Models.Booking b = top.booking;
            top = top.next;
            return b;
        }
    }

    // Hash table for user lookup by email.
    public static class UserHashTable {
        private static class Entry {
            String key;
            Models.User value;

            Entry(String key, Models.User value) {
                this.key = key;
                this.value = value;
            }
        }

        private final List<List<Entry>> buckets;
        private final int capacity;

        public UserHashTable(int capacity) {
            this.capacity = Math.max(10, capacity);
            this.buckets = new ArrayList<>();
            for (int i = 0; i < this.capacity; i++) {
                buckets.add(new ArrayList<>());
            }
        }

        private int index(String key) {
            return Math.abs(key.toLowerCase().hashCode()) % capacity;
        }

        public void put(String key, Models.User value) {
            List<Entry> bucket = buckets.get(index(key));
            for (Entry e : bucket) {
                if (e.key.equalsIgnoreCase(key)) {
                    e.value = value;
                    return;
                }
            }
            bucket.add(new Entry(key, value));
        }

        public Models.User get(String key) {
            List<Entry> bucket = buckets.get(index(key));
            for (Entry e : bucket) {
                if (e.key.equalsIgnoreCase(key)) {
                    return e.value;
                }
            }
            return null;
        }

        public boolean containsKey(String key) {
            return get(key) != null;
        }
    }

    public static List<Models.Destination> linearSearchDestinations(List<Models.Destination> list, String query) {
        List<Models.Destination> out = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Models.Destination d : list) {
            if (d.destinationName.toLowerCase().contains(q)) {
                out.add(d);
            }
        }
        return out;
    }

    public static Models.Hotel binarySearchHotelByName(List<Models.Hotel> sortedHotels, String hotelName) {
        int left = 0;
        int right = sortedHotels.size() - 1;
        String target = hotelName.toLowerCase().trim();

        while (left <= right) {
            int mid = left + (right - left) / 2;
            String current = sortedHotels.get(mid).hotelName.toLowerCase();
            int cmp = current.compareTo(target);
            if (cmp == 0) {
                return sortedHotels.get(mid);
            }
            if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    public static List<Models.Hotel> bubbleSortHotelsByPrice(List<Models.Hotel> hotels) {
        List<Models.Hotel> sorted = new ArrayList<>(hotels);
        int n = sorted.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (sorted.get(j).price > sorted.get(j + 1).price) {
                    Models.Hotel temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        return sorted;
    }

    // Priority queue behavior: higher priority first.
    public static List<Models.WaitingEntry> sortWaitingByPriority(List<Models.WaitingEntry> waiting) {
        List<Models.WaitingEntry> out = new ArrayList<>(waiting);
        out.sort(Comparator.comparingInt((Models.WaitingEntry e) -> e.priority).reversed());
        return out;
    }
}
