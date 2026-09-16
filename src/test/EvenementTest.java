import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;




public class EvenementTest {

    @Test
    void unEvenementDansLePasseEstDetectePasse() {
        Evenement e = new Evenement(
                "Réunion",
                "desc",
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                "Travail"
        );
        assertTrue(e.estPasse());
    }

    @Test
    void unEvenementDansLeFuturNestPasPasse() {
        Evenement e = new Evenement(
                "Réunion",
                "desc",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Travail"
        );
        assertFalse(e.estPasse());
    }

    @Test
    void unEvenementAujourdhuiAvecHeurePasseeEstPasse() {
        Evenement e = new Evenement(
                "Réunion",
                "desc",
                LocalDate.now(),
                LocalTime.now().minusMinutes(1),
                "Travail"
        );
        assertTrue(e.estPasse());
    }

    @Test
    void unEvenementAujourdhuiAvecHeureFutureNestPasPasse() {
        Evenement e = new Evenement(
                "Réunion",
                "desc",
                LocalDate.now(),
                LocalTime.now().plusHours(2),
                "Travail"
        );
        assertFalse(e.estPasse());
    }

    @Test
    void compareToTrieParDateCroissante() {
        Evenement premier = new Evenement("A", "", LocalDate.now(), LocalTime.of(9, 0), "Autre");
        Evenement second = new Evenement("B", "", LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Autre");
        assertTrue(premier.compareTo(second) < 0);
        assertTrue(second.compareTo(premier) > 0);
    }

    @Test
    void compareToTrieParHeureQuandMemeDate() {
        LocalDate meme = LocalDate.now();
        Evenement tot = new Evenement("A", "", meme, LocalTime.of(8, 0), "Autre");
        Evenement tard = new Evenement("B", "", meme, LocalTime.of(18, 0), "Autre");
        assertTrue(tot.compareTo(tard) < 0);
    }

    @Test
    void gettersEtSettersFonctionnentCorrectement() {
        Evenement e = new Evenement("Titre", "Description", LocalDate.now(), LocalTime.of(12, 0), "Santé");
        e.setTitre("Nouveau titre");
        e.setDescription("Nouvelle description");
        e.setCategorie("Personnel");

        assertEquals("Nouveau titre", e.getTitre());
        assertEquals("Nouvelle description", e.getDescription());
        assertEquals("Personnel", e.getCategorie());
    }

    @Test
    void toStringContientTitreEtDate() {
        LocalDate date = LocalDate.of(2026, 12, 25);
        Evenement e = new Evenement("Noël", "", date, null, "Personnel");
        assertEquals("Noël - 2026-12-25", e.toString());
    }
}