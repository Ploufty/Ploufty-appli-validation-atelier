# V0 — Technique

> Étape 5 sur 5 (périmètre → parcours → écrans → données → **technique**).
> Statut : **à valider**. Les points marqués 🔶 attendent une décision.
> Aucun code ici : ce document décrit les choix, pour que le développement démarre sans surprise.

## 1. Les briques retenues

| Besoin | Choix | Pourquoi |
|---|---|---|
| Langage | **Kotlin** | Langage officiel d'Android. |
| Écrans | **Jetpack Compose** | Outil officiel actuel pour construire les écrans ; adapté aux grandes cartes et aux animations simples. |
| Appareil photo | **CameraX** (bibliothèque officielle) | Caméra **dans** l'appli : la photo ne passe jamais par la galerie. Choix caméra avant / arrière par atelier. |
| Fiches (élèves, ateliers, traces) | **Room** (petite base SQLite dans l'appli) | Standard, fiable, hors ligne. |
| Photos | Fichiers JPEG dans le **dossier privé** de l'appli | Invisible pour les autres applis et la galerie. |
| Référentiel | Fichier intégré à l'appli (≈ 100 Ko), lecture seule | Mis à jour avec l'appli. |
| Version d'Android | **Android 13 minimum** | Décidé. |
| Dépendances | Uniquement des bibliothèques officielles Android (Google/AndroidX) | Pas de service tiers, pas de publicité, pas de mesure d'audience. |

## 2. Empêcher l'enfant de sortir de l'appli (exigence vitale)

### Choix pour la V0 : le « mode classe » par épinglage d'écran

1. L'enseignant ouvre l'espace enseignant et touche **« Lancer le mode classe »**.
2. Android affiche une fois : « Épingler Foteli ? » → l'enseignant confirme.
3. La tablette est verrouillée sur Foteli :
   - boutons Accueil et Applis récentes inactifs ;
   - volet de notifications bloqué ;
   - l'écran reste allumé pendant le mode classe (🔶 option).
4. Pour en sortir : espace enseignant (appui long + PIN) → **« Quitter le mode classe »**.

Si un enfant tente la manœuvre système pour désépingler (maintenir deux boutons ou un geste précis), Android **demande le code de verrouillage de la tablette**. Il faut pour cela :
- un **code de verrouillage** sur la tablette (réglé par l'enseignant) ;
- l'option Android **« Demander le code avant de retirer l'épinglage »** activée.

Foteli guidera l'enseignant la première fois (écran d'aide avec les étapes).

### Plus tard si besoin : le mode kiosque complet

Foteli devient le « propriétaire » de la tablette : aucune sortie possible, aucune confirmation à chaque séance.
- Plus sûr, mais la tablette doit être **réinitialisée et préparée une fois depuis un ordinateur**.
- Incompatible avec une tablette déjà gérée par la mairie ou l'académie.
- À envisager si l'épinglage ne suffit pas pendant le test.

## 3. Photos

- **Orientation bloquée en paysage** dans tout l'espace élève.
- **Caméra** : celle réglée dans l'atelier (arrière par défaut) ; celle des Réglages pour les Souvenirs.
- **Aucune position enregistrée** : l'appli ne demande pas la localisation, et les métadonnées de la photo (modèle de tablette, etc.) sont retirées à l'enregistrement.
- **Photo non validée** : effacée immédiatement (↻, retour, inactivité).
- **Déclencheur (D3, à préciser)** : la V0 construit **deux déclencheurs identiques, à gauche et à droite**. Le choix final se fera après observation en classe (version 0.4 ci-dessous). Piste complémentaire : les boutons de volume comme déclencheur (habitude de nombreux appareils photo).

## 4. Sécurité et vie privée

| Mesure | Effet |
|---|---|
| **Pas de permission Internet** | Android interdit toute connexion à l'appli. Vérifiable par n'importe qui dans les informations de l'appli. |
| **Sauvegarde automatique Android désactivée** | Aucune copie des données vers le compte Google de la tablette. |
| **Chiffrement de la tablette** | Android 13 chiffre déjà les données des applis quand la tablette est verrouillée : pas besoin d'un chiffrement supplémentaire de la base en V0. |
| **Écran protégé** 🔶 | Captures d'écran interdites et aperçu masqué dans les applis récentes, pour que les photos d'enfants n'apparaissent nulle part ailleurs. |
| **PIN, code de secours** | Seule une empreinte est stockée ; blocage progressif après 5 essais. |
| **Mot de passe de sauvegarde** | Conservé dans le coffre-fort de clés d'Android. |
| **Sauvegarde** | ZIP chiffré AES-256 (`donnees.md` § 6), écrit uniquement où l'enseignant le choisit (sélecteur Android). |
| **Permissions demandées** | **Appareil photo uniquement.** |

## 5. Fabriquer et installer l'APK

### Fabrication automatique

Cet environnement de travail ne peut pas télécharger les outils Android : l'APK sera donc fabriquée par **GitHub Actions** (les serveurs de GitHub), à chaque version.
1. Je pousse le code sur le repo.
2. GitHub compile, vérifie, et produit l'APK signée.
3. L'APK apparaît dans une **Release** GitHub, à télécharger depuis la tablette ou un ordinateur.

### Signature : une clé à ne jamais perdre

- Toute APK est signée par une **clé**. Une mise à jour ne s'installe **par-dessus** l'ancienne version (en gardant les données) que si elle porte **la même signature**.
- Perdre la clé = impossible de mettre à jour sans désinstaller, donc **sans tout effacer**.
- Organisation proposée :
  - la clé est créée une fois ;
  - elle est stockée dans les **secrets du repo GitHub** (invisibles, même dans un repo public) ;
  - **tu en gardes une copie hors ligne** (clé USB rangée).

### Installer sur la tablette

1. Télécharger l'APK depuis la Release (navigateur de la tablette).
2. Android demande d'autoriser ce navigateur à **« Installer des applis inconnues »** : accepter, une seule fois.
3. Installer. Pour une mise à jour : même procédure, les données sont conservées.

À vérifier dès la version 0.1 : la tablette réelle accepte-t-elle l'installation ? (C'était le risque F11.)

