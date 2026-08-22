# ANAGR@FE - Ecosistema completo

## 1. Scopo del progetto

ANAGR@FE e un ecosistema locale per la simulazione di un ufficio anagrafe.
Il progetto comprende:

- un programma Java da terminale;
- un server HTTP Rust;
- un'estensione WebExtension per Chrome, Edge, Opera e Firefox;
- un sito web separato;
- archivi CSV e dati dei comuni italiani.

L'obiettivo e permettere al programma Java, al server e all'estensione di
condividere gli stessi dati senza cambiare l'interfaccia grafica esistente.

## 2. Componenti

### Programma Java

Il programma Java e il cuore applicativo. Gestisce:

- creazione di cittadini;
- calcolo del codice fiscale;
- controllo del codice fiscale;
- gestione dei comuni;
- ricerca nell'archivio;
- modifica ed eliminazione dei cittadini;
- esportazione CSV e TXT;
- statistiche;
- persistenza dei dati.

Il punto di ingresso e `src/Main.java`. Il menu e coordinato da
`src/Menu.java`, mentre i servizi applicativi sono separati in classi dedicate.

### Bridge Java locale

`src/EcosistemaBridge.java` e il collegamento interno tra Java e Rust.

Il bridge:

- si avvia automaticamente insieme a Java;
- ascolta soltanto su `127.0.0.1`;
- usa la porta `8090`;
- non e raggiungibile dalla rete esterna;
- espone lo stato del cittadino calcolato ma non ancora confermato.

Endpoint interno:

```text
GET http://127.0.0.1:8090/api/stato
```

Risposta quando non esiste un calcolo temporaneo:

```json
{"cittadino":null}
```

Risposta durante la pausa prima di premere Invio:

```json
{
  "cittadino": {
    "nome": "Mario",
    "cognome": "Rossi",
    "dataNascita": "1980-01-01",
    "sesso": "M",
    "comune": "Roma",
    "provincia": "RM",
    "codiceComune": "H501",
    "codiceFiscale": "RSSMRA80A01H501Z"
  }
}
```

Il cittadino temporaneo viene memorizzato dopo il calcolo e viene rimosso dopo
la pressione di Invio. Non viene scritto nel CSV, quindi il comportamento del
menu Java resta invariato.

### Server Rust

Il server Rust si trova in `SERVER/` e fornisce l'API usata dall'estensione.

Funzioni principali:

- legge il CSV persistente prodotto da Java;
- interroga il bridge Java per recuperare il dato temporaneo;
- unisce il dato temporaneo ai risultati senza duplicarlo;
- filtra per nome, cognome o codice fiscale;
- ordina i risultati per cognome, nome e codice fiscale;
- abilita CORS per consentire le richieste dell'estensione;
- espone una modalita Native Messaging per l'avvio nascosto.

Endpoint pubblici del server Rust:

```text
GET http://127.0.0.1:8080/api
GET http://127.0.0.1:8080/api/cittadini?query=mario
```

La risposta di `/api/cittadini` e un array JSON. Ogni elemento contiene almeno:

```json
{
  "nome": "Mario",
  "cognome": "Rossi",
  "dataNascita": "1980-01-01",
  "comune": "Roma",
  "provincia": "RM",
  "codiceFiscale": "RSSMRA80A01H501Z"
}
```

### Estensione browser

I sorgenti TypeScript sono in `Anagr@Fe_Extension/typescript/`.

I pacchetti pronti per il caricamento sono:

- `Anagr@Fe_Extension/chrome-edge-opera/`;
- `Anagr@Fe_Extension/firefox/`.

L'estensione:

- controlla la disponibilita dell'API;
- avvia l'host Native Messaging all'apertura del popup;
- cerca cittadini tramite l'API Rust;
- visualizza nome, cognome, codice fiscale, comune, provincia e data di nascita;
- salva l'indirizzo API nelle impostazioni locali del browser.

HTML e CSS non sono stati modificati per cambiare la grafica.

## 3. Flusso completo

```text
Programma Java
      |
      | cittadino temporaneo in memoria
      v
Bridge Java 127.0.0.1:8090
      ^
      | GET /api/stato
      |
Server Rust 127.0.0.1:8080
      ^
      | Native Messaging all'apertura del popup
      |
Estensione browser
```

Il flusso operativo e questo:

1. Java viene avviato normalmente.
2. `Main` avvia automaticamente `EcosistemaBridge`.
3. L'utente calcola un codice fiscale dal menu Java.
4. Il cittadino calcolato viene conservato temporaneamente in memoria.
5. L'utente apre il popup dell'estensione.
6. L'estensione richiede l'host Native Messaging.
7. L'host avvia il binario Rust senza mostrare una finestra.
8. Rust legge il CSV e interroga il bridge Java.
9. Il risultato temporaneo viene mostrato insieme ai dati persistenti.
10. Quando l'utente preme Invio in Java, il dato temporaneo viene cancellato.

## 4. Archivio condiviso

Java salva l'archivio persistente nel percorso applicativo del sistema:

- Linux: `~/.config/Anagr_fe/data/cittadini.csv`;
- macOS: `~/Library/Anagr_fe/data/cittadini.csv`;
- Windows: `%APPDATA%/Anagr_fe/data/cittadini.csv`.

Il formato interno e:

```text
nome;cognome;dataNascita;sesso;comune;provincia;codiceComune;codiceFiscale
```

Il codice catastale viene mantenuto nel CSV per permettere a Java di ricalcolare
o modificare correttamente i codici fiscali. Il server Rust non lo mostra nella
risposta destinata all'estensione.

Per usare una cartella dati diversa si puo impostare:

