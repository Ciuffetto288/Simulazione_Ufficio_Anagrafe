"use strict";
(() => {
    "use strict";
    const browserApi = globalThis.browser
        ?? globalThis.chrome;
    const query = (selector) => document.querySelector(selector);
    const elements = {
        apiUrl: query("#api-url"), dot: query("#connection-dot"),
        message: query("#search-message"), refresh: query("#refresh-button"),
        results: query("#results"), search: query("#search-form"),
        status: query("#server-status"), input: query("#search-input"), save: query("#save-button")
    };
    const storage = browserApi.storage.local;
    try {
        browserApi.runtime?.connectNative?.("anagrafe.server")?.postMessage({ command: "start" });
    }
    catch {
        // L'API HTTP puo essere gia attiva anche senza host Native Messaging.
    }
    const normaliseBaseUrl = (value) => value.trim().replace(/\/+$/, "");
    async function getBaseUrl() { const saved = await storage.get("apiBaseUrl"); return normaliseBaseUrl(saved.apiBaseUrl || elements.apiUrl.value); }
    function setConnection(online, label) { elements.dot.classList.toggle("online", online); elements.dot.classList.toggle("offline", !online); elements.status.textContent = label; }
    async function checkServer() {
        setConnection(false, "Controllo in corso...");
        try {
            const response = await fetch(`${await getBaseUrl()}/api`);
            if (!response.ok)
                throw new Error();
            setConnection(true, "Connesso e operativo");
        }
        catch {
            setConnection(false, "Server non raggiungibile");
        }
    }
    function showMessage(text, isError = false) { elements.message.textContent = text; elements.message.classList.toggle("error", isError); }
    function escapeHtml(value) { const div = document.createElement("div"); div.textContent = value; return div.innerHTML; }
    function renderResults(payload) {
        const records = Array.isArray(payload) ? payload : payload.cittadini || payload.data || [];
        elements.results.replaceChildren();
        if (!records.length) {
            elements.results.innerHTML = '<div class="empty">Nessun cittadino trovato.</div>';
            return;
        }
        records.slice(0, 8).forEach((record) => {
            const card = document.createElement("article");
            card.className = "result";
            const name = record.nome && record.cognome ? `${record.nome} ${record.cognome}` : record.nome || record.cognome || "Cittadino";
            const comune = record.comune && record.provincia ? `${record.comune} (${record.provincia})` : record.comune;
            const details = [record.codiceFiscale, comune, record.dataNascita].filter(Boolean).join("  ·  ");
            card.innerHTML = `<strong>${escapeHtml(name)}</strong><span>${escapeHtml(details || "Dati disponibili nell'archivio")}</span>`;
            elements.results.append(card);
        });
    }
    async function searchCitizens(event) {
        event.preventDefault();
        const searchValue = elements.input.value.trim();
        if (searchValue.length < 2) {
            showMessage("Inserisci almeno due caratteri.", true);
            return;
        }
        showMessage("Ricerca in corso...");
        try {
            const response = await fetch(`${await getBaseUrl()}/api/cittadini?query=${encodeURIComponent(searchValue)}`);
            if (!response.ok)
                throw new Error();
            renderResults(await response.json());
            showMessage("Ricerca completata.");
        }
        catch {
            elements.results.replaceChildren();
            showMessage("Impossibile completare la ricerca. Controlla il server.", true);
        }
    }
    elements.save.addEventListener("click", async () => { await storage.set({ apiBaseUrl: normaliseBaseUrl(elements.apiUrl.value) }); showMessage("Indirizzo API salvato."); checkServer(); });
    elements.refresh.addEventListener("click", checkServer);
    elements.search.addEventListener("submit", searchCitizens);
    elements.input.focus();
    storage.get("apiBaseUrl").then((saved) => { if (saved.apiBaseUrl)
        elements.apiUrl.value = saved.apiBaseUrl; checkServer(); });
})();
