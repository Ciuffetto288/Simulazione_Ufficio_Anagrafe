import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

/**
 * Menu interattivo e collegamento tra l'utente e i servizi.
 * Qui dentro non c'e' logica di calcolo: si leggono i dati, si chiamano i
 * servizi e si stampa il risultato.
 */
public final class Menu {

    private final Scanner scanner;

    private final ConsoleUtils console;

    private final ComuneService comuneService;

    private final ArchivioService archivioService;

    private final CodiceFiscaleService codiceFiscaleService;

    private final ExportService exportService;

    private final StatisticheService statisticheService;

    /**
     * Crea i servizi una volta sola: comuni e archivio leggono i file nel
     * costruttore, quindi conviene farlo prima dello splash screen.
     *
     * @param scanner Scanner condiviso per gli input da tastiera
     */
    public Menu(Scanner scanner) {
        this.scanner = scanner;
        this.console = new ConsoleUtils(scanner);
        this.comuneService = new ComuneService();
        this.archivioService = new ArchivioService();
        this.codiceFiscaleService = new CodiceFiscaleService();
        this.exportService = new ExportService();
        this.statisticheService = new StatisticheService();
    }

    /**
     * Ciclo principale: si ripete finche' l'utente non sceglie 0.
     *
     * @param fastSplash true per accorciare la schermata iniziale
     */
    public void start(boolean fastSplash) {

        AsciiArt.showSplash(console, fastSplash);

        int choice;

        do {

            printMenu();

            choice = console.readInt("Scelta", 0, 9);

            switch (choice) {

                case 1 -> nuovoCittadino();

                case 2 -> calcolaCodiceFiscale();

                case 3 -> archivioCittadini();

                case 4 -> cercaCittadino();

                case 5 -> verificaCodiceFiscale();

                case 6 -> modificaCittadino();

                case 7 -> eliminaCittadino();

                case 8 -> esportaArchivio();

                case 9 -> statisticheArchivio();

                case 0 -> System.out.println(
                        AnsiColor.paint(
                                "Uscita da ANAGR@FE.",
                                AnsiColor.GREEN
                        )
                );

                default -> System.out.println("Scelta non valida.");
            }

        } while (choice != 0);
    }

    // le due righe in basso servono a capire da dove arrivano i dati,
    // e' comodo quando i comuni non vengono caricati
    private void printMenu() {

        console.clearScreen();

        System.out.println(
                AnsiColor.paint("""
                +------------------------------------------------+
                |                    ANAGR@FE                    |
                +------------------------------------------------+
                | 1. Nuovo cittadino                             |
                | 2. Calcola codice fiscale                      |
                | 3. Archivio cittadini                          |
                | 4. Cerca cittadino                             |
                | 5. Verifica codice fiscale                     |
                | 6. Modifica cittadino                          |
                | 7. Elimina cittadino                           |
                | 8. Esporta archivio                            |
                | 9. Statistiche archivio                        |
                | 0. Esci                                       |
                +------------------------------------------------+
                """,
                        AnsiColor.CYAN
                )
        );

        System.out.println(
                AnsiColor.paint(
                        "Comuni caricati: "
                                + comuneService.size()
                                + " da "
                                + comuneService.getSource(),
                        AnsiColor.DIM
                )
        );

        System.out.println(
                AnsiColor.paint(
                        "Archivio: " + archivioService.getPath(),
                        AnsiColor.DIM
                )
        );
    }

    private void nuovoCittadino() {

        printSection("CREAZIONE NUOVO CITTADINO");

        Cittadino cittadino =
                inputCittadino(
                        Optional.empty(),
                        archivioService.fiscalCodes()
                );

        archivioService.add(cittadino);

        System.out.println(
                AnsiColor.paint(
                        "\nCittadino salvato con successo.",
                        AnsiColor.GREEN
                )
        );

        System.out.println(cittadino.toCard());

        console.pause();
    }

