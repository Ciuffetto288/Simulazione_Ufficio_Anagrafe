import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Servizio dedicato alla generazione, verifica e gestione del Codice Fiscale italiano.
 * 
 * Implementa le regole ufficiali ministeriali per:
 * <ul>
 *     <li>Calcolo delle componenti anagrafiche</li>
 *     <li>Gestione del carattere di controllo</li>
 *     <li>Validazione strutturale del codice fiscale</li>
 *     <li>Supporto delle varianti omocodiche</li>
 * </ul>
 */
public final class CodiceFiscaleService {

    /**
     * Costruisce un servizio per calcolo, verifica e gestione dell'omocodia.
     */
    public CodiceFiscaleService() {
    }

    /**
     * Mappa di conversione tra numero del mese e relativo codice alfabetico ministeriale.
     */
    private static final Map<Integer, Character> MONTH_CODES = Map.ofEntries(
            Map.entry(1, 'A'),
            Map.entry(2, 'B'),
            Map.entry(3, 'C'),
            Map.entry(4, 'D'),
            Map.entry(5, 'E'),
            Map.entry(6, 'H'),
            Map.entry(7, 'L'),
            Map.entry(8, 'M'),
            Map.entry(9, 'P'),
            Map.entry(10, 'R'),
            Map.entry(11, 'S'),
            Map.entry(12, 'T')
    );

    /**
     * Mappa inversa dei codici mese utilizzata in fase di validazione e decodifica.
     */
    private static final Map<Character, Integer> MONTH_VALUES = createMonthValues();

    /**
     * Mappa ufficiale di conversione numeri-lettere per la gestione dell'omocodia.
     */
    private static final Map<Character, Character> OMOCODIA = Map.of(
            '0', 'L',
            '1', 'M',
            '2', 'N',
            '3', 'P',
            '4', 'Q',
            '5', 'R',
            '6', 'S',
            '7', 'T',
            '8', 'U',
            '9', 'V'
    );

    /**
     * Mappa inversa utilizzata per decodificare le lettere omocodiche nei valori numerici originali.
     */
    private static final Map<Character, Character> OMOCODIA_REVERSE = createReverseOmocodia();

    /**
     * Posizioni ufficiali del codice fiscale soggette ad omocodia.
     */
    private static final int[] OMOCODIA_POSITIONS = {6, 7, 9, 10, 12, 13, 14};

    /**
     * Tabella dei valori associati ai caratteri in posizione dispari per il calcolo del checksum.
     */
    private static final Map<Character, Integer> ODD_VALUES = createOddValues();

    /**
     * Tabella dei valori associati ai caratteri in posizione pari per il calcolo del checksum.
     */
    private static final Map<Character, Integer> EVEN_VALUES = createEvenValues();

    /**
     * Genera un codice fiscale completo partendo dai dati di un cittadino.
     * 
     * @param cittadino Oggetto contenente i dati anagrafici del cittadino
     * @param codiciEsistenti Insieme dei codici fiscali già presenti per evitare collisioni
     * @return Il codice fiscale generato
     */
    public String genera(Cittadino cittadino, Set<String> codiciEsistenti) {
        return genera(
                cittadino.getNome(),
                cittadino.getCognome(),
                cittadino.getDataNascita(),
                cittadino.getSesso(),
                cittadino.getCodiceComune(),
                codiciEsistenti
        );
    }

    /**
     * Genera un codice fiscale applicando automaticamente eventuali varianti omocodiche.
     * 
     * @param nome Nome della persona
     * @param cognome Cognome della persona
     * @param dataNascita Data di nascita
     * @param sesso Sesso anagrafico ('M' oppure 'F')
     * @param codiceComune Codice catastale del comune
     * @param codiciEsistenti Archivio dei codici già esistenti
     * @return Un codice fiscale univoco
     * @throws IllegalStateException Se tutte le varianti omocodiche risultano già utilizzate
     */
    public String genera(
            String nome,
            String cognome,
            LocalDate dataNascita,
            char sesso,
            String codiceComune,
            Set<String> codiciEsistenti
    ) {
        String base15 = codiceCognome(cognome)
                + codiceNome(nome)
                + codiceData(dataNascita, sesso)
                + codiceComune.toUpperCase(Locale.ITALIAN);

        String base = base15 + carattereControllo(base15);

        Set<String> used = codiciEsistenti == null ? Set.of() : new HashSet<>(codiciEsistenti);

        if (!used.contains(base)) {
            return base;
        }

        for (String variant : generaOmocodie(base)) {
            if (!used.contains(variant)) {
                return variant;
            }
        }

        throw new IllegalStateException("Tutte le varianti omocodiche sono gia presenti in archivio.");
    }

