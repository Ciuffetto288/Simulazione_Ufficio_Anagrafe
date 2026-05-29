
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Classe di Servizio per il calcolo e la generazione di metriche statistiche sui cittadini registrati.
 * Estrae informazioni aggregate quali distribuzioni di genere, età media e densità geografica.
 */
public final class StatisticheService {

    /**
     * Costruisce un servizio statistico senza stato interno.
     */
    public StatisticheService() {
    }

    /**
     * Elabora i dati anagrafici dei cittadini forniti e genera un report testuale riassuntivo delle statistiche.
     * Il report include il totale dei record, il conteggio per sesso, l'età media e i primi 10 comuni più frequenti.
     * 
     * @param cittadini La lista di oggetti {@link Cittadino} da analizzare
     * @return Una stringa multilinea contenente il report statistico formattato pronto per la stampa
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
