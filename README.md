# Agenda (Java)

Application d'agenda / calendrier avec interface graphique Swing.

## Fonctionnalités
- Calendrier mensuel interactif avec navigation (mois précédent / suivant / aujourd'hui)
- Indicateur visuel du nombre d'événements sur chaque jour
- Ajouter / modifier / supprimer des événements (titre, date, heure, catégorie, description)
- Catégories d'événements : Personnel, Travail, Études, Santé, Autre
- Vue "Tous les événements" listant tout l'agenda, triée par date, avec statut (À venir / Passé)
- Sauvegarde automatique dans un fichier `agenda.dat`
- Chargement automatique des données au démarrage

## Compiler et exécuter

```bash
javac -d bin src/*.java
java -cp bin Agenda
```

## Structure du projet
```
Agenda/
├── src/
│   ├── Evenement.java   (modèle représentant un événement)
│   └── Agenda.java      (interface graphique principale)
├── bin/                  (généré après compilation)
└── README.md
```

## Utilisation
1. Cliquez sur un jour dans le calendrier pour voir ses événements à droite.
2. Utilisez "Ajouter un événement" pour créer un nouvel événement à cette date.
3. Naviguez entre les mois avec les flèches ◀ / ▶, ou revenez au jour actuel avec "Aujourd'hui".
4. Consultez l'onglet "Tous les événements" pour une vue d'ensemble complète.

## Notes techniques
- Les données sont sauvegardées automatiquement après chaque ajout/modification/suppression,
  et aussi à la fermeture de l'application.
- Le fichier `agenda.dat` est créé au premier lancement, dans le dossier où vous
  exécutez la commande `java`.
- Le format de date attendu dans les formulaires est `jj/mm/aaaa`, et l'heure `hh:mm`.

## Idées d'améliorations possibles
- Rappels/notifications avant un événement
- Répétition d'événements (quotidien, hebdomadaire...)
- Export au format iCal (.ics)
