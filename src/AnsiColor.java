
/**
 * Costanti ANSI per colorare e formattare il testo stampato a terminale.
 */
public final class AnsiColor {

    public static final String RESET = "\u001B[0m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // stili
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    private AnsiColor() {
        // solo metodi statici, non si istanzia
    }

    /**
     * Racchiude il testo tra il codice colore e il reset.
     * Serve soprattutto per non dimenticarsi il RESET finale e ritrovarsi
     * la console colorata per il resto dell'esecuzione.
     *
     * @param text  il testo da colorare
     * @param color il codice ANSI da applicare
     * @return la stringa pronta per la stampa
     */
    public static String paint(String text, String color) {
        return color + text + RESET;
    }
}
