# Native Messaging host

L'host viene avviato automaticamente dall'estensione quando il popup si apre.
Non mostra finestre: esegue direttamente il binario Rust come processo locale.

Prima dell'uso:

1. Compilare il server con `cargo build --release`.
2. Sostituire `path` nei manifest con il percorso assoluto del binario `anagrafe-server`.
3. Nel manifest Chromium sostituire `REPLACE_WITH_EXTENSION_ID` con l'ID mostrato dal browser.
4. Installare il manifest nella directory Native Messaging del browser.

Su Linux:

- Chromium: `~/.config/google-chrome/NativeMessagingHosts/anagrafe.server.json`
- Firefox: `~/.mozilla/native-messaging-hosts/anagrafe.server.json`

Il programma Java deve essere avviato normalmente: il suo bridge locale viene
avviato automaticamente da `Main` e resta accessibile solo su `127.0.0.1`.
