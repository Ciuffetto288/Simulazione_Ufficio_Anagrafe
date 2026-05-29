/**
 * Classe Utility per la gestione e la visualizzazione di grafiche in formato ASCII Art sul terminale.
 */
public final class AsciiArt {
    /**
     * Stringa contenente il logo monumentale "ANAGRAFE" in formato ASCII Art.
     */
    private static final String AsciiArtAnagrafe = "  ______   __    __   ______    ______   _______       ______      ________  ________ \r\n" + //
                " /      \\ /  \\  /  | /      \\  /      \\ /       \\    _/      \\_   /        |/        |\r\n" + //
                "/$$$$$$  |$$  \\ $$ |/$$$$$$  |/$$$$$$  |$$$$$$$  |  / $$$$$$   \\  $$$$$$$$/ $$$$$$$$/ \r\n" + //
                "$$ |__$$ |$$$  \\$$ |$$ |__$$ |$$ | _$$/ $$ |__$$ | /$$$ ___$$$  \\ $$ |__    $$ |__    \r\n" + //
                "$$    $$ |$$$$  $$ |$$    $$ |$$ |/    |$$    $$< /$$/ /     $$  |$$    |   $$    |   \r\n" + //
                "$$$$$$$$ |$$ $$ $$ |$$$$$$$$ |$$ |$$$$ |$$$$$$$  |$$ |/$$$$$ |$$ |$$$$$/    $$$$$/    \r\n" + //
                "$$ |  $$ |$$ |$$$$ |$$ |  $$ |$$ \\__$$ |$$ |  $$ |$$ |$$  $$ |$$ |$$ |      $$ |_____ \r\n" + //
                "$$ |  $$ |$$ | $$$ |$$ |  $$ |$$    $$/ $$ |  $$ |$$ |$$  $$  $$/ $$ |      $$       |\r\n" + //
                "$$/   $$/ $$/   $$/ $$/   $$/  $$$$$$/  $$/   $$/ $$  \\$$$$$$$$/  $$/       $$$$$$$$/ \r\n" + //
                "                                                   $$   \\__/   |                      \r\n" + //
                "                                                    $$$    $$$/                       \r\n" + //
                "                                                      $$$$$$/                         ";
                
    /**
     * Costruttore privato vuoto per impedire l'istanziazione della classe.
     * Essendo una classe di sole utility con metodi statici, evita l'uso non necessario di 'new AsciiArt()'.
     */
    private AsciiArt() {
    }

    /**
     * Pulisce lo schermo e mostra la schermata iniziale di benvenuto (Splash Screen).
     * Stampa il logo colorato di verde, i dati dell'autore, la versione e mette in pausa il programma.
     * 
     * @param console L'istanza di {@link ConsoleUtils} utilizzata per pulire lo schermo della console
     * @param fast    Se impostato a true riduce la pausa iniziale a 300ms, altrimenti attende 5000ms (5 secondi)
     */
    public static void showSplash(ConsoleUtils console, boolean fast) {
        console.clearScreen();
        System.out.println(AnsiColor.paint(AsciiArtAnagrafe, AnsiColor.GREEN));
        System.out.println("Autore: Anthony");
        System.out.println("Versione: 5.3.1-Beta");
        System.out.println("Sistema gestione anagrafe");
        try {
            Thread.sleep(fast ? 300 : 5000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        console.clearScreen();
    }
}
