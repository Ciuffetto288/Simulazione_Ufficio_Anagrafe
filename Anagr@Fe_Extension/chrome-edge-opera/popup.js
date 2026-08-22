"use strict";
(() => {
    "use strict";
    const browserApi = globalThis.browser
        ?? globalThis.chrome;
    const query = (selector) => document.querySelector(selector);
    const elements = {
        dot: query("#connection-dot"),
        message: query("#search-message"),
        results: query("#results"), search: query("#search-form"),
        input: query("#search-input")
    };
    const apiBaseUrl = "http://localhost:8080";
    let nativeReady = Promise.resolve();
    try {
        const nativePort = browserApi.runtime?.connectNative?.("anagrafe.server");
        nativeReady = new Promise((resolve) => {
            let settled = false;
            const finish = () => {
                if (!settled) {
                    settled = true;
                    resolve();
                }
            };
            nativePort?.onMessage?.addListener(finish);
            nativePort?.onDisconnect?.addListener(finish);
            nativePort?.postMessage({ command: "start" });
            window.setTimeout(finish, 1500);
        });
    }
    catch {
        // L'API HTTP puo essere gia attiva anche senza host Native Messaging.
    }
    const normaliseBaseUrl = (value) => value.trim().replace(/\/+$/, "");
    async function getBaseUrl() { return apiBaseUrl; }
    function setConnection(online) { elements.dot?.classList.toggle("online", online); elements.dot?.classList.toggle("offline", !online); }
    async function checkServer() {
        setConnection(false);
        await nativeReady;
        try {
            const response = await fetch(`${await getBaseUrl()}/api`);
            if (!response.ok)
                throw new Error();
            setConnection(true);
        }
        catch {
            setConnection(false);
        }
    }
    function showMessage(text, isError = false) {
        const message = elements.message;
        if (!message)
            return;
        message.textContent = text;
        message.classList.toggle("error", isError);
    }
    function escapeHtml(value) { const div = document.createElement("div"); div.textContent = value; return div.innerHTML; }
    async function copyCitizenData(value, button) {
        const fallbackCopy = () => {
            const textarea = document.createElement("textarea");
            textarea.value = value;
            textarea.setAttribute("readonly", "");
            textarea.style.position = "fixed";
            textarea.style.opacity = "0";
            document.body.append(textarea);
            textarea.select();
            document.execCommand("copy");
            textarea.remove();
        };
        try {
            if (navigator.clipboard?.writeText) {
                await navigator.clipboard.writeText(value);
            }
            else {
                fallbackCopy();
            }
            if (button) {
                const previousText = button.textContent;
                button.textContent = "✓";
                button.classList.add("copied");
                button.disabled = true;
                window.setTimeout(() => {
                    button.textContent = previousText;
                    button.classList.remove("copied");
                    button.disabled = false;
                }, 1200);
            }
            showMessage("Dati copiati negli appunti.");
        }
        catch {
            if (button) {
                button.textContent = "Errore";
                window.setTimeout(() => {
                    button.textContent = "Copia";
                }, 1200);
            }
            showMessage("Copia non disponibile in questo contesto.", true);
        }
    }
    function renderResults(payload) {
        const results = elements.results;
        if (!results)
            return;
        const records = Array.isArray(payload) ? payload : payload.cittadini || payload.data || [];
        results.replaceChildren();
        if (!records.length) {
            results.innerHTML = '<div class="empty">Nessun cittadino trovato.</div>';
            return;
        }
        records.slice(0, 8).forEach((record) => {
            const card = document.createElement("article");
            card.className = "citizen-card result-card";
            const name = record.nome && record.cognome ? `${record.nome} ${record.cognome}` : record.nome || record.cognome || "Cittadino";
            const comune = record.comune && record.provincia ? `${record.comune} (${record.provincia})` : record.comune || "N/D";
            const codiceFiscale = record.codiceFiscale || "CF non disponibile";
            const nascita = record.dataNascita || "Data non disponibile";
            const copyText = [name, `CF: ${codiceFiscale}`, `Comune: ${comune}`, `Nato/a il: ${nascita}`].join("\n");
            card.innerHTML = `
        <div class="citizen-info">
          <p class="citizen-name">${escapeHtml(name)}</p>
          <p class="citizen-meta">${escapeHtml(comune)} · ${escapeHtml(nascita)}</p>
        </div>
        <div class="cf-row">
          <div class="cf-chip">${escapeHtml(codiceFiscale)}</div>
          <button class="copy-button" type="button">Copia</button>
        </div>
      `;
            const copyButton = card.querySelector(".copy-button");
            copyButton?.addEventListener("click", () => void copyCitizenData(copyText, copyButton));
            results.append(card);
        });
    }
    async function searchCitizens(event) {
        const input = elements.input;
        if (!input)
            return;
        event.preventDefault();
        const searchValue = input.value.trim();
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
            const results = elements.results;
            if (results)
                results.replaceChildren();
            showMessage("Impossibile completare la ricerca. Controlla il server.", true);
        }
    }
    const searchForm = elements.search;
    const searchInput = elements.input;
    if (searchForm && searchInput) {
        searchForm.addEventListener("submit", searchCitizens);
        searchInput.focus();
    }
    checkServer();
})();
