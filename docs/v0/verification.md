# V0 — Double vérification avant l'étape 5

> Deux relectures de tout ce qui a été décidé (cahier des charges, périmètre, parcours, maquette, données) avant de passer à la technique.
> 1. **Ergonomie et faisabilité** : est-ce utilisable en classe, et est-ce réalisable sur Android ?
> 2. **Cohérence** : les documents et la maquette disent-ils la même chose ?
>
> Légende : ✅ OK · ⚠️ point d'attention (déjà traité ou à surveiller) · 🔶 décision nécessaire.

---

## Vérification 1 — Ergonomie et faisabilité

### 1.1 Côté enfant (« un enfant de 4-5 ans non lecteur comprend-il ? »)

| # | Point vérifié | Verdict |
|---|---|---|
| E1 | **4 touchers** pour une trace, sans lecture | ✅ Robot → carte → déclencheur → ✅. |
| E2 | **Taille de la grille d'accueil** | ✅ jusqu'à 28-30 élèves : grille 7 × 4, cartes d'environ 3 cm sur une tablette 10". ⚠️ Au-delà de 32 élèves, il faudra faire défiler ou attendre les groupes (V1). |
| E3 | **Robots différenciables** | ⚠️ 36 robots = 9 couleurs × 4 formes de tête. Deux robots de même couleur ne diffèrent que par la forme : risque de confusion chez les PS. Parades déjà prévues : prénom sous le robot, photo ou prénom en grand. **À observer pendant le test.** |
| E4 | **Nombre d'ateliers actifs à la fois** | ⚠️ Au-delà de 6 cartes, elles deviennent petites. Recommandation : l'espace enseignant affiche un avertissement au-delà de 6 ateliers actifs (sans bloquer). |
| E5 | **Déclencheur utilisable « quelle que soit la manière dont l'enfant tient la tablette »** (cahier § 11) | ⚠️ **Fonctionnalité à préciser** (voir D3). Déjà décidé : la caméra (arrière / avant) est fixée par atelier par l'enseignant, sans choix pour l'enfant. |
| E6 | **✅ et ↻ distinguables sans la couleur** | ✅ Formes d'icônes différentes (coche / flèche), utile aussi pour les daltoniens. |
| E7 | **Retour positif** | ✅ Coche animée 2 s, sobre (pas de récompense excessive). Son de réussite : amélioration facultative (`a-faire.md`). |
| E8 | **Erreur de profil ou d'atelier** | ✅ Bouton retour à chaque écran, retour automatique après 1 min, photo non validée effacée. ⚠️ Une photo validée sous le mauvais prénom ne peut être que **supprimée** en V0 (la réattribution est prévue en V1). |
| E9 | **Aucun atelier actif** | ✅ Lune, élèves non touchables. |
| E10 | **L'enfant ne peut rien casser** | ✅ Aucun bouton de suppression côté enfant ; accès enseignant caché (appui long 3 s) + PIN + blocage après 5 essais. |

### 1.2 Côté enseignant

