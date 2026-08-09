import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Agenda extends JFrame {

    private List<Evenement> evenements;
    private static final String FICHIER_DONNEES = "agenda.dat";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale LOCALE_FR = Locale.FRENCH;

    private YearMonth moisAffiche;
    private LocalDate dateSelectionnee;

    private JPanel panneauCalendrier;
    private JLabel labelMoisAnnee;

    private DefaultTableModel modeleTableJour;
    private JTable tableJour;
    private JLabel labelJourSelectionne;

    private DefaultTableModel modeleTableTous;
    private JTable tableTous;

    private JTabbedPane onglets;

    public Agenda() {
        super("Agenda");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        evenements = chargerDonnees();
        moisAffiche = YearMonth.now();
        dateSelectionnee = LocalDate.now();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sauvegarderDonnees();
                System.exit(0);
            }
        });

        construireInterface();
        construireCalendrier();
        rafraichirJourSelectionne();
        rafraichirTableTous();
    }

    private void construireInterface() {
        setLayout(new BorderLayout(10, 10));

        onglets = new JTabbedPane();

        // ============ Onglet Calendrier ============
        JPanel panneauCalendrierGlobal = new JPanel(new BorderLayout(10, 10));
        panneauCalendrierGlobal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Panneau gauche : calendrier mensuel ----
        JPanel panneauGauche = new JPanel(new BorderLayout(5, 5));
        panneauGauche.setPreferredSize(new Dimension(480, 0));

        JPanel panneauNavigation = new JPanel(new BorderLayout());
        JButton boutonPrecedent = new JButton("◀");
        JButton boutonSuivant = new JButton("▶");
        JButton boutonAujourdHui = new JButton("Aujourd'hui");
        labelMoisAnnee = new JLabel("", SwingConstants.CENTER);
        labelMoisAnnee.setFont(new Font("SansSerif", Font.BOLD, 18));

        boutonPrecedent.addActionListener(e -> { moisAffiche = moisAffiche.minusMonths(1); construireCalendrier(); });
        boutonSuivant.addActionListener(e -> { moisAffiche = moisAffiche.plusMonths(1); construireCalendrier(); });
        boutonAujourdHui.addActionListener(e -> {
            moisAffiche = YearMonth.now();
            dateSelectionnee = LocalDate.now();
            construireCalendrier();
            rafraichirJourSelectionne();
        });

        JPanel panneauBoutonsNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panneauBoutonsNav.add(boutonPrecedent);
        panneauBoutonsNav.add(boutonAujourdHui);
        panneauBoutonsNav.add(boutonSuivant);

        panneauNavigation.add(labelMoisAnnee, BorderLayout.CENTER);
        panneauNavigation.add(panneauBoutonsNav, BorderLayout.SOUTH);
        panneauGauche.add(panneauNavigation, BorderLayout.NORTH);

        panneauCalendrier = new JPanel(new GridLayout(0, 7, 3, 3));
        panneauGauche.add(panneauCalendrier, BorderLayout.CENTER);

        panneauCalendrierGlobal.add(panneauGauche, BorderLayout.WEST);

        // ---- Panneau droit : événements du jour sélectionné ----
        JPanel panneauDroit = new JPanel(new BorderLayout(5, 5));
        panneauDroit.setBorder(BorderFactory.createTitledBorder("Événements du jour"));

        labelJourSelectionne = new JLabel(" ", SwingConstants.CENTER);
        labelJourSelectionne.setFont(new Font("SansSerif", Font.BOLD, 15));
        panneauDroit.add(labelJourSelectionne, BorderLayout.NORTH);

        modeleTableJour = new DefaultTableModel(new Object[]{"Heure", "Titre", "Catégorie", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableJour = new JTable(modeleTableJour);
        panneauDroit.add(new JScrollPane(tableJour), BorderLayout.CENTER);

        JPanel panneauBoutonsJour = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton boutonAjouter = new JButton("Ajouter un événement");
        JButton boutonModifier = new JButton("Modifier");
        JButton boutonSupprimer = new JButton("Supprimer");
        boutonAjouter.addActionListener(e -> ajouterEvenement());
        boutonModifier.addActionListener(e -> modifierEvenement());
        boutonSupprimer.addActionListener(e -> supprimerEvenement());
        panneauBoutonsJour.add(boutonAjouter);
        panneauBoutonsJour.add(boutonModifier);
        panneauBoutonsJour.add(boutonSupprimer);
        panneauDroit.add(panneauBoutonsJour, BorderLayout.SOUTH);

        panneauCalendrierGlobal.add(panneauDroit, BorderLayout.CENTER);

        onglets.addTab("Calendrier", panneauCalendrierGlobal);

        // ============ Onglet Tous les événements ============
        JPanel panneauTous = new JPanel(new BorderLayout(10, 10));
        panneauTous.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeleTableTous = new DefaultTableModel(new Object[]{"Date", "Heure", "Titre", "Catégorie", "Description", "État"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableTous = new JTable(modeleTableTous);
        panneauTous.add(new JScrollPane(tableTous), BorderLayout.CENTER);

        JButton boutonSupprimerTous = new JButton("Supprimer l'événement sélectionné");
        boutonSupprimerTous.addActionListener(e -> supprimerDepuisListeGlobale());
        panneauTous.add(boutonSupprimerTous, BorderLayout.SOUTH);

        onglets.addTab("Tous les événements", panneauTous);

        add(onglets, BorderLayout.CENTER);

        // ---------- Menu ----------
        JMenuBar barreMenu = new JMenuBar();
        JMenu menuFichier = new JMenu("Fichier");
        JMenuItem itemSauvegarder = new JMenuItem("Sauvegarder");
        itemSauvegarder.addActionListener(e -> {
            sauvegarderDonnees();
            JOptionPane.showMessageDialog(this, "Données sauvegardées avec succès.");
        });
        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> {
            sauvegarderDonnees();
            System.exit(0);
        });
        menuFichier.add(itemSauvegarder);
        menuFichier.addSeparator();
        menuFichier.add(itemQuitter);
        barreMenu.add(menuFichier);
        setJMenuBar(barreMenu);
    }

    // ---------- Construction du calendrier mensuel ----------

    private void construireCalendrier() {
        panneauCalendrier.removeAll();

        String nomMois = moisAffiche.getMonth().getDisplayName(TextStyle.FULL, LOCALE_FR);
        nomMois = nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1);
        labelMoisAnnee.setText(nomMois + " " + moisAffiche.getYear());

        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String jour : joursSemaine) {
            JLabel label = new JLabel(jour, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            panneauCalendrier.add(label);
        }

        LocalDate premierJourMois = moisAffiche.atDay(1);
        int decalage = premierJourMois.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();

        for (int i = 0; i < decalage; i++) {
            panneauCalendrier.add(new JLabel(""));
        }

        int nbJours = moisAffiche.lengthOfMonth();
        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate date = moisAffiche.atDay(jour);
            panneauCalendrier.add(creerBoutonJour(date));
        }

        panneauCalendrier.revalidate();
        panneauCalendrier.repaint();
    }

    private JButton creerBoutonJour(LocalDate date) {
        int nbEvenements = compterEvenements(date);
        String texte = "<html><div style='text-align:center;'>" + date.getDayOfMonth()
                + (nbEvenements > 0 ? "<br><span style='font-size:9px;color:#0066cc;'>● " + nbEvenements + "</span>" : "")
                + "</div></html>";

        JButton bouton = new JButton(texte);
        bouton.setMargin(new Insets(2, 2, 2, 2));

        if (date.isEqual(LocalDate.now())) {
            bouton.setBackground(new Color(255, 244, 200));
            bouton.setOpaque(true);
        }
        if (date.isEqual(dateSelectionnee)) {
            Border bordure = BorderFactory.createLineBorder(new Color(0, 102, 204), 2);
            bouton.setBorder(bordure);
        }

        bouton.addActionListener(e -> {
            dateSelectionnee = date;
            construireCalendrier();
            rafraichirJourSelectionne();
        });

        return bouton;
    }

    private int compterEvenements(LocalDate date) {
        int compte = 0;
        for (Evenement ev : evenements) {
            if (ev.getDate().isEqual(date)) compte++;
        }
        return compte;
    }

    // ---------- Gestion des événements ----------

    private void ajouterEvenement() {
        Evenement nouveau = ouvrirFormulaireEvenement(null);
        if (nouveau != null) {
            evenements.add(nouveau);
            construireCalendrier();
            rafraichirJourSelectionne();
            rafraichirTableTous();
            sauvegarderDonnees();
        }
    }

    private void modifierEvenement() {
        int ligne = tableJour.getSelectedRow();
        List<Evenement> duJour = evenementsDuJour(dateSelectionnee);
        if (ligne < 0 || ligne >= duJour.size()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un événement.", "Aucune sélection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Evenement existant = duJour.get(ligne);
        Evenement modifie = ouvrirFormulaireEvenement(existant);
        if (modifie != null) {
            existant.setTitre(modifie.getTitre());
            existant.setDescription(modifie.getDescription());
            existant.setDate(modifie.getDate());
            existant.setHeure(modifie.getHeure());
            existant.setCategorie(modifie.getCategorie());
            construireCalendrier();
            rafraichirJourSelectionne();
            rafraichirTableTous();
            sauvegarderDonnees();
        }
    }

    private void supprimerEvenement() {
        int ligne = tableJour.getSelectedRow();
        List<Evenement> duJour = evenementsDuJour(dateSelectionnee);
        if (ligne < 0 || ligne >= duJour.size()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un événement.", "Aucune sélection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Evenement aSupprimer = duJour.get(ligne);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Supprimer l'événement \"" + aSupprimer.getTitre() + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            evenements.remove(aSupprimer);
            construireCalendrier();
            rafraichirJourSelectionne();
            rafraichirTableTous();
            sauvegarderDonnees();
        }
    }

    private void supprimerDepuisListeGlobale() {
        int ligne = tableTous.getSelectedRow();
        List<Evenement> tries = new ArrayList<>(evenements);
        java.util.Collections.sort(tries);
        if (ligne < 0 || ligne >= tries.size()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un événement.", "Aucune sélection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Evenement aSupprimer = tries.get(ligne);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Supprimer l'événement \"" + aSupprimer.getTitre() + "\" du " + aSupprimer.getDate().format(FORMAT_DATE) + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            evenements.remove(aSupprimer);
            construireCalendrier();
            rafraichirJourSelectionne();
            rafraichirTableTous();
            sauvegarderDonnees();
        }
    }

    private Evenement ouvrirFormulaireEvenement(Evenement existant) {
        JTextField champTitre = new JTextField(existant != null ? existant.getTitre() : "");
        JTextField champDate = new JTextField(existant != null ? existant.getDate().format(FORMAT_DATE) : dateSelectionnee.format(FORMAT_DATE));
        JTextField champHeure = new JTextField(existant != null && existant.getHeure() != null ? existant.getHeure().format(FORMAT_HEURE) : "09:00");
        JComboBox<String> comboCategorie = new JComboBox<>(new String[]{"Personnel", "Travail", "Études", "Santé", "Autre"});
        if (existant != null) comboCategorie.setSelectedItem(existant.getCategorie());
        JTextArea champDescription = new JTextArea(existant != null ? existant.getDescription() : "", 4, 20);
        champDescription.setLineWrap(true);

        Object[] message = {
                "Titre :", champTitre,
                "Date (jj/mm/aaaa) :", champDate,
                "Heure (hh:mm) :", champHeure,
                "Catégorie :", comboCategorie,
                "Description :", new JScrollPane(champDescription)
        };

        String titreFenetre = existant != null ? "Modifier l'événement" : "Ajouter un événement";
        int resultat = JOptionPane.showConfirmDialog(this, message, titreFenetre, JOptionPane.OK_CANCEL_OPTION);

        if (resultat != JOptionPane.OK_OPTION) return null;

        String titre = champTitre.getText().trim();
        if (titre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le titre est obligatoire.", "Erreur", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(champDate.getText().trim(), FORMAT_DATE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format de date invalide. Utilisez jj/mm/aaaa.", "Erreur", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        LocalTime heure = null;
        String texteHeure = champHeure.getText().trim();
        if (!texteHeure.isEmpty()) {
            try {
                heure = LocalTime.parse(texteHeure, FORMAT_HEURE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Format d'heure invalide. Utilisez hh:mm.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        return new Evenement(titre, champDescription.getText().trim(), date, heure, (String) comboCategorie.getSelectedItem());
    }

    // ---------- Rafraîchissement de l'affichage ----------

    private List<Evenement> evenementsDuJour(LocalDate date) {
        List<Evenement> resultat = new ArrayList<>();
        for (Evenement ev : evenements) {
            if (ev.getDate().isEqual(date)) resultat.add(ev);
        }
        java.util.Collections.sort(resultat);
        return resultat;
    }

    private void rafraichirJourSelectionne() {
        String nomJour = dateSelectionnee.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE_FR);
        nomJour = nomJour.substring(0, 1).toUpperCase() + nomJour.substring(1);
        labelJourSelectionne.setText(nomJour + " " + dateSelectionnee.format(FORMAT_DATE));

        modeleTableJour.setRowCount(0);
        for (Evenement ev : evenementsDuJour(dateSelectionnee)) {
            String heure = ev.getHeure() != null ? ev.getHeure().format(FORMAT_HEURE) : "-";
            modeleTableJour.addRow(new Object[]{heure, ev.getTitre(), ev.getCategorie(), ev.getDescription()});
        }
    }

    private void rafraichirTableTous() {
        modeleTableTous.setRowCount(0);
        List<Evenement> tries = new ArrayList<>(evenements);
        java.util.Collections.sort(tries);
        for (Evenement ev : tries) {
            String heure = ev.getHeure() != null ? ev.getHeure().format(FORMAT_HEURE) : "-";
            String etat = ev.estPasse() ? "Passé" : "À venir";
            modeleTableTous.addRow(new Object[]{
                    ev.getDate().format(FORMAT_DATE), heure, ev.getTitre(), ev.getCategorie(), ev.getDescription(), etat
            });
        }
    }

    // ---------- Persistance des données ----------

    @SuppressWarnings("unchecked")
    private List<Evenement> chargerDonnees() {
        File fichier = new File(FICHIER_DONNEES);
        if (!fichier.exists()) return new ArrayList<>();

        try (ObjectInputStream flux = new ObjectInputStream(new FileInputStream(fichier))) {
            return (List<Evenement>) flux.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les données existantes :\n" + ex.getMessage(),
                    "Erreur de chargement", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    private void sauvegarderDonnees() {
        try (ObjectOutputStream flux = new ObjectOutputStream(new FileOutputStream(FICHIER_DONNEES))) {
            flux.writeObject(evenements);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de sauvegarder les données :\n" + ex.getMessage(),
                    "Erreur de sauvegarde", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            Agenda app = new Agenda();
            app.setVisible(true);
        });
    }
}
