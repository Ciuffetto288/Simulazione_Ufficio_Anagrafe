import java.util.Locale;
import java.util.Objects;


public final class Comune {

    private final String nome;
    private final String provincia;

    
    private final String codiceCatastale;

    
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

    
    public boolean matchesName(String query) {
        return StringUtils.normalizeSearch(nome)
                .contains(StringUtils.normalizeSearch(query));
    }

    
    @Override
    public String toString() {
        return nome + " (" + provincia + ")";
    }

    
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
