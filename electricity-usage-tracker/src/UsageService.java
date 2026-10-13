import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class UsageService {

    private final ArrayList<Resident> residents;
    private final Set<String> meterNumbers;
    private final FileManager fileManager;
    private int nextResidentId;

    public UsageService(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        this.residents = fileManager.loadResidents();
        this.meterNumbers = new HashSet<>();
        rebuildMeterSet();
        nextResidentId = calculateNextId();
    }

    //Rebuilds the meter-number HashSet from loaded records.
    private void rebuildMeterSet() {
        meterNumbers.clear();
        for (Resident resident : residents) {
            meterNumbers.add(resident.getMeterNumber().trim().toUpperCase(Locale.ROOT));
        }
    }

    //Finds the next resident ID.
    private int calculateNextId() {
        int largestId = 0;
        for (Resident resident : residents) {
            if (resident.getId() > largestId) {
                largestId = resident.getId();
            }
        }
        return largestId + 1;
    }

    //Registers a new resident.
    public Resident registerResident(String name, String meterNumber, int openingReading) throws IOException {
        name = name.trim();
        meterNumber = meterNumber.trim().toUpperCase(Locale.ROOT);
        validateText(name, "Name");
        validateText(meterNumber, "Meter number");
        validateReading(openingReading);
        if (meterNumbers.contains(meterNumber)) {
            throw new IllegalArgumentException("Meter number already exists.");
        }
        int[] readings = {openingReading, -1, -1, -1};
        Resident resident = new Resident(nextResidentId, name, meterNumber, readings);
        // Keep old state in case saving fails.
        residents.add(resident);
        meterNumbers.add(meterNumber);

        try {
            fileManager.saveResidents(residents);
        } catch (IOException e) {
            residents.remove(resident);
            meterNumbers.remove(meterNumber);
            throw e;
        }
        nextResidentId++;
        return resident;
    }

    //Records the next month-end reading.
    public int recordReading(int residentId, int newReading) throws IOException {
        Resident resident = findResident(residentId);
        if (resident == null) {
            throw new IllegalArgumentException("Resident ID not found.");
        }
        validateReading(newReading);
        int[] oldReadings = resident.getReadings();
        // Make a copy of the original state.
        int[] newReadings = oldReadings.clone();
        int month = -1;
        for (int i = 1; i < newReadings.length; i++) {
            if (newReadings[i] == -1) {
                month = i;
                break;
            }
        }
        if (month == -1) {
            throw new IllegalStateException("All three month-end readings are already recorded.");
        }
        if (newReading < newReadings[month - 1]) {
            throw new IllegalArgumentException("Reading cannot be lower than the previous reading.");
        }
        int units = newReading - newReadings[month - 1];
        newReadings[month] = newReading;
        /*
         * We cannot directly replace the Resident's array because
         * the project keeps the ID and object stable.
         *
         * Resident's update method is used below.
         */
        resident.recordNextMonth(newReading);

        try {
            fileManager.saveResidents(residents);
        } catch (IOException e) {
            /*
             * Restore the old reading.
             */
            restoreResidentReadings(resident, oldReadings);
            throw e;
        }
        return units;
    }
    //Restores a resident's readings after save failure.
    private void restoreResidentReadings(Resident resident, int[] original) {
        int[] current = resident.getReadings();
        for (int i = 1; i < current.length; i++) {
            if (current[i] != original[i]) {
                /*
                 * Reconstruct the resident from the original
                 * values is not necessary. Instead, this method
                 * uses reflection-free rebuilding through the
                 * helper below.
                 */
                resident.restoreReadings(original);
                return;
            }
        }
    }

    //Finds a resident by ID.
    public Resident findResident(int id) {
        for (Resident resident : residents) {
            if (resident.getId() == id) {
                return resident;
            }
        }
        return null;
    }
    //Returns all residents.
    public ArrayList<Resident> getResidents() {
        return new ArrayList<>(residents);
    }
    /*
     * Displays all residents and their usage.
     */
    public void showUsage(int residentId) {
        Resident resident = findResident(residentId);
        if (resident == null) {
            throw new IllegalArgumentException("Resident ID not found.");
        }
        System.out.println();
        System.out.println("===== USAGE DETAILS =====");
        System.out.println("Resident ID  : " + resident.getId());
        System.out.println("Name         : " + resident.getName());
        System.out.println("Meter Number : " + resident.getMeterNumber());
        for (int month = 1; month <= 3; month++) {
            int usage = resident.calculateMonthUsage(month);
            if (usage == -1) {
                System.out.println("Month " + month + ": Not recorded");
            } else {
                System.out.println("Month " + month + ": " + usage + " units");
            }
        }
        System.out.println("Total recorded units: " + resident.calculateUsage());
    }

    //Displays the complete report on console.
    public void showReport() {
        System.out.println();
        System.out.println("========== USAGE REPORT ==========");
        if (residents.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        int totalUsage = 0;
        int missingMonths = 0;
        for (Resident resident : residents) {
            System.out.println();
            System.out.println("Resident ID: " + resident.getId());
            System.out.println("Name: " + resident.getName());
            System.out.println("Meter: " + resident.getMeterNumber());
            for (int month = 1; month <= 3; month++) {
                int usage = resident.calculateMonthUsage(month);
                if (usage == -1) {
                    System.out.println("Month " + month + ": Not recorded");
                    missingMonths++;
                } else {
                    System.out.println("Month " + month + ": " + usage + " units");
                    totalUsage += usage;
                }
            }
            System.out.println("Total: " + resident.calculateUsage() + " units");
        }
        System.out.println();
        System.out.println("Total usage across residents: " + totalUsage + " units");
        System.out.println("Missing resident-month readings: " + missingMonths);
    }

    //Saves the usage report.
    public void saveReport() throws IOException {
        fileManager.saveReport(residents);
    }
    //Validates name and meter number.
    private void validateText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(fieldName + " cannot contain | or line breaks.");
        }
    }
    //Validates meter reading.
    private void validateReading(int reading) {
        if (reading < 0 || reading > 1_000_000) {
            throw new IllegalArgumentException("Reading must be between 0 and 1000000.");
        }
    }
}