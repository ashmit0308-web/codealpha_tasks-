import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

class Main {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    enum Category { STANDARD, DELUXE, SUITE }

    
    static class Room {
        final int roomNumber;
        final Category category;
        final double pricePerNight;
        boolean booked;

        Room(int roomNumber, Category category, double pricePerNight) {
            this.roomNumber = roomNumber;
            this.category = category;
            this.pricePerNight = pricePerNight;
            this.booked = false;
        }
    }
    static class Reservation {
        final int reservationId;
        final String guestName;
        final String phoneNumber;
        final Room room;
        final LocalDate checkIn;
        final LocalDate checkOut;
        final int nights;
        final double totalPrice;
        boolean paid;

        Reservation(int reservationId, String guestName, String phoneNumber, Room room,
                    LocalDate checkIn, LocalDate checkOut) {
            this.reservationId = reservationId;
            this.guestName = guestName;
            this.phoneNumber = phoneNumber;
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
            this.totalPrice = room.pricePerNight * this.nights;
            this.paid = false;
        }
    }

    static class Hotel {
        final List<Room> rooms = new ArrayList<>();
        final List<Reservation> reservations = new ArrayList<>();
        int nextReservationId = 1;

        Hotel() {
            int roomNum = 100;
            for (int i = 0; i < 5; i++) rooms.add(new Room(roomNum++, Category.STANDARD, 80.0));
            for (int i = 0; i < 3; i++) rooms.add(new Room(roomNum++, Category.DELUXE, 140.0));
            for (int i = 0; i < 2; i++) rooms.add(new Room(roomNum++, Category.SUITE, 250.0));
        }

        List<Room> getAvailableRooms(Category category) {
            List<Room> available = new ArrayList<>();
            for (Room r : rooms) {
                if (!r.booked && (category == null || r.category == category)) {
                    available.add(r);
                }
            }
            return available;
        }

        Room findRoomByNumber(int roomNumber) {
            for (Room r : rooms) if (r.roomNumber == roomNumber) return r;
            return null;
        }

        Reservation findReservation(int id) {
            for (Reservation r : reservations) if (r.reservationId == id) return r;
            return null;
        }

        Reservation book(String guestName, String phone, Room room, LocalDate checkIn, LocalDate checkOut) {
            room.booked = true;
            Reservation res = new Reservation(nextReservationId++, guestName, phone, room, checkIn, checkOut);
            reservations.add(res);
            return res;
        }

        boolean cancel(int reservationId) {
            Reservation res = findReservation(reservationId);
            if (res == null) return false;
            res.room.booked = false;
            reservations.remove(res);
            return true;
        }
    }

    private final Hotel hotel = new Hotel();
    private final Scanner scanner = new Scanner(System.in);
    private static final String SAVE_FILE = "bookings.txt";
    public static void main(String[] args) {
    new Main().run();
}

    private void run() {
        loadBookings();
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> searchAvailableRooms();
                case 2 -> bookRoom();
                case 3 -> cancelReservation();
                case 4 -> viewReservationDetails();
                case 5 -> processPayment();
                case 6 -> viewAllReservations();
                case 7 -> occupancyReport();
                case 8 -> {
                    saveBookings();
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }

    private void printBanner() {
        System.out.println("=========================================");
        System.out.println("   HOTEL RESERVATION SYSTEM - CodeAlpha");
        System.out.println("=========================================");
    }

    private void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Search Available Rooms");
        System.out.println("2. Book a Room");
        System.out.println("3. Cancel a Reservation");
        System.out.println("4. View Reservation Details");
        System.out.println("5. Process Payment (Generates Receipt)");
        System.out.println("6. View All Reservations");
        System.out.println("7. Room Occupancy Report");
        System.out.println("8. Save & Exit");
    }

    private void searchAvailableRooms() {
        System.out.println("Filter by category? 1=Standard 2=Deluxe 3=Suite 0=All");
        int choice = readInt("Choice: ");
        Category category = switch (choice) {
            case 1 -> Category.STANDARD;
            case 2 -> Category.DELUXE;
            case 3 -> Category.SUITE;
            default -> null;
        };
        List<Room> available = hotel.getAvailableRooms(category);
        System.out.println("\n===== AVAILABLE ROOMS =====");
        if (available.isEmpty()) {
            System.out.println("No available rooms in that category.");
            return;
        }
        System.out.printf("%-10s %-10s %-12s%n", "Room #", "Category", "Price/Night");
        for (Room r : available) {
            System.out.printf("%-10d %-10s $%-11.2f%n", r.roomNumber, r.category, r.pricePerNight);
        }
    }

    private void bookRoom() {
        searchAvailableRooms();
        int roomNumber = readInt("Enter room number to book: ");
        Room room = hotel.findRoomByNumber(roomNumber);
        if (room == null || room.booked) {
            System.out.println("That room is not available.");
            return;
        }
        System.out.print("Enter guest name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Guest name cannot be empty.");
            return;
        }
        System.out.print("Enter guest phone number: ");
        String phone = scanner.nextLine().trim();

