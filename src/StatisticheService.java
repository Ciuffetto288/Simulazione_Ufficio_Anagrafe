
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Produce il report di riepilogo sull'archivio (conteggi, eta media, comuni).
 */
public final class StatisticheService {

    public StatisticheService() {
    }

    /**
     * Totale record, uomini, donne, eta media e i 10 comuni piu' ricorrenti.
     * Il testo torna gia' impaginato, pronto da stampare a schermo.
     *
     * @param cittadini archivio da analizzare
     * @return il report su piu' righe
     */
    public String report(List<Cittadino> cittadini) {
        int total = cittadini.size();
        long men = cittadini.stream().filter(c -> c.getSesso() == 'M').count();
        long women = cittadini.stream().filter(c -> c.getSesso() == 'F').count();
        double averageAge = cittadini.stream()
                .mapToInt(c -> DateUtils.age(c.getDataNascita()))
                .average()
                .orElse(0.0);

        StringBuilder report = new StringBuilder();
        report.append("STATISTICHE ARCHIVIO\n");
        report.append("--------------------\n");
        report.append("Totale cittadini : ").append(total).append('\n');
        report.append("Uomini           : ").append(men).append('\n');
        report.append("Donne            : ").append(women).append('\n');
        report.append("Eta media        : ").append(String.format("%.1f", averageAge)).append('\n');
        report.append("\nComuni piu presenti:\n");

        // conteggio per comune, ordinato per numero decrescente e a pari
        // merito in ordine alfabetico, altrimenti l'ordine cambia ogni volta
        Map<String, Long> byComune = cittadini.stream()
                .collect(Collectors.groupingBy(Cittadino::getComune, Collectors.counting()));
        byComune.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
                .limit(10)
                .forEach(entry -> report.append(String.format(" - %-25s %d%n", entry.getKey(), entry.getValue())));

        if (byComune.isEmpty()) {
            report.append(" - nessun cittadino in archivio\n");
        }
        return report.toString();
    }
}
