import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Servizio dedicato alla gestione dell'archivio dei comuni italiani.
 * 
 * Supporta:
 * <ul>
 *     <li>Ricerca per nome</li>
 *     <li>Ricerca per codice catastale</li>
 *     <li>Caricamento dati da file CSV</li>
 *     <li>Caricamento dati da file XLSX</li>
 *     <li>Fallback su database interno predefinito</li>
 * </ul>
 */
public final class ComuneService {

    /**
     * Pattern per intercettare input come "Rovigo (RO)".
     */
    private static final Pattern PROVINCE_IN_PARENTHESES =
            Pattern.compile("^(.*?)[\\s,;-]*\\(([A-Za-z]{2})\\)\\s*$");

    /**
     * Pattern per intercettare input come "Monselice PD" o "Rovigo - RO".
     */
    private static final Pattern TRAILING_PROVINCE =
            Pattern.compile("^(.*?)[\\s,;-]+([A-Za-z]{2})\\s*$");

    /**
     * Archivio dei comuni indicizzati per codice catastale.
     */
    private final Map<String, Comune> byCode = new LinkedHashMap<>();

    /**
     * Lista completa dei comuni caricati.
     */
    private final List<Comune> comuni = new ArrayList<>();

    /**
     * Descrizione della sorgente dati attualmente utilizzata.
     */
    private String source = "database interno";

    /**
     * Costruisce il servizio caricando automaticamente l'archivio comuni.
     */
    public ComuneService() {
        load();
    }

    /**
     * Cerca comuni tramite nome, parte del nome o nome con provincia finale.
     *
     * <p>La ricerca viene effettuata in forma normalizzata ignorando maiuscole,
     * accenti e punteggiatura. Se l'utente scrive la sigla della provincia
     * insieme al comune, ad esempio {@code Rovigo (RO)} oppure {@code Monselice PD},
     * la sigla viene estratta e usata per filtrare i risultati. I risultati
     * esatti vengono ordinati prima delle corrispondenze parziali, cosi un
     * comune come {@code Rovigo} non viene confuso con {@code Costa di Rovigo}.</p>
     *
     * @param query testo di ricerca inserito dall'utente
     * @return lista ordinata dei comuni trovati, limitata ai primi 20 risultati
     */
    public List<Comune> searchByName(String query) {
        ComuneQuery parsed = parseComuneQuery(query, "");

        if (parsed.nome().isBlank()) {
            return List.of();
        }

        String normalized = normalizeComuneName(parsed.nome());
        String compact = compactComuneName(parsed.nome());

        if (normalized.isBlank() && compact.isBlank()) {
            return List.of();
        }

        return comuni.stream()
                .filter(comune -> parsed.provincia().isBlank()
                        || comune.getProvincia().equalsIgnoreCase(parsed.provincia()))
                .filter(comune -> matchesComuneName(comune, normalized, compact))
                .sorted(Comparator.<Comune>comparingInt(comune ->
                                comuneMatchScore(comune, normalized, compact))
                        .thenComparing(Comune::getNome)
                        .thenComparing(Comune::getProvincia))
                .limit(20)
                .toList();
    }

    /**
     * Cerca un comune tramite nome e provincia con confronto esatto ma flessibile.
     *
     * <p>Accetta sia i campi separati, ad esempio nome {@code Rovigo} e provincia
     * {@code RO}, sia un unico testo naturale come {@code Rovigo (RO)},
     * {@code Rovigo RO} o {@code Monselice PD}. Il confronto sul nome ignora
     * accenti e punteggiatura, ma resta esatto: le corrispondenze parziali vengono
     * lasciate ai suggerimenti di {@link #searchByName(String)}.</p>
     *
     * @param nome nome del comune o nome con provincia finale
     * @param provincia sigla della provincia, opzionale se gia presente nel nome
     * @return {@link Optional} contenente il comune trovato, oppure vuoto
     */
    public Optional<Comune> findByNameAndProvince(String nome, String provincia) {
        ComuneQuery parsed = parseComuneQuery(nome, provincia);

        return comuni.stream()
                .filter(comune -> sameComuneName(comune.getNome(), parsed.nome()))
                .filter(comune -> parsed.provincia().isBlank()
                        || comune.getProvincia().equalsIgnoreCase(parsed.provincia()))
                .findFirst();
    }

