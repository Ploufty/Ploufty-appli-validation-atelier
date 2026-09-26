# Foteli

Application Android (nom provisoire : **Foteli**, ex « ABC – Bilan Photo ») permettant aux élèves de maternelle de photographier leurs réalisations d'ateliers, rangées automatiquement par élève / atelier / domaine / compétences.

## Référence permanente

Le cahier des charges complet est dans `docs/cahier-des-charges.md`. Le lire avant toute réponse sur le projet et s'y conformer.

Les références institutionnelles sont dans `references/` : programme officiel de maternelle (BO 2024, consolidé mai 2026), référentiel de compétences Foteli (`references/referentiel-competences.md` / `.csv`, 6 domaines, identifiants stables) et analyse des écarts. Le programme actuel compte 6 domaines ; « Explorer le monde » n'existe plus.

## Règles essentielles

- **Phase actuelle : DÉVELOPPEMENT de la V0** (validé par l'utilisateur), version par version selon `docs/v0/technique.md` § 6. Ne pas sauter d'étape : une version installable et testée avant la suivante.
- Build : impossible dans l'environnement cloud (outils Android bloqués) ; l'APK est fabriquée par GitHub Actions (`.github/workflows/android.yml`). Vérifier le résultat de la CI après chaque envoi.
- Jamais de clé de signature ni de donnée d'élève dans le dépôt.
- Simplicité avant tout. Question de référence : « Un enfant de 4-5 ans non lecteur peut-il comprendre cet écran ? »
- Espace enseignant : tout doit être **logique, pratique et faire gagner du temps à l'adulte** (valeurs par défaut, raccourcis, pas de saisie inutile). Question de référence : « Est-ce le moins de gestes possible pour l'enseignant ? »
- Local-first, offline-first, privacy by design : aucune donnée élève ni photo ne quitte la tablette sans action volontaire de l'enseignant. Pas de compte, cloud, sync, pub, analytics.
- Photos stockées dans l'espace privé de l'app, jamais dans la galerie.
- Séparer données pédagogiques (domaines, compétences, ateliers) et données personnelles (élèves, traces).
- Garder la V0 en ligne de mire ; ne pas proposer les fonctions listées en section 23 du cahier des charges.
- Toute nouvelle fonctionnalité s'analyse au format : Objectif / Utilisateur / Parcours / Interface / Données / Local-RGPD / Priorité / Risques / Recommandation.
- Tablette Android = appareil principal ; téléphone compatible.
- Répondre en français.
