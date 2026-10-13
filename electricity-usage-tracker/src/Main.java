import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static UsageService usageService;
    private static final Operator operator = new Operator();

    public static void main(String[] args) {
        FileManager fileManager = new FileManager();
        try {
            fileManager.initialize();
            usageService = new UsageService(fileManager);
        } catch (IOException e) {
            System.out.println("Unable to start the application.");
            System.out.println("File error: " + e.getMessage());
            scanner.close();
            return;
        }
        System.out.println("======================================");
        System.out.println("     ELECTRICITY USAGE TRACKER");
        System.out.println("======================================");
        mainMenu();
        scanner.close();
    }

    //MAIN MENU

    private static void mainMenu() {
        while (true) {
            System.out.println();
            System.out.println("========== MAIN MENU ==========");
            System.out.println("1. Operator menu");
            System.out.println("2. Resident menu");
            System.out.println("0. Exit");
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1:
                    operatorMenu();
                    break;
                case 2:
                    residentMenu();
                    break;
                case 0:
                    System.out.println("Thank you for using Electricity Usage Tracker.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    //OPERATOR MENU
    private static void operatorMenu() {

        while (true) {
            System.out.println();
            System.out.println("========== OPERATOR MENU ==========");
            System.out.println("1. Register resident and opening reading");
            System.out.println("2. View residents");
            System.out.println("3. Record next month reading");
            System.out.println("4. View usage and reports");
            System.out.println("0. Back");
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1:
                    registerResident();
                    break;
                case 2:
                    viewResidents();
                    break;
                case 3:
                    recordNextMonthReading();
                    break;
                case 4:
                    usageAndReports();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    //Register a new resident.
    private static void registerResident() {
        System.out.println();
        System.out.println("===== REGISTER RESIDENT =====");
        String name = readText("Enter resident name: ");
        String meter = readText("Enter meter number: ");
        int opening = readReading("Enter opening reading: ");
        try {
            Resident resident = usageService.registerResident(name, meter, opening);
            System.out.println();
            System.out.println("Resident " + resident.getId() + " saved successfully.");
            System.out.println("Opening reading: " + opening);
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Saving failed: " + e.getMessage());
        }
    }

    //Display all residents.
    private static void viewResidents() {
        System.out.println();
        System.out.println("========== RESIDENT RECORDS ==========");
        ArrayList<Resident> residents = usageService.getResidents();
        if (residents.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Resident resident : residents) {
            System.out.println();
            System.out.println("ID          : " + resident.getId());
            System.out.println("Name        : " + resident.getName());
            System.out.println("Meter       : " + resident.getMeterNumber());
            System.out.println("Months done : " + resident.getCompletedMonths() + "/3");
        }
    }

    //Record next month-end reading.
    private static void recordNextMonthReading() {

        System.out.println();
        System.out.println("===== RECORD MONTHLY READING =====");
        int residentId = readInt("Enter resident ID: ");

        Resident resident = usageService.findResident(residentId);
        if (resident == null) {
            System.out.println("Resident ID not found.");
            return;
        }
        int completed = resident.getCompletedMonths();
        if (completed == 3) {
            System.out.println("All three months are already recorded.");
            return;
        }
        int month = completed + 1;
        int previousReading = resident.getReadings()[month - 1];
        System.out.println("Recording Month " + month);
        System.out.println("Previous cumulative reading: " + previousReading);
        int newReading = readReading("Enter Month " + month + " reading: ");
        try {
            int units = usageService.recordReading(residentId, newReading);
            System.out.println();
            System.out.println("Month " + month + " saved successfully.");
            System.out.println("Units used: " + units);
        } catch (IllegalArgumentException e) {
            System.out.println("Reading rejected: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Reading rejected: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Saving failed: " + e.getMessage());
        }
    }

    //Usage and report menu.
    private static void usageAndReports() {
        while (true) {
            System.out.println();
            System.out.println("========== USAGE & REPORTS ==========");
            System.out.println("1. View usage for a resident");
            System.out.println("2. View complete usage report");
            System.out.println("3. Save report to report.txt");
            System.out.println("0. Back");
            System.out.println("Enter choice: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    int residentId = readInt("Enter resident ID: ");
                    try {
                        usageService.showUsage(residentId);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    usageService.showReport();
                    break;
                case 3:
                    try {
                        usageService.saveReport();
                        System.out.println("Report saved successfully to data/report.txt.");
                    } catch (IOException e) {
                        System.out.println("Report saving failed: " + e.getMessage());
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    //RESIDENT MENU
    private static void residentMenu() {
        System.out.println();
        System.out.println("========== RESIDENT MENU ==========");
        int residentId = readInt("Enter resident ID: ");
        Resident resident = usageService.findResident(residentId);
        if (resident == null) {
            System.out.println("Resident ID not found.");
            return;
        }
        while (true) {
            System.out.println();
            System.out.println("Welcome, " + resident.getName());
            System.out.println("1. View my profile");
            System.out.println("2. View my readings and usage");
            System.out.println("0. Back");
            System.out.println("Enter choice: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    resident.showProfile();
                    break;
                case 2:
                    showResidentReadings(resident);
                    break;
                case 0:
                    return;
                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    //Shows resident readings and usage.
    private static void showResidentReadings(Resident resident) {
        System.out.println();
        System.out.println("========== MY READINGS ==========");
        int[] readings = resident.getReadings();
        System.out.println("Opening reading: " + readings[0]);
        for (int month = 1; month <= 3; month++) {
            if (readings[month] == -1) {
                System.out.println("Month " + month + ": Not recorded");
            } else {
                int usage = readings[month] - readings[month - 1];
                System.out.println("Month " + month + ": Reading = " + readings[month] + ", Usage = " + usage + " units");
            }
        }
        System.out.println("Total recorded units: " + resident.calculateUsage());
    }

    //Reads an integer using nextLine().
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    //Reads a reading and validates range.
    private static int readReading(String message) {
        while (true) {
            int value = readInt(message);
            if (value >= 0 && value <= 1_000_000) {
                return value;
            }
            System.out.println("Reading must be between 0 and 1000000.");
        }
    }

    //Reads a non-empty text value.
    private static String readText(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Value cannot be blank.");
                continue;
            }
            if (input.contains("|")
                    || input.contains("\n")
                    || input.contains("\r")) {
                System.out.println("Value cannot contain | or line breaks.");
                continue;
            }
            return input;
        }
    }
}
