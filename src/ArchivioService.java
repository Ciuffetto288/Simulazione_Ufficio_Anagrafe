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


public final class ArchivioService {
    
    private static final String HEADER = "nome;cognome;dataNascita;sesso;comune;provincia;codiceComune;codiceFiscale";
    
    private final Path path;
    private final List<Cittadino> cittadini = new ArrayList<>();

    
    public ArchivioService() {
        this.path = AppPaths.dataFile("cittadini.csv");
        load();
    }

    
    public List<Cittadino> all() {
        return cittadini.stream()
                .sorted(Comparator.comparing(Cittadino::getCognome)
                        .thenComparing(Cittadino::getNome)
                        .thenComparing(Cittadino::getCodiceFiscale))
                .toList();
    }

    
    public void add(Cittadino cittadino) {
        cittadini.add(cittadino);
        save();
    }

    
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

    
    public boolean delete(String codiceFiscale) {
        boolean removed = cittadini.removeIf(c -> c.getCodiceFiscale().equalsIgnoreCase(codiceFiscale));
        if (removed) {
            save();
        }
        return removed;
    }

    
    public Optional<Cittadino> findByCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null) {
            return Optional.empty();
        }
        return cittadini.stream()
                .filter(c -> c.getCodiceFiscale().equalsIgnoreCase(codiceFiscale.trim()))
                .findFirst();
    }

    
    public List<Cittadino> search(String query) {
        return cittadini.stream()
                .filter(c -> c.matches(query))
                .sorted(Comparator.comparing(Cittadino::getCognome).thenComparing(Cittadino::getNome))
                .toList();
    }

    
    public Set<String> fiscalCodes() {
        return cittadini.stream()
                .map(Cittadino::getCodiceFiscale)
                .collect(Collectors.toSet());
    }

    
    public Set<String> fiscalCodesExcept(String codiceFiscale) {
        return cittadini.stream()
                .map(Cittadino::getCodiceFiscale)
                .filter(cf -> !cf.equalsIgnoreCase(codiceFiscale))
                .collect(Collectors.toSet());
    }

    public Path getPath() {
        return path;
    }

    
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