    /**
     * Genera un codice fiscale standard senza effettuare controlli di omocodia.
     * 
     * @param nome Nome della persona
     * @param cognome Cognome della persona
     * @param dataNascita Data di nascita
     * @param sesso Sesso anagrafico
     * @param codiceComune Codice catastale del comune
     * @return Codice fiscale standard
     */
    public String generaSenzaOmocodia(
            String nome,
            String cognome,
            LocalDate dataNascita,
            char sesso,
            String codiceComune
    ) {
        String base15 = codiceCognome(cognome)
                + codiceNome(nome)
                + codiceData(dataNascita, sesso)
                + codiceComune.toUpperCase(Locale.ITALIAN);

        return base15 + carattereControllo(base15);
    }

    /**
     * Verifica la correttezza formale e strutturale di un codice fiscale.
     * 
     * Effettua controlli su:
     * <ul>
     *     <li>Lunghezza</li>
     *     <li>Formato ufficiale</li>
     *     <li>Checksum</li>
     *     <li>Data di nascita</li>
     *     <li>Codice comune</li>
     *     <li>Omocodia</li>
     * </ul>
     * 
     * @param codiceFiscale Codice fiscale da validare
     * @param comuneService Servizio per il controllo dei codici catastali
     * @return Oggetto contenente esito e dettagli della validazione
     */
    public ValidationResult verifica(String codiceFiscale, ComuneService comuneService) {
        List<String> messages = new ArrayList<>();

        String cf = codiceFiscale == null
                ? ""
                : codiceFiscale.trim().toUpperCase(Locale.ITALIAN).replaceAll("\\s+", "");

        if (cf.length() != 16) {
            messages.add("Lunghezza non valida: il codice fiscale deve avere 16 caratteri.");
            return new ValidationResult(false, messages);
        }

        String pattern = "[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]";

        if (!cf.matches(pattern)) {
            messages.add("Formato non valido: lettere, data, mese o codice comune non rispettano la struttura ufficiale.");
            return new ValidationResult(false, messages);
        }

        char expectedControl = carattereControllo(cf.substring(0, 15));

        if (cf.charAt(15) != expectedControl) {
            messages.add("Carattere di controllo errato: atteso " + expectedControl + ".");
            return new ValidationResult(false, messages);
        }

        messages.add("Checksum corretto.");

        String decoded = decodificaOmocodia(cf.substring(0, 15)) + cf.charAt(15);

        int month = MONTH_VALUES.getOrDefault(decoded.charAt(8), -1);

        if (month < 1) {
            messages.add("Mese di nascita non valido.");
            return new ValidationResult(false, messages);
        }

        int dayCode = Integer.parseInt(decoded.substring(9, 11));
        int day = dayCode > 40 ? dayCode - 40 : dayCode;
        char sesso = dayCode > 40 ? 'F' : 'M';

        if (!validMonthDay(month, day)) {
            messages.add("Giorno di nascita non valido.");
            return new ValidationResult(false, messages);
        }

        messages.add("Data coerente: giorno " + day + ", mese " + month + ", sesso " + sesso + ".");

        String codiceComune = "" + decoded.charAt(11) + decoded.substring(12, 15);

        if (comuneService != null) {
            Optional<Comune> comune = comuneService.findByCode(codiceComune);

            if (comune.isEmpty()) {
                messages.add("Codice comune " + codiceComune + " non presente nell'archivio comuni caricato.");
                return new ValidationResult(false, messages);
            }

            messages.add("Comune coerente: " + comune.get().getNome() + " (" + comune.get().getProvincia() + ").");
        } else {
            messages.add("Codice comune coerente: " + codiceComune + ".");
        }

        if (!decoded.substring(0, 15).equals(cf.substring(0, 15))) {
            messages.add("Omocodia rilevata e decodificata correttamente.");
        }

        return new ValidationResult(true, messages);
    }