```sh
ANAGRAFE_DATA_DIR=/percorso/dati cargo run
```

La variabile deve indicare la cartella che contiene `cittadini.csv`, non il
percorso completo del file.

## 5. Avvio e compilazione

### Procedura automatica macOS

Per macOS Apple Silicon e disponibile lo script `setup-macos.sh`. Il progetto
deve essere presente sul Mac, non soltanto nel container Linux.

Dalla cartella principale del progetto:

```sh
chmod +x setup-macos.sh
./setup-macos.sh
```

Il primo avvio compila Java, Rust ed estensione. Poi importa
`Anagr@Fe_Extension/chrome-edge-opera` in `chrome://extensions`, copia l'ID
mostrato da Chrome e rilancia:

```sh
./setup-macos.sh ID_ESTENSIONE_CHROME
```

Lo script installa automaticamente il manifest Native Messaging in:

```text
~/Library/Application Support/Google/Chrome/NativeMessagingHosts/anagrafe.server.json
```

Per usare lo script servono Homebrew, JDK 17 o superiore, Node.js/npm e
Rust/Cargo. Il comando per clonare il progetto e:

```sh
git clone https://github.com/Ciuffetto288/Simulazione_Ufficio_Anagrafe.git
cd Simulazione_Ufficio_Anagrafe
```

### Java

Il progetto Java richiede una versione JDK compatibile con le funzionalita
presenti nel codice, inclusi pattern matching e text block.

```sh
javac -d build/classes src/*.java
java -cp build/classes Main
```

Per l'avvio rapido:

```sh
java -cp build/classes Main --fast
```

Per i controlli interni:

```sh
java -cp build/classes Main --self-test
```

### Rust

Dalla cartella `SERVER/`:

```sh
cargo build --release
cargo test
```

Il binario di produzione sara normalmente:

```text
SERVER/target/release/anagrafe-server
```

### TypeScript

Dalla radice del repository:

```sh
npm install --prefix Anagr@Fe_Extension
npm run --prefix Anagr@Fe_Extension typecheck
npm run --prefix Anagr@Fe_Extension build
```

La build compila i sorgenti TypeScript e aggiorna automaticamente i file
JavaScript dentro i pacchetti Chrome/Edge/Opera e Firefox.

## 6. Avvio nascosto tramite Native Messaging

Per motivi di sicurezza, un'estensione browser non puo eseguire direttamente un
programma locale. L'host Native Messaging e quindi necessario per il primo
collegamento tecnico.

I manifest di esempio sono in:

- `Anagr@Fe_Extension/native-messaging/anagrafe.server.chrome.json`;
- `Anagr@Fe_Extension/native-messaging/anagrafe.server.firefox.json`.

Prima dell'installazione occorre:

1. compilare il server Rust in modalita release;
2. sostituire nel manifest il percorso del binario Rust;
3. sostituire l'ID segnaposto dell'estensione Chromium;
4. installare il manifest nella directory Native Messaging del browser.

Percorsi Linux:

```text
Chromium: ~/.config/google-chrome/NativeMessagingHosts/anagrafe.server.json
Firefox:  ~/.mozilla/native-messaging-hosts/anagrafe.server.json
```

Dopo questa configurazione iniziale, l'utente apre normalmente l'estensione:
il server viene avviato in background senza una finestra visibile.

## 7. Sicurezza e limiti

- Il bridge Java accetta richieste soltanto da `127.0.0.1`.
- Il server Rust ascolta localmente su `127.0.0.1:8080`.
- Il Native Messaging host deve essere installato nel sistema operativo.
- Il codice fiscale temporaneo non viene persistito automaticamente.
- Premere Invio in Java elimina il valore temporaneo dal bridge.
- Il server Rust ricarica il CSV a ogni ricerca, quindi rileva subito le nuove
  registrazioni Java.
- Se il bridge Java non e attivo, Rust continua a funzionare usando il CSV.
- Se l'host Native Messaging non e installato, l'estensione non si blocca, ma
  il server Rust deve essere avviato in altro modo.

## 8. Struttura principale

```text
src/
  Main.java
  Menu.java
  EcosistemaBridge.java
  ArchivioService.java
  Cittadino.java
  CodiceFiscaleService.java
  ComuneService.java
  ConsoleUtils.java
  DateUtils.java
  ExportService.java
  StatisticheService.java
  StringUtils.java

SERVER/
  Cargo.toml
  src/main.rs

Anagr@Fe_Extension/
  typescript/
    content.ts
    popup.ts
  chrome-edge-opera/
  firefox/
  native-messaging/

sito-anagrafe/
  index.html
  encrypted.html
  assets/

data/
  cittadini.csv
  comuni.csv
```

## 9. Conteggio del codice

Il conteggio e stato eseguito sull'intero progetto, considerando i file sorgente
Java, Rust, TypeScript, JavaScript, HTML e CSS.

Sono state escluse:

- righe vuote;
- righe costituite soltanto da commenti;
- documentazione Markdown;
- JavaDoc generato;
- file CSV e altri dati;
- directory `node_modules`.

Sono stati inclusi anche i file JavaScript, HTML e CSS copiati nei pacchetti
browser, perche sono file effettivamente presenti nel progetto.

| Linguaggio | Righe effettive |
|---|---:|
| Java | 2.293 |
| Rust | 212 |
| TypeScript | 82 |
| JavaScript | 312 |
| HTML | 1.066 |
| CSS | 872 |
| **Totale** | **4.837** |

Il totale effettivo rilevato e quindi:

# 4.837 righe di codice

Il numero puo cambiare dopo una nuova build o dopo l'aggiunta di nuovi file nei
pacchetti dell'estensione.
