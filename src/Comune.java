import java.util.Locale;
import java.util.Objects;

/**
 * Rappresenta un comune italiano identificato tramite:
 * <ul>
 *     <li>Nome del comune</li>
 *     <li>Sigla della provincia</li>
 *     <li>Codice catastale ministeriale</li>
 * </ul>
 * 
 * L'oggetto è immutabile e viene utilizzato per la gestione
 * dei dati territoriali associati al codice fiscale.
 */
public final class Comune {

    /**
     * Nome ufficiale del comune.
     */
    private final String nome;

    /**
     * Sigla automobilistica della provincia.
     */
    private final String provincia;

    /**
     * Codice catastale ufficiale del comune.
     */
    private final String codiceCatastale;

    /**
     * Costruisce un nuovo oggetto Comune normalizzando automaticamente i valori testuali.
     * 
     * @param nome Nome del comune
     * @param provincia Sigla della provincia
     * @param codiceCatastale Codice catastale ministeriale
     */
    public Comune(String nome, String provincia, String codiceCatastale) {
        this.nome = nome == null ? "" : nome.trim();
        this.provincia = provincia == null
                ? ""
                : provincia.trim().toUpperCase(Locale.ITALIAN);

        this.codiceCatastale = codiceCatastale == null
                ? ""
                : codiceCatastale.trim().toUpperCase(Locale.ITALIAN);
    }

    /**
     * Restituisce il nome del comune.
     * 
     * @return Nome del comune
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce la sigla della provincia.
     * 
     * @return Provincia del comune
     */
    public String getProvincia() {
        return provincia;
    }

    /**
     * Restituisce il codice catastale del comune.
     * 
     * @return Codice catastale ministeriale
     */
    public String getCodiceCatastale() {
        return codiceCatastale;
    }

    /**
     * Verifica se il nome del comune corrisponde alla query di ricerca specificata.
     * 
     * Il confronto viene effettuato in forma normalizzata ignorando:
     * <ul>
     *     <li>Maiuscole e minuscole</li>
     *     <li>Accenti</li>
     *     <li>Caratteri speciali</li>
     * </ul>
     * 
     * @param query Testo da cercare
     * @return true se il nome contiene la query normalizzata
     */
    public boolean matchesName(String query) {
        return StringUtils.normalizeSearch(nome)
                .contains(StringUtils.normalizeSearch(query));
    }

    /**
     * Restituisce una rappresentazione leggibile del comune.
     *
     * <p>La stringa non mostra il codice catastale, cosi gli elenchi e i
     * suggerimenti restano comprensibili anche a chi conosce solo comune e
     * provincia.</p>
     *
     * @return stringa descrittiva nel formato {@code Nome (Provincia)}
     */
    @Override
    public String toString() {
        return nome + " (" + provincia + ")";
    }

    /**
     * Confronta due comuni utilizzando il codice catastale come identificatore univoco.
     * 
     * @param obj Oggetto da confrontare
     * @return true se i comuni possiedono lo stesso codice catastale
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Comune other)) {
            return false;
        }

        return codiceCatastale.equals(other.codiceCatastale);
    }

    /**
     * Restituisce l'hash associato al comune basato sul codice catastale.
     * 
     * @return Valore hash del comune
     */
    @Override
    public int hashCode() {
        return Objects.hash(codiceCatastale);
    }
}