    /**
     * Genera tutte le possibili varianti omocodiche di un codice fiscale base.
     * 
     * @param codiceFiscaleBase Codice fiscale di partenza
     * @return Lista delle varianti omocodiche generate
     */
    public List<String> generaOmocodie(String codiceFiscaleBase) {
        String cf = codiceFiscaleBase.toUpperCase(Locale.ITALIAN);

        if (cf.length() != 16) {
            return List.of();
        }

        String base15 = decodificaOmocodia(cf.substring(0, 15));

        List<String> variants = new ArrayList<>();

        for (int mask = 1; mask < (1 << OMOCODIA_POSITIONS.length); mask++) {
            char[] chars = base15.toCharArray();

            for (int bit = 0; bit < OMOCODIA_POSITIONS.length; bit++) {
                int position = OMOCODIA_POSITIONS[OMOCODIA_POSITIONS.length - 1 - bit];

                if ((mask & (1 << bit)) != 0 && Character.isDigit(chars[position])) {
                    chars[position] = OMOCODIA.get(chars[position]);
                }
            }

            String first15 = new String(chars);

            variants.add(first15 + carattereControllo(first15));
        }

        return variants;
    }

    /**
     * Genera il codice alfabetico associato al cognome.
     * 
     * @param cognome Cognome da elaborare
     * @return Codice di tre caratteri
     */
    private static String codiceCognome(String cognome) {
        return consonantsThenVowels(cognome).substring(0, 3);
    }

    /**
     * Genera il codice alfabetico associato al nome secondo le regole ministeriali.
     * 
     * @param nome Nome da elaborare
     * @return Codice di tre caratteri
     */
    private static String codiceNome(String nome) {
        String clean = StringUtils.cleanName(nome);

        StringBuilder consonants = new StringBuilder();
        StringBuilder vowels = new StringBuilder();

        for (char ch : clean.toCharArray()) {
            if ("AEIOU".indexOf(ch) >= 0) {
                vowels.append(ch);
            } else {
                consonants.append(ch);
            }
        }

        if (consonants.length() >= 4) {
            return "" + consonants.charAt(0) + consonants.charAt(2) + consonants.charAt(3);
        }

        return (consonants + vowels.toString() + "XXX").substring(0, 3);
    }

    /**
     * Restituisce una stringa composta prima dalle consonanti e poi dalle vocali.
     * 
     * @param value Valore anagrafico da elaborare
     * @return Sequenza consonanti-vocali completata con caratteri di riempimento
     */
    private static String consonantsThenVowels(String value) {
        String clean = StringUtils.cleanName(value);

        StringBuilder consonants = new StringBuilder();
        StringBuilder vowels = new StringBuilder();

        for (char ch : clean.toCharArray()) {
            if ("AEIOU".indexOf(ch) >= 0) {
                vowels.append(ch);
            } else {
                consonants.append(ch);
            }
        }

        return consonants + vowels.toString() + "XXX";
    }

    /**
     * Genera la porzione del codice fiscale relativa alla data di nascita e al sesso.
     * 
     * @param dataNascita Data di nascita
     * @param sesso Sesso anagrafico
     * @return Codice data composto da anno, mese e giorno
     */
    private static String codiceData(LocalDate dataNascita, char sesso) {
        int year = dataNascita.getYear() % 100;

        char monthCode = MONTH_CODES.get(dataNascita.getMonthValue());

        int day = dataNascita.getDayOfMonth();

        if (Character.toUpperCase(sesso) == 'F') {
            day += 40;
        }

        return String.format("%02d%c%02d", year, monthCode, day);
    }

