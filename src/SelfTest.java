import java.time.LocalDate;
import java.util.Set;


public final class SelfTest {

    private SelfTest() {
    }

    
    public static void run() {
        CodiceFiscaleService codiceService = new CodiceFiscaleService();
        ComuneService comuneService = new ComuneService();

        String marioRossi = codiceService.genera(
                "Mario",
                "Rossi",
                LocalDate.of(1980, 1, 1),
                'M',
                "H501",
                Set.of()
        );
        assertEquals("RSSMRA80A01H501U", marioRossi, "Mario Rossi Roma");

        Cittadino base = new Cittadino(
                "Mario",
                "Rossi",
                LocalDate.of(1980, 1, 1),
                'M',
                "Roma",
                "RM",
                "H501",
                marioRossi
        );
        
        String omocode = codiceService.genera(base, Set.of(marioRossi));
        assertTrue(!omocode.equals(marioRossi), "Omocodia generata in caso di collisione");

        assertTrue(
                codiceService.verifica(marioRossi, comuneService).isValid(),
                "Validazione codice fiscale base"
        );
        assertTrue(
                codiceService.verifica(omocode, comuneService).isValid(),
                "Validazione codice fiscale omocodico"
        );
        assertTrue(
                comuneService.findByNameAndProvince("Roma (RM)", "").isPresent(),
                "Ricerca comune con provincia tra parentesi"
        );
        assertTrue(
                comuneService.findByNameAndProvince("Milano MI", "").isPresent(),
                "Ricerca comune con provincia finale"
        );
        assertTrue(
                comuneService.findByNameAndProvince("Rovigo (RO)", "").isPresent(),
                "Ricerca Rovigo con provincia tra parentesi"
        );
        assertEquals(
                "Rovigo",
                comuneService.searchByName("Rovigo (RO)").get(0).getNome(),
                "Ordinamento suggerimenti Rovigo prima di Costa di Rovigo"
        );
        assertTrue(
                comuneService.findByNameAndProvince("Monselice PD", "").isPresent(),
                "Ricerca Monselice con provincia finale"
        );

        System.out.println("Self-test completato: generazione, omocodia e validazione OK.");
    }

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(label + " - atteso " + expected + ", ottenuto " + actual);
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new IllegalStateException(label + " - condizione non soddisfatta");
        }
    }
}
