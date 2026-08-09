import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Evenement implements Serializable, Comparable<Evenement> {
    private static final long serialVersionUID = 1L;

    private String titre;
    private String description;
    private LocalDate date;
    private LocalTime heure;
    private String categorie;

    public Evenement(String titre, String description, LocalDate date, LocalTime heure, String categorie) {
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.heure = heure;
        this.categorie = categorie;
    }

    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public String getCategorie() { return categorie; }

    public void setTitre(String titre) { this.titre = titre; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeure(LocalTime heure) { this.heure = heure; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public boolean estPasse() {
        LocalDate aujourdHui = LocalDate.now();
        if (date.isBefore(aujourdHui)) return true;
        if (date.isEqual(aujourdHui) && heure != null) {
            return heure.isBefore(LocalTime.now());
        }
        return false;
    }

    @Override
    public int compareTo(Evenement autre) {
        int comparaisonDate = this.date.compareTo(autre.date);
        if (comparaisonDate != 0) return comparaisonDate;
        if (this.heure == null && autre.heure == null) return 0;
        if (this.heure == null) return -1;
        if (autre.heure == null) return 1;
        return this.heure.compareTo(autre.heure);
    }

    @Override
    public String toString() {
        return titre + " - " + date;
    }
}
