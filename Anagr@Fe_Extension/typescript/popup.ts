interface Cittadino {
  nome?: string;
  cognome?: string;
  codiceFiscale?: string;
  comune?: string;
  provincia?: string;
  dataNascita?: string;
}

interface ApiPayload {
  cittadini?: Cittadino[];
  data?: Cittadino[];
}

interface BrowserStorage {
  get(key: string): Promise<Record<string, string>>;
  set(value: Record<string, string>): Promise<void>;
}

interface BrowserApi {
  storage: { local: BrowserStorage };
  runtime?: {
    connectNative?: (hostName: string) => {
      postMessage(message: unknown): void;
      onMessage?: { addListener(listener: (message: unknown) => void): void };
      onDisconnect?: { addListener(listener: () => void): void };
    };
  };
}

(() => {
  "use strict";

  const browserApi = (globalThis as typeof globalThis & { browser?: BrowserApi; chrome?: BrowserApi }).browser
    ?? (globalThis as typeof globalThis & { chrome: BrowserApi }).chrome;
  const query = <T extends Element>(selector: string) => document.querySelector<T>(selector);
  const elements = {
    dot: query<HTMLSpanElement>("#connection-dot"),
    message: query<HTMLParagraphElement>("#search-message"),
    results: query<HTMLDivElement>("#results"), search: query<HTMLFormElement>("#search-form"),
    input: query<HTMLInputElement>("#search-input")
  };
  const apiBaseUrl = "http://localhost:8080";
  let nativeReady: Promise<void> = Promise.resolve();
  try {
    const nativePort = browserApi.runtime?.connectNative?.("anagrafe.server");
    nativeReady = new Promise<void>((resolve) => {
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
  } catch {
    // L'API HTTP puo essere gia attiva anche senza host Native Messaging.
  }
  const normaliseBaseUrl = (value: string) => value.trim().replace(/\/+$/, "");
  async function getBaseUrl(): Promise<string> { return apiBaseUrl; }
  function setConnection(online: boolean): void { elements.dot?.classList.toggle("online", online); elements.dot?.classList.toggle("offline", !online); }
  async function checkServer(): Promise<void> {
    setConnection(false);
    await nativeReady;
    try { const response = await fetch(`${await getBaseUrl()}/api`); if (!response.ok) throw new Error(); setConnection(true); }
    catch { setConnection(false); }
  }
  function showMessage(text: string, isError = false): void {
    const message = elements.message;
    if (!message) return;
    message.textContent = text;
    message.classList.toggle("error", isError);
  }
  function escapeHtml(value: string): string { const div = document.createElement("div"); div.textContent = value; return div.innerHTML; }
  function renderResults(payload: Cittadino[] | ApiPayload): void {
    const results = elements.results;
    if (!results) return;
    const records = Array.isArray(payload) ? payload : payload.cittadini || payload.data || [];
    results.replaceChildren();
    if (!records.length) { results.innerHTML = '<div class="empty">Nessun cittadino trovato.</div>'; return; }
    records.slice(0, 8).forEach((record) => {
      const card = document.createElement("article"); card.className = "result";
      const name = record.nome && record.cognome ? `${record.nome} ${record.cognome}` : record.nome || record.cognome || "Cittadino";
      const comune = record.comune && record.provincia ? `${record.comune} (${record.provincia})` : record.comune;
      const details = [record.codiceFiscale, comune, record.dataNascita].filter(Boolean).join("  ·  ");
      card.innerHTML = `<strong>${escapeHtml(name)}</strong><span>${escapeHtml(details || "Dati disponibili nell'archivio")}</span>`;
      results.append(card);
    });
  }
  async function searchCitizens(event: SubmitEvent): Promise<void> {
    const input = elements.input;
    if (!input) return;
    event.preventDefault(); const searchValue = input.value.trim();
    if (searchValue.length < 2) { showMessage("Inserisci almeno due caratteri.", true); return; }
    showMessage("Ricerca in corso...");
    try { const response = await fetch(`${await getBaseUrl()}/api/cittadini?query=${encodeURIComponent(searchValue)}`); if (!response.ok) throw new Error(); renderResults(await response.json() as Cittadino[] | ApiPayload); showMessage("Ricerca completata."); }
    catch {
      const results = elements.results;
      if (results) results.replaceChildren();
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