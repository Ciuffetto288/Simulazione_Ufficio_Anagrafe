import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Decide dove tenere i file dell'applicazione.
 * I dati non stanno nella cartella del programma ma nella cartella utente del
 * sistema (APPDATA su Windows, ~/Library su mac, ~/.config su Linux), dentro
 * una sottocartella "data": in questo modo l'archivio sopravvive anche se si
 * sposta o ricompila il progetto.
 */
public final class AppPaths {

    // calcolata una volta sola al primo utilizzo della classe
    private static final Path APP_DIR = detectAppDir();

    private AppPaths() {
    }

    public static Path appDir() {
        return APP_DIR;
    }

    /**
     * Cartella dei dati, creata al volo se non esiste ancora.
     *
     * @return il percorso della cartella
     * @throws IllegalStateException se non si riesce a crearla
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
     * @param fileName nome del file dentro la cartella dati
     * @return il percorso completo
     */
    public static Path dataFile(String fileName) {
        return dataDir().resolve(fileName);
    }

    // sceglie la cartella in base al sistema operativo e la crea se manca
    private static Path detectAppDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path appDir;

        if (os.contains("win")) {
            // su Windows il posto giusto e' APPDATA, non la home
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