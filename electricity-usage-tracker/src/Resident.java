import java.util.Arrays;
import java.util.Locale;

public class Resident extends User {

    private final String meterNumber;
    private final int[] readings;

    public Resident(int id, String name, String meterNumber, int[] readings) {
        super(id, name);
        this.meterNumber = meterNumber.trim().toUpperCase(Locale.ROOT);
        this.readings = readings.clone();
    }

    public String getMeterNumber() {

        return meterNumber;
    }

    public int[] getReadings() {

        return readings.clone();

    }

    public int recordNextMonth(int newReading) {

        for (int month = 1; month < readings.length; month++) {
            if (readings[month] == -1) {
                if (newReading < readings[month - 1]) {
                    throw new IllegalArgumentException("Reading cannot be lower than the previous reading.");
                }
                int units = newReading - readings[month - 1];
                readings[month] = newReading;
                return units;
            }
        }
        throw new IllegalStateException("All three month-end readings are already recorded.");
    }

    public int calculateMonthUsage(int month) {
        if (month < 1 || month > 3) {
            throw new IllegalArgumentException("Invalid month.");
        }

        if (readings[month] == -1) {
            return -1;
        }

        return readings[month] - readings[month - 1];
    }

    public int calculateUsage() {
        int total = 0;
        for (int month = 1; month < readings.length; month++) {
            if (readings[month] == -1) {
                break;
            }
            total += readings[month] - readings[month - 1];
        }
        return total;
    }

    public int getCompletedMonths() {
        int count = 0;
        for (int month = 1; month < readings.length; month++) {
            if (readings[month] == -1) {
                break;
            }
            count++;
        }
        return count;
    }

    public boolean isComplete() {

        return readings[1] != -1 && readings[2] != -1 && readings[3] != -1;
    }

    //Used to restore the original array if file saving fails.
    public void restoreReadings(int[] originalReadings) {
        if (originalReadings == null || originalReadings.length != 4) {
            throw new IllegalArgumentException("Invalid readings.");
        }
        System.arraycopy(originalReadings, 0, readings, 0, 4);
    }

    @Override
    public void showProfile() {
        System.out.println("--------------------------------");
        System.out.println("Resident ID   : " + getId());
        System.out.println("Name          : " + getName());
        System.out.println("Meter Number  : " + meterNumber);
        System.out.println("Readings      : " + Arrays.toString(readings));
        System.out.println("--------------------------------");
    }
}
