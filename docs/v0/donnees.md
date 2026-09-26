# V0 — Données

> Étape 4 sur 5 (périmètre → parcours → écrans → **données** → technique).
> Statut : **validé**.

## 1. Ce qui est enregistré, et rien de plus

### A. Données pédagogiques (sans donnée d'élève, partageables un jour)

| Objet | Champs | Remarque |
|---|---|---|
| Référentiel | domaines, sous-domaines, compétences (identifiant, libellé, niveau) | Intégré à l'appli, **lecture seule**. Mis à jour avec l'appli. |
| Atelier | identifiant, titre, photo modèle, liste d'identifiants de compétences (choix multiple), **caméra utilisée** (arrière ou avant), actif oui/non, date de création | Le domaine n'est pas saisi : il se déduit des compétences, et un atelier peut relever de **plusieurs domaines**. |

### B. Données personnelles (ne quittent jamais la tablette sans action de l'enseignant)

| Objet | Champs | Remarque |
|---|---|---|
| Classe | nom | Une seule classe en V0. |
| Élève | identifiant, prénom, représentation (robot n°, photo, ou prénom en grand) | Pas de nom de famille, date de naissance, adresse ni contact parents. |
| Trace | identifiant, photo, élève, atelier, date et heure, **copie des compétences au moment de la prise** | Modifier un atelier ne réécrit pas l'historique (Q4). |

### C. Réglages et sécurité

| Donnée | Stockage |
|---|---|
| Code PIN (4 chiffres) | Jamais en clair : seule une **empreinte** (hachage) est gardée. |
| Code de secours | Idem, empreinte seulement. |
| Fermeture automatique | oui / non |
| Date de la dernière sauvegarde | date |
| Compteur d'essais PIN ratés | nombre + heure du blocage |

**Ce qui n'est jamais collecté** : compte, e-mail, identifiant de la tablette, localisation, statistiques d'usage, journal d'activité envoyé où que ce soit.

## 2. Où c'est rangé

- **Tout dans l'espace privé de l'appli** sur la tablette : une petite base de données pour les fiches, un dossier privé pour les photos.
- **Rien dans la galerie**, rien dans les dossiers partagés, rien de visible par les autres applis.
- **Pas de sauvegarde automatique Android** (Google) : elle sera explicitement désactivée, sinon Android pourrait copier les données de l'appli dans le compte Google de la tablette.
- **Désinstaller l'appli efface tout.**

## 3. Code PIN, code oublié et code de secours

Exigence : une procédure **faisable par un adulte**, et **pas par un enfant jusqu'à 9 ans**.

### Au premier lancement
1. L'enseignant choisit son PIN (4 chiffres) et le confirme.
2. L'appli affiche un **code de secours de 12 caractères** (ex. `K7PM-4XRT-9HWB`), **une seule fois**.
3. Consigne à l'écran : « Recopiez ce code sur papier et rangez-le **hors de la classe**. Il permet de changer le code PIN si vous l'oubliez. »
4. Pour continuer, l'enseignant doit **retaper les 4 derniers caractères** (preuve qu'il l'a noté).

### PIN oublié
1. Appui long de 3 s → écran PIN → lien discret **« Code oublié ? »**.
2. Saisie du **code de secours complet** (12 caractères, lettres et chiffres).
3. Choix d'un nouveau PIN, puis **un nouveau code de secours** est généré (l'ancien ne marche plus).

### Pourquoi un enfant n'y arrive pas
- Il faut déjà connaître le **geste caché** (appui long 3 s dans un coin).
- Le code de secours est **long, sans signification**, et **n'est pas dans la classe**.
- Le saisir demande de **lire et recopier** 12 caractères au clavier : hors de portée d'un enfant de maternelle, et très improbable pour un enfant de 9 ans sans le papier.

### Contre les essais au hasard
- Après **5 PIN faux** : blocage **1 minute**, puis 5 minutes, puis 15 minutes.
- Les PIN trop évidents sont refusés à la création : `0000`, `1111`, `1234`, `4321`…

### Code de secours perdu **et** PIN oublié
Aucune porte dérobée. Il faut **réinstaller l'appli** (tout est effacé), puis **restaurer la dernière sauvegarde**. D'où l'importance du rappel « dernière sauvegarde ».

## 4. Zone dangereuse : supprimer sans se tromper

