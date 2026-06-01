import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe di Servizio per la gestione, la persistenza e la manipolazione dell'archivio dei cittadini.
 * Gestisce la lettura e la scrittura dei dati su un file CSV dedicato.
 */
public final class ArchivioService {
    /**
     * Intestazione standard del file CSV dell'archivio.
     */
    private static final String HEADER = "nome;cognome;dataNascita;sesso;comune;provincia;codiceComune;codiceFiscale";
    
    /**
     * Il percorso del file CSV in cui vengono salvati i dati.
     */
    private final Path path;
    
    /**
     * Lista in memoria contenente tutti i cittadini caricati dall'archivio.
     */
    private final List<Cittadino> cittadini = new ArrayList<>();

    /**
     * Inizializza un nuovo servizio di archivio.
     * Imposta il percorso del file CSV tramite {@link AppPaths} e carica i dati esistenti in memoria.
     */
    public ArchivioService() {
        this.path = AppPaths.dataFile("cittadini.csv");
        load();
    }

    /**
     * Restituisce la lista di tutti i cittadini presenti in archivio.
     * La lista viene ordinata per cognome, nome e infine codice fiscale.
     * 
     * @return Una lista ordinata di oggetti {@link Cittadino}
     */
    public List<Cittadino> all() {
        return cittadini.stream()
                .sorted(Comparator.comparing(Cittadino::getCognome)
                        .thenComparing(Cittadino::getNome)
                        .thenComparing(Cittadino::getCodiceFiscale))
                .toList();
    }

    /**
     * Aggiunge un nuovo cittadino all'archivio in memoria e salva le modifiche su file.
     * 
     * @param cittadino Il cittadino da aggiungere
     */
    public void add(Cittadino cittadino) {
        cittadini.add(cittadino);
        save();
    }

    /**
     * Aggiorna i dati di un cittadino esistente identificato dal suo vecchio codice fiscale.
     * Salva automaticamente le modifiche su file se l'operazione va a buon fine.
     * 
     * @param oldCodiceFiscale Il codice fiscale del cittadino da modificare
     * @param updated          I nuovi dati aggiornati del cittadino
     * @throws IllegalArgumentException Se il cittadino con il codice fiscale specificato non viene trovato
     */
    public void update(String oldCodiceFiscale, Cittadino updated) {
        for (int i = 0; i < cittadini.size(); i++) {
            if (cittadini.get(i).getCodiceFiscale().equalsIgnoreCase(oldCodiceFiscale)) {
                cittadini.set(i, updated);
                save();
                return;
            }
        }
        throw new IllegalArgumentException("Cittadino non trovato: " + oldCodiceFiscale);
    }

    /**
     * Rimuove un cittadino dall'archivio in base al codice fiscale e aggiorna il file.
     * 
     * @param codiceFiscale Il codice fiscale del cittadino da eliminare
     * @return true se il cittadino è stato rimosso con successo, false altrimenti
     */
    public boolean delete(String codiceFiscale) {
        boolean removed = cittadini.removeIf(c -> c.getCodiceFiscale().equalsIgnoreCase(codiceFiscale));
        if (removed) {
            save();
        }
        return removed;
    }

    /**
     * Cerca un cittadino all'interno dell'archivio tramite il codice fiscale.
     * 
     * @param codiceFiscale Il codice fiscale da cercare
     * @return Un {@link Optional} contenente il cittadino trovato, oppure vuoto se non presente o se il parametro è null
     */
    public Optional<Cittadino> findByCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null) {
            return Optional.empty();
        }
        return cittadini.stream()
                .filter(c -> c.getCodiceFiscale().equalsIgnoreCase(codiceFiscale.trim()))
                .findFirst();
    }

    /**
     * Esegue una ricerca testuale flessibile all'interno dell'archivio dei cittadini.
     * Rispetta i criteri di corrispondenza interni della classe Cittadino e ordina per cognome e nome.
     * 
     * @param query Il testo o criterio di ricerca
     * @return Una lista di cittadini che corrispondono alla ricerca
     */
    public List<Cittadino> search(String query) {
        return cittadini.stream()
                .filter(c -> c.matches(query))
                .sorted(Comparator.comparing(Cittadino::getCognome).thenComparing(Cittadino::getNome))
                .toList();
    }

    /**
     * Estrae tutti i codici fiscali univoci attualmente registrati nell'archivio.
     * 
     * @return Un insieme (Set) di stringhe contenente i codici fiscali
     */
    public Set<String> fiscalCodes() {
        return cittadini.stream()
                .map(Cittadino::getCodiceFiscale)
                .collect(Collectors.toSet());
    }

    /**
     * Estrae tutti i codici fiscali univoci dall'archivio, escludendo quello specificato.
     * Utile per i controlli di unicità in fase di modifica dati.
     * 
     * @param codiceFiscale Il codice fiscale da escludere dall'insieme finale
     * @return Un insieme (Set) di stringhe contenente i codici fiscali rimanenti
     */
    public Set<String> fiscalCodesExcept(String codiceFiscale) {
        return cittadini.stream()
                .map(Cittadino::getCodiceFiscale)
                .filter(cf -> !cf.equalsIgnoreCase(codiceFiscale))
                .collect(Collectors.toSet());
    }

    /**
     * Restituisce il percorso del file CSV associato a questo archivio.
     * 
     * @return Il Path del file dei dati
     */
    public Path getPath() {
        return path;
    }

    /**
     * Sincronizza la lista in memoria leggendo tutte le righe dal file CSV.
     * Se il file non esiste, si occupa di generarlo vuoto con la sola intestazione.
     * 
     * @throws IllegalStateException Se si verifica un errore IO durante la lettura del file
     */
    private void load() {
        cittadini.clear();
        if (!Files.exists(path)) {
            save();
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank() || line.toLowerCase().startsWith("nome;")) {
                    continue;
                }
                Cittadino.fromStorageLine(line).ifPresent(cittadini::add);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile leggere archivio cittadini: " + path, ex);
        }
    }

    /**
     * Scrive i dati della lista in memoria direttamente all'interno del file CSV.
     * Applica un ordinamento automatico per cognome e nome prima del salvataggio.
     * 
     * @throws IllegalStateException Se si verifica un errore IO durante la scrittura o la creazione delle directory
     */
    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        cittadini.stream()
                .sorted(Comparator.comparing(Cittadino::getCognome).thenComparing(Cittadino::getNome))
                .map(Cittadino::toStorageLine)
                .forEach(lines::add);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile salvare archivio cittadini: " + path, ex);
        }
    }
}
