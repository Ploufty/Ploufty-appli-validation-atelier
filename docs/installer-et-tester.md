# Installer et tester Foteli

## 1. Récupérer l'APK

1. Ouvrir le dépôt sur GitHub → onglet **Actions** → workflow **« APK Foteli »**.
2. Cliquer sur la dernière exécution réussie (✓ verte).
3. En bas, section **Artifacts** : télécharger **`foteli-<version>-signee`** (ou `-test` tant que la clé de signature n'est pas configurée).
4. C'est un `.zip` : l'ouvrir, il contient le fichier `.apk`.

Il faut être connecté à GitHub pour télécharger.

## 2. Tester sur Windows, sans tablette

### Solution conseillée : l'émulateur d'Android Studio (gratuit, officiel)

1. Installer **Android Studio** (developer.android.com/studio).
2. Au premier lancement, laisser l'assistant installer les composants par défaut.
3. **More Actions → Virtual Device Manager → + (Create device)** → catégorie **Tablet** → **Pixel Tablet** → image système **Android 13 (API 33)** ou plus récente → Finish.
4. Démarrer la tablette virtuelle (bouton ▶).
5. **Glisser-déposer le fichier `.apk`** sur la fenêtre de l'émulateur : il s'installe tout seul.
6. Ouvrir Foteli dans la liste des applis.

Remarques :
- Il faut un PC assez récent (16 Go de mémoire conseillés) avec la **virtualisation activée**. Si l'émulateur refuse de démarrer, activer « Plateforme de l'hyperviseur Windows » dans *Activer ou désactiver des fonctionnalités Windows*.
- L'émulateur simule aussi un appareil photo (une pièce virtuelle) : utile pour tester la version 0.4.
- Rien n'est envoyé nulle part : l'émulateur tourne sur le PC.

### À éviter

- **Windows Subsystem for Android** : arrêté par Microsoft en 2025.
- **BlueStacks** et émulateurs de jeux similaires : ils fonctionnent, mais contiennent de la publicité et du pistage. Acceptables pour regarder une version vide, **jamais avec de vraies photos d'élèves**.

### Sans rien installer

À chaque version, GitHub lance aussi Foteli sur une **tablette virtuelle** et publie des **captures d'écran** : artifact **`captures-ecran`** de la même exécution.

## 3. Installer sur la tablette (Android 13+)

1. Copier l'`.apk` sur la tablette (câble, clé USB) ou le télécharger directement depuis GitHub avec le navigateur de la tablette.
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
