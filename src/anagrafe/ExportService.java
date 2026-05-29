import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per l'esportazione dell'archivio cittadini in file esterni.
 *
 * <p>Supporta il formato testuale tabellare e il formato CSV. Le esportazioni
 * sono pensate per l'utente finale: mostrano nome, cognome, data, sesso, comune,
 * provincia e codice fiscale, ma non il codice catastale usato internamente.</p>
 */
public final class ExportService {

    /**
     * Formattatore temporale per generare un suffisso univoco nei file esportati.
     */
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Costruisce un servizio di esportazione senza stato interno.
     */
    public ExportService() {
    }

    /**
     * Esporta i cittadini in un file di testo con colonne allineate.
     *
     * @param cittadini lista di oggetti {@link Cittadino} da includere nell'esportazione
     * @return {@link Path} del file di testo creato nella cartella dati
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
     * Esporta i cittadini in un file CSV importabile in fogli di calcolo.
     *
     * <p>L'intestazione non include il codice catastale, perché chi consulta il
     * file deve leggere direttamente comune e provincia.</p>
     *
     * @param cittadini lista di oggetti {@link Cittadino} da esportare
     * @return {@link Path} del file CSV creato nella cartella dati
     */
    public Path exportCsv(List<Cittadino> cittadini) {
        Path output = AppPaths.dataFile("export_cittadini_" + STAMP.format(LocalDateTime.now()) + ".csv");
        List<String> lines = new ArrayList<>();
        lines.add("nome;cognome;dataNascita;sesso;comune;provincia;codiceFiscale");
        cittadini.forEach(c -> lines.add(c.toCsvLine()));
        write(output, lines);
        return output;
    }

    /**
     * Scrive le righe di esportazione su disco creando la cartella di destinazione se necessario.
     *
     * @param output percorso del file di destinazione
     * @param lines righe di testo da scrivere
     * @throws IllegalStateException se si verifica un errore IO durante la scrittura
     */
    private void write(Path output, List<String> lines) {
        try {
            Files.createDirectories(output.getParent());
            Files.write(output, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile esportare: " + output, ex);
        }
    }
}
