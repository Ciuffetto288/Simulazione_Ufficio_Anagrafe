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
 * Archivio dei cittadini.
 * I record stanno in memoria in una lista e vengono riscritti interamente su
 * cittadini.csv dopo ogni modifica: l'archivio e' piccolo, quindi conviene
 * tenerlo semplice invece di aggiornare il file riga per riga.
 */
public final class ArchivioService {
    // prima riga del file, riscritta a ogni salvataggio
    private static final String HEADER = "nome;cognome;dataNascita;sesso;comune;provincia;codiceComune;codiceFiscale";
    
    private final Path path;
    private final List<Cittadino> cittadini = new ArrayList<>();

    /**
     * Carica subito l'archivio esistente, cosi' il menu parte con i dati pronti.
     */
    public ArchivioService() {
        this.path = AppPaths.dataFile("cittadini.csv");
        load();
    }

    /**
     * @return tutti i cittadini, ordinati per cognome, nome e codice fiscale
     */
    public List<Cittadino> all() {
        return cittadini.stream()
                .sorted(Comparator.comparing(Cittadino::getCognome)
                        .thenComparing(Cittadino::getNome)
                        .thenComparing(Cittadino::getCodiceFiscale))
                .toList();
    }

    /**
     * @param cittadino record da inserire (il file viene aggiornato subito)
     */
    public void add(Cittadino cittadino) {
        cittadini.add(cittadino);
        save();
    }

    /**
     * Sostituisce un record. Serve il vecchio codice fiscale perche' dopo una
     * modifica dei dati anagrafici quello nuovo e' diverso.
     *
     * @param oldCodiceFiscale codice fiscale attuale del record
     * @param updated          record con i dati nuovi
     * @throws IllegalArgumentException se quel codice fiscale non e' in archivio
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
     * @param codiceFiscale codice fiscale del record da eliminare
     * @return true se c'era qualcosa da eliminare
     */
    public boolean delete(String codiceFiscale) {
        boolean removed = cittadini.removeIf(c -> c.getCodiceFiscale().equalsIgnoreCase(codiceFiscale));
        if (removed) {
            save();
        }
        return removed;
    }

    /**
     * Ricerca esatta per codice fiscale, ignorando maiuscole e spazi ai bordi.
     *
     * @param codiceFiscale codice da cercare
     * @return il cittadino, oppure Optional vuoto
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
     * Ricerca libera (vedi {@link Cittadino#matches(String)}).
     *
     * @param query testo cercato
     * @return i risultati ordinati per cognome e nome
     */
    public List<Cittadino> search(String query) {
        return cittadini.stream()
                .filter(c -> c.matches(query))
                .sorted(Comparator.comparing(Cittadino::getCognome).thenComparing(Cittadino::getNome))
                .toList();
    }

    /**
     * @return i codici fiscali gia' usati, per controllare le collisioni
     */
    public Set<String> fiscalCodes() {
        return cittadini.stream()
                .map(Cittadino::getCodiceFiscale)
                .collect(Collectors.toSet());
    }

    /**
     * Come fiscalCodes() ma senza un codice preciso: in modifica il record che
     * si sta modificando non deve contare come collisione con se stesso.
     *
     * @param codiceFiscale codice da escludere
     * @return gli altri codici fiscali in archivio
     */
    public Set<String> fiscalCodesExcept(String codiceFiscale) {
        return cittadini.stream()
                .map(Cittadino::getCodiceFiscale)
                .filter(cf -> !cf.equalsIgnoreCase(codiceFiscale))
                .collect(Collectors.toSet());
    }

    public Path getPath() {
        return path;
    }

    // rilegge il file dall'inizio; al primo avvio lo crea con la sola intestazione
    private void load() {
        cittadini.clear();
        if (!Files.exists(path)) {
            save();
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                // salto righe vuote e intestazione
                if (line.isBlank() || line.toLowerCase().startsWith("nome;")) {
                    continue;
                }
                Cittadino.fromStorageLine(line).ifPresent(cittadini::add);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile leggere archivio cittadini: " + path, ex);
        }
    }

    // riscrive tutto il file, ordinato, cosi' resta leggibile anche a mano
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
