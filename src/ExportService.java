import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public final class ExportService {

    
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public ExportService() {
    }

    
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

    
    public Path exportCsv(List<Cittadino> cittadini) {
        Path output = AppPaths.dataFile("export_cittadini_" + STAMP.format(LocalDateTime.now()) + ".csv");
        List<String> lines = new ArrayList<>();
        lines.add("nome;cognome;dataNascita;sesso;comune;provincia;codiceFiscale");
        cittadini.forEach(c -> lines.add(c.toCsvLine()));
        write(output, lines);
        return output;
    }

    
    private void write(Path output, List<String> lines) {
        try {
            Files.createDirectories(output.getParent());
            Files.write(output, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile esportare: " + output, ex);
        }
    }
}
