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
 * Generazione e verifica del codice fiscale.
 *
 * Il codice e' fatto di 16 caratteri: 3 per il cognome, 3 per il nome, 2 per
 * l'anno, 1 per il mese, 2 per il giorno (con +40 per le donne), 4 per il
 * comune e 1 di controllo. Se il codice risultante e' gia' in archivio si
 * passa all'omocodia, cioe' si sostituiscono le cifre con lettere partendo
 * da destra.
 */
public final class CodiceFiscaleService {

    public CodiceFiscaleService() {
    }

    // lettera del mese: non e' in ordine alfabetico, e' la tabella ufficiale
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

    // stessa tabella al contrario, per rileggere un codice esistente
    private static final Map<Character, Integer> MONTH_VALUES = createMonthValues();

    // cifra -> lettera usata in caso di omocodia
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

    // e il contrario, per tornare al codice base
    private static final Map<Character, Character> OMOCODIA_REVERSE = createReverseOmocodia();

    // le uniche posizioni che possono contenere cifre, quindi le uniche
    // che l'omocodia puo' sostituire. Vanno usate da destra verso sinistra
    private static final int[] OMOCODIA_POSITIONS = {6, 7, 9, 10, 12, 13, 14};

    // tabelle del carattere di controllo: i caratteri in posizione dispari
    // pesano diversamente da quelli in posizione pari
    private static final Map<Character, Integer> ODD_VALUES = createOddValues();

    private static final Map<Character, Integer> EVEN_VALUES = createEvenValues();

    /**
     * Scorciatoia che prende i dati direttamente da un {@link Cittadino}.
     *
     * @param cittadino dati anagrafici
     * @param codiciEsistenti codici gia' in archivio
     * @return il codice fiscale
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
     * Calcola il codice e, se risulta gia' occupato, cerca la prima variante
     * omocodica libera.
     *
     * @param nome nome
     * @param cognome cognome
     * @param dataNascita data di nascita
     * @param sesso 'M' oppure 'F'
     * @param codiceComune codice catastale del comune di nascita
     * @param codiciEsistenti codici gia' assegnati (puo' essere null)
     * @return un codice non ancora usato
     * @throws IllegalStateException se anche tutte le varianti sono occupate
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

        // collisione: due persone diverse con lo stesso codice. Provo le
        // varianti in ordine, la prima libera va bene
        for (String variant : generaOmocodie(base)) {
            if (!used.contains(variant)) {
                return variant;
            }
        }

        throw new IllegalStateException("Tutte le varianti omocodiche sono gia presenti in archivio.");
    }

    /**
     * Solo il calcolo, senza guardare l'archivio: usato dalla voce di menu che
     * mostra il codice fiscale senza salvare niente.
     *
     * @param nome nome
     * @param cognome cognome
     * @param dataNascita data di nascita
     * @param sesso 'M' oppure 'F'
     * @param codiceComune codice catastale del comune
     * @return il codice fiscale base
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
     * Controlla un codice fiscale inserito a mano, nell'ordine: lunghezza,
     * formato, carattere di controllo, data e comune. Al primo errore si ferma,
     * perche' i controlli successivi leggerebbero dati senza senso.
     *
     * @param codiceFiscale codice da verificare
     * @param comuneService serve per controllare il codice catastale (puo' essere null)
     * @return esito e messaggi da mostrare all'utente
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

        // le lettere ammesse dove ci sarebbero le cifre sono quelle dell'omocodia
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

        // riporto le eventuali lettere omocodiche a cifre, altrimenti non
        // riesco a rileggere data e codice comune
        String decoded = decodificaOmocodia(cf.substring(0, 15)) + cf.charAt(15);

        int month = MONTH_VALUES.getOrDefault(decoded.charAt(8), -1);

        if (month < 1) {
            messages.add("Mese di nascita non valido.");
            return new ValidationResult(false, messages);
        }

        // giorno oltre 40 significa donna: si sottrae 40 per il giorno vero
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
     * Tutte le combinazioni di sostituzione possibili sulle 7 posizioni
     * numeriche, quindi 127 varianti. Il contatore mask viene usato come
     * maschera di bit: ogni bit dice se sostituire una posizione, partendo
     * dall'ultima. Ogni variante ha il suo carattere di controllo ricalcolato.
     *
     * @param codiceFiscaleBase codice di partenza
     * @return le varianti, dalla piu' vicina al codice originale
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

    // cognome: prime tre consonanti, poi le vocali, poi X di riempimento
    private static String codiceCognome(String cognome) {
        return consonantsThenVowels(cognome).substring(0, 3);
    }

    /**
     * Il nome segue una regola diversa dal cognome: con quattro o piu'
     * consonanti si prendono la prima, la terza e la quarta (non la seconda).
     *
     * @param nome nome da elaborare
     * @return tre caratteri
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

    // le XXX in coda coprono i nomi troppo corti tipo "Bo" o "Li"
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

    // anno (ultime due cifre) + lettera del mese + giorno, +40 se femmina
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
     * Sedicesimo carattere: si sommano i valori dei primi 15 usando due tabelle
     * diverse a seconda della posizione, poi si prende il resto della divisione
     * per 26 come indice della lettera.
     *
     * @param first15 i primi 15 caratteri
     * @return la lettera di controllo
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

    // riporta un codice omocodico al suo codice base
    private static String decodificaOmocodia(String first15) {
        char[] chars = first15.toCharArray();

        for (int position : OMOCODIA_POSITIONS) {
            chars[position] = OMOCODIA_REVERSE.getOrDefault(chars[position], chars[position]);
        }

        return new String(chars);
    }

    // MonthDay accetta il 29 febbraio, giusto: nel codice fiscale non c'e'
    // l'anno completo, quindi non si puo' sapere se era bisestile
    private static boolean validMonthDay(int month, int day) {
        try {
            MonthDay.of(month, day);
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }

    private static Map<Character, Integer> createMonthValues() {
        Map<Character, Integer> values = new HashMap<>();

        MONTH_CODES.forEach((month, code) -> values.put(code, month));

        return values;
    }

    private static Map<Character, Character> createReverseOmocodia() {
        Map<Character, Character> reverse = new HashMap<>();

        OMOCODIA.forEach((digit, letter) -> reverse.put(letter, digit));

        return reverse;
    }

    // posizioni pari: cifre e lettere valgono la loro posizione naturale
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

    // posizioni dispari: valori sparsi, trascritti dalla tabella ufficiale
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
     * Esito della verifica piu' l'elenco dei messaggi da stampare, cosi' il
     * menu non deve ricostruire nessuna spiegazione.
     */
    public static final class ValidationResult {

        private final boolean valid;
        private final List<String> messages;

        public ValidationResult(boolean valid, List<String> messages) {
            this.valid = valid;
            this.messages = List.copyOf(messages);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
