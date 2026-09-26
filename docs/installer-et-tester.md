# Installer et tester Foteli

## 1. Récupérer l'APK

### Le plus simple : le lien direct (depuis la tablette ou l'ordinateur)

**https://github.com/Ploufty/Ploufty-appli-validation-atelier/releases/latest/download/foteli.apk**

- Ce lien télécharge **toujours la dernière version**, directement en `.apk` (pas de zip).
- Pas besoin d'être connecté à GitHub.
- Conseil : le mettre en **favori** dans le navigateur de la tablette.

Toutes les versions et leurs notes : onglet **Releases** du dépôt (colonne de droite de la page d'accueil du dépôt).

### Autre voie (pour les versions en cours de test)

Onglet **Actions** → dernière exécution réussie (✓ verte) → rubrique **Artifacts** en bas de page → `foteli-<version>-signee` (ou `-test`). C'est un `.zip` contenant l'`.apk`. Il faut être connecté à GitHub et passer par un navigateur (pas l'appli GitHub).

## 2. Tester sur Windows, sans tablette

### Solution conseillée : l'émulateur d'Android Studio (gratuit, officiel)

On n'utilise **que la tablette virtuelle** d'Android Studio : pas besoin de créer de projet ni de toucher au code.

**0. Vérifier le PC** (Windows 10 ou 11, 64 bits)
- 16 Go de mémoire conseillés (8 Go minimum), environ 20 Go d'espace disque libre.
- Virtualisation active : *Gestionnaire des tâches* (Ctrl + Maj + Échap) → onglet *Performance* → *Processeur* → ligne **Virtualisation : Activé**.

**1. Installer Android Studio**
1. Télécharger depuis **developer.android.com/studio** → *Download Android Studio*.
2. Lancer l'installeur : *Next* à chaque écran, en laissant **Android Virtual Device** coché → *Install* → *Finish*.
3. Au premier démarrage : *Do not import settings* → assistant **Standard** → pour chaque licence listée à gauche, cliquer dessus puis **Accept** → *Finish*. Le téléchargement des composants prend un moment.

**2. Créer la tablette virtuelle (une seule fois)**
1. Écran d'accueil *Welcome to Android Studio* → **More Actions** (ou les trois points ⋮) → **Virtual Device Manager**.
2. **+** (ou *Create Virtual Device*).
3. Catégorie **Tablet** → **Pixel Tablet** → *Next*.
4. Image système : **API 34** (Android 14) ou **API 33** (Android 13). Cliquer sur la flèche ⬇ à côté pour la télécharger, accepter la licence, attendre → *Next* → *Finish*.

**3. Démarrer la tablette**
1. Dans le *Device Manager*, cliquer sur ▶ à côté de *Pixel Tablet*.
2. Une fenêtre-tablette apparaît (1 à 2 minutes au premier démarrage).
3. Les boutons de rotation dans la barre à côté de la tablette la passent en paysage.

**4. Installer Foteli**
- **Méthode A (glisser-déposer)** : télécharger `foteli.apk` sur le PC (lien direct en haut de ce document), puis **glisser le fichier depuis l'Explorateur Windows sur l'écran de la tablette virtuelle**. Attendre quelques secondes.
- **Méthode B (comme sur une vraie tablette)** : dans la tablette virtuelle, ouvrir **Chrome**, taper le lien direct, télécharger, toucher le fichier, autoriser « Installer des applis inconnues », *Installer*.

**5. Ouvrir Foteli**
Sur l'écran d'accueil de la tablette virtuelle, **glisser de bas en haut** pour afficher toutes les applis → icône **Foteli** (robot blanc sur fond bleu).

**Les fois suivantes** : ouvrir Android Studio → *More Actions* → *Virtual Device Manager* → ▶. Pour une nouvelle version, refaire l'étape 4.

**Si ça bloque**
| Problème | Solution |
|---|---|
| Message sur l'hyperviseur ou l'accélération | *Activer ou désactiver des fonctionnalités Windows* → cocher **Plateforme de l'hyperviseur Windows** → redémarrer le PC. |
| « Virtualisation : Désactivé » | À activer dans le BIOS du PC (souvent « Intel VT-x » ou « SVM / AMD-V ») ; demander de l'aide si besoin. |
| Tablette virtuelle très lente | Fermer les autres logiciels ; choisir l'image API 33 plutôt que 34. |
| Le glisser-déposer ne fait rien | Utiliser la méthode B (Chrome dans la tablette virtuelle). |

### À éviter

- **Windows Subsystem for Android** : arrêté par Microsoft en 2025.
- **BlueStacks** et émulateurs de jeux similaires : ils fonctionnent, mais contiennent de la publicité et du pistage. Acceptables pour regarder une version vide, **jamais avec de vraies photos d'élèves**.

### Sans rien installer

À chaque version, GitHub lance aussi Foteli sur une **tablette virtuelle** et publie des **captures d'écran** : artifact **`captures-ecran`** de la même exécution.

## 3. Installer sur la tablette (Android 13+)

1. Sur la tablette, ouvrir le **lien direct** ci-dessus dans le navigateur (ou copier l'`.apk` par câble / clé USB).
2. Toucher le fichier `.apk`.
3. Android demande d'autoriser l'appli qui ouvre le fichier (Fichiers ou navigateur) à **« Installer des applis inconnues »** : accepter.
4. Installer, puis ouvrir Foteli.

Si l'installation est refusée ou le réglage introuvable, la tablette est probablement **gérée** (mairie, académie) : il faudra l'accord du service informatique.

## 4. Mettre à jour

Installer la nouvelle APK **par-dessus** l'ancienne : les données sont conservées, **à condition** que les deux versions soient signées avec la **même clé**. Une APK `-test` ne peut pas être remplacée par une APK `-signee` : il faut alors désinstaller (ce qui efface les données — sans importance tant qu'aucune vraie classe n'est enregistrée).

## 5. Ce que montre la version 0.1

Un écran d'accueil avec des vérifications **mesurées sur la tablette elle-même** :
- installation réussie ;
- aucune permission Internet ;
- pas de copie vers le compte Google ;
- captures d'écran bloquées ;
- modèle, version d'Android et taille d'écran, utiles pour la suite.
