import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe di utilità per la gestione dei percorsi utilizzati dall'applicazione.
 * <p>
 * Tutti i dati persistenti vengono memorizzati in una directory appropriata
 * per il sistema operativo corrente:
 * <ul>
 *     <li>Windows: {@code %APPDATA%\Anagr_fe}</li>
 *     <li>macOS: {@code ~/Library/Anagr_fe}</li>
 *     <li>Linux: {@code ~/.config/Anagr_fe}</li>
 * </ul>
 * e nella relativa sottocartella:
 * <pre>
 * data
 * </pre>
 */
public final class AppPaths {

    /**
     * Directory principale dell'applicazione.
     */
    private static final Path APP_DIR = detectAppDir();

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     * Essendo una utility class composta esclusivamente da metodi statici,
     * non deve essere creata tramite operatore {@code new}.
     */
    private AppPaths() {
    }

    /**
     * Restituisce il percorso della directory principale dell'applicazione.
     *
     * @return percorso della directory applicativa
     */
    public static Path appDir() {
        return APP_DIR;
    }

    /**
     * Restituisce il percorso della directory contenente i dati applicativi.
     * <p>
     * Se la cartella non esiste viene creata automaticamente.
     *
     * @return percorso della cartella dati dell'applicazione
     * @throws IllegalStateException se la cartella non può essere creata
     */
    public static Path dataDir() {
        Path dir = APP_DIR.resolve("data");

        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Impossibile creare la cartella data: " + dir,
                    ex
            );
        }

        return dir;
    }

    /**
     * Restituisce il percorso completo di un file situato nella cartella dati
     * dell'applicazione.
     *
     * @param fileName nome del file da localizzare
     * @return percorso completo del file richiesto
     */
    public static Path dataFile(String fileName) {
        return dataDir().resolve(fileName);
    }

    /**
     * Individua e prepara la directory principale dell'applicazione
     * in una posizione appropriata per il sistema operativo corrente.
     * <p>
     * Percorsi utilizzati:
     * <ul>
     *     <li>Windows: {@code %APPDATA%\Anagr_fe}</li>
     *     <li>macOS: {@code ~/Library/Anagr_fe}</li>
     *     <li>Linux: {@code ~/.config/Anagr_fe}</li>
     * </ul>
     *
     * @return percorso della directory applicativa
     * @throws IllegalStateException se la directory non può essere determinata
     *                               oppure creata
     */
    private static Path detectAppDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path appDir;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");

            if (appData == null || appData.isBlank()) {
                throw new IllegalStateException(
                        "Variabile d'ambiente APPDATA non disponibile"
                );
            }

            appDir = Paths.get(appData, "Anagr_fe");

        } else if (os.contains("mac")) {
            appDir = Paths.get(
                    System.getProperty("user.home"),
                    "Library",
                    "Anagr_fe"
            );

        } else {
            appDir = Paths.get(
                    System.getProperty("user.home"),
                    ".config",
                    "Anagr_fe"
            );
        }

        try {
            Files.createDirectories(appDir);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Impossibile creare la cartella applicativa: " + appDir,
                    ex
            );
        }

        return appDir;
    }
}