    /**
     * Rappresenta una query comune gia separata in nome e provincia.
     *
     * @param nome nome del comune ripulito dalla provincia finale
     * @param provincia sigla provincia normalizzata in maiuscolo
     */
    private record ComuneQuery(String nome, String provincia) {
    }

    /**
     * Separa nome e provincia anche quando l'utente li inserisce nello stesso campo.
     *
     * @param nome testo del comune, eventualmente comprensivo di provincia
     * @param provincia provincia inserita nel campo dedicato
     * @return query normalizzata con nome e provincia distinti
     */
    private static ComuneQuery parseComuneQuery(String nome, String provincia) {
        String cleanName = nome == null ? "" : nome.trim();
        String explicitProvince = normalizeProvince(provincia);
        String embeddedProvince = "";

        Matcher parenthesized = PROVINCE_IN_PARENTHESES.matcher(cleanName);

        if (parenthesized.matches()) {
            cleanName = parenthesized.group(1).trim();
            embeddedProvince = normalizeProvince(parenthesized.group(2));
        } else {
            Matcher trailing = TRAILING_PROVINCE.matcher(cleanName);

            if (trailing.matches() && !trailing.group(1).isBlank()) {
                cleanName = trailing.group(1).trim();
                embeddedProvince = normalizeProvince(trailing.group(2));
            }
        }

        String finalProvince = explicitProvince.isBlank()
                ? embeddedProvince
                : explicitProvince;

        return new ComuneQuery(cleanName, finalProvince);
    }

    /**
     * Normalizza la sigla provincia eliminando spazi e forzando il maiuscolo.
     *
     * @param provincia valore da normalizzare
     * @return sigla provincia normalizzata, oppure stringa vuota
     */
    private static String normalizeProvince(String provincia) {
        return provincia == null
                ? ""
                : provincia.trim().toUpperCase(Locale.ITALIAN);
    }

    /**
     * Normalizza un nome comune per il confronto testuale.
     *
     * @param value nome originale
     * @return nome senza accenti, punteggiatura superflua e spazi multipli
     */
    private static String normalizeComuneName(String value) {
        return StringUtils.normalizeSearch(value)
                .replaceAll("[^A-Z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Crea una versione compatta del nome comune senza spazi.
     *
     * @param value nome originale
     * @return nome normalizzato e compattato
     */
    private static String compactComuneName(String value) {
        return normalizeComuneName(value).replace(" ", "");
    }

    /**
     * Verifica se due nomi di comune coincidono dopo la normalizzazione.
     *
     * @param left primo nome da confrontare
     * @param right secondo nome da confrontare
     * @return {@code true} se i nomi rappresentano lo stesso comune
     */
    private static boolean sameComuneName(String left, String right) {
        String normalizedLeft = normalizeComuneName(left);
        String normalizedRight = normalizeComuneName(right);

        return normalizedLeft.equals(normalizedRight)
                || compactComuneName(left).equals(compactComuneName(right));
    }

    /**
     * Stabilisce se un comune soddisfa una ricerca testuale.
     *
     * @param comune comune da verificare
     * @param normalized query normalizzata con spazi
     * @param compact query normalizzata senza spazi
     * @return {@code true} se il nome del comune contiene la query
     */
    private static boolean matchesComuneName(
            Comune comune,
            String normalized,
            String compact
    ) {
        String comuneName = normalizeComuneName(comune.getNome());
        String compactComune = compactComuneName(comune.getNome());

        return comuneName.contains(normalized)
                || (!compact.isBlank() && compactComune.contains(compact));
    }

    /**
     * Calcola un punteggio di pertinenza per ordinare i risultati della ricerca.
     *
     * @param comune comune candidato
     * @param normalized query normalizzata con spazi
     * @param compact query normalizzata senza spazi
     * @return punteggio crescente: valori piu bassi indicano risultati migliori
     */
    private static int comuneMatchScore(
            Comune comune,
            String normalized,
            String compact
    ) {
        String comuneName = normalizeComuneName(comune.getNome());
        String compactComune = compactComuneName(comune.getNome());

        if (comuneName.equals(normalized)) {
            return 0;
        }

        if (!compact.isBlank() && compactComune.equals(compact)) {
            return 1;
        }

        if (comuneName.startsWith(normalized)) {
            return 2;
        }

        if (!compact.isBlank() && compactComune.startsWith(compact)) {
            return 3;
        }

        return 4;
    }

    /**
     * Cerca un comune tramite codice catastale.
     * 
     * @param code Codice catastale ministeriale
     * @return Optional contenente il comune trovato
     */
    public Optional<Comune> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                byCode.get(code.trim().toUpperCase(Locale.ITALIAN))
        );
    }

