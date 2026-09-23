import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;


public final class ConsoleUtils {

    private final Scanner scanner;

    
    public ConsoleUtils(Scanner scanner) {
        this.scanner = scanner;
    }

    
    public void clearScreen() {
        try {

            if (System.getProperty("os.name")
                    .toLowerCase()
                    .contains("windows")) {

                new ProcessBuilder("cmd", "/c", "cls")
                        .inheritIO()
                        .start()
                        .waitFor();

            } else {

                System.out.print("\033[H\033[2J");
                System.out.flush();
            }

        } catch (Exception ex) {

            
            System.out.println();
        }
    }

    
    public void pause() {
        System.out.print(
                AnsiColor.paint(
                        "\nPremi INVIO per continuare...",
                        AnsiColor.DIM
                )
        );

        scanner.nextLine();
    }

    
    public String readLine(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    
    public String readRequired(String label) {

        while (true) {

            String value = readLine(label);

            if (!value.isBlank()) {
                return value;
            }

            System.out.println(
                    AnsiColor.paint(
                            "Valore obbligatorio.",
                            AnsiColor.YELLOW
                    )
            );
        }
    }

    
    public String readOptional(String label, String currentValue) {

        System.out.print(label + " [" + currentValue + "]: ");

        String value = scanner.nextLine().trim();

        return value.isBlank()
                ? currentValue
                : value;
    }

    
    public LocalDate readDate(String label) {

        while (true) {

            String value =
                    readRequired(label + " (gg/mm/aaaa)");

            Optional<LocalDate> date =
                    DateUtils.parseItalianDate(value);

            if (date.isPresent()) {
                return date.get();
            }

            System.out.println(
                    AnsiColor.paint(
                            "Data non valida. Esempio: 14/03/1998",
                            AnsiColor.YELLOW
                    )
            );
        }
    }

    
    public LocalDate readOptionalDate(
            String label,
            LocalDate currentValue
    ) {

        while (true) {

            System.out.print(
                    label
                            + " ["
                            + DateUtils.formatItalian(currentValue)
                            + "]: "
            );

            String value = scanner.nextLine().trim();

            if (value.isBlank()) {
                return currentValue;
            }

            Optional<LocalDate> date =
                    DateUtils.parseItalianDate(value);

            if (date.isPresent()) {
                return date.get();
            }

            System.out.println(
                    AnsiColor.paint(
                            "Data non valida. Esempio: 14/03/1998",
                            AnsiColor.YELLOW
                    )
            );
        }
    }

    
    public char readSex(String label) {

        while (true) {

            String value =
                    readRequired(label + " (M/F)")
                            .toUpperCase();

            if (value.equals("M") || value.equals("F")) {
                return value.charAt(0);
            }

            System.out.println(
                    AnsiColor.paint(
                            "Inserisci M oppure F.",
                            AnsiColor.YELLOW
                    )
            );
        }
    }

    
    public char readOptionalSex(String label, char currentValue) {

        while (true) {

            System.out.print(label + " [" + currentValue + "]: ");

            String value =
                    scanner.nextLine()
                            .trim()
                            .toUpperCase();

            if (value.isBlank()) {
                return currentValue;
            }

            if (value.equals("M") || value.equals("F")) {
                return value.charAt(0);
            }

            System.out.println(
                    AnsiColor.paint(
                            "Inserisci M oppure F.",
                            AnsiColor.YELLOW
                    )
            );
        }
    }

    
    public int readInt(String label, int min, int max) {

        while (true) {

            System.out.print(label + ": ");

            String value = scanner.nextLine().trim();

            try {

                int number = Integer.parseInt(value);

                if (number >= min && number <= max) {
                    return number;
                }

            } catch (NumberFormatException ignored) {
            }

            System.out.println(
                    AnsiColor.paint(
                            "Scelta non valida.",
                            AnsiColor.YELLOW
                    )
            );
        }
    }

    
    public boolean confirm(String label) {

        while (true) {

            System.out.print(label + " (S/N): ");

            String value =
                    scanner.nextLine()
                            .trim()
                            .toUpperCase();

            if (value.equals("S") || value.equals("SI")) {
                return true;
            }

            if (value.equals("N") || value.equals("NO")) {
                return false;
            }

            System.out.println(
                    AnsiColor.paint(
                            "Rispondi S oppure N.",
                            AnsiColor.YELLOW
                    )
            );
        }
    }
}