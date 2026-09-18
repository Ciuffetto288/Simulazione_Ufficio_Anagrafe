import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

/**
 * Tutte le letture da tastiera passano da qui.
 * I metodi read* insistono finche' il dato non e' valido, invece di propagare
 * eccezioni al menu: in un programma interattivo conviene richiedere il dato
 * sul posto. I metodi readOptional* servono in modifica, dove il campo lasciato
 * vuoto significa "tieni il valore che c'e' gia'".
 */
public final class ConsoleUtils {

    private final Scanner scanner;

    /**
     * @param scanner lo Scanner aperto da Main, condiviso da tutta l'applicazione
     */
    public ConsoleUtils(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Pulisce lo schermo. Su Windows serve il comando cls, altrove basta la
     * sequenza ANSI. Se non funziona nessuno dei due si stampa solo una riga
     * vuota: peggiora l'estetica ma non blocca il programma.
     */
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

            // terminale che non supporta ne' cls ne' ANSI: mi limito a una riga vuota
            System.out.println();
        }
    }

    /** Aspetta INVIO prima di tornare al menu. */
    public void pause() {
        System.out.print(
                AnsiColor.paint(
                        "\nPremi INVIO per continuare...",
                        AnsiColor.DIM
                )
        );

        scanner.nextLine();
    }

    /**
     * @param label etichetta mostrata prima dei due punti
     * @return il testo inserito, senza spazi ai bordi
     */
    public String readLine(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Richiede il dato finche' non arriva qualcosa di diverso dal vuoto.
     *
     * @param label etichetta mostrata all'utente
     * @return il valore inserito
     */
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

    /**
     * @param label etichetta mostrata all'utente
     * @param currentValue valore da tenere se l'utente batte solo INVIO
     * @return il nuovo valore oppure quello vecchio
     */
    public String readOptional(String label, String currentValue) {

        System.out.print(label + " [" + currentValue + "]: ");

        String value = scanner.nextLine().trim();

        return value.isBlank()
                ? currentValue
                : value;
    }

    /**
     * Data in formato GG/MM/AAAA, richiesta finche' non e' valida.
     *
     * @param label etichetta mostrata all'utente
     * @return la data inserita
     */
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

    /**
     * @param label etichetta mostrata all'utente
     * @param currentValue data da tenere se il campo resta vuoto
     * @return la data nuova oppure quella vecchia
     */
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

    /**
     * @param label etichetta mostrata all'utente
     * @return 'M' oppure 'F', gli unici valori accettati dal codice fiscale
     */
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

    /**
     * @param label etichetta mostrata all'utente
     * @param currentValue valore da tenere se il campo resta vuoto
     * @return il sesso nuovo oppure quello vecchio
     */
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

    /**
     * Numero intero dentro un intervallo, usato per le scelte di menu.
     * Rifiuta sia il testo non numerico sia i valori fuori intervallo.
     *
     * @param label etichetta mostrata all'utente
     * @param min minimo accettato
     * @param max massimo accettato
     * @return il numero inserito
     */
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

    /**
     * Conferma S/N (accetta anche SI e NO scritti per intero).
     *
     * @param label domanda mostrata all'utente
     * @return true se ha risposto si'
     */
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