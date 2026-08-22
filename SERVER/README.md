# Anagr@Fe Server

Backend Rust del progetto. Espone API HTTP sulla porta `8080`.

## Avvio

```sh
cargo run
```

Il binario usa il protocollo Native Messaging su standard input/output e
pubblica comunque l'API HTTP sulla porta `8080`; normalmente viene avviato
automaticamente dall'estensione tramite l'host installato.

## API disponibili

- `GET /api`: verifica che il server sia attivo.
- `GET /api/cittadini?query=mario`: cerca per nome, cognome o codice fiscale.

Il server legge a ogni ricerca lo stesso archivio CSV usato da Java, quindi le
modifiche salvate dal programma console sono subito disponibili nell'estensione.
Su Linux il percorso predefinito e `~/.config/Anagr_fe/data/cittadini.csv`, in
coerenza con `AppPaths.java`. Per eseguire il server con un archivio portabile:

```sh
ANAGRAFE_DATA_DIR=/percorso/con/data cargo run
```

Il file deve avere l'intestazione e il formato interno Java:
`nome;cognome;dataNascita;sesso;comune;provincia;codiceComune;codiceFiscale`.