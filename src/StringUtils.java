import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Funzioni di appoggio sulle stringhe: pulizia di nomi e cognomi,
 * normalizzazione per le ricerche e gestione del CSV.
 */
public final class StringUtils {
    private StringUtils() {
    }
    /**
     * Riduce un nome o cognome alle sole lettere A-Z maiuscole.
     * Serve al codice fiscale, che non ammette accenti, apostrofi o spazi
     * (quindi "D'Angelo" diventa DANGELO).
     *
     * @param value nome o cognome da ripulire
     * @return solo lettere maiuscole, stringa vuota se null
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
     * Versione piu' morbida di cleanName: toglie accenti e mette in maiuscolo
     * ma lascia spazi e punteggiatura, cosi' "Forli" trova "Forli'".
     *
     * @param value testo da normalizzare
     * @return testo senza accenti in maiuscolo
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
     * Mette l'iniziale maiuscola a ogni parola (per come vengono mostrati
     * nome, cognome e comune a schermo).
     *
     * @param value testo da formattare
     * @return testo con le iniziali maiuscole
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
     * Prepara un valore per il CSV: raddoppia le virgolette e mette la cella
     * tra virgolette se contiene separatori o newline, altrimenti la riga
     * verrebbe letta con le colonne sbagliate.
     *
     * @param value valore da scrivere
     * @return valore pronto per il file
     */
    public static String csvEscape(String value) {
        String safe = value == null ? "" : value;
        boolean needsQuotes = safe.contains(";") || safe.contains("\"") || safe.contains("\n") || safe.contains(",");
        safe = safe.replace("\"", "\"\"");
        return needsQuotes ? "\"" + safe + "\"" : safe;
    }
    /**
     * Divide una riga CSV in celle. Non si puo' usare String.split perche'
     * il separatore dentro le virgolette non va considerato.
     *
     * @param line      riga da dividere
     * @param separator separatore di colonna (qui ';')
     * @return le celle trovate
     */
    public static List<String> splitCsvLine(String line, char separator) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                // due virgolette di fila dentro una cella = una virgoletta vera
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
        // ultima cella: dopo di lei non c'e' separatore
        cells.add(current.toString());
        return cells;
    }
/**
     * Allinea a sinistra in una colonna di larghezza fissa, troncando con un
     * punto finale se il testo e' troppo lungo. Usato nelle tabelle a schermo.
     *
     * @param value testo da incolonnare
     * @param width larghezza della colonna
     * @return testo della lunghezza richiesta
     */
    public static String left(String value, int width) {
        String safe = value == null ? "" : value;
        if (safe.length() > width) {
            return safe.substring(0, Math.max(0, width - 1)) + ".";
        }
        return String.format("%-" + width + "s", safe);
    }
}
