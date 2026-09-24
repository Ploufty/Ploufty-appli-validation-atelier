# Références institutionnelles

Tout ce qui sert de référence pédagogique à Foteli : programme officiel, référentiel de compétences et analyse.

Ces fichiers sont des **données pédagogiques** : ils ne contiennent aucune donnée d'élève et peuvent être partagés librement (cahier des charges, § 18).

## Contenu

| Fichier | Rôle |
|---|---|
| `referentiel-competences.md` | Le référentiel de travail : 6 domaines → sous-domaines → PS / MS / GS → 493 compétences avec identifiant stable. **C'est la référence à utiliser.** |
| `referentiel-competences.csv` | Le même référentiel en tableur (séparateur `;`, UTF-8). Base du futur import dans l'appli. |
| `comparaison-programme.md` | Écarts entre la liste d'origine et le programme officiel, corrections appliquées et apports du programme pour la conception de Foteli. |
| `sources/programme-maternelle-cycle1-consolide-2026.pdf` | Programme officiel de l'école maternelle (BO n° 41 du 31/10/2024, consolidé BO n° 19 du 7/05/2026). Contient aussi les « exemples de réussite ». |
| `sources/referentiel-enseignant-origine.txt` | Liste de compétences d'origine, conservée telle quelle. |

## Les 6 domaines

| Code | Domaine | Sous-domaines |
|---|---|---|
| LAN | Le développement et la structuration du langage oral et écrit | VOC, SYN, ARL, DIS, SON, SYL, NLE, SLE, DLI, SUP, TXT, GES, ORE, PEC |
| EPS | Agir, s'exprimer, comprendre à travers les activités physiques | DEP, EQU, EXP, COO |
| ART | Agir, s'exprimer, comprendre à travers les activités artistiques | DES, GRA, COM, IMA, VOI, INS, ECO, PRA, SPE |
| MAT | L'acquisition des premiers outils mathématiques | QTE, RAN, PBM, FOR, GRD, MOT |
| TES | Se repérer dans le temps et l'espace | REP, CHR, DUR, ESP, RES, ENV |
| VIV | Découvrir le monde du vivant, de la matière et des objets | ANI, CRP, OBJ, ETA |

## Identifiants

Format : `DOMAINE-SOUSDOMAINE-NIVEAU-NN`, par exemple `MAT-FOR-MS-04` pour « Je reproduis des modèles (puzzles, pavages) jusqu'à 5 éléments ».

Un atelier fera référence à une compétence par cet identifiant. On peut donc corriger le libellé d'une compétence sans casser les ateliers ni les traces existantes.

## Mise à jour

Pour modifier le référentiel, on édite la compétence dans le `.csv` et le `.md`. Règles :
- ne jamais réutiliser l'identifiant d'une compétence supprimée ;
- une nouvelle compétence prend le numéro suivant disponible dans son sous-domaine et son niveau.
