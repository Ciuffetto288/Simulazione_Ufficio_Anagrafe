import java.util.Arrays;
import java.util.Scanner;
/**
 * Classe principale (Entry Point) dell'applicazione per la simulazione del calcolo del codice fiscale.
 * Si occupa di elaborare gli argomenti della riga di comando e di avviare il menu di interfaccia utente.
 */
public final class Main {
        /**
     * Costruttore privato vuoto per impedirne l'istanziazione.
     * Trattandosi dell'entry point dell'applicazione, evita l'uso non necessario di 'new Main()'.
     */
    private Main() {
    }
    /**
     * Il punto di inizio dell'esecuzione del programma.
     * Analizza i flag passati in input e decide se avviare la suite di self-test o l'interfaccia menu.
     * 
     * @param args Gli argomenti passati da riga di comando (es. "--self-test" o "--fast")
     */
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--self-test")) {
            SelfTest.run();
            return;
        }

        boolean fastSplash = Arrays.asList(args).contains("--fast");
        EcosistemaBridge bridge = new EcosistemaBridge();
        bridge.start();
        try (Scanner scanner = new Scanner(System.in)) {
            new Menu(scanner, bridge).start(fastSplash);
        } finally {
            bridge.stop();
        }
    }
}