        if (!phone.matches("\\d{10}")) {
            System.out.println("Phone number must be exactly 10 digits.");
            return;
        }
        LocalDate checkIn = readDate("Enter check-in date (dd-MM-yyyy): ");
        LocalDate checkOut = readDate("Enter check-out date (dd-MM-yyyy): ");
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            System.out.println("Invalid dates. Check-out must be after check-in.");
            return;
        }
        Reservation res = hotel.book(name, phone, room, checkIn, checkOut);
        System.out.printf("Booked! Reservation ID: %d | Room: %d | %d night(s) | Total: $%.2f (unpaid)%n",
                res.reservationId, room.roomNumber, res.nights, res.totalPrice);
    }

    private void cancelReservation() {
        int id = readInt("Enter reservation ID to cancel: ");
        boolean success = hotel.cancel(id);
        System.out.println(success ? "Reservation cancelled." : "Reservation not found.");
    }

    private void viewReservationDetails() {
        int id = readInt("Enter reservation ID: ");
        Reservation res = hotel.findReservation(id);
        if (res == null) {
            System.out.println("Reservation not found.");
            return;
        }
        printReservation(res);
    }

    private void processPayment() {
        int id = readInt("Enter reservation ID to pay for: ");
        Reservation res = hotel.findReservation(id);
        if (res == null) {
            System.out.println("Reservation not found.");
            return;
        }
        if (res.paid) {
            System.out.println("This reservation is already paid.");
            return;
        }
        System.out.printf("Amount due: $%.2f%n", res.totalPrice);
        System.out.print("Simulate payment now? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            res.paid = true;
            System.out.println("Payment successful (simulated). Reservation confirmed.");
            printReceipt(res);
        } else {
            System.out.println("Payment cancelled.");
        }
    }

    private void printReceipt(Reservation res) {
        System.out.println("\n" + "=".repeat(42));
        System.out.println("           BOOKING RECEIPT");
        System.out.println("=".repeat(42));
        System.out.printf("Reservation ID : %d%n", res.reservationId);
        System.out.printf("Guest Name     : %s%n", res.guestName);
        System.out.printf("Phone Number   : %s%n", res.phoneNumber);
        System.out.printf("Room Number    : %d (%s)%n", res.room.roomNumber, res.room.category);
        System.out.printf("Check-in       : %s%n", res.checkIn.format(DATE_FORMAT));
        System.out.printf("Check-out      : %s%n", res.checkOut.format(DATE_FORMAT));
        System.out.printf("Nights Stayed  : %d%n", res.nights);
        System.out.printf("Rate/Night     : $%.2f%n", res.room.pricePerNight);
        System.out.println("-".repeat(42));
        System.out.printf("TOTAL PAID     : $%.2f%n", res.totalPrice);
        System.out.println("=".repeat(42));
        System.out.println("      Thank you for staying with us!");
        System.out.println("=".repeat(42));
    }

    private void viewAllReservations() {
        System.out.println("\n===== ALL RESERVATIONS =====");
        if (hotel.reservations.isEmpty()) {
            System.out.println("No reservations yet.");
            return;
        }
        for (Reservation res : hotel.reservations) {
            printReservation(res);
            System.out.println("-----------------------------");
        }
    }

    private void occupancyReport() {
        System.out.println("\n===== ROOM OCCUPANCY REPORT =====");
        int totalRooms = hotel.rooms.size();
        int bookedRooms = 0;
        Map<Category, int[]> categoryStats = new EnumMap<>(Category.class); // [total, booked]
        for (Category c : Category.values()) categoryStats.put(c, new int[2]);

        for (Room r : hotel.rooms) {
            categoryStats.get(r.category)[0]++;
            if (r.booked) {
                bookedRooms++;
                categoryStats.get(r.category)[1]++;
            }
        }

        System.out.printf("%-10s %-8s %-8s %-12s%n", "Category", "Total", "Booked", "Occupancy %");
        for (Category c : Category.values()) {
            int[] stats = categoryStats.get(c);
            double pct = stats[0] == 0 ? 0 : (stats[1] * 100.0 / stats[0]);
            System.out.printf("%-10s %-8d %-8d %-12.1f%n", c, stats[0], stats[1], pct);
        }
        System.out.println("-".repeat(40));
        double overallPct = totalRooms == 0 ? 0 : (bookedRooms * 100.0 / totalRooms);
        System.out.printf("TOTAL: %d/%d rooms occupied (%.1f%%)%n", bookedRooms, totalRooms, overallPct);
    }

    private void printReservation(Reservation res) {
        System.out.printf("ID: %d | Guest: %s | Phone: %s | Room: %d (%s) | %s to %s | Nights: %d | Total: $%.2f | Paid: %s%n",
                res.reservationId, res.guestName, res.phoneNumber, res.room.roomNumber, res.room.category,
                res.checkIn.format(DATE_FORMAT), res.checkOut.format(DATE_FORMAT),
                res.nights, res.totalPrice, res.paid ? "Yes" : "No");
    }

    private void saveBookings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            writer.println("NEXT_ID," + hotel.nextReservationId);
            for (Reservation res : hotel.reservations) {
                writer.println("RES," + res.reservationId + "," + res.guestName + "," + res.phoneNumber + "," +
                        res.room.roomNumber + "," + res.checkIn + "," + res.checkOut + "," + res.paid);
            }
        } catch (IOException e) {
            System.out.println("Could not save bookings: " + e.getMessage());
        }
    }

    private void loadBookings() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("NEXT_ID")) {
                    hotel.nextReservationId = Integer.parseInt(parts[1]);
                } else if (parts[0].equals("RES")) {
                    int id = Integer.parseInt(parts[1]);
                    String guest = parts[2];
                    String phone = parts[3];
                    int roomNum = Integer.parseInt(parts[4]);
                    LocalDate checkIn = LocalDate.parse(parts[5]);
                    LocalDate checkOut = LocalDate.parse(parts[6]);
                    boolean paid = Boolean.parseBoolean(parts[7]);
                    Room room = hotel.findRoomByNumber(roomNum);
                    if (room != null) {
                        room.booked = true;
                        Reservation res = new Reservation(id, guest, phone, room, checkIn, checkOut);
                        res.paid = paid;
                        hotel.reservations.add(res);
                    }
                }
            }
            System.out.println("Loaded saved bookings from " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Could not load bookings: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return LocalDate.parse(input, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            System.out.println("Invalid date format.");
            return null;
        }
    }
}
