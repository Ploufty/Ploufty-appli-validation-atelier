# V0 — Périmètre

> Étape 1 sur 5 de la conception de la V0 (périmètre → parcours → écrans → données → technique).
> Statut : **à valider**.

## Objectif de la V0

Tester en classe une seule question :

> **« Un enfant de maternelle peut-il, seul, photographier sa réalisation et la voir rangée au bon endroit ? »**

La V0 est un prototype : une seule tablette, une seule classe, quelques semaines d'essai. Elle n'a pas besoin d'être complète. Elle doit être **solide** sur le parcours enfant.

## Ce qui est DANS la V0

### Espace élève

| Fonction | Détail |
|---|---|
| Accueil | Grille des élèves : robot, photo ou prénom en grand. |
| Choix de l'atelier | **Mode B uniquement** : l'enfant voit toujours les grandes cartes des ateliers actifs (photo modèle) et touche le sien, même s'il n'y a qu'un atelier actif. |
| Prise de photo | Écran caméra plein écran, très gros bouton déclencheur. |
| Vérification | La photo s'affiche, avec ✅ (garder) et ↻ (recommencer). Rien d'autre. |
| Retour positif | Une coche animée brève, puis retour à l'accueil. |
| Erreur de profil | Gros bouton retour sur chaque écran enfant. |
| Inactivité | Retour automatique à l'accueil après **1 minute** sans action. |

### Espace enseignant

| Fonction | Détail |
|---|---|
| Accès protégé | Appui long de 3 s dans un coin de l'accueil, puis code PIN à 4 chiffres. Le PIN est choisi au premier lancement. |
| Classe | Une seule classe. Ajouter, modifier ou supprimer un élève : prénom, et robot (par défaut), photo ou prénom en grand. |
| Ateliers | Créer ou modifier un atelier : titre, photo modèle (prise avec la tablette), une ou plusieurs compétences choisies dans le référentiel. Activer ou désactiver. |
| Compétences | Référentiel officiel embarqué (493 compétences), **en lecture seule**. Choix par domaine → sous-domaine → niveau. |
| Photos par élève | Toutes les traces d'un élève, groupées par atelier. |
| Photos par atelier | Liste de la classe : élève ✓ avec sa photo, ou — sans trace. |
| Trier les photos | **Le tri est à la charge de l'enseignant** : il supprime les photos ratées ou prises par erreur (avec confirmation). Les enfants ne peuvent rien supprimer. |
| Sauvegarder / restaurer | Bouton « Sauvegarder ma classe » : un seul fichier (photos + données) que l'enseignant copie lui-même (clé USB, ordinateur). Bouton « Restaurer » pour tout récupérer. Aucun envoi en ligne. |

### Principes techniques non négociables (dès la V0)

- Fonctionne **entièrement sans Internet**, sans compte.
- Photos stockées dans l'**espace privé de l'appli**, jamais dans la galerie.
- Aucune donnée ne sort de la tablette.
- Tablette Android en **paysage** comme appareil de référence.

## Ce qui n'est PAS dans la V0

| Fonction | Prévue en |
|---|---|
| Mode A (atelier imposé, appareil photo direct) | V1 |
| Groupes d'élèves, plusieurs classes | V1 |
| Dupliquer un atelier | V1 |
| Réattribuer une photo à un autre élève ou atelier | V1 |
| Filtres (domaine, période…) | V1 |
| Archivage des ateliers et des élèves | V1 (en V0 : désactiver un atelier suffit) |
| Grilles de suivi, états (✓, ●…) | V2 |
| Fiche atelier imprimable, bilan, export PDF | V2 |
| Import CSV, partage de modèles, compétences personnalisées | V3 |
| Programme EVAR | Dès que le texte officiel est disponible (`docs/a-faire.md`) |

## Écarts avec la roadmap du cahier des charges

Cinq éléments arrivent plus tôt que prévu, parce que le test en classe en a besoin :

1. **Plusieurs ateliers actifs et choix de l'atelier par l'enfant — mode B** (prévus en V1). C'est l'hypothèse la plus risquée : il faut la tester dès la V0.
2. **Protection de l'espace enseignant** (prévue en V1). Sans elle, un enfant pourrait modifier ou supprimer des données pendant le test.
3. **Référentiel embarqué** (bibliothèque prévue en V1). Il existe déjà (`references/`), et il évite la saisie libre.
4. **Supprimer une photo** (prévu en V1). Le tri des photos revient à l'enseignant : une photo floue, prise par erreur ou montrant un autre enfant doit pouvoir disparaître. C'est aussi le minimum RGPD.
5. **Sauvegarde / restauration locale** (prévue en V1). Les photos des enfants ne se refont pas.

## Stockage et sauvegarde

- **Tout reste en local**, sur la tablette. **Aucune sauvegarde en ligne**, ni en V0 ni plus tard sans décision explicite.
- **Sauvegarde locale manuelle dès la V0** (prévue en V1 dans le cahier des charges) : les photos des enfants ne se refont pas, le test doit être protégé.
- Le fichier de sauvegarde contient des photos d'élèves : c'est une donnée personnelle, l'enseignant le range en lieu sûr.

## Critères de réussite du test en classe

La V0 est réussie si, sur quelques semaines d'essai :

1. la plupart des enfants de MS et GS enregistrent une trace **seuls** après une ou deux démonstrations ;
2. une trace se prend en **4 touchers maximum** (moi → mon atelier → photo → ✅) ;
3. les photos arrivent **au bon élève et au bon atelier**, sans reclassement ;
4. l'enseignant voit en un coup d'œil **qui est passé ou non** à un atelier ;
5. créer un atelier prend **moins de 2 minutes**.

À observer aussi : les enfants de PS y arrivent-ils ? Les erreurs de profil sont-elles fréquentes ?

## Décisions validées

| # | Décision |
|---|---|
| 1 | **Mode B uniquement** en V0 : l'enfant choisit toujours son atelier parmi les cartes, même s'il n'y en a qu'un. Parcours identique à chaque fois. Le mode A (appareil photo direct) est reporté en V1. |
| 2 | Accès enseignant : appui long 3 s + PIN 4 chiffres. |
| 3 | Référentiel de compétences embarqué, en lecture seule. |
| 4 | Élève représenté par un robot (par défaut, tous différents), une photo ou son prénom en grand. |
| 5 | Gros bouton retour ; retour automatique à l'accueil après 1 minute d'inactivité. |
| 6 | Une seule classe, sans groupes. |
| 7 | Le tri des photos (suppression) est à la charge de l'enseignant. |
| 8 | Données uniquement en local ; aucune sauvegarde en ligne. |
| 9 | Sauvegarde et restauration locales manuelles dès la V0. |
