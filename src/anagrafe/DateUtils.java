import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;
/**
 * Classe Utility per la gestione, la validazione, il parsing e la formattazione delle date.
 * Configura formati rigidi sia per lo standard di archiviazione che per la visualizzazione italiana.
 */
public final class DateUtils {
    /**
     * Formattatore standard ISO per l'archiviazione interna dei dati (AAAA-MM-GG).
     */
    public static final DateTimeFormatter STORAGE = DateTimeFormatter.ISO_LOCAL_DATE;
      /**
     * Formattatore rigoroso per le date nel classico formato italiano (GG/MM/AAAA).
     * Utilizza {@link ResolverStyle#STRICT} per invalidare date fittizie (es. 29/02 in anni non bisestili).
     */
    public static final DateTimeFormatter ITALIAN = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
  /**
     * Costruttore privato vuoto per impedire l'istanziazione della classe.
     * Essendo una classe di sole utility con metodi statici, evita l'uso non necessario di 'new DateUtils()'.
     */
    private DateUtils() {
    }
    /**
     * Tenta il parsing di una stringa di testo interpretandola secondo il formato data italiano (GG/MM/AAAA).
     * 
     * @param value La stringa contenente la data da analizzare
     * @return Un {@link Optional} contenente l'oggetto {@link LocalDate} se valido, altrimenti un Optional vuoto
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
     * Tenta il parsing di una stringa di testo interpretandola secondo lo standard ISO di archiviazione (AAAA-MM-GG).
     * 
     * @param value La stringa contenente la data di storage da analizzare
     * @return Un {@link Optional} contenente l'oggetto {@link LocalDate} se valido, altrimenti un Optional vuoto
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
     * Converte un oggetto LocalDate in una stringa formattata secondo lo standard italiano (GG/MM/AAAA).
     * 
     * @param date L'oggetto data da formattare
     * @return La stringa testuale della data formattata, o una stringa vuota se l'input è null
     */
    public static String formatItalian(LocalDate date) {
        return date == null ? "" : date.format(ITALIAN);
    }
   /**
     * Converte un oggetto LocalDate in una stringa formattata secondo lo standard ISO di storage (AAAA-MM-GG).
     * 
     * @param date L'oggetto data da formattare
     * @return La stringa testuale della data pronta per l'archivio, o una stringa vuota se l'input è null
     */
    public static String formatStorage(LocalDate date) {
        return date == null ? "" : date.format(STORAGE);
    }
    /**
     * Calcola l'età anagrafica espressa in anni calcolando la differenza temporale tra la data di nascita e il momento attuale.
     * 
     * @param birthDate La data di nascita da verificare
     * @return L'età calcolata in anni compiuti, oppure 0 se il parametro è null
     */
    public static int age(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
