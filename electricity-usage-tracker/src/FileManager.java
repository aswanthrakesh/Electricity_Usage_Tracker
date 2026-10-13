import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FileManager {

    private final Path dataDirectory;
    private final Path residentsFile;
    private final Path reportFile;

    public FileManager() {
        dataDirectory = Path.of("data");
        residentsFile = dataDirectory.resolve("residents.txt");
        reportFile = dataDirectory.resolve("report.txt");
    }

    //Creates the data directory if it does not exist.
    public void initialize() throws IOException {
        Files.createDirectories(dataDirectory);
    }

    public ArrayList<Resident> loadResidents() throws IOException {
        ArrayList<Resident> temporaryResidents = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<String> meters = new HashSet<>();
        if (!Files.exists(residentsFile)) {
            return temporaryResidents;
        }
        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(residentsFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    throw new IOException("Invalid empty line in " + residentsFile + " at line " + lineNumber);
                }
                String[] fields = line.split("\\|", -1);
                // id | name | meterNumber | readings
                if (fields.length != 4) {
                    throw new IOException("Invalid field count in " + residentsFile + " at line " + lineNumber);
                }
                int id;
                try {
                    id = Integer.parseInt(fields[0].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid resident ID in " + residentsFile + " at line " + lineNumber);
                }
                if (id <= 0) {
                    throw new IOException(
                            "Resident ID must be positive in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                if (!ids.add(id)) {
                    throw new IOException(
                            "Duplicate resident ID in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                String name = fields[1].trim();
                String meterNumber = fields[2].trim()
                        .toUpperCase(Locale.ROOT);

                if (name.isEmpty()) {
                    throw new IOException(
                            "Resident name cannot be empty in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                if (name.contains("|") || name.contains("\n")
                        || name.contains("\r")) {

                    throw new IOException(
                            "Invalid resident name in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                if (meterNumber.isEmpty()) {
                    throw new IOException(
                            "Meter number cannot be empty in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                if (meterNumber.contains("|")
                        || meterNumber.contains("\n")
                        || meterNumber.contains("\r")) {

                    throw new IOException(
                            "Invalid meter number in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                if (!meters.add(meterNumber)) {
                    throw new IOException(
                            "Duplicate meter number in "
                                    + residentsFile
                                    + " at line "
                                    + lineNumber
                    );
                }

                int[] readings = parseReadings(
                        fields[3],
                        lineNumber
                );

                temporaryResidents.add(
                        new Resident(
                                id,
                                name,
                                meterNumber,
                                readings
                        )
                );
            }
        }

        return temporaryResidents;
    }

    /*
     * Converts the reading text into int[4].
     */
    private int[] parseReadings(String readingText, int lineNumber) throws IOException {

        String[] values = readingText.split(",", -1);

        if (values.length != 4) {
            throw new IOException(
                    "Exactly four readings required at line "
                            + lineNumber
            );
        }

        int[] readings = new int[4];

        for (int i = 0; i < 4; i++) {

            try {
                readings[i] = Integer.parseInt(values[i].trim());
            } catch (NumberFormatException e) {

                throw new IOException(
                        "Invalid reading at line "
                                + lineNumber
                );
            }
        }

        validateReadings(readings, lineNumber);

        return readings;
    }

    /*
     * Validates the complete reading sequence.
     */
    private void validateReadings(
            int[] readings,
            int lineNumber) throws IOException {

        // Opening reading must be valid.
        if (readings[0] < 0 || readings[0] > 1_000_000) {

            throw new IOException(
                    "Opening reading out of range at line "
                            + lineNumber
            );
        }

        boolean missingFound = false;

        for (int i = 1; i < readings.length; i++) {

            int current = readings[i];

            // -1 means not recorded.
            if (current == -1) {
                missingFound = true;
                continue;
            }

            // Values below -1 are invalid.
            if (current < -1 || current > 1_000_000) {

                throw new IOException(
                        "Reading out of range at line "
                                + lineNumber
                );
            }

            // A recorded value cannot come after -1.
            if (missingFound) {

                throw new IOException(
                        "Recorded value cannot follow -1 at line "
                                + lineNumber
                );
            }

            // Cumulative reading must not decrease.
            if (current < readings[i - 1]) {

                throw new IOException(
                        "Readings must be nondecreasing at line "
                                + lineNumber
                );
            }
        }
    }

    /*
     * Saves all resident records.
     *
     * The change is first written to a temporary file.
     * Only after writing succeeds is the original replaced.
     */
    public void saveResidents(
            ArrayList<Resident> residents) throws IOException {

        Path tempFile = dataDirectory.resolve("residents.tmp");

        try {

            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {

                for (Resident resident : residents) {

                    int[] readings = resident.getReadings();

                    writer.write(
                            resident.getId()
                                    + "|"
                                    + resident.getName()
                                    + "|"
                                    + resident.getMeterNumber()
                                    + "|"
                                    + readings[0]
                                    + ","
                                    + readings[1]
                                    + ","
                                    + readings[2]
                                    + ","
                                    + readings[3]
                    );

                    writer.newLine();
                }
            }

            Files.move(
                    tempFile,
                    residentsFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    /*
     * Saves the usage report.
     */
    public void saveReport(ArrayList<Resident> residents) throws IOException {

        Path tempFile = dataDirectory.resolve("report.tmp");

        int totalUsage = 0;
        int missingMonths = 0;

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {

                writer.write("ELECTRICITY USAGE REPORT");
                writer.newLine();
                writer.write("=========================");
                writer.newLine();

                if (residents.isEmpty()) {

                    writer.write("No records found.");
                    writer.newLine();

                } else {

                    for (Resident resident : residents) {

                        writer.write(
                                "Resident ID: "
                                        + resident.getId()
                        );
                        writer.newLine();

                        writer.write(
                                "Name: "
                                        + resident.getName()
                        );
                        writer.newLine();

                        writer.write(
                                "Meter Number: "
                                        + resident.getMeterNumber()
                        );
                        writer.newLine();

                        int completed =
                                resident.getCompletedMonths();

                        writer.write(
                                "Completed Months: "
                                        + completed
                        );
                        writer.newLine();

                        for (int month = 1; month <= 3; month++) {

                            int usage =
                                    resident.calculateMonthUsage(month);

                            if (usage == -1) {

                                writer.write(
                                        "Month "
                                                + month
                                                + ": Not recorded"
                                );

                                missingMonths++;

                            } else {

                                writer.write(
                                        "Month "
                                                + month
                                                + ": "
                                                + usage
                                                + " units"
                                );

                                totalUsage += usage;
                            }

                            writer.newLine();
                        }

                        writer.write(
                                "Total Recorded Usage: "
                                        + resident.calculateUsage()
                                        + " units"
                        );

                        writer.newLine();
                        writer.write("-------------------------");
                        writer.newLine();
                    }

                    writer.write(
                            "TOTAL USAGE ACROSS RESIDENTS: "
                                    + totalUsage
                                    + " units"
                    );

                    writer.newLine();

                    writer.write(
                            "MISSING RESIDENT-MONTH READINGS: "
                                    + missingMonths
                    );

                    writer.newLine();
                }
            }

            Files.move(
                    tempFile,
                    reportFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    public Path getResidentsFile() {
        return residentsFile;
    }

    public Path getReportFile() {
        return reportFile;
    }
}