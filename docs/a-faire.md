# À faire plus tard

Idées et améliorations gardées de côté. Aucune n'est dans la V0, sauf mention contraire.

## Références

- **Ajouter le programme EVAR** (éducation à la vie affective et relationnelle), BO n° 6 du 6 février 2025.
  - Bloquant : texte officiel non récupéré (réseau de l'environnement cloud).
  - Pour débloquer : déposer le PDF officiel dans la session, ou autoriser `education.gouv.fr` / `eduscol.education.gouv.fr` dans les réglages réseau de l'environnement.
  - Intégration prévue : 7e domaine `EVA` dans `references/referentiel-competences.*`, avec sa comparaison.
  - Peu d'objectifs photographiables, mais utile pour une bibliothèque complète.

## Amélioration — Photos

### Taille des photos
- Réduire chaque photo à l'enregistrement (≈ 300 Ko au lieu de 1 à 3 Mo) : 5 à 6 fois moins de place, qualité suffisante pour l'écran et l'impression d'un bilan.
- Ordre de grandeur sans réduction : 28 élèves × 20 photos ≈ 1 Go par an.

### Extraction / export des photos (V1 ou V2)
Toujours à l'initiative de l'enseignant, jamais automatique.
- **Deux sorties** :
  - **clé USB** (ou dossier) : les photos elles-mêmes, rangées par élève et atelier ;
  - **fichier PDF personnalisable** = une **fiche bilan**.
- **Construire la fiche bilan** : cocher les ateliers, sélectionner les élèves, puis générer.
- **À penser** : la date de chaque photo, les compétences associées, le format (A4, nombre de photos par page, portrait / paysage).
- **En-tête** : le nom de la classe (décidé au premier lancement).

### Outils photo avant export
- **Redimensionner / recadrer** : les photos d'enfants seront souvent mal cadrées.
- **Masquer un visage non désiré** avec un emoji posé dessus (par exemple un autre enfant visible en arrière-plan).
  - Piste citée : le projet **FACEPRIVACY** (forge des communs numériques éducatifs), à étudier.
  - ⚠️ Rester sur un placement **manuel** de l'emoji. La détection automatique des visages relève de la « reconnaissance automatique / analyse d'image » que le cahier des charges demande de ne pas ajouter trop tôt (§ 23). Si on l'envisage un jour : uniquement sur la tablette, sans connexion.

## Amélioration — Suivi

- **Voir ce qui n'a pas encore été fait** :
  - par élève : la liste des ateliers **pas encore réalisés** ;
  - par atelier : la liste des élèves sans trace (déjà dans la V0, vue « Par atelier »).
- Base naturelle pour les grilles de suivi (V2).

## Canal de diffusion

Pour le test : **APK installée manuellement** (sources inconnues). À penser ensuite :
- **Google Play** : compte développeur, fiche « Sécurité des données » (facile : aucune donnée collectée), règles Play pour les applis destinées aux enfants (programme « Familles »).
- **Diffusion via la gestion de flotte** des tablettes (MDM de la mairie ou de l'académie) : souvent la seule voie sur des tablettes gérées.
- **F-Droid** (magasin d'applis libres) si le projet devient un commun numérique.
- **Forge des communs numériques éducatifs** : hébergement du code et visibilité auprès des enseignants.
- Dans tous les cas : **signature** de l'APK avec une clé conservée en lieu sûr (sans elle, pas de mise à jour possible).

## Interface et identité

- **Direction artistique** : palette, typographies, style des illustrations.
- **Mascotte ou logo** : un personnage peut accompagner l'enfant (accueil, bravo). Les robots des élèves sont une piste de cohérence.
- **Mode sombre** pour l'espace enseignant (et éventuellement l'espace élève).

## Accessibilité

Viser le **RGAA 4.1** (référentiel français, basé sur les WCAG 2.1 niveau AA et la norme EN 301 549, applicable aussi aux applications mobiles).

Déjà exigé en V0 (voir `v0/donnees.md` et `v0/verification.md`) :
- grandes zones tactiles (enfant ≥ 2 cm, enseignant ≥ 48 dp) ;
- contrastes suffisants ;
- information jamais portée par la seule couleur (✓ / — et icônes distinctes) ;
- libellés lisibles par TalkBack sur tous les boutons ;
- respect de la taille de texte du système dans l'espace enseignant ;
- animations réduites si l'option système est activée.

Plus tard :
- **audit complet** et **déclaration d'accessibilité** ;
- **options dans les Réglages** : taille des cartes, son de réussite (désactivé par défaut), contraste renforcé, durée avant retour automatique ;
- **guidage sonore facultatif** pour les enfants non lecteurs ou malvoyants.
