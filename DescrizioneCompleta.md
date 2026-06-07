# Descrizione Completa

Questo documento descrive in modo dettagliato la struttura del progetto ANAGR@FE. L'applicazione è organizzata in classi Java con responsabilità separate: alcune rappresentano i dati, altre gestiscono servizi applicativi, altre ancora curano input, output, esportazioni e test interni. Il codice catastale rimane un dato tecnico necessario al calcolo del codice fiscale, mentre l'interfaccia utente privilegia comune e provincia perché sono informazioni comprensibili a chi usa il programma.

1. `AnsiColor.java`: contiene le costanti ANSI usate per colorare il testo nel terminale. Definisce colori come rosso, verde, giallo, ciano e stili come grassetto o testo attenuato. Il metodo `paint` incapsula il testo tra codice colore e reset, evitando di lasciare la console colorata per errore dopo una stampa.

2. `AppPaths.java`: centralizza la gestione dei percorsi del progetto. Rileva automaticamente la cartella radice cercando le directory `src` e `data`, restituisce il percorso della cartella dati e costruisce percorsi sicuri per file come `cittadini.csv`, `comuni.csv` e `comuni.xlsx`. Crea la cartella `data` se manca.

3. `ArchivioService.java`: gestisce l'archivio persistente dei cittadini. Legge `data/cittadini.csv`, ricostruisce gli oggetti `Cittadino`, salva le modifiche, aggiunge nuovi cittadini, aggiorna record esistenti, elimina record e fornisce ricerche per codice fiscale o testo. Mantiene il codice catastale nello storage interno perché serve per ricalcolo e modifica.

4. `AsciiArt.java`: mostra la schermata iniziale dell'applicazione. Contiene il logo testuale ANAGR@FE, stampa autore, versione e descrizione breve, poi attende qualche secondo o una pausa ridotta quando viene usato il flag `--fast`.

5. `Cittadino.java`: rappresenta un cittadino dell'archivio. Conserva nome, cognome, data di nascita, sesso, comune, provincia, codice catastale interno e codice fiscale. Fornisce metodi per visualizzare il cittadino in tabella o scheda, salvare il record nel CSV interno e generare righe CSV di esportazione senza mostrare il codice catastale.

6. `CodiceFiscaleService.java`: implementa la logica del codice fiscale italiano. Calcola codice cognome, codice nome, data, sesso, comune, carattere di controllo e varianti omocodiche. Verifica anche un codice fiscale esistente controllando lunghezza, formato, checksum, data, sesso e coerenza del comune tramite `ComuneService`.

7. `Comune.java`: rappresenta un comune italiano con nome, sigla provincia e codice catastale. È immutabile: i campi vengono assegnati nel costruttore e non cambiano. Il metodo `toString` restituisce solo `Nome (Provincia)` per non mostrare codici catastali negli elenchi rivolti all'utente.

8. `ComuneService.java`: carica e interroga l'archivio dei comuni. Prova prima `comuni.xlsx`, poi `comuni.csv`, poi un elenco interno minimo. Legge il file Excel come archivio ZIP e parser XML. La ricerca è flessibile: riconosce input come `Roma (RM)`, `Roma RM` e `Roma - RM`, separando automaticamente nome e provincia.

9. `ConsoleUtils.java`: raccoglie le operazioni ripetitive di input da console. Legge stringhe obbligatorie o opzionali, interi entro un intervallo, date nel formato italiano, sesso `M/F` e conferme `S/N`. Gestisce anche la pulizia dello schermo e la pausa con INVIO.

10. `DateUtils.java`: contiene metodi statici per lavorare con le date. Gestisce il formato interno ISO `yyyy-MM-dd`, il formato italiano `dd/MM/yyyy`, il parsing sicuro con `Optional`, la formattazione e il calcolo dell'età anagrafica.

11. `ExportService.java`: esporta l'archivio cittadini in formato TXT o CSV. Il TXT usa colonne allineate, mentre il CSV è adatto all'apertura in fogli di calcolo. Le esportazioni mostrano comune e provincia e non includono il codice catastale, così il risultato è leggibile anche per utenti non tecnici.

12. `Main.java`: è il punto di ingresso del programma. Controlla gli argomenti della riga di comando: con `--self-test` esegue i test interni, con `--fast` avvia il menu saltando quasi tutta la pausa iniziale. In assenza di flag apre il menu principale.

13. `Menu.java`: coordina l'interfaccia interattiva. Mostra il menu, richiama i servizi, gestisce creazione, calcolo codice fiscale, archivio, ricerca, verifica, modifica, eliminazione, esportazione e statistiche. È il punto in cui l'utente inserisce i dati e vede i risultati.

14. `SelfTest.java`: contiene controlli automatici rapidi. Verifica la generazione del codice fiscale di esempio, la produzione di una variante omocodica, la validazione dei codici e la ricerca flessibile dei comuni con provincia scritta nello stesso campo, compresi i casi `Rovigo (RO)` e `Monselice PD`.

15. `StatisticheService.java`: genera un report testuale sull'archivio cittadini. Calcola totale record, numero di uomini, numero di donne, età media e classifica dei comuni più presenti. Se l'archivio è vuoto produce comunque un messaggio leggibile.

16. `StringUtils.java`: fornisce funzioni di supporto sulle stringhe. Normalizza nomi e ricerche, capitalizza parole, esegue escaping CSV e divide righe CSV rispettando virgolette, separatori interni e virgolette raddoppiate.

