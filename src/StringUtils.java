import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StringUtils {
    private StringUtils() {
    }
    
    public static String cleanName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ITALIAN);
        return normalized.replaceAll("[^A-Z]", "");
    }

    public static String normalizeSearch(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ITALIAN);
    }
 
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
 
    public static String csvEscape(String value) {
        String safe = value == null ? "" : value;
        boolean needsQuotes = safe.contains(";") || safe.contains("\"") || safe.contains("\n") || safe.contains(",");
        safe = safe.replace("\"", "\"\"");
        return needsQuotes ? "\"" + safe + "\"" : safe;
    }
    
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

    public static String left(String value, int width) {
        String safe = value == null ? "" : value;
        if (safe.length() > width) {
            return safe.substring(0, Math.max(0, width - 1)) + ".";
        }
        return String.format("%-" + width + "s", safe);
    }
}
