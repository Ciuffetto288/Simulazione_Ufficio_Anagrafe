import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Esporta l'archivio in TXT (colonne allineate) o CSV (per Excel).
 * In entrambi i casi si mostrano comune e provincia, non il codice catastale:
 * quello resta un dato interno che serve solo al calcolo.
 */
public final class ExportService {

    // data e ora nel nome del file, cosi' un export non sovrascrive il precedente
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public ExportService() {
    }

    /**
     * Export leggibile a schermo o da stampare.
     *
     * @param cittadini record da esportare
     * @return il file creato nella cartella dati
     */
    public Path exportTxt(List<Cittadino> cittadini) {
        Path output = AppPaths.dataFile("export_cittadini_" + STAMP.format(LocalDateTime.now()) + ".txt");
        List<String> lines = new ArrayList<>();
        lines.add("ANAGR@FE - ESPORTAZIONE ARCHIVIO");
        lines.add("Generato: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lines.add("");
        lines.add(String.format("%-18s %-18s %-12s %-4s %-22s %-4s %-16s",
                "Nome", "Cognome", "Nascita", "S", "Comune", "Prov", "Codice Fiscale"));
        lines.add("-".repeat(100));
        cittadini.forEach(c -> lines.add(c.toTableLine()));
        write(output, lines);
        return output;
    }

    /**
     * Export da aprire con un foglio di calcolo.
     *
     * @param cittadini record da esportare
     * @return il file creato nella cartella dati
     */
    public Path exportCsv(List<Cittadino> cittadini) {
        Path output = AppPaths.dataFile("export_cittadini_" + STAMP.format(LocalDateTime.now()) + ".csv");
        List<String> lines = new ArrayList<>();
        lines.add("nome;cognome;dataNascita;sesso;comune;provincia;codiceFiscale");
        cittadini.forEach(c -> lines.add(c.toCsvLine()));
        write(output, lines);
        return output;
    }

    // scrittura vera e propria, in UTF-8 per non perdere gli accenti
    private void write(Path output, List<String> lines) {
        try {
            Files.createDirectories(output.getParent());
            Files.write(output, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile esportare: " + output, ex);
        }
    }
}