    // come nuovoCittadino ma senza salvare: passo un set vuoto di codici
    // esistenti, cosi' non viene applicata l'omocodia
    private void calcolaCodiceFiscale() {

        printSection("CALCOLO CODICE FISCALE");

        Cittadino cittadino =
                inputCittadino(Optional.empty(), Set.of());

        System.out.println(
                AnsiColor.paint(
                        "\nCodice fiscale calcolato: "
                                + cittadino.getCodiceFiscale(),
                        AnsiColor.GREEN
                )
        );

        printValidation(
                codiceFiscaleService.verifica(
                        cittadino.getCodiceFiscale(),
                        comuneService
                )
        );

        console.pause();
    }

    private void archivioCittadini() {

        printSection("ARCHIVIO CITTADINI");

        List<Cittadino> cittadini = archivioService.all();

        if (cittadini.isEmpty()) {

            System.out.println("Archivio vuoto.");

            console.pause();

            return;
        }

        System.out.println("1. Tabella compatta");
        System.out.println("2. Card singole");

        int choice =
                console.readInt(
                        "Formato visualizzazione",
                        1,
                        2
                );

        if (choice == 1) {
            printTable(cittadini);
        } else {
            printCards(cittadini);
        }

        console.pause();
    }

    private void cercaCittadino() {

        printSection("RICERCA CITTADINO");

        String query =
                console.readRequired(
                        "Cerca per nome, cognome o codice fiscale"
                );

        List<Cittadino> results =
                archivioService.search(query);

        if (results.isEmpty()) {

            System.out.println(
                    AnsiColor.paint(
                            "Nessun cittadino trovato.",
                            AnsiColor.YELLOW
                    )
            );

        } else {

            printCards(results);
        }

        console.pause();
    }

    private void verificaCodiceFiscale() {

        printSection("VERIFICA CODICE FISCALE");

        String codiceFiscale =
                console.readRequired("Codice fiscale");

        printValidation(
                codiceFiscaleService.verifica(
                        codiceFiscale,
                        comuneService
                )
        );

        console.pause();
    }

    // il codice fiscale vecchio serve due volte: per trovare il record e per
    // escluderlo dal controllo delle collisioni
    private void modificaCittadino() {

        printSection("MODIFICA CITTADINO");

        String codiceFiscale =
                console.readRequired(
                        "Codice fiscale del cittadino da modificare"
                );

        Optional<Cittadino> found =
                archivioService.findByCodiceFiscale(codiceFiscale);

        if (found.isEmpty()) {

            System.out.println(
                    AnsiColor.paint(
                            "Cittadino non trovato.",
                            AnsiColor.YELLOW
                    )
            );

            console.pause();

            return;
        }

        Cittadino current = found.get();

        System.out.println(current.toCard());

        System.out.println(
                "\nLascia vuoto un campo per mantenere il valore attuale."
        );

        Cittadino updated =
                inputCittadino(
                        Optional.of(current),
                        archivioService.fiscalCodesExcept(
                                current.getCodiceFiscale()
                        )
                );

        archivioService.update(
                current.getCodiceFiscale(),
                updated
        );

        System.out.println(
                AnsiColor.paint(
                        "\nModifica salvata.",
                        AnsiColor.GREEN
                )
        );

        System.out.println(updated.toCard());

        console.pause();
    }

    private void eliminaCittadino() {

        printSection("ELIMINA CITTADINO");

        String codiceFiscale =
                console.readRequired(
                        "Codice fiscale del cittadino da eliminare"
                );

        Optional<Cittadino> found =
                archivioService.findByCodiceFiscale(codiceFiscale);

        if (found.isEmpty()) {

            System.out.println(
                    AnsiColor.paint(
                            "Cittadino non trovato.",
                            AnsiColor.YELLOW
                    )
            );

            console.pause();

            return;
        }

        System.out.println(found.get().toCard());

        if (console.confirm("Confermi eliminazione definitiva")) {

            archivioService.delete(
                    found.get().getCodiceFiscale()
            );

            System.out.println(
                    AnsiColor.paint(
                            "Cittadino eliminato.",
                            AnsiColor.GREEN
                    )
            );

        } else {

            System.out.println("Operazione annullata.");
        }

        console.pause();
    }

