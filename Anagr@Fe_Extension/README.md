# Anagr@Fe browser extension

Struttura WebExtension multipiattaforma per Chrome, Edge, Opera, Firefox e Safari.

## Struttura codice

- `typescript/`: sorgenti TypeScript;
- `chrome-edge-opera/`: pacchetto caricabile nei browser Chromium;
- `firefox/`: pacchetto caricabile in Firefox;
- `safari/`: solo README per la conversione Xcode.

## Sviluppo TypeScript

Dalla radice del progetto esegui:

```sh
npm install --prefix Anagr@Fe_Extension
npm run --prefix Anagr@Fe_Extension typecheck
npm run --prefix Anagr@Fe_Extension build
```

Il comando `build` compila `typescript/` e aggiorna automaticamente i file
JavaScript caricabili dai browser nelle cartelle `chrome-edge-opera/` e
`firefox/`.

## Installazione locale

- **Chrome**: apri `chrome://extensions`, attiva la modalita sviluppatore e scegli
  "Carica estensione non pacchettizzata", selezionando `chrome-edge-opera/`.
- **Edge**: apri `edge://extensions` e carica `chrome-edge-opera/`.
- **Opera**: apri `opera://extensions` e carica `chrome-edge-opera/`.
- **Firefox**: apri `about:debugging#/runtime/this-firefox`, scegli "Carica
  componente aggiuntivo temporaneo" e seleziona `firefox/manifest.json`.
- **Safari**: segui `safari/README.md` per convertire la WebExtension in un
  progetto Safari Web Extension con Xcode su macOS.

L'estensione chiama `http://localhost:8080/api`; il server Rust locale deve
essere attivo per ricevere dati. Il server legge l'archivio creato dal
programma Java nel percorso applicativo del sistema (`~/.config/Anagr_fe/data`
su Linux). Per un archivio in una directory diversa, avvia il server con
`ANAGRAFE_DATA_DIR` e imposta lo stesso indirizzo nell'estensione.

## Funzioni incluse

- dashboard popup con grafica responsive;
- controllo dello stato del server locale;
- ricerca cittadini tramite `GET /api/cittadini?query=...`;
- visualizzazione di nome, cognome, codice fiscale, comune e data di nascita;
- configurazione e salvataggio dell'indirizzo base dell'API;
- icona personalizzata Anagr@Fe.

Il backend dovrebbe restituire un array di cittadini oppure un oggetto con una
proprieta `cittadini` o `data`. Ogni record puo contenere `nome`, `cognome`,
`codiceFiscale`, `comune` e `dataNascita`.