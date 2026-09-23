import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class AppPaths {

    
    private static final Path APP_DIR = detectAppDir();

    private AppPaths() {
    }

    public static Path appDir() {
        return APP_DIR;
    }

    
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

    
    public static Path dataFile(String fileName) {
        return dataDir().resolve(fileName);
    }

    
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