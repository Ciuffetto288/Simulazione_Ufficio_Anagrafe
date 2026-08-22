(() => {
  "use strict";

  const apiUrl = "http://localhost:8080/api";

  console.log("Anagr@Fe: estensione attivata sulla pagina.");

  fetch(apiUrl)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`Risposta HTTP ${response.status}`);
      }

      return response.json();
    })
    .then((data) => {
      console.log("Anagr@Fe: dati ricevuti dal server locale.", data);
    })
    .catch((error) => {
      console.warn("Anagr@Fe: server locale non raggiungibile.", error);
    });
})();