| # | Point vérifié | Verdict |
|---|---|---|
| T1 | **Créer un atelier en moins de 2 min** | ✅ Titre + photo + 4 à 6 touchers pour une compétence (domaine → sous-domaine → niveau → case). ⚠️ Une recherche par mot aiderait (V1). |
| T2 | **« Qui est passé ? » en un coup d'œil** | ✅ Vue Photos → Par atelier : ✓ / — pour toute la classe. |
| T3 | **Suppression sans erreur** | ✅ Zone dangereuse : conséquences chiffrées, saisie du prénom ou d'`EFFACER`, rappel de la dernière sauvegarde. |
| T4 | **PIN oublié** | ✅ Code de secours : faisable par un adulte, pas par un enfant jusqu'à 9 ans. |
| T5 | **Fermeture automatique en pleine saisie** | ⚠️ Si l'enseignant réfléchit plus de 5 min sur un formulaire, il perd sa saisie. Toute frappe remet le compteur à zéro, et l'option est désactivable. Acceptable en V0. |
| T6 | **Accessibilité de base** | ✅ Exigences listées (zones tactiles, contrastes, TalkBack, pas d'info par la seule couleur). Audit complet plus tard. |

### 1.3 Faisabilité Android

| # | Point vérifié | Verdict |
|---|---|---|
| F1 | **Appareil photo intégré à l'appli**, photos hors galerie | ✅ Standard (bibliothèque caméra officielle d'Android) ; photos écrites dans l'espace privé de l'appli. |
| F2 | **Empêcher l'enfant de sortir de l'appli** | ✅ Faisable. Deux niveaux, à choisir à l'étape 5 : **épinglage d'écran** (intégré à Android, rien à installer ; l'enseignant confirme l'épinglage au début de la séance, et le désépinglage demande le code de la tablette) ou **mode kiosque** (plus fort, mais la tablette doit être préparée une fois depuis un ordinateur). |
| F3 | **Sauvegarde sur clé USB** | ✅ Via le sélecteur de fichiers Android. ⚠️ Dépend de la tablette : elle doit accepter une clé USB (adaptateur USB-C / OTG). Sinon : dossier de la tablette puis copie sur ordinateur. |
| F4 | **Sauvegarde chiffrée par mot de passe** | ✅ Faisable sans Internet, avec les outils de chiffrement standard d'Android. |
| F5 | **PIN et code de secours jamais en clair** | ✅ Stockage d'une empreinte uniquement. |
| F6 | **Zéro réseau** | ✅ L'appli ne demandera **pas la permission Internet** : Android lui interdit alors toute connexion. C'est une garantie vérifiable, pas une promesse. |
| F7 | **Sauvegarde automatique Google** | ⚠️ **Piège RGPD** : par défaut, Android peut copier les données d'une appli dans le compte Google de la tablette. **Elle sera désactivée** (noté dans `donnees.md` § 2). |
| F8 | **Captures d'écran / aperçu des applis récentes** | ⚠️ L'aperçu des applis récentes peut montrer des photos d'enfants. Option : masquer l'aperçu (et interdire les captures) dans l'espace enseignant. À décider à l'étape 5. |
| F9 | **Référentiel embarqué** | ✅ Simple fichier intégré à l'appli (≈ 100 Ko). |
| F10 | **Fabrication de l'APK** | ✅ Automatique sur GitHub à chaque version. ⚠️ L'APK doit être **signée** avec une clé à conserver précieusement (sans elle, impossible de mettre à jour l'appli sans la désinstaller, donc sans tout effacer). |
| F11 | **Installation sur la tablette** | ⚠️ Décidé : APK en **sources inconnues** pour le test. **Risque principal**, à vérifier sur la tablette réelle : Les tablettes d'école sont souvent gérées par la mairie, l'académie ou un prestataire (MDM). Installer une APK hors Play Store peut y être **bloqué** ou demander l'accord du service informatique. **À vérifier avant de coder.** |
| F12 | **Versions d'Android** | ✅ **Android 13 minimum** (décidé). Avantage : permissions photo modernes, épinglage d'écran et gestion du stockage privé homogènes. |
| F13 | **Place disponible** | ✅ Sans réduction : ≈ 1 Go par an pour 28 élèves. Réduction des photos en amélioration. |

---

## Vérification 2 — Cohérence avant l'étape 5

### 2.1 Écarts trouvés et corrigés

| # | Écart | Correction |
|---|---|---|
| C1 | La maquette n'avait ni code de secours, ni blocage après 5 essais, ni zone dangereuse, ni « Tout effacer ». | ✅ Ajoutés à la maquette (version 2). |
| C2 | Le parcours décrivait une suppression d'élève par simple confirmation. | ✅ Remplacée par la zone dangereuse avec saisie du prénom (`parcours.md` B3). |
| C3 | Le périmètre ne mentionnait ni le blocage de sortie de l'appli ni l'accessibilité de base. | ✅ Ajoutés aux principes non négociables (`perimetre.md`). |
| C4 | L'idée d'export ne figurait que sur une ligne. | ✅ Détaillée dans `a-faire.md` (clé USB, PDF bilan, recadrage, emoji sur les visages). |
| C5 | Les PIN évidents (`1234`) étaient acceptés au premier lancement. | ✅ Refusés dans les documents et la maquette. ⚠️ Le code de démonstration de la maquette reste `1234`, pour la simplicité du test. |

### 2.2 Points déjà cohérents

- **Cahier des charges** : les écarts avec la roadmap sont listés et justifiés (`perimetre.md`, 5 éléments avancés).
- **Espace enseignant** : le cahier des charges prévoit 5 rubriques (Classe, Ateliers, Photos, Bilans, Réglages). La V0 en a 4, et Bilans arrive en V2. C'est cohérent avec la roadmap.
- **« Explorer le monde »** : les exemples du cahier des charges utilisent ce domaine, qui n'existe plus. C'est signalé dans `references/comparaison-programme.md`. La maquette utilise les nouveaux domaines.
- **Local-first** : aucune fonction de la V0 ne fait sortir une donnée sans action de l'enseignant (sauvegarde manuelle uniquement).
- **Séparation des données** : les données pédagogiques et les données personnelles sont bien distinguées (`donnees.md` § 1).
- **Mode B uniquement**, **robots / photo / prénom**, **1 min**, **5 min désactivable**, **coin haut droit** : identiques dans le périmètre, le parcours et la maquette.

### 2.3 Décisions prises

| # | Écart | Décision |
|---|---|---|
| D1 | Domaine d'un atelier : « un domaine » dans le cahier des charges, plusieurs dans la maquette. | **Plusieurs domaines autorisés** (choix multiple de compétences). |
| D2 | Historique des compétences. | **Copie des compétences dans chaque trace.** |
| D3 | Position du déclencheur. | **À préciser** : fonctionnalité à définir avec l'étape 5 et le test. Proposition de départ : un déclencheur de chaque côté, écran bloqué en paysage. |
| D4 | Âge des utilisateurs. | La V0 vise la **maternelle** ; la sécurité (PIN, code de secours) est pensée pour résister jusqu'à 9 ans. |

## Vérification 3 — Après les derniers ajouts (mode libre, raccourcis, sauvegarde)

| # | Point vérifié | Verdict |
|---|---|---|
| V1 | Mode libre : parcours enfant toujours en 4 touchers | ✅ |
| V2 | Mode libre : la carte « Photo libre » compte dans le nombre de cartes | ⚠️ L'avertissement « au-delà de 6 cartes » doit compter la carte Souvenirs (5 ateliers + Souvenirs = 6). |
| V3 | Accueil : la lune ne s'affiche que si **aucun atelier actif et mode libre désactivé** | ✅ Parcours, maquette et documents alignés. |
| V4 | Souvenirs dans la suppression et la sauvegarde | ✅ Effacés avec l'élève et par « Tout effacer » ; inclus dans la sauvegarde (dossier `souvenirs/`). |
| V5 | Sauvegarde exploitable | ✅ ZIP chiffré standard : restaurable dans Foteli **et** lisible sur ordinateur avec le mot de passe (`donnees.md` § 6). |
| V6 | Restaurer sur une nouvelle tablette | ✅ Proposé dès le premier lancement (« Restaurer une sauvegarde »). Ajouté au parcours et à la maquette. |
| V7 | Rappel de sauvegarde pratique pour l'adulte | ✅ Bandeau dans l'espace enseignant après 14 jours, avec un bouton « Sauvegarder maintenant ». |
| V8 | Emplacement du bouton Restaurer | ✅ Corrigé : Réglages → Sauvegarde (avec confirmation rouge), identique dans les documents et la maquette. |
| V9 | Duplication d'atelier retirée de la liste « hors V0 » | ✅ |

## Récapitulatif : ce qu'il faut avant l'étape 5

1. ✅ Décisions Q1 à Q5 et D1, D2, D4 prises. D3 (déclencheur) sera précisé avec l'étape 5.
2. **Vérifier sur la tablette réelle** (Android 13+) que l'installation d'une APK en sources inconnues est autorisée (F11).
3. **Tester la maquette**, idéalement avec un ou deux élèves, en observant E3 (confusion de robots) et E5 (position du déclencheur).