    private void esportaArchivio() {

        printSection("ESPORTA ARCHIVIO");

        List<Cittadino> cittadini = archivioService.all();

        if (cittadini.isEmpty()) {

            System.out.println(
                    "Archivio vuoto: niente da esportare."
            );

            console.pause();

            return;
        }

        System.out.println("1. TXT allineato");
        System.out.println("2. CSV");

        int choice =
                console.readInt("Formato", 1, 2);

        Path output =
                choice == 1
                        ? exportService.exportTxt(cittadini)
                        : exportService.exportCsv(cittadini);

        System.out.println(
                AnsiColor.paint(
                        "Archivio esportato in: " + output,
                        AnsiColor.GREEN
                )
        );

        console.pause();
    }

    private void statisticheArchivio() {

        printSection("STATISTICHE ARCHIVIO");

        System.out.println(
                statisticheService.report(
                        archivioService.all()
                )
        );

        console.pause();
    }

    /**
     * Unico punto di inserimento dati, usato sia in creazione sia in modifica.
     * Se current e' presente siamo in modifica e ogni campo lasciato vuoto
     * mantiene il valore di prima; se e' vuoto tutti i campi sono obbligatori.
     * Il codice fiscale viene ricalcolato in ogni caso, perche' basta cambiare
     * una lettera del nome per cambiarlo.
     *
     * @param current cittadino da modificare, oppure Optional vuoto
     * @param codiciEsistenti codici da evitare
     * @return il cittadino con i dati inseriti
     */
    private Cittadino inputCittadino(
            Optional<Cittadino> current,
            Set<String> codiciEsistenti
    ) {

        String nome = current
                .map(c -> console.readOptional("Nome", c.getNome()))
                .orElseGet(() -> console.readRequired("Nome"));

        String cognome = current
                .map(c -> console.readOptional("Cognome", c.getCognome()))
                .orElseGet(() -> console.readRequired("Cognome"));

        LocalDate dataNascita = current
                .map(c -> console.readOptionalDate(
                        "Data nascita",
                        c.getDataNascita()
                ))
                .orElseGet(() -> console.readDate("Data nascita"));

        char sesso = current
                .map(c -> console.readOptionalSex(
                        "Sesso",
                        c.getSesso()
                ))
                .orElseGet(() -> console.readSex("Sesso"));

        Comune comune = inputComune(current);

        String codiceFiscale =
                codiceFiscaleService.genera(
                        nome,
                        cognome,
                        dataNascita,
                        sesso,
                        comune.getCodiceCatastale(),
                        codiciEsistenti
                );

        return new Cittadino(
                nome,
                cognome,
                dataNascita,
                sesso,
                comune.getNome(),
                comune.getProvincia(),
                comune.getCodiceCatastale(),
                codiceFiscale
        );
    }

    /**
     * In modifica il comune viene richiesto a parte: se l'utente lo lascia
     * invariato si riusa il codice catastale che era gia' salvato, senza
     * rifare la ricerca.
     *
     * @param current cittadino in modifica, oppure Optional vuoto
     * @return il comune scelto
     */
    private Comune inputComune(Optional<Cittadino> current) {

        if (current.isPresent()) {

            Cittadino c = current.get();

            System.out.print(
                    "Comune [" + c.getComune() + "]: "
            );

            String name = scanner.nextLine().trim();

            if (name.isBlank()) {

                return new Comune(
                        c.getComune(),
                        c.getProvincia(),
                        c.getCodiceComune()
                );
            }

            String province =
                    console.readOptional(
                            "Provincia",
                            c.getProvincia()
                    );

            return resolveComune(name, province);
        }

        String name = console.readRequired("Comune");

        String province =
                console.readLine(
                        "Provincia (sigla, opzionale)"
                ).toUpperCase();

        return resolveComune(name, province);
    }

