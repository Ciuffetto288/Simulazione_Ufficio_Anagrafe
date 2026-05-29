import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Classe Utility per la manipolazione, la normalizzazione e il parsing di stringhe di testo.
 * Fornisce strumenti specifici per la gestione dei dati anagrafici e la codifica in formato CSV.
 */
public final class StringUtils {
        /**
     * Costruttore privato vuoto per impedire l'istanziazione della classe.
     * Essendo una classe di sole utility con metodi statici, evita l'uso non necessario di 'new StringUtils()'.
     */
    private StringUtils() {
    }
    /**
     * Pulisce un nome o cognome rimuovendo accenti, spazi superflui e caratteri non alfabetici.
     * Converte tutto in maiuscolo, lasciando esclusivamente le lettere da A a Z (utile per il calcolo del codice fiscale).
     * 
     * @param value La stringa del nome o del cognome da ripulire
     * @return La stringa normalizzata contenente solo lettere maiuscole non accentuate, o stringa vuota se null
     */
    public static String cleanName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ITALIAN);
        return normalized.replaceAll("[^A-Z]", "");
    }
/**
     * Normalizza una stringa per ottimizzare le operazioni di ricerca testuale.
     * Rimuove i segni diacritici (accenti) e trasforma l'intero testo in lettere maiuscole.
     * 
     * @param value La stringa da preparare per la ricerca
     * @return La stringa normalizzata in maiuscolo e senza accenti, o stringa vuota se null
     */
    public static String normalizeSearch(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ITALIAN);
    }
 /**
     * Capitalizza ogni singola parola presente all'interno di una stringa di testo.
     * Converte la prima lettera di ciascuna parola in maiuscolo e le successive in minuscolo.
     * 
     * @param value La stringa di testo da formattare
     * @return La stringa con le iniziali di ogni parola maiuscole, o stringa vuota se null/blank
     */
    public static String capitalizeWords(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String[] parts = value.trim().toLowerCase(Locale.ITALIAN).split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (result.length() > 0) {
                    result.append(' ');
                }
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }
 /**
     * Applica il sistema di escaping standard per inserire in sicurezza una stringa in un file CSV.
     * Raddoppia le virgolette interne e racchiude il testo tra virgolette se contiene caratteri speciali.
     * 
     * @param value La stringa di testo da inserire nella cella del CSV
     * @return La stringa formattata e protetta contro la rottura del layout CSV
     */
    public static String csvEscape(String value) {
        String safe = value == null ? "" : value;
        boolean needsQuotes = safe.contains(";") || safe.contains("\"") || safe.contains("\n") || safe.contains(",");
        safe = safe.replace("\"", "\"\"");
        return needsQuotes ? "\"" + safe + "\"" : safe;
    }
    /**
     * Esegue il parsing avanzato di una singola riga CSV rispettando la presenza di celle racchiuse tra virgolette.
     * Gestisce correttamente i separatori interni e le doppie virgolette di escape.
     * 
     * @param line      La riga di testo CSV completa da dividere
     * @param separator Il carattere utilizzato come separatore di colonna (es. ';')
     * @return Una lista di stringhe contenente i valori estratti dalle singole celle
     */
    public static List<String> splitCsvLine(String line, char separator) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == separator && !quoted) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        cells.add(current.toString());
        return cells;
    }
/**
     * Allinea a sinistra il testo all'interno di uno spazio di larghezza fissa riempiendolo con spazi vuoti.
     * Se la stringa supera la larghezza massima, viene troncata inserendo un punto "." finale.
     * 
     * @param value L'espressione testuale da formattare
     * @param width La larghezza fissa totale della colonna desiderata
     * @return La stringa formattata a spaziatura fissa o troncata nei limiti
     */
    public static String left(String value, int width) {
        String safe = value == null ? "" : value;
        if (safe.length() > width) {
            return safe.substring(0, Math.max(0, width - 1)) + ".";
        }
        return String.format("%-" + width + "s", safe);
    }
}
