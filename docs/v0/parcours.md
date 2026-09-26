# V0 — Parcours

> Étape 2 sur 5 (périmètre → **parcours** → écrans → données → technique).
> Statut : **validé**.

Un parcours décrit, action par action, ce que fait la personne et ce que fait l'appli. Les écrans seront dessinés à l'étape 3.

---

## A. Parcours ENFANT — enregistrer une réussite

C'est le parcours le plus important. Objectif : **4 touchers**, sans lire.

| # | L'enfant… | L'appli… |
|---|---|---|
| 1 | arrive devant la tablette, sur l'**accueil** | affiche la grille des élèves (robot, photo ou prénom en grand ; prénom en petit sous le robot ou la photo). |
| 2 | **touche son robot / sa photo / son prénom** (toucher 1) | affiche les **ateliers actifs** en grandes cartes (photo modèle). Son robot ou sa photo reste visible en haut : « c'est moi ». |
| 3 | **touche la carte de son atelier** (toucher 2) | ouvre l'**appareil photo** en plein écran, avec un très gros bouton déclencheur. |
| 4 | **touche le déclencheur** (toucher 3) | affiche la photo prise, avec un gros ✅ vert et un gros ↻. |
| 5a | **touche ✅** (toucher 4) | enregistre la trace (élève + atelier + date/heure), montre une coche animée ~2 s, revient à l'accueil. |
| 5b | ou **touche ↻** | efface la photo non validée, revient à l'appareil photo (étape 3). |

### Cas particuliers