    /**
     * Calcola il carattere di controllo finale del codice fiscale.
     * 
     * @param first15 Prime quindici posizioni del codice fiscale
     * @return Carattere alfabetico di controllo
     */
    public static char carattereControllo(String first15) {
        String value = first15.toUpperCase(Locale.ITALIAN);

        int sum = 0;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            boolean oddPosition = (i + 1) % 2 == 1;

            sum += oddPosition
                    ? ODD_VALUES.getOrDefault(ch, 0)
                    : EVEN_VALUES.getOrDefault(ch, 0);
        }

        return (char) ('A' + (sum % 26));
    }

    /**
     * Decodifica le lettere omocodiche riportandole ai rispettivi valori numerici.
     * 
     * @param first15 Prime quindici posizioni del codice fiscale
     * @return Stringa decodificata
     */
    private static String decodificaOmocodia(String first15) {
        char[] chars = first15.toCharArray();

        for (int position : OMOCODIA_POSITIONS) {
            chars[position] = OMOCODIA_REVERSE.getOrDefault(chars[position], chars[position]);
        }

        return new String(chars);
    }

    /**
     * Verifica la validità di una combinazione mese-giorno.
     * 
     * @param month Numero del mese
     * @param day Giorno del mese
     * @return true se la data è valida, false altrimenti
     */
    private static boolean validMonthDay(int month, int day) {
        try {
            MonthDay.of(month, day);
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }

    /**
     * Crea la mappa inversa dei codici mese.
     * 
     * @return Mappa codice mese -> numero mese
     */
    private static Map<Character, Integer> createMonthValues() {
        Map<Character, Integer> values = new HashMap<>();

        MONTH_CODES.forEach((month, code) -> values.put(code, month));

        return values;
    }

    /**
     * Crea la mappa inversa utilizzata per decodificare l'omocodia.
     * 
     * @return Mappa lettera omocodica -> cifra originale
     */
    private static Map<Character, Character> createReverseOmocodia() {
        Map<Character, Character> reverse = new HashMap<>();

        OMOCODIA.forEach((digit, letter) -> reverse.put(letter, digit));

        return reverse;
    }

    /**
     * Costruisce la tabella dei valori associati ai caratteri in posizione pari.
     * 
     * @return Mappa carattere -> valore numerico
     */
    private static Map<Character, Integer> createEvenValues() {
        Map<Character, Integer> values = new HashMap<>();

        for (char ch = '0'; ch <= '9'; ch++) {
            values.put(ch, ch - '0');
        }

        for (char ch = 'A'; ch <= 'Z'; ch++) {
            values.put(ch, ch - 'A');
        }

        return values;
    }

    /**
     * Costruisce la tabella dei valori associati ai caratteri in posizione dispari.
     * 
     * @return Mappa carattere -> valore checksum
     */
    private static Map<Character, Integer> createOddValues() {
        Map<Character, Integer> values = new HashMap<>();

        int[] digitValues = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21};

        for (int i = 0; i <= 9; i++) {
            values.put((char) ('0' + i), digitValues[i]);
        }

        int[] letterValues = {
                1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18,
                20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23
        };

        for (int i = 0; i < letterValues.length; i++) {
            values.put((char) ('A' + i), letterValues[i]);
        }

        return values;
    }

    /**
     * Oggetto risultato utilizzato per rappresentare l'esito di una validazione.
     */
    public static final class ValidationResult {

        /**
         * Indica se il codice fiscale è valido.
         */
        private final boolean valid;

        /**
         * Elenco dettagliato dei messaggi di validazione.
         */
        private final List<String> messages;

        /**
         * Costruisce un nuovo risultato di validazione.
         * 
         * @param valid true se valido
         * @param messages Messaggi descrittivi della validazione
         */
        public ValidationResult(boolean valid, List<String> messages) {
            this.valid = valid;
            this.messages = List.copyOf(messages);
        }

        /**
         * Restituisce l'esito della validazione.
         * 
         * @return true se valido
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Restituisce i messaggi dettagliati della validazione.
         * 
         * @return Lista dei messaggi
         */
        public List<String> getMessages() {
            return messages;
        }
    }
}
