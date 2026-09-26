# V0 — Périmètre

> Étape 1 sur 5 de la conception de la V0 (périmètre → parcours → écrans → données → technique).
> Statut : **validé**, complété par l'étape 4.

## Objectif de la V0

Tester en classe une seule question :

> **« Un enfant de maternelle peut-il, seul, photographier sa réalisation et la voir rangée au bon endroit ? »**

La V0 est un prototype : une seule tablette, une seule classe, quelques semaines d'essai. Elle n'a pas besoin d'être complète. Elle doit être **solide** sur le parcours enfant.

## Ce qui est DANS la V0

### Espace élève

| Fonction | Détail |
|---|---|
| Accueil | Grille des élèves : robot, photo ou prénom en grand. |
| Mode libre | Carte violette **« Photo libre »** parmi les ateliers : l'enfant photographie ce qu'il veut. Les photos vont dans **Souvenirs**, sans compétence. Activable / désactivable par l'enseignant, caméra réglable. |
| Choix de l'atelier | **Mode B uniquement** : l'enfant voit toujours les grandes cartes des ateliers actifs (photo modèle) et touche le sien, même s'il n'y a qu'un atelier actif. |
| Prise de photo | Écran caméra plein écran, très gros bouton déclencheur. |
| Vérification | La photo s'affiche, avec ✅ (garder) et ↻ (recommencer). Rien d'autre. |
| Retour positif | Une coche animée brève, puis retour à l'accueil. |
| Erreur de profil | Gros bouton retour sur chaque écran enfant. |
| Inactivité | Retour automatique à l'accueil après **1 minute** sans action. |

### Espace enseignant

| Fonction | Détail |
|---|---|
| Accès protégé | Appui long de 3 s dans le coin haut droit de l'accueil, puis code PIN à 4 chiffres. PIN choisi au premier lancement, avec un **code de secours** en cas d'oubli. Blocage temporaire après 5 codes faux. |
| Classe | Une seule classe. Ajouter, modifier ou supprimer un élève : prénom, et robot (par défaut), photo ou prénom en grand. |
| Ateliers | Créer ou modifier un atelier : titre, photo modèle (prise avec la tablette), une ou plusieurs compétences choisies dans le référentiel (plusieurs domaines possibles), caméra utilisée (arrière / avant). Activer ou désactiver. Supprimer (zone dangereuse). |
| Compétences | Référentiel officiel embarqué (493 compétences), **en lecture seule**. Choix par domaine → sous-domaine → niveau. |
| Photos par élève | Toutes les traces d'un élève, groupées par atelier. |
| Photos par atelier | Liste de la classe : élève ✓ avec sa photo, ou — sans trace. |
| Trier les photos | **Le tri est à la charge de l'enseignant** : il supprime les photos ratées ou prises par erreur (avec confirmation). Les enfants ne peuvent rien supprimer. |
| Zone dangereuse | Supprimer un élève (et ses photos) ou **tout effacer** : encadré rouge, conséquences chiffrées, saisie de confirmation. |
| Sauvegarder / restaurer | Bouton « Sauvegarder ma classe » : un seul fichier (photos + données) que l'enseignant copie lui-même (clé USB, ordinateur). Bouton « Restaurer » pour tout récupérer. Aucun envoi en ligne. |

### Espace enseignant : pratique et simplifiant

Principe : **le moins de gestes possible pour l'adulte**.

| Raccourci | Détail |
|---|---|
| Ajouter plusieurs élèves | Un prénom par ligne ; un robot différent est attribué automatiquement. |
| Valeurs par défaut | Robot attribué, caméra arrière, atelier inactif tant qu'on ne l'active pas. |
| Enregistrer et activer | Un seul bouton pour créer un atelier et le rendre visible aux enfants. |
| Dupliquer un atelier | Puzzle 6 → Puzzle 12 sans tout ressaisir (avancé depuis la V1 : le cahier des charges le juge important). |
| Tout désactiver | Un geste en fin de séance. |
| Avancement visible | Chaque carte d'atelier affiche « 5/12 élèves ». |
| Caméra modifiable | À tout moment, dans la fiche de l'atelier. |

### Principes techniques non négociables (dès la V0)

- Fonctionne **entièrement sans Internet**, sans compte.
- Photos stockées dans l'**espace privé de l'appli**, jamais dans la galerie.
- Aucune donnée ne sort de la tablette.
- Tablette Android **13 ou plus récente**, en **paysage**, comme appareil de référence.
- Distribution : **APK installée manuellement** (« sources inconnues ») pour le test. Play Store ou autre canal : voir `docs/a-faire.md`.
- L'enfant **ne peut pas sortir** de l'appli (épinglage ou mode kiosque, étape 5).
- **Accessibilité de base** : grandes zones tactiles, contrastes suffisants, jamais d'information par la seule couleur, boutons lisibles par TalkBack (voir `docs/a-faire.md`).

## Ce qui n'est PAS dans la V0

| Fonction | Prévue en |
|---|---|
| Mode A (atelier imposé, appareil photo direct) | V1 |
| Groupes d'élèves, plusieurs classes | V1 |
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

## Mode libre « Souvenirs » (analyse § 30 du cahier des charges)

| | |
|---|---|
| **Objectif** | Laisser l'enfant photographier ce qu'il veut (une construction spontanée, un moment de classe) sans le rattacher à un atelier. |
| **Utilisateur** | Élève (prise), enseignant (tri). |
| **Parcours** | Robot → carte violette « Photo libre » → photo → ✅. Toujours 4 touchers. |
| **Interface** | Une carte de plus, visuellement différente des ateliers (violette, appareil photo étoilé). |
| **Données** | Trace sans atelier ni compétence, rangée dans **Souvenirs**. |
| **Local / RGPD** | Rien ne sort. ⚠️ Risque accru de photos d'autres enfants : le tri par l'enseignant est d'autant plus important. |
| **Priorité** | V0, **activable / désactivable** dans Réglages. |
| **Risques** | Beaucoup de photos à trier ; enfants qui choisissent « libre » au lieu de leur atelier. Parade : l'enseignant désactive le mode libre pendant les séances d'ateliers. |
| **Recommandation** | Une carte, une catégorie, un interrupteur. Plus tard (V1) : reclasser un souvenir dans un atelier. |

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
| 10 | Code de secours pour le PIN oublié, infaisable pour un enfant jusqu'à 9 ans. |
| 11 | Zone dangereuse pour les suppressions importantes, dont « Tout effacer ». |
| 12 | Android 13 minimum. |
| 13 | Distribution par APK en sources inconnues pour le test. |
| 14 | Caméra (arrière / avant) choisie par atelier par l'enseignant, modifiable à tout moment ; non modifiable par les enfants. |
| 15 | Espace enseignant pensé pour simplifier la vie de l'adulte (raccourcis ci-dessus, dont la duplication d'atelier). |
| 16 | **Mode libre « Souvenirs »** dans la V0, activable par l'enseignant. |
