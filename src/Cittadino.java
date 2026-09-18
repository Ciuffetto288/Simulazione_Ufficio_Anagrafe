import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Il record di un cittadino in archivio.
 * Tiene dentro anche il codice catastale del comune, che serve per ricalcolare
 * il codice fiscale in caso di modifica, ma non lo mostra mai all'utente:
 * nelle schede e negli export si vedono comune e provincia.
 */
public final class Cittadino {

    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    // M o F
    private char sesso;
    private String comune;
    private String provincia;
    // catastale: dato interno, non si mostra all'utente
    private String codiceComune;
    private String codiceFiscale;

    /**
     * Normalizza tutto quello che arriva: iniziali maiuscole su nome, cognome e
     * comune, maiuscolo pieno su provincia, codici e sesso. Cosi' i dati
     * risultano uniformi anche se l'utente scrive "mario ROSSI".
     *
     * @param nome nome del cittadino
     * @param cognome cognome del cittadino
     * @param dataNascita data di nascita
     * @param sesso M oppure F
     * @param comune comune di nascita
     * @param provincia sigla della provincia
     * @param codiceComune codice catastale del comune
     * @param codiceFiscale codice fiscale calcolato
     */
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

    /**
     * Usato da calcolo e salvataggio, non dalle stampe a schermo.
     *
     * @return il codice catastale del comune di nascita
     */
    public String getCodiceComune() {
        return codiceComune;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    /**
     * Sovrascrive tutti i campi, con le stesse regole del costruttore.
     * Viene chiamato dalla voce "modifica cittadino" del menu.
     *
     * @param nome nuovo nome
     * @param cognome nuovo cognome
     * @param dataNascita nuova data di nascita
     * @param sesso nuovo sesso
     * @param comune nuovo comune
     * @param provincia nuova provincia
     * @param codiceComune nuovo codice catastale
     * @param codiceFiscale codice fiscale ricalcolato
     */
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

    /**
     * Ricerca libera su nome, cognome e codice fiscale.
     *
     * @param query testo inserito dall'utente
     * @return true se almeno uno dei tre campi contiene la query
     */
    public boolean matches(String query) {
        String normalized = StringUtils.normalizeSearch(query);
        return StringUtils.normalizeSearch(nome).contains(normalized)
                || StringUtils.normalizeSearch(cognome).contains(normalized)
                || codiceFiscale.contains(normalized);
    }

    /**
     * Riga da scrivere in cittadini.csv: 8 campi, codice catastale compreso,
     * data in ISO. E' il formato che si rilegge all'avvio.
     *
     * @return la riga separata da punto e virgola
     */
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

    /**
     * Riga per l'export: 7 campi, senza codice catastale e con la data in
     * formato italiano. Non e' lo stesso formato del salvataggio.
     *
     * @return la riga da mettere nel file esportato
     */
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

    /**
     * @return la riga a colonne fisse usata nella tabella e nell'export TXT
     */
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

    /**
     * Scheda singola con la cornice in ASCII, quella che si vede dopo un
     * salvataggio o una ricerca.
     *
     * @return la scheda su piu' righe
     */
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

    /**
     * Ricostruisce un cittadino da una riga del file.
     * Le righe rovinate o incomplete vengono scartate senza bloccare la
     * lettura del resto dell'archivio.
     *
     * @param line riga letta dal CSV
     * @return il cittadino, oppure Optional vuoto se la riga non va bene
     */
    public static Optional<Cittadino> fromStorageLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        List<String> parts = StringUtils.splitCsvLine(line, ';');
        if (parts.size() < 8) {
            return Optional.empty();
        }
        // normalmente la data e' in ISO, ma provo anche il formato italiano
        // per gli archivi salvati con le prime versioni del programma
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

    // una riga della scheda: etichetta a sinistra, valore a destra
    private static String row(String label, String value) {
        return String.format("| %-18s | %-34s |", label, trim(value, 34));
    }

    // taglia i valori troppo lunghi, altrimenti la cornice si sfonda
    private static String trim(String value, int max) {
        String safe = value == null ? "" : value;
        if (safe.length() <= max) {
            return safe;
        }
        return safe.substring(0, Math.max(0, max - 1)) + ".";
    }
}
