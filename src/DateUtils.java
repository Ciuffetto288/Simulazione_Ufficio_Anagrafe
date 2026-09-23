import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;

public final class DateUtils {
    
    public static final DateTimeFormatter STORAGE = DateTimeFormatter.ISO_LOCAL_DATE;
      
      
    public static final DateTimeFormatter ITALIAN = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private DateUtils() {
    }
    
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
   
    public static String formatItalian(LocalDate date) {
        return date == null ? "" : date.format(ITALIAN);
    }
   
    public static String formatStorage(LocalDate date) {
        return date == null ? "" : date.format(STORAGE);
    }
    
    public static int age(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
