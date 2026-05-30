# ANAGR@FE - Simulatore Ufficio Anagrafe

ANAGR@FE è un'applicazione Java da terminale per simulare alcune operazioni di un ufficio anagrafe: creazione cittadini, calcolo del codice fiscale, ricerca, modifica, eliminazione, verifica del codice fiscale, esportazione e statistiche.

## Tecnologie usate

- Java: linguaggio principale dell'applicazione.
- CSV: formato usato per salvare l'archivio cittadini e per esportare dati leggibili da fogli di calcolo.
- XLSX: formato usato come archivio esterno dei comuni italiani.
- XML DOM e ZIP standard Java: usati per leggere internamente il file `.xlsx` senza librerie esterne.
- ANSI escape code: usati per colorare e pulire la console.
- JavaDoc: usato per produrre documentazione HTML separata dal codice sorgente.

## Avvio rapido

Da questa cartella:

```bash
./run.sh
```

Per saltare quasi del tutto la schermata iniziale:

```bash
./run.sh --fast
```

Per eseguire i controlli automatici interni:

```bash
./run.sh --self-test
```

## Compilazione separata

Per compilare senza mischiare i file `.class` con i sorgenti:

```bash
javac -d build/classes src/anagrafe/*.java
```

Per avviare la versione compilata:

```bash
java -cp build/classes Main
```

## Come funziona

All'avvio `Main` crea il menu principale. `Menu` coordina tutti i servizi: legge gli input da console, chiama il servizio comuni per risolvere il comune corretto, usa il servizio codice fiscale per generare o verificare il codice, salva i cittadini nell'archivio CSV e permette esportazioni o statistiche.

Il programma carica i comuni da `data/comuni.xlsx`; se il file non è leggibile prova `data/comuni.csv`; se anche quello non è disponibile usa un elenco interno minimo. La ricerca comuni accetta sia campi separati sia input naturali come `Rovigo (RO)` o `Monselice PD`.

L'archivio interno conserva il codice catastale perché serve al calcolo del codice fiscale. Nelle visualizzazioni e nelle esportazioni per l'utente, però, vengono mostrati comune e provincia, non il codice catastale.

## File principali

- `src/anagrafe/Main.java`: punto di ingresso dell'applicazione.
- `src/anagrafe/Menu.java`: menu interattivo e coordinamento dei servizi.
- `src/anagrafe/Cittadino.java`: modello dati del cittadino.
- `src/anagrafe/Comune.java`: modello dati del comune.
- `src/anagrafe/ComuneService.java`: caricamento e ricerca dei comuni.
- `src/anagrafe/CodiceFiscaleService.java`: generazione, omocodia e validazione del codice fiscale.
- `src/anagrafe/ArchivioService.java`: lettura e scrittura del file `data/cittadini.csv`.
- `src/anagrafe/ExportService.java`: esportazione TXT e CSV dell'archivio cittadini.
- `src/anagrafe/StatisticheService.java`: report statistico sull'archivio.
- `src/anagrafe/ConsoleUtils.java`: input, conferme, date e pulizia console.
- `src/anagrafe/DateUtils.java`: parsing, formattazione ed età.
- `src/anagrafe/StringUtils.java`: normalizzazione testi e parsing CSV.
- `src/anagrafe/AppPaths.java`: percorsi di progetto e cartella dati.
- `src/anagrafe/AnsiColor.java`: colori ANSI da terminale.
- `src/anagrafe/AsciiArt.java`: schermata iniziale.
- `src/anagrafe/SelfTest.java`: controlli automatici interni.

## File dati

- `data/comuni.xlsx`: archivio principale dei comuni.
- `data/comuni.csv`: archivio comuni alternativo o di fallback.
- `data/cittadini.csv`: archivio persistente dei cittadini creati.

