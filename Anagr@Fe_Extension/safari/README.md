# Safari

Safari usa lo stesso standard WebExtension, ma il pacchetto finale viene
generato tramite Xcode su macOS.

## Conversione

1. Copia la cartella `chrome-edge-opera/` in un percorso accessibile su macOS.
2. Esegui nel Terminale:

   ```sh
   xcrun safari-web-extension-converter ./chrome-edge-opera \
     --project-location ./SafariExtension \
     --app-name Anagr@Fe
   ```

3. Apri il progetto `.xcodeproj` generato in Xcode.
4. Configura Team e Bundle Identifier, poi esegui l'app host per installare
   l'estensione in Safari.

Il progetto Xcode nativo non puo essere generato in questo ambiente Linux:
richiede macOS, Xcode e il relativo certificato di firma. Il codice
WebExtension e gia pronto per la conversione.