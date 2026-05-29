import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Classe Utility per la gestione e la localizzazione dei percorsi (Path) all'interno del progetto.
 */
public final class AppPaths {
    /**
     * Localizzazione della directory principale del progetto.
     */
    private static final Path PROJECT_DIR = detectProjectDir();
    /**
     * Costruttore privato vuoto per impedire l'istanziazione della classe.
     * Essendo una classe di sole utility con metodi statici, evita l'uso non necessario di 'new AppPaths()'.
     */
    private AppPaths() {
    }
    
     /**
     * Restituisce il percorso della directory principale del progetto.
     * 
     * @return Il Path della directory radice del progetto
     */
    public static Path projectDir() {
        return PROJECT_DIR;
    }
    /**
     * Restituisce il percorso della cartella "data".
     * Se la cartella non esiste, tenta di crearla automaticamente in modo sicuro.
     * 
     * @return Il Path della cartella dei dati
     * @throws IllegalStateException Se si verifica un errore IO durante la creazione della cartella
     */
    public static Path dataDir() {
        Path dir = PROJECT_DIR.resolve("data");
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile creare la cartella data: " + dir, ex);
        }
        return dir;
    }
   /**
     * Restituisce il percorso specifico di un file posizionato all'interno della cartella "data".
     * 
     * @param fileName Il nome del file da localizzare
     * @return Il Path completo del file richiesto
     */
    public static Path dataFile(String fileName) {
        return dataDir().resolve(fileName);
    }
   /**
     * Rileva automaticamente la directory radice del progetto risalendo l'albero delle cartelle.
     * Cerca la presenza contemporanea delle cartelle "src" e "data".
     * 
     * @return Il Path rilevato del progetto, oppure il percorso corrente come fallback
     */
    private static Path detectProjectDir() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        Path cursor = current;
        while (cursor != null) {
            if (Files.isDirectory(cursor.resolve("src")) && Files.isDirectory(cursor.resolve("data"))) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        Path nested = current.resolve("ANAGRAFE");
        if (Files.isDirectory(nested.resolve("src")) && Files.isDirectory(nested.resolve("data"))) {
            return nested;
        }
        return current;
    }
}
