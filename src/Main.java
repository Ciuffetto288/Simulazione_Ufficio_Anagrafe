import java.util.Arrays;
import java.util.Scanner;
/**
 * Entry point del programma. Legge i flag da riga di comando e avvia il menu.
 */
public final class Main {
    private Main() {
    }
    /**
     * Flag riconosciuti: --self-test esegue i controlli interni ed esce,
     * --fast accorcia la schermata iniziale.
     *
     * @param args argomenti da riga di comando
     */
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--self-test")) {
            SelfTest.run();
            return;
        }

        boolean fastSplash = Arrays.asList(args).contains("--fast");

        // un solo Scanner per tutta l'applicazione, chiuso all'uscita
        try (Scanner scanner = new Scanner(System.in)) {
            new Menu(scanner).start(fastSplash);
        }
    }
}