### Le repo

🔶 Le repo `Ploufty-appli-validation-atelier` est **public**.
- **Aucune donnée d'élève n'y sera jamais** : il ne contient que le code, les documents et le référentiel.
- Un repo public facilite un futur partage (forge des communs, F-Droid).
- Il faudra alors choisir une **licence libre**.
- S'il doit rester privé pour l'instant, on peut le basculer dans les réglages GitHub.

## 6. Découpage en versions installables

Chaque version produit une APK que tu installes et testes sur la tablette. On ne passe à la suivante que quand la précédente fonctionne. Chaque version est taguée : on peut toujours revenir à la précédente.

| Version | Contenu | Ce que tu testes sur la tablette |
|---|---|---|
| **0.1 Squelette** | Appli vide qui s'ouvre, fabrication automatique de l'APK, signature | **L'installation fonctionne** (risque n° 1 levé) |
| **0.2 Classe** | Premier lancement (PIN, code de secours, nom de classe), élèves (robots, photo, prénom), ajout groupé, accueil enfant, accès enseignant caché | Créer sa classe, voir la grille d'accueil |
| **0.3 Ateliers** | Ateliers, photo modèle, référentiel et choix des compétences, caméra par atelier, activer / dupliquer / tout désactiver, écran de choix enfant | Créer un atelier en moins de 2 min |
| **0.4 Photo** | Caméra, aperçu ✅ / ↻, traces, bravo, retour auto 1 min, mode libre Souvenirs, deux déclencheurs | **Premier essai avec des enfants** |
| **0.5 Suivi et tri** | Photos par atelier / par élève, « 5/12 élèves », suppression photo, zones dangereuses, tout effacer | « Qui est passé ? » en un coup d'œil |
| **0.6 Verrouillage** | Mode classe (épinglage), écran protégé, blocage PIN, fermeture auto 5 min | Un enfant ne peut pas sortir |
| **0.7 Sauvegarde** | ZIP chiffré, restauration, rappel 14 jours, restauration au premier lancement | Sauvegarder sur clé USB, restaurer sur une tablette vierge |
| **0.8 Finitions = V0** | Accessibilité de base (TalkBack, contrastes, tailles), relecture complète | Test en classe sur plusieurs semaines |

## 7. Vérifications à chaque version

- **Automatiques** (sur GitHub, à chaque envoi) :
  - la compilation ;
  - des tests des règles importantes : suppression en cascade, code PIN et blocage, copie des compétences dans la trace, contenu de la sauvegarde ;
  - l'absence de permission Internet dans l'APK.
- **Manuelles** (sur ta tablette) : une courte liste de contrôle fournie avec chaque version.

## 8. Décisions à prendre (🔶)

| # | Question | Recommandation |
|---|---|---|
| T1 | Verrouillage V0 par **épinglage d'écran** (mode classe), kiosque complet seulement si besoin ? | **Oui.** |
| T2 | **Écran toujours allumé** pendant le mode classe (sinon la tablette se met en veille et demande son code) ? | **Oui, en option activée par défaut.** |
| T3 | **Écran protégé** (pas de captures, aperçu masqué) dans toute l'appli ? | **Oui.** |
| T4 | **Repo public** (code et documents seulement) avec une licence libre à choisir plus tard, ou repo privé ? | Public, puisque rien de personnel n'y est ; à toi de voir. |
| T5 | Clé de signature : créée par GitHub, stockée en secret, **copie hors ligne gardée par toi** ? | **Oui.** |