| Situation | Comportement |
|---|---|
| L'enfant s'est trompé de profil ou d'atelier | Gros bouton **retour** (flèche) sur les écrans 2, 3 et 4 : revient d'un écran en arrière. |
| L'enfant part sans finir | Après **1 minute** sans action : retour à l'accueil. Une photo non validée est **effacée**, rien n'est enregistré. |
| Aucun atelier actif | L'accueil affiche un grand pictogramme « pas d'atelier » (lune qui dort) et les élèves ne sont pas touchables. |
| L'enfant reprend une 2e photo du même atelier | Autorisé : toutes les photos validées sont gardées. Le tri revient à l'enseignant. |
| L'enfant appuie sur les boutons Android (accueil, retour système, volet de notifications) | **Exigence vitale : l'enfant ne doit jamais pouvoir sortir de l'appli.** Solution technique à choisir à l'étape 5 (épinglage d'écran avec code, ou mode kiosque). |

Ce que l'enfant **ne voit jamais** : compétences, domaines, dates, réglages, bouton supprimer, texte à lire (hors prénoms).

---

## B. Parcours ENSEIGNANT

### B1. Premier lancement (une seule fois)

1. L'appli affiche un écran de bienvenue.
2. L'enseignant **choisit un code PIN** à 4 chiffres, puis le **confirme**. Les codes évidents (`0000`, `1234`…) sont refusés.
2 bis. L'appli affiche le **code de secours** (12 caractères) une seule fois. L'enseignant le recopie sur papier, le range hors de la classe, et retape ses 4 derniers caractères pour continuer (voir `donnees.md` § 3).
3. L'appli demande l'**autorisation d'utiliser l'appareil photo** (fenêtre Android). C'est fait ici, pour qu'un enfant ne tombe jamais sur cette fenêtre.
4. L'enseignant donne un **nom à la classe** (ex. « MS-GS Mme X — 2026-2027 »). Ce nom servira d'**en-tête** lors de l'extraction des photos (export, prévu après la V0) et dans le nom du fichier de sauvegarde.
5. L'appli ouvre l'espace enseignant sur la rubrique **Classe**, vide.

### B2. Entrer dans l'espace enseignant (au quotidien)

1. Sur l'accueil enfant, **appui long de 3 s** dans le **coin haut droit**.
2. Le clavier PIN apparaît. L'enseignant tape son code.
3. Code juste → espace enseignant. Code faux → le clavier se vide. Après 5 codes faux : blocage 1 min, puis 5, puis 15.
3 bis. **Code oublié ?** (lien discret sous le clavier) → saisie du code de secours → nouveau PIN → nouveau code de secours.
4. Pour revenir à l'accueil enfant : bouton « Mode élève ».
5. **Fermeture automatique** : l'espace enseignant se referme après 5 minutes sans action, pour qu'un enfant n'y reste pas si la tablette est oubliée ouverte. **Option activable / désactivable** dans Réglages (activée par défaut).

### B3. Créer la classe (au début)

Pour chaque élève :
1. **Classe** → « Ajouter un élève ».
2. Tape le **prénom**.
3. Choisit la représentation de l'élève sur l'accueil :
   - **robot** (par défaut) : choisi dans une planche de **robots tous différents** (au moins 30 ; un robot ne peut pas être donné à deux élèves) ;
   - **ou photo** de l'élève, prise avec la tablette ;
   - **ou prénom en grand** (pour les élèves qui reconnaissent leur prénom écrit).
4. Valide. L'élève apparaît dans la grille d'accueil.

Modifier : toucher l'élève → changer prénom ou image.
Supprimer : toucher l'élève → **zone dangereuse** (encadré rouge en bas de la fiche) → **« Supprimer Nelia ? Cela efface ses 12 photos dans 4 ateliers. Dernière sauvegarde : il y a 12 jours. »** → taper le prénom → le bouton rouge « Supprimer Nelia » s'active → confirmer.

### B4. Préparer un atelier

Exemple de référence : « Puzzle 12 pièces ».

1. **Ateliers** → « Nouvel atelier ».
2. Tape le **titre** : « Puzzle 12 pièces ».
3. **Photographie le modèle** avec la tablette (↻ pour reprendre).
4. **Choisit une ou plusieurs compétences** :
   domaine (*Premiers outils mathématiques*) → sous-domaine (*Explorer les solides et les formes planes*) → niveau (*MS*) → coche *« Je reproduis des modèles (puzzles, pavages) jusqu'à 5 éléments »*.
   Choix multiple possible, y compris dans plusieurs domaines. Le domaine de l'atelier découle des compétences choisies : rien d'autre à saisir.
4 bis. **Choisit la caméra** utilisée par les enfants pour cet atelier : **arrière** (par défaut, pour photographier une production posée) ou **avant** (par exemple pour une posture ou un mime). Les enfants ne peuvent pas la changer.
5. Enregistre. L'atelier est créé **inactif**.
6. **Active** l'atelier (interrupteur sur sa carte). Il apparaît aussitôt côté enfants.

Objectif : moins de 2 minutes.
Désactiver : même interrupteur. L'atelier disparaît côté enfants ; ses photos restent.
Modifier : toucher l'atelier → changer titre, photo modèle ou compétences.

### B5. Voir qui est passé à un atelier (vue la plus importante)

1. **Photos** → **Par atelier** → touche « Puzzle 12 pièces ».
2. L'appli affiche **toute la classe** :

   | Élève | Trace |
   |---|---|
   | Adem | ✓ (vignette) |
   | Assia | — |
   | Ava | ✓ (vignette, « 2 photos ») |

3. Toucher une vignette → la photo en grand, avec la date et l'heure.

### B6. Voir les traces d'un élève

1. **Photos** → **Par élève** → touche « Nelia ».
2. L'appli affiche ses photos **groupées par atelier**, les plus récentes en premier.
3. Toucher une photo → en grand, avec atelier, compétences, date et heure.

### B7. Trier les photos

1. Depuis n'importe quelle photo affichée en grand : bouton **Supprimer**.
2. **« Supprimer cette photo ? »** → confirmer.
3. La photo est effacée définitivement de la tablette.

### B8. Sauvegarder la classe

1. **Réglages** → **Sauvegarder ma classe**.
2. Android ouvre son sélecteur d'emplacement : l'enseignant choisit où mettre le fichier (clé USB, dossier de la tablette…).
3. L'appli crée **un seul fichier** (photos + élèves + ateliers), nommé avec la classe et la date, ex. `foteli-MS-GS-Mme-X-2026-10-15`.
4. Message : « Sauvegarde terminée. Ce fichier contient les photos des élèves : rangez-le en lieu sûr. »

Rappel : Réglages affiche « Dernière sauvegarde : il y a 12 jours ». L'appli ne sauvegarde jamais toute seule.

### B8 bis. Tout effacer (fin d'essai, fin d'année)

1. **Réglages** → **zone dangereuse** → **Tout effacer**.
2. « Effacer toute la classe : 28 élèves, 9 ateliers, 412 photos ? Rien ne pourra être récupéré sans sauvegarde. » + bouton « Sauvegarder d'abord ».
3. Taper `EFFACER`, puis le code PIN.
4. L'appli efface élèves, ateliers et photos, puis revient à la rubrique Classe, vide.

### B9. Restaurer une sauvegarde

1. **Réglages** → **Restaurer une sauvegarde**.
2. L'enseignant choisit le fichier.
3. **« Remplacer toutes les données actuelles par cette sauvegarde du 15/10/2026 ? »** → confirmer.
4. L'appli remplace tout, puis revient à l'accueil.

---

## C. Ce que l'appli fait toute seule

À chaque ✅ d'un enfant, l'appli enregistre la **trace** :

| Élément | Source |
|---|---|
| Photo | appareil photo, stockée dans l'espace privé de l'appli |
| Élève | toucher 1 |
| Atelier | toucher 2 |
| Date et heure | horloge de la tablette |
| Domaine et compétences | retrouvés à partir de l'atelier (non recopiés) |

Aucune autre action automatique : pas d'envoi, pas de synchronisation, pas de sauvegarde automatique.

---

## D. Décisions

| # | Décision |
|---|---|
| 1 | Aucun atelier actif : pictogramme « pas d'atelier » (lune), élèves non touchables. |
| 2 | **Vital** : l'enfant ne doit jamais pouvoir sortir de l'appli. Solution technique à l'étape 5. |
| 3 | Nom de la classe demandé au premier lancement ; il sert d'en-tête à l'extraction des photos et au nom de la sauvegarde. |
| 4 | Appui long de 3 s dans le coin haut droit pour l'espace enseignant. |
| 5 | Fermeture automatique de l'espace enseignant après 5 min, option activable / désactivable (activée par défaut). |
| 6 | Élève représenté par un robot (par défaut, tous différents), une photo ou son prénom en grand. |
| 7 | Rappel « dernière sauvegarde » dans Réglages, sans sauvegarde automatique (retenu par défaut, sauf avis contraire). |
| 8 | Code de secours pour le PIN oublié : faisable par un adulte, pas par un enfant jusqu'à 9 ans (`donnees.md` § 3). |
| 9 | Toute suppression importante passe par une **zone dangereuse** qui dit exactement ce qui disparaît (`donnees.md` § 4). |
