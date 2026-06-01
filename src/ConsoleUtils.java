import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

/**
 * Classe utility dedicata alla gestione dell'input/output da console.
 * 
 * Fornisce metodi semplificati per:
 * <ul>
 *     <li>Lettura sicura dei dati utente</li>
 *     <li>Validazione degli input</li>
 *     <li>Gestione delle date</li>
 *     <li>Conferme interattive</li>
 *     <li>Messaggi colorati in console</li>
 * </ul>
 */
public final class ConsoleUtils {

    /**
     * Scanner condiviso utilizzato per leggere gli input dalla console.
     */
    private final Scanner scanner;

    /**
     * Costruisce un nuovo helper per la gestione della console.
     * 
     * @param scanner Scanner utilizzato per leggere gli input utente
     */
    public ConsoleUtils(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Pulisce la schermata della console.
     * 
     * Su Windows utilizza il comando "cls",
     * mentre sugli altri sistemi usa sequenze ANSI.
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

            System.out.println();
        }
    }

    /**
     * Mette in pausa l'esecuzione attendendo la pressione del tasto INVIO.
     */
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
     * Legge una riga di testo dalla console.
     * 
     * @param label Etichetta mostrata all'utente
     * @return Testo inserito ripulito dagli spazi esterni
     */
    public String readLine(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Legge un valore obbligatorio dalla console.
     * 
     * Continua a richiedere l'input finché l'utente
     * non inserisce un valore non vuoto.
     * 
     * @param label Etichetta mostrata all'utente
     * @return Valore obbligatorio inserito
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
     * Legge un valore opzionale mantenendo quello corrente se l'utente lascia vuoto.
     * 
     * @param label Etichetta mostrata all'utente
     * @param currentValue Valore corrente
     * @return Nuovo valore oppure quello esistente
     */
    public String readOptional(String label, String currentValue) {

        System.out.print(label + " [" + currentValue + "]: ");

        String value = scanner.nextLine().trim();

        return value.isBlank()
                ? currentValue
                : value;
    }

    /**
     * Legge una data obbligatoria nel formato italiano GG/MM/AAAA.
     * 
     * Continua a richiedere il valore finché la data non risulta valida.
     * 
     * @param label Etichetta mostrata all'utente
     * @return Data validata
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
     * Legge una data opzionale mantenendo quella corrente se il campo resta vuoto.
     * 
     * @param label Etichetta mostrata all'utente
     * @param currentValue Data attualmente salvata
     * @return Nuova data oppure quella corrente
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
     * Legge il sesso anagrafico validando esclusivamente i valori M oppure F.
     * 
     * @param label Etichetta mostrata all'utente
     * @return Carattere rappresentante il sesso
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
     * Legge il sesso anagrafico mantenendo il valore corrente se vuoto.
     * 
     * @param label Etichetta mostrata all'utente
     * @param currentValue Valore attuale
     * @return Nuovo valore oppure quello corrente
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
     * Legge un numero intero compreso in un intervallo specificato.
     * 
     * @param label Etichetta mostrata all'utente
     * @param min Valore minimo consentito
     * @param max Valore massimo consentito
     * @return Numero validato
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
     * Richiede una conferma booleana all'utente.
     * 
     * Accetta:
     * <ul>
     *     <li>S / SI</li>
     *     <li>N / NO</li>
     * </ul>
     * 
     * @param label Messaggio di conferma
     * @return true se confermato, false altrimenti
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