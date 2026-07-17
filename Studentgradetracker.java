import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

 class Studentgradetracker {
public static void main(String[] args) {
    new Studentgradetracker().run();
}
    

    private static final double PASS_MARK = 40.0;

   
    static class Student {
        private final String id;
        private final String name;
        private final List<Double> grades = new ArrayList<>();

        Student(String id, String name) {
            this.id = id;
            this.name = name;
        }

        void addGrade(double grade) {
            grades.add(grade);
        }

        String getId() { return id; }
        String getName() { return name; }
        int getSubjectCount() { return grades.size(); }

        double getAverage() {
            if (grades.isEmpty()) return 0.0;
            double sum = 0;
            for (double g : grades) sum += g;
            return sum / grades.size();
        }

        double getHighest() {
            if (grades.isEmpty()) return 0.0;
            double max = grades.get(0);
            for (double g : grades) if (g > max) max = g;
            return max;
        }

        double getLowest() {
            if (grades.isEmpty()) return 0.0;
            double min = grades.get(0);
            for (double g : grades) if (g < min) min = g;
            return min;
        }

        String getLetterGrade() {
            double avg = getAverage();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }

        String getStatus() {
            return getAverage() >= PASS_MARK ? "PASS" : "FAIL";
        }
    }

  
    private final List<Student> students = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private int nextId = 1;

    private void run() {
        boolean running = true;
        printBanner();
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> addGradeToStudent();
                case 3 -> viewSummaryReport();
                case 4 -> viewClassStatistics();
                case 5 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    private void printBanner() {
        System.out.println("=========================================");
        System.out.println("   STUDENT GRADE TRACKER - CodeAlpha");
        System.out.println("=========================================");
    }

    private void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Add a new student");
        System.out.println("2. Add a grade to a student");
        System.out.println("3. View summary report (all students)");
        System.out.println("4. View class statistics (top/bottom/class average)");
        System.out.println("5. Exit");
    }

    private void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String id = String.format("STU%03d", nextId++);
        students.add(new Student(id, name));
        System.out.println("Student \"" + name + "\" added with ID " + id + ".");
    }

    private void addGradeToStudent() {
        Student student = selectStudent();
        if (student == null) return;
        double grade = readDouble("Enter grade (0-100): ");
        if (grade < 0 || grade > 100) {
            System.out.println("Grade must be between 0 and 100.");
            return;
        }
        student.addGrade(grade);
        System.out.println("Grade added to " + student.getName() + ".");
    }

    private void viewSummaryReport() {
        if (students.isEmpty()) {
            System.out.println("No students added yet.");
            return;
        }
        System.out.println("\n===== SUMMARY REPORT =====");
        System.out.printf("%-8s %-15s %-9s %-9s %-9s %-9s %-7s %-6s%n",
                "ID", "Name", "Subjects", "Average", "Highest", "Lowest", "Grade", "Status");
        System.out.println("-".repeat(80));
        for (Student s : students) {
            System.out.printf("%-8s %-15s %-9d %-9.2f %-9.2f %-9.2f %-7s %-6s%n",
                    s.getId(), s.getName(), s.getSubjectCount(), s.getAverage(),
                    s.getHighest(), s.getLowest(), s.getLetterGrade(), s.getStatus());
        }
    }

    private void viewClassStatistics() {
        if (students.isEmpty()) {
            System.out.println("No students added yet.");
            return;
        }
        Student top = students.get(0);
        Student bottom = students.get(0);
        double sumOfAverages = 0;
        for (Student s : students) {
            if (s.getAverage() > top.getAverage()) top = s;
            if (s.getAverage() < bottom.getAverage()) bottom = s;
            sumOfAverages += s.getAverage();
        }
        double classAverage = sumOfAverages / students.size();
        System.out.println("\n===== CLASS STATISTICS =====");
        System.out.printf("Top performer:    %s - %s (Avg: %.2f)%n", top.getId(), top.getName(), top.getAverage());
        System.out.printf("Bottom performer: %s - %s (Avg: %.2f)%n", bottom.getId(), bottom.getName(), bottom.getAverage());
        System.out.printf("Class average:    %.2f%n", classAverage);
    }

    private Student selectStudent() {
        if (students.isEmpty()) {
            System.out.println("No students available. Add a student first.");
            return null;
        }
        System.out.println("Select a student:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i).getId() + " - " + students.get(i).getName());
        }
        int index = readInt("Enter number: ") - 1;
        if (index < 0 || index >= students.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return students.get(index);
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

    private double readDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }
}
    

