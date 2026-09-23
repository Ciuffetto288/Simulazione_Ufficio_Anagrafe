import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


public final class Cittadino {

    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    
    private char sesso;
    private String comune;
    private String provincia;
    
    private String codiceComune;
    private String codiceFiscale;

    
    public Cittadino(
            String nome,
            String cognome,
            LocalDate dataNascita,
            char sesso,
            String comune,
            String provincia,
            String codiceComune,
            String codiceFiscale
    ) {
        this.nome = StringUtils.capitalizeWords(nome);
        this.cognome = StringUtils.capitalizeWords(cognome);
        this.dataNascita = dataNascita;
        this.sesso = Character.toUpperCase(sesso);
        this.comune = StringUtils.capitalizeWords(comune);
        this.provincia = provincia == null ? "" : provincia.trim().toUpperCase(Locale.ITALIAN);
        this.codiceComune = codiceComune == null ? "" : codiceComune.trim().toUpperCase(Locale.ITALIAN);
        this.codiceFiscale = codiceFiscale == null ? "" : codiceFiscale.trim().toUpperCase(Locale.ITALIAN);
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public char getSesso() {
        return sesso;
    }

    public String getComune() {
        return comune;
    }

    public String getProvincia() {
        return provincia;
    }

    
    public String getCodiceComune() {
        return codiceComune;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    
    public void update(
            String nome,
            String cognome,
            LocalDate dataNascita,
            char sesso,
            String comune,
            String provincia,
            String codiceComune,
            String codiceFiscale
    ) {
        this.nome = StringUtils.capitalizeWords(nome);
        this.cognome = StringUtils.capitalizeWords(cognome);
        this.dataNascita = dataNascita;
        this.sesso = Character.toUpperCase(sesso);
        this.comune = StringUtils.capitalizeWords(comune);
        this.provincia = provincia == null ? "" : provincia.trim().toUpperCase(Locale.ITALIAN);
        this.codiceComune = codiceComune == null ? "" : codiceComune.trim().toUpperCase(Locale.ITALIAN);
        this.codiceFiscale = codiceFiscale == null ? "" : codiceFiscale.trim().toUpperCase(Locale.ITALIAN);
    }

    
    public boolean matches(String query) {
        String normalized = StringUtils.normalizeSearch(query);
        return StringUtils.normalizeSearch(nome).contains(normalized)
                || StringUtils.normalizeSearch(cognome).contains(normalized)
                || codiceFiscale.contains(normalized);
    }

    
    public String toStorageLine() {
        return String.join(";",
                StringUtils.csvEscape(nome),
                StringUtils.csvEscape(cognome),
                DateUtils.formatStorage(dataNascita),
                Character.toString(sesso),
                StringUtils.csvEscape(comune),
                StringUtils.csvEscape(provincia),
                StringUtils.csvEscape(codiceComune),
                StringUtils.csvEscape(codiceFiscale)
        );
    }

    
    public String toCsvLine() {
        return String.join(";",
                StringUtils.csvEscape(nome),
                StringUtils.csvEscape(cognome),
                DateUtils.formatItalian(dataNascita),
                Character.toString(sesso),
                StringUtils.csvEscape(comune),
                StringUtils.csvEscape(provincia),
                StringUtils.csvEscape(codiceFiscale)
        );
    }

    
    public String toTableLine() {
        return String.format("%-18s %-18s %-12s %-4s %-22s %-4s %-16s",
                trim(nome, 18),
                trim(cognome, 18),
                DateUtils.formatItalian(dataNascita),
                sesso,
                trim(comune, 22),
                provincia,
                codiceFiscale
        );
    }

    
    public String toCard() {
        String[] rows = {
                row("Nome", nome),
                row("Cognome", cognome),
                row("Data nascita", DateUtils.formatItalian(dataNascita)),
                row("Comune", comune),
                row("Provincia", provincia),
                row("Sesso", Character.toString(sesso)),
                row("Codice Fiscale", codiceFiscale)
        };
        StringBuilder card = new StringBuilder();
        card.append("+----------------------------------------------------------+\n");
        card.append("|                       CITTADINO                          |\n");
        card.append("+----------------------------------------------------------+\n");
        for (String row : rows) {
            card.append(row).append('\n');
        }
        card.append("+----------------------------------------------------------+");
        return card.toString();
    }

    
    public static Optional<Cittadino> fromStorageLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        List<String> parts = StringUtils.splitCsvLine(line, ';');
        if (parts.size() < 8) {
            return Optional.empty();
        }
        
        
        Optional<LocalDate> date = DateUtils.parseStorageDate(parts.get(2));
        if (date.isEmpty()) {
            date = DateUtils.parseItalianDate(parts.get(2));
        }
        if (date.isEmpty() || parts.get(3).isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new Cittadino(
                parts.get(0),
                parts.get(1),
                date.get(),
                parts.get(3).charAt(0),
                parts.get(4),
                parts.get(5),
                parts.get(6),
                parts.get(7)
        ));
    }

    
    private static String row(String label, String value) {
        return String.format("| %-18s | %-34s |", label, trim(value, 34));
    }

    
    private static String trim(String value, int max) {
        String safe = value == null ? "" : value;
        if (safe.length() <= max) {
            return safe;
        }
        return safe.substring(0, Math.max(0, max - 1)) + ".";
    }
}