    /**
     * Tre tentativi in cascata: ricerca esatta, lista di suggerimenti da cui
     * scegliere, e in ultima istanza inserimento a mano del codice catastale
     * (serve per i comuni soppressi e per chi e' nato all'estero, che nei
     * file dei comuni non ci sono).
     *
     * @param name nome scritto dall'utente
     * @param province sigla provincia, eventualmente vuota
     * @return il comune da usare per il calcolo
     */
    private Comune resolveComune(String name, String province) {

        Optional<Comune> found =
                comuneService.findByNameAndProvince(
                        name,
                        province
                );

        if (found.isPresent()) {
            return found.get();
        }

        List<Comune> suggestions =
                comuneService.searchByName(name);

        if (!suggestions.isEmpty()) {

            System.out.println(
                    AnsiColor.paint(
                            "\nComune non trovato esattamente. Suggerimenti:",
                            AnsiColor.YELLOW
                    )
            );

            for (int i = 0; i < suggestions.size(); i++) {

                System.out.printf(
                        "%2d. %s%n",
                        i + 1,
                        suggestions.get(i)
                );
            }

            if (console.confirm(
                    "Vuoi scegliere uno dei suggerimenti"
            )) {

                int choice =
                        console.readInt(
                                "Numero suggerimento",
                                1,
                                suggestions.size()
                        );

                return suggestions.get(choice - 1);
            }
        }

        System.out.println(
                AnsiColor.paint(
                        "Inserimento manuale codice catastale.",
                        AnsiColor.YELLOW
                )
        );

        String code;

        // formato del codice catastale: una lettera e tre cifre
        do {

            code = console
                    .readRequired(
                            "Codice catastale (es. H501)"
                    )
                    .toUpperCase();

            if (!code.matches("[A-Z][0-9]{3}")) {

                System.out.println(
                        AnsiColor.paint(
                                "Codice catastale non valido. Usa una lettera e tre numeri.",
                                AnsiColor.YELLOW
                        )
                );
            }

        } while (!code.matches("[A-Z][0-9]{3}"));

        String finalProvince =
                province == null || province.isBlank()
                        ? console.readRequired("Provincia")
                        : province;

        return new Comune(name, finalProvince, code);
    }

    private void printValidation(
            CodiceFiscaleService.ValidationResult result
    ) {

        String title =
                result.isValid()
                        ? "CODICE FISCALE VALIDO"
                        : "CODICE FISCALE NON VALIDO";

        System.out.println(
                AnsiColor.paint(
                        "\n" + title,
                        result.isValid()
                                ? AnsiColor.GREEN
                                : AnsiColor.RED
                )
        );

        for (String message : result.getMessages()) {
            System.out.println(" - " + message);
        }
    }

    // intestazione con lo stesso formato usato da Cittadino.toTableLine()
    private void printTable(List<Cittadino> cittadini) {

        System.out.printf(
                "%-18s %-18s %-12s %-4s %-22s %-4s %-16s%n",
                "Nome",
                "Cognome",
                "Nascita",
                "S",
                "Comune",
                "Prov",
                "Codice Fiscale"
        );

        System.out.println("-".repeat(100));

        cittadini.forEach(
                c -> System.out.println(c.toTableLine())
        );
    }

    private void printCards(List<Cittadino> cittadini) {

        for (int i = 0; i < cittadini.size(); i++) {

            System.out.println(
                    cittadini.get(i).toCard()
            );

            if (i < cittadini.size() - 1) {
                System.out.println();
            }
        }
    }

    private void printSection(String title) {

        console.clearScreen();

        System.out.println(
                AnsiColor.paint(
                        title,
                        AnsiColor.BOLD + AnsiColor.CYAN
                )
        );

        System.out.println("-".repeat(title.length()));
    }
}