import java.util.Locale;
import java.util.Objects;

/**
 * Un comune italiano: nome, sigla della provincia e codice catastale.
 * I campi vengono assegnati nel costruttore e non cambiano piu'.
 */
public final class Comune {

    private final String nome;
    private final String provincia;

    // il codice che finisce nelle posizioni 12-15 del codice fiscale
    private final String codiceCatastale;

    /**
     * Normalizza i valori in ingresso: provincia e codice catastale vengono
     * messi in maiuscolo, cosi' i confronti successivi non devono pensarci.
     *
     * @param nome nome del comune
     * @param provincia sigla della provincia
     * @param codiceCatastale codice catastale
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

    public String getNome() {
        return nome;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getCodiceCatastale() {
        return codiceCatastale;
    }

    /**
     * Confronto usato dalla ricerca: ignora maiuscole e accenti, quindi
     * "forli" trova "Forli'" e "SAN GIOVANNI" trova "San Giovanni".
     *
     * @param query testo cercato
     * @return true se il nome contiene la query
     */
    public boolean matchesName(String query) {
        return StringUtils.normalizeSearch(nome)
                .contains(StringUtils.normalizeSearch(query));
    }

    /**
     * Volutamente senza codice catastale: negli elenchi a schermo servono
     * solo nome e provincia.
     *
     * @return il comune nel formato {@code Nome (Provincia)}
     */
    @Override
    public String toString() {
        return nome + " (" + provincia + ")";
    }

    /**
     * Due comuni sono lo stesso comune se hanno lo stesso codice catastale
     * (il nome da solo non basta, ci sono omonimie tra province diverse).
     *
     * @param obj oggetto da confrontare
     * @return true se il codice catastale coincide
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

    @Override
    public int hashCode() {
        return Objects.hash(codiceCatastale);
    }
}
