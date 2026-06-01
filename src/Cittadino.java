import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Rappresenta un cittadino gestito dall'archivio anagrafico.
 *
 * <p>La classe conserva sia i dati leggibili dall'utente, come nome, cognome,
 * comune e provincia, sia il codice catastale necessario al calcolo del codice
 * fiscale. Il codice catastale resta quindi disponibile internamente, ma non
 * viene mostrato nelle schede e nelle esportazioni pensate per l'utente finale.</p>
 */
public final class Cittadino {

    /**
     * Nome del cittadino.
     */
    private String nome;

    /**
     * Cognome del cittadino.
     */
    private String cognome;

    /**
     * Data di nascita del cittadino.
     */
    private LocalDate dataNascita;

    /**
     * Sesso anagrafico del cittadino, espresso come {@code M} oppure {@code F}.
     */
    private char sesso;

    /**
     * Nome leggibile del comune di nascita.
     */
    private String comune;

    /**
     * Sigla della provincia di nascita.
     */
    private String provincia;

    /**
     * Codice catastale del comune, usato solo per calcolo e persistenza interna.
     */
    private String codiceComune;

    /**
     * Codice fiscale del cittadino.
     */
    private String codiceFiscale;

    /**
     * Costruisce un nuovo cittadino normalizzando i valori testuali ricevuti.
     *
     * @param nome nome del cittadino
     * @param cognome cognome del cittadino
     * @param dataNascita data di nascita
     * @param sesso sesso anagrafico, {@code M} oppure {@code F}
     * @param comune comune di nascita
     * @param provincia sigla della provincia
     * @param codiceComune codice catastale del comune, necessario al codice fiscale
     * @param codiceFiscale codice fiscale del cittadino
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

    /**
     * Restituisce il nome del cittadino.
     *
     * @return nome del cittadino
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce il cognome del cittadino.
     *
     * @return cognome del cittadino
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Restituisce la data di nascita.
     *
     * @return data di nascita del cittadino
     */
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    /**
     * Restituisce il sesso anagrafico.
     *
     * @return {@code M} oppure {@code F}
     */
    public char getSesso() {
        return sesso;
    }

    /**
     * Restituisce il comune di nascita in forma leggibile.
     *
     * @return nome del comune
     */
    public String getComune() {
        return comune;
    }

    /**
     * Restituisce la sigla della provincia di nascita.
     *
     * @return sigla provincia
     */
    public String getProvincia() {
        return provincia;
    }

    /**
     * Restituisce il codice catastale del comune.
     *
     * <p>Questo dato è necessario per il calcolo del codice fiscale e per lo
     * storage interno, ma viene nascosto nelle visualizzazioni rivolte all'utente.</p>
     *
     * @return codice catastale del comune
     */
    public String getCodiceComune() {
        return codiceComune;
    }

    /**
     * Restituisce il codice fiscale.
     *
     * @return codice fiscale del cittadino
     */
    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    /**
     * Aggiorna tutti i dati anagrafici applicando la stessa normalizzazione del costruttore.
     *
     * @param nome nuovo nome
     * @param cognome nuovo cognome
     * @param dataNascita nuova data di nascita
     * @param sesso nuovo sesso anagrafico
     * @param comune nuovo comune di nascita
     * @param provincia nuova provincia
     * @param codiceComune nuovo codice catastale interno del comune
     * @param codiceFiscale nuovo codice fiscale
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
     * Verifica se il cittadino corrisponde a una query testuale.
     *
     * @param query testo inserito dall'utente per la ricerca
     * @return {@code true} se la query è contenuta in nome, cognome o codice fiscale
     */
    public boolean matches(String query) {
        String normalized = StringUtils.normalizeSearch(query);
        return StringUtils.normalizeSearch(nome).contains(normalized)
                || StringUtils.normalizeSearch(cognome).contains(normalized)
                || codiceFiscale.contains(normalized);
    }

    /**
     * Genera una riga CSV per il salvataggio persistente dell'archivio.
     *
     * <p>Questa riga mantiene anche il codice catastale, perché il file di storage
     * deve conservare tutti i dati necessari a ricalcoli e modifiche future.</p>
     *
     * @return stringa CSV interna con i valori separati da punto e virgola
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
     * Genera una riga CSV destinata all'esportazione per l'utente.
     *
     * <p>La riga mostra comune e provincia, ma non il codice catastale, così il
     * file esportato rimane leggibile senza conoscere codici amministrativi.</p>
     *
     * @return stringa CSV per esportazione esterna
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
     * Genera una riga a colonne fisse per la visualizzazione tabellare.
     *
     * @return stringa formattata per viste tabellari da terminale
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
     * Genera una scheda testuale del cittadino incorniciata in ASCII.
     *
     * <p>La scheda espone i dati utili a una persona: nome, cognome, data,
     * comune, provincia, sesso e codice fiscale. Il codice catastale resta
     * nascosto perché è un dato tecnico interno.</p>
     *
     * @return stringa multilinea contenente la scheda grafica del cittadino
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
     * Ricostruisce un cittadino partendo da una riga CSV letta dallo storage.
     *
     * @param line riga di testo CSV letta dal file
     * @return {@link Optional} con il cittadino ricostruito, oppure vuoto se la riga non è valida
     */
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

    /**
     * Genera una riga della scheda testuale allineando etichetta e valore.
     *
     * @param label etichetta del campo informativo
     * @param value valore testuale associato all'etichetta
     * @return stringa formattata e racchiusa tra i bordi della scheda
     */
    private static String row(String label, String value) {
        return String.format("| %-18s | %-34s |", label, trim(value, 34));
    }

    /**
     * Taglia una stringa se supera la lunghezza massima consentita.
     *
     * @param value stringa di testo originale da verificare
     * @param max numero massimo di caratteri consentiti
     * @return stringa originale se rientra nei limiti, altrimenti valore troncato con punto finale
     */
    private static String trim(String value, int max) {
        String safe = value == null ? "" : value;
        if (safe.length() <= max) {
            return safe;
        }
        return safe.substring(0, Math.max(0, max - 1)) + ".";
    }
}