    /**
     * Verifica se un codice catastale esiste nell'archivio.
     * 
     * @param code Codice catastale da verificare
     * @return true se presente
     */
    public boolean existsCode(String code) {
        return findByCode(code).isPresent();
    }

    /**
     * Restituisce il numero totale dei comuni caricati.
     * 
     * @return Numero di comuni disponibili
     */
    public int size() {
        return comuni.size();
    }

    /**
     * Restituisce la sorgente dati utilizzata per il caricamento.
     * 
     * @return Percorso o descrizione della sorgente
     */
    public String getSource() {
        return source;
    }

    /**
     * Carica l'archivio comuni da sorgenti esterne o dal database interno.
     * 
     * Ordine di caricamento:
     * <ol>
     *     <li>File XLSX</li>
     *     <li>File CSV</li>
     *     <li>Database interno embedded</li>
     * </ol>
     */
    private void load() {
        Path xlsx = AppPaths.dataFile("comuni.xlsx");
        Path csv = AppPaths.dataFile("comuni.csv");

        try {
            if (Files.exists(xlsx)) {
                List<Comune> loaded = loadXlsx(xlsx);

                if (!loaded.isEmpty()) {
                    replaceData(loaded);
                    source = xlsx.toString();
                    return;
                }
            }

            if (Files.exists(csv)) {
                List<Comune> loaded = loadCsv(csv);

                if (!loaded.isEmpty()) {
                    replaceData(loaded);
                    source = csv.toString();
                    return;
                }
            }
        } catch (Exception ex) {
            System.out.println(
                    "Avviso: archivio comuni esterno non leggibile, uso database interno. Dettaglio: "
                            + ex.getMessage()
            );
        }

        replaceData(defaultComuni());
    }

    /**
     * Sostituisce completamente l'archivio interno dei comuni.
     * 
     * @param loaded Lista dei comuni caricati
     */
    private void replaceData(List<Comune> loaded) {
        byCode.clear();
        comuni.clear();

        for (Comune comune : loaded) {
            if (isValidCatastalCode(comune.getCodiceCatastale())) {
                byCode.put(comune.getCodiceCatastale(), comune);
            }
        }

        comuni.addAll(byCode.values());
    }

