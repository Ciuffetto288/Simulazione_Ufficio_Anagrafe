
/**
 * Classe Utility per la formattazione e colorazione del testo tramite codici ANSI.
 */
public final class AnsiColor {
    /**
     * Codice ANSI per ripristinare colore e stile predefiniti.
     */
    public static final String RESET = "\u001B[0m";

    /**
     * Codice ANSI per colorare il testo in rosso.
     */
    public static final String RED = "\u001B[31m";

    /**
     * Codice ANSI per colorare il testo in verde.
     */
    public static final String GREEN = "\u001B[32m";

    /**
     * Codice ANSI per colorare il testo in giallo.
     */
    public static final String YELLOW = "\u001B[33m";

    /**
     * Codice ANSI per colorare il testo in blu.
     */
    public static final String BLUE = "\u001B[34m";

    /**
     * Codice ANSI per colorare il testo in magenta.
     */
    public static final String MAGENTA = "\u001B[35m";

    /**
     * Codice ANSI per colorare il testo in ciano.
     */
    public static final String CYAN = "\u001B[36m";

    /**
     * Codice ANSI per colorare il testo in bianco.
     */
    public static final String WHITE = "\u001B[37m";

    /**
     * Codice ANSI per applicare lo stile grassetto.
     */
    public static final String BOLD = "\u001B[1m";

    /**
     * Codice ANSI per applicare uno stile attenuato.
     */
    public static final String DIM = "\u001B[2m";

    /**
     * Costruttore privato vuoto per impedire l'istanziazione della classe.
     * Essendo una classe di sole utility con metodi statici, evita l'uso non necessario di 'new AnsiColor()'.
     */
    private AnsiColor() {
    }

    /**
     * Metodo per formattare il testo in modo veloce riducendo il margine di errore.
     * Evita di dimenticare il codice di reset alla fine della stringa.
     * 
     * @param text  Il testo da colorare
     * @param color Il codice ANSI del colore da applicare
     * @return La stringa formattata e colorata, pronta per la stampa
     */
    public static String paint(String text, String color) {
        return color + text + RESET;
    }
}
