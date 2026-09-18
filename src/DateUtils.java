import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;
/**
 * Metodi statici per parsing, formattazione e calcolo dell'eta.
 * Nel CSV le date sono in ISO, all'utente si mostrano in formato italiano.
 */
public final class DateUtils {
    // formato usato nel file CSV (yyyy-MM-dd)
    public static final DateTimeFormatter STORAGE = DateTimeFormatter.ISO_LOCAL_DATE;
      // formato mostrato/letto a schermo. STRICT serve a scartare date tipo
      // 30/02 o 29/02 in anni non bisestili, che altrimenti verrebbero corrette
    public static final DateTimeFormatter ITALIAN = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private DateUtils() {
    }
    /**
     * Legge una data scritta come GG/MM/AAAA.
     *
     * @param value testo da interpretare
     * @return la data, oppure Optional vuoto se il testo non e' una data valida
     */
    public static Optional<LocalDate> parseItalianDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), ITALIAN));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }
    /**
     * Come sopra ma per il formato ISO salvato su file.
     *
     * @param value testo da interpretare
     * @return la data, oppure Optional vuoto
     */
    public static Optional<LocalDate> parseStorageDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), STORAGE));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }
   /**
     * @param date data da formattare
     * @return la data come GG/MM/AAAA, stringa vuota se null
     */
    public static String formatItalian(LocalDate date) {
        return date == null ? "" : date.format(ITALIAN);
    }
   /**
     * @param date data da formattare
     * @return la data in ISO, pronta per il CSV
     */
    public static String formatStorage(LocalDate date) {
        return date == null ? "" : date.format(STORAGE);
    }
    /**
     * Anni compiuti alla data di oggi.
     *
     * @param birthDate data di nascita
     * @return l'eta in anni, 0 se la data manca
     */
    public static int age(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