    /**
     * Carica l'archivio comuni da un file CSV.
     * 
     * @param path Percorso del file CSV
     * @return Lista dei comuni caricati
     * @throws IOException In caso di errore di lettura
     */
    private static List<Comune> loadCsv(Path path) throws IOException {
        List<Comune> result = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            String line;

            boolean headerRead = false;

            int nameIndex = 0;
            int provinceIndex = 1;
            int codeIndex = 2;

            char separator = ';';

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                if (!headerRead) {
                    separator = line.contains(";") ? ';' : ',';

                    List<String> header =
                            StringUtils.splitCsvLine(line, separator);

                    if (looksLikeHeader(header)) {
                        nameIndex = findHeaderIndex(
                                header,
                                "DENOMINAZIONE",
                                "COMUNE",
                                "NOME"
                        );

                        provinceIndex = findHeaderIndex(
                                header,
                                "SIGLA",
                                "PROVINCIA",
                                "PROV"
                        );

                        codeIndex = findHeaderIndex(
                                header,
                                "CATASTALE",
                                "BELFIORE",
                                "CODICE"
                        );

                        headerRead = true;
                        continue;
                    }

                    headerRead = true;
                }

                List<String> cells =
                        StringUtils.splitCsvLine(line, separator);

                if (cells.size()
                        <= Math.max(codeIndex,
                        Math.max(nameIndex, provinceIndex))) {
                    continue;
                }

                Comune comune = new Comune(
                        cells.get(nameIndex),
                        cells.get(provinceIndex),
                        cells.get(codeIndex)
                );

                if (isValidCatastalCode(comune.getCodiceCatastale())) {
                    result.add(comune);
                }
            }
        }

        return result;
    }

    /**
     * Carica l'archivio comuni da un file XLSX.
     * 
     * @param path Percorso del file Excel
     * @return Lista dei comuni caricati
     * @throws Exception In caso di errore di parsing
     */
    private static List<Comune> loadXlsx(Path path) throws Exception {
        try (ZipFile zip = new ZipFile(path.toFile())) {

            List<String> sharedStrings = readSharedStrings(zip);

            ZipEntry sheetEntry =
                    zip.getEntry("xl/worksheets/sheet1.xml");

            if (sheetEntry == null) {
                return List.of();
            }

            Document sheet =
                    parseXml(zip.getInputStream(sheetEntry));

            NodeList rowNodes =
                    sheet.getElementsByTagName("row");

            List<Map<Integer, String>> rows = new ArrayList<>();

            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element row = (Element) rowNodes.item(i);
                rows.add(readRow(row, sharedStrings));
            }

            return comuniFromRows(rows);
        }
    }

    /**
     * Converte le righe lette dal file XLSX in oggetti Comune.
     * 
     * @param rows Righe estratte dal foglio Excel
     * @return Lista dei comuni generati
     */
    private static List<Comune> comuniFromRows(List<Map<Integer, String>> rows) {

        int headerRow = -1;
        int nameIndex = -1;
        int provinceIndex = -1;
        int codeIndex = -1;

        for (int i = 0; i < Math.min(15, rows.size()); i++) {

            Map<Integer, String> row = rows.get(i);

            for (Map.Entry<Integer, String> cell : row.entrySet()) {

                String normalized =
                        StringUtils.normalizeSearch(cell.getValue());

                if (codeIndex < 0
                        && (normalized.contains("CATASTALE")
                        || normalized.contains("BELFIORE"))) {

                    codeIndex = cell.getKey();
                    headerRow = i;
                }

                if (nameIndex < 0
                        && (normalized.contains("DENOMINAZIONE")
                        || normalized.equals("COMUNE")
                        || normalized.contains("NOME COMUNE"))) {

                    nameIndex = cell.getKey();
                    headerRow = i;
                }

                if (provinceIndex < 0
                        && (normalized.contains("SIGLA AUTOMOBILISTICA")
                        || normalized.equals("SIGLA")
                        || normalized.equals("PROV")
                        || normalized.equals("PROVINCIA"))) {

                    provinceIndex = cell.getKey();
                    headerRow = i;
                }
            }

            if (nameIndex >= 0
                    && provinceIndex >= 0
                    && codeIndex >= 0) {
                break;
            }
        }

        if (headerRow < 0
                || nameIndex < 0
                || codeIndex < 0) {
            return List.of();
        }

        List<Comune> result = new ArrayList<>();

        for (int i = headerRow + 1; i < rows.size(); i++) {

            Map<Integer, String> row = rows.get(i);

            String name = row.getOrDefault(nameIndex, "");

            String province = provinceIndex >= 0
                    ? row.getOrDefault(provinceIndex, "")
                    : "";

            String code = row.getOrDefault(codeIndex, "");

            Comune comune = new Comune(name, province, code);

            if (isValidCatastalCode(comune.getCodiceCatastale())) {
                result.add(comune);
            }
        }

        return result;
    }

    /**
     * Legge una singola riga XML del file XLSX.
     * 
     * @param row Nodo XML della riga
     * @param sharedStrings Tabelle shared strings del workbook
     * @return Mappa colonna -> valore
     */
    private static Map<Integer, String> readRow(
            Element row,
            List<String> sharedStrings
    ) {

        Map<Integer, String> values = new HashMap<>();

        NodeList cells = row.getElementsByTagName("c");

        for (int i = 0; i < cells.getLength(); i++) {

            Element cell = (Element) cells.item(i);

            String ref = cell.getAttribute("r");

            int column = columnIndex(ref);

            String type = cell.getAttribute("t");

            String value = "";

            if ("s".equals(type)) {

                String index = childText(cell, "v");

                if (!index.isBlank()) {

                    int sharedIndex = Integer.parseInt(index);

                    if (sharedIndex >= 0
                            && sharedIndex < sharedStrings.size()) {

                        value = sharedStrings.get(sharedIndex);
                    }
                }

            } else if ("inlineStr".equals(type)) {

                value = childText(cell, "t");

            } else {

                value = childText(cell, "v");
            }

            values.put(column, value.trim());
        }

        return values;
    }

    /**
     * Legge la tabella shared strings di un file XLSX.
     * 
     * @param zip Archivio ZIP del file Excel
     * @return Lista delle stringhe condivise
     * @throws Exception In caso di errore XML
     */
    private static List<String> readSharedStrings(ZipFile zip) throws Exception {

        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");

        if (entry == null) {
            return List.of();
        }

        Document doc = parseXml(zip.getInputStream(entry));

        NodeList siNodes = doc.getElementsByTagName("si");

        List<String> values = new ArrayList<>();

        for (int i = 0; i < siNodes.getLength(); i++) {

            Element si = (Element) siNodes.item(i);

            NodeList textNodes = si.getElementsByTagName("t");

            StringBuilder value = new StringBuilder();

            for (int j = 0; j < textNodes.getLength(); j++) {
                value.append(textNodes.item(j).getTextContent());
            }

            values.add(value.toString());
        }

        return values;
    }

    /**
     * Effettua il parsing sicuro di un documento XML.
     * 
     * Disabilita le entità esterne per prevenire vulnerabilità XXE.
     * 
     * @param inputStream Stream XML da leggere
     * @return Documento XML parsato
     * @throws Exception In caso di errore di parsing
     */
    private static Document parseXml(InputStream inputStream) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setExpandEntityReferences(false);

        return factory.newDocumentBuilder().parse(inputStream);
    }

    /**
     * Estrae il testo del primo nodo figlio con il tag specificato.
     * 
     * @param parent Nodo padre
     * @param tag Nome del tag figlio
     * @return Contenuto testuale del nodo
     */
    private static String childText(Element parent, String tag) {

        NodeList nodes = parent.getElementsByTagName(tag);

        if (nodes.getLength() == 0) {
            return "";
        }

        Node node = nodes.item(0);

        return node == null ? "" : node.getTextContent();
    }

    /**
     * Converte il riferimento Excel della colonna nel relativo indice numerico.
     * 
     * Esempi:
     * <ul>
     *     <li>A -> 0</li>
     *     <li>B -> 1</li>
     *     <li>AA -> 26</li>
     * </ul>
     * 
     * @param cellRef Riferimento della cella Excel
     * @return Indice della colonna
     */
    private static int columnIndex(String cellRef) {

        int index = 0;

        for (int i = 0; i < cellRef.length(); i++) {

            char ch = cellRef.charAt(i);

            if (!Character.isLetter(ch)) {
                break;
            }

            index = index * 26
                    + (Character.toUpperCase(ch) - 'A' + 1);
        }

        return Math.max(0, index - 1);
    }

    /**
     * Verifica se una riga CSV/XLSX sembra rappresentare l'intestazione.
     * 
     * @param cells Celle della riga
     * @return true se identificata come header
     */
    private static boolean looksLikeHeader(List<String> cells) {

        String joined =
                StringUtils.normalizeSearch(String.join(" ", cells));

        return joined.contains("COMUNE")
                || joined.contains("DENOMINAZIONE")
                || joined.contains("CATASTALE");
    }

    /**
     * Cerca l'indice di una colonna header tramite parole chiave.
     * 
     * @param header Lista intestazioni
     * @param keys Chiavi di ricerca
     * @return Indice della colonna trovata
     */
    private static int findHeaderIndex(List<String> header, String... keys) {

        for (int i = 0; i < header.size(); i++) {

            String normalized =
                    StringUtils.normalizeSearch(header.get(i));

            for (String key : keys) {

                if (normalized.contains(key)) {
                    return i;
                }
            }
        }

        return 0;
    }

    /**
     * Verifica la validità formale di un codice catastale.
     * 
     * Formato previsto:
     * <ul>
     *     <li>1 lettera</li>
     *     <li>3 cifre</li>
     * </ul>
     * 
     * @param code Codice catastale da verificare
     * @return true se valido
     */
    private static boolean isValidCatastalCode(String code) {
        return code != null
                && code.toUpperCase(Locale.ITALIAN)
                .matches("[A-Z][0-9]{3}");
    }

    /**
     * Restituisce un archivio interno minimo di comuni italiani.
     * 
     * Utilizzato come fallback nel caso in cui i file esterni
     * non siano disponibili o risultino corrotti.
     * 
     * @return Lista predefinita di comuni
     */
    private static List<Comune> defaultComuni() {

        String csv = """
                Roma;RM;H501
                Milano;MI;F205
                Torino;TO;L219
                Napoli;NA;F839
                Palermo;PA;G273
                Genova;GE;D969
                Bologna;BO;A944
                Firenze;FI;D612
                Bari;BA;A662
                Catania;CT;C351
                Venezia;VE;L736
                Verona;VR;L781
                Padova;PD;G224
                Monselice;PD;F382
                Trieste;TS;L424
                Brescia;BS;B157
                Parma;PR;G337
                Modena;MO;F257
                Reggio Calabria;RC;H224
                Reggio Emilia;RE;H223
                Perugia;PG;G478
                Livorno;LI;E625
                Ravenna;RA;H199
                Cagliari;CA;B354
                Foggia;FG;D643
                Rimini;RN;H294
                Salerno;SA;H703
                Ferrara;FE;D548
                Costa di Rovigo;RO;D105
                Rovigo;RO;H620
                Sassari;SS;I452
                Latina;LT;E472
                Giugliano in Campania;NA;E054
                Monza;MB;F704
                Siracusa;SR;I754
                Pescara;PE;G482
                Bergamo;BG;A794
                Forli;FC;D704
                Trento;TN;L378
                Vicenza;VI;L840
                Terni;TR;L117
                Bolzano;BZ;A952
                Novara;NO;F952
                Piacenza;PC;G535
                Ancona;AN;A271
                Andria;BT;A285
                Arezzo;AR;A390
                Udine;UD;L483
                Cesena;FC;C573
                Lecce;LE;E506
                Pesaro;PU;G479
                Alessandria;AL;A182
                La Spezia;SP;E463
                Pisa;PI;G702
                Catanzaro;CZ;C352
                Brindisi;BR;B180
                Lucca;LU;E715
                Treviso;TV;L407
                Como;CO;C933
                Busto Arsizio;VA;B300
                Varese;VA;L682
                Grosseto;GR;E202
                Sesto San Giovanni;MI;I690
                Casoria;NA;B990
                Gela;CL;D960
                Cosenza;CS;D086
                Cremona;CR;D150
                Pavia;PV;G388
                Trapani;TP;L331
                Massa;MS;F023
                Vigevano;PV;L872
                Crotone;KR;D122
                Asti;AT;A479
                Caltanissetta;CL;B429
                Carpi;MO;B819
                Benevento;BN;A783
                Savona;SV;I480
                Matera;MT;F052
                Potenza;PZ;G942
                Campobasso;CB;B519
                Aosta;AO;A326
                L'Aquila;AQ;A345
                """;

        List<Comune> result = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new StringReader(csv))) {

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> cells =
                        StringUtils.splitCsvLine(line, ';');

                if (cells.size() >= 3) {
                    result.add(
                            new Comune(
                                    cells.get(0),
                                    cells.get(1),
                                    cells.get(2)
                            )
                    );
                }
            }

        } catch (IOException ignored) {
        }

        return result;
    }
}
