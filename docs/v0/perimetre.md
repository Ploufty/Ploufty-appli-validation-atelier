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
| Accueil | Grille des élèves : photo ou avatar, prénom en petit dessous. |
| Choix de l'atelier | **Automatique** : un seul atelier actif → appareil photo directement ; plusieurs → grandes cartes avec la photo modèle. |
| Prise de photo | Écran caméra plein écran, très gros bouton déclencheur. |
| Vérification | La photo s'affiche, avec ✅ (garder) et ↻ (recommencer). Rien d'autre. |
| Retour positif | Une coche animée brève, puis retour à l'accueil. |
| Erreur de profil | Gros bouton retour sur chaque écran enfant. |
| Inactivité | Retour automatique à l'accueil après **1 minute** sans action. |

### Espace enseignant

| Fonction | Détail |
|---|---|
| Accès protégé | Appui long de 3 s dans un coin de l'accueil, puis code PIN à 4 chiffres. Le PIN est choisi au premier lancement. |
| Classe | Une seule classe. Ajouter, modifier ou supprimer un élève : prénom, photo **ou** avatar (avatar par défaut). |
| Ateliers | Créer ou modifier un atelier : titre, photo modèle (prise avec la tablette), une ou plusieurs compétences choisies dans le référentiel. Activer ou désactiver. |
| Compétences | Référentiel officiel embarqué (493 compétences), **en lecture seule**. Choix par domaine → sous-domaine → niveau. |
| Photos par élève | Toutes les traces d'un élève, groupées par atelier. |
| Photos par atelier | Liste de la classe : élève ✓ avec sa photo, ou — sans trace. |
| Supprimer une photo | Avec confirmation. |

### Principes techniques non négociables (dès la V0)

- Fonctionne **entièrement sans Internet**, sans compte.
- Photos stockées dans l'**espace privé de l'appli**, jamais dans la galerie.
- Aucune donnée ne sort de la tablette.
- Tablette Android en **paysage** comme appareil de référence.

## Ce qui n'est PAS dans la V0

| Fonction | Prévue en |
|---|---|
| Groupes d'élèves, plusieurs classes | V1 |
| Dupliquer un atelier | V1 |
| Réattribuer une photo à un autre élève ou atelier | V1 |
| Filtres (domaine, période…) | V1 |
| Sauvegarde / restauration | V1 |
| Archivage des ateliers et des élèves | V1 (en V0 : désactiver un atelier suffit) |
| Grilles de suivi, états (✓, ●…) | V2 |
| Fiche atelier imprimable, bilan, export PDF | V2 |
| Import CSV, partage de modèles, compétences personnalisées | V3 |
| Programme EVAR | Dès que le texte officiel est disponible (`docs/a-faire.md`) |

## Écarts avec la roadmap du cahier des charges

Quatre éléments arrivent plus tôt que prévu, parce que le test en classe en a besoin :

1. **Plusieurs ateliers actifs** (prévu en V1). Sans ça, on ne peut pas tester le choix de l'atelier par l'enfant, qui est l'hypothèse la plus risquée.
2. **Protection de l'espace enseignant** (prévue en V1). Sans elle, un enfant pourrait modifier ou supprimer des données pendant le test.
3. **Référentiel embarqué** (bibliothèque prévue en V1). Il existe déjà (`references/`), et il évite la saisie libre.
4. **Supprimer une photo** (prévu en V1). Une photo floue, prise par erreur ou montrant un autre enfant doit pouvoir disparaître : c'est le minimum RGPD.

## Risque accepté

**Pas de sauvegarde en V0.** Si la tablette casse pendant le test, les photos sont perdues. C'est acceptable pour un prototype d'essai de quelques semaines, mais il faut le dire clairement aux personnes qui testent.

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
| 1 | Le mode (atelier imposé ou choix) se déduit du nombre d'ateliers actifs. Pas de réglage. |
| 2 | Accès enseignant : appui long 3 s + PIN 4 chiffres. |
| 3 | Référentiel de compétences embarqué, en lecture seule. |
| 4 | Élève représenté par une photo ou un avatar ; avatar par défaut. |
| 5 | Gros bouton retour ; retour automatique à l'accueil après 1 minute d'inactivité. |
| 6 | Une seule classe, sans groupes. |