Toute action qui efface des données personnelles est rangée dans une **zone dangereuse** clairement signalée :
- encadré **rouge**, en bas de l'écran concerné, séparé du reste ;
- icône ⚠️ ;
- bouton rouge au libellé précis (« Supprimer Ava », jamais « OK »).

La confirmation **dit exactement ce qui va disparaître**, avec les nombres réels.

| Action | Où | Ce que dit la confirmation | Protection |
|---|---|---|---|
| Supprimer une photo | Photo en grand | « Supprimer cette photo d'Ava (Puzzle 12 pièces, 14 oct.) ? Elle sera effacée de la tablette. » | Confirmation simple |
| Supprimer un élève | Fiche de l'élève, zone dangereuse | « Supprimer **Ava** ? Cela efface **ses 12 photos** dans **4 ateliers**. Cette action est définitive. Dernière sauvegarde : il y a 12 jours. » | Confirmation + **taper le prénom** de l'élève |
| Supprimer un atelier | Fiche de l'atelier, zone dangereuse | « Supprimer **Puzzle 12 pièces** ? Cela efface **ses 23 photos** (15 élèves). Pour garder les photos, désactivez plutôt l'atelier. » | Confirmation + taper le titre |
| **Tout effacer** (fin d'essai, fin d'année) | Réglages, zone dangereuse | « Effacer **toute la classe** : **28 élèves**, **9 ateliers**, **412 photos** ? Rien ne pourra être récupéré sans sauvegarde. » | Confirmation + **taper `EFFACER`** + **code PIN** |

Règles communes :
- Le bouton rouge reste **désactivé** tant que la saisie ne correspond pas.
- La confirmation rappelle toujours la **date de la dernière sauvegarde**, avec un bouton « Sauvegarder d'abord ».
- **Aucune suppression** n'est possible côté enfant.

## 5. Ce qui disparaît quand on supprime

| On supprime… | … disparaît aussi | … reste |
|---|---|---|
| une photo | — | l'élève, l'atelier |
| un élève | toutes ses photos | les ateliers ; son robot redevient disponible |
| un atelier | toutes ses photos | les élèves ; les compétences (référentiel) |
| tout (« Tout effacer ») | élèves, ateliers, photos, nom de classe | PIN et code de secours, réglages ; le référentiel |
| l'appli (désinstallation) | **tout** | les sauvegardes déjà copiées ailleurs |

**Désactiver** un atelier ne supprime rien : il disparaît seulement côté enfants.

## 6. Sauvegarde

- **Un seul fichier** : fiches (classe, élèves, ateliers, traces) + photos + photos modèles.
- **Nom** : `foteli-<nom-de-classe>-<date>` (ex. `foteli-MS-GS-Mme-Martin-2026-10-15`).
- **Emplacement** choisi par l'enseignant avec le sélecteur Android : clé USB, dossier de la tablette, ordinateur branché.
- **Contenu non inclus** : PIN, code de secours (la restauration sur une nouvelle tablette demande de créer un nouveau PIN).
- **Chiffrement** : fichier protégé par un **mot de passe choisi au moment de la sauvegarde**. Sans lui, le fichier est illisible, même si la clé USB est perdue.
- **Restaurer** remplace **tout** le contenu actuel, après une confirmation en zone dangereuse.

## 7. Photos

- Prises avec l'appareil photo **dans** l'appli, jamais via l'appli Appareil photo d'Android (sinon la photo finirait dans la galerie).
- Qualité standard en V0. Réduction de taille : voir `docs/a-faire.md` (amélioration).
- Une photo non validée (↻ ou retour automatique) est **effacée immédiatement**.

## 8. Décisions

| # | Décision |
|---|---|
| Q1 | Sauvegarde **chiffrée** par un mot de passe choisi au moment de la sauvegarde. |
| Q2 | **Supprimer un atelier** (avec ses photos) possible en V0, en zone dangereuse. |
| Q3 | **Choix multiple** de compétences par atelier, pris dans le référentiel (domaines et programme du cycle 1). Un atelier peut donc relever de plusieurs domaines. |
| Q4 | Chaque trace garde une **copie des compétences** au moment de la photo. |
| Q5 | **Caméra choisie par l'enseignant** à la création de l'atelier (arrière par défaut, ou avant). Les enfants ne peuvent pas la changer. |
