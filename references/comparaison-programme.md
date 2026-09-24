# Comparaison : liste enseignant ↔ programme officiel

- **Programme de référence** : *Programme d'enseignement — École maternelle*, d'après le BO n° 41 du 31 octobre 2024 et le BO n° 19 du 7 mai 2026 (`sources/programme-maternelle-cycle1-consolide-2026.pdf`, 63 pages).
- **Liste comparée** : `sources/referentiel-enseignant-origine.txt` (472 lignes, 473 compétences).
- **Résultat** : `referentiel-competences.md` / `.csv` (493 compétences, dont 469 issues de la liste enseignant et 24 ajoutées depuis le programme).

## 1. Bilan général

La liste enseignant reprend fidèlement la structure et les **objectifs d'apprentissage** du programme 2024-2026, reformulés en « Je… ». Les écarts sont peu nombreux :

- quelques **niveaux décalés** (section 3) ;
- des **noms de domaines et de sous-domaines** raccourcis (section 2) ;
- une trentaine de **fautes de frappe ou formulations floues** (section 4) ;
- quelques **doublons** (section 5) ;
- des **objectifs du programme absents** de la liste (section 6).

## 2. Structure officielle : 6 domaines

> ⚠️ Correction d'une information donnée plus tôt dans la conception : le programme actuel compte **six** domaines, pas cinq. L'ancien domaine « Explorer le monde » (programme 2015/2021) n'existe plus. Il est remplacé par **deux** domaines : « Se repérer dans le temps et l'espace » et « Découvrir le monde du vivant, de la matière et des objets ».
> Le cahier des charges cite encore « Explorer le monde » dans ses exemples (Puzzle, Kapla). Avec le nouveau programme, le puzzle relève de `MAT-FOR` (reproduire un assemblage) et le Kapla de `MAT-FOR` ou `TES-ESP` (constructions, assemblages).

| Code | Nom officiel (programme) | Nom dans la liste enseignant |
|---|---|---|
| LAN | Le développement et la structuration du langage oral et écrit | Structuration du langage oral et écrit |
| EPS | Agir, s'exprimer, comprendre à travers les activités physiques | Activité physique |
| ART | Agir, s'exprimer, comprendre à travers les activités artistiques | *(identique)* |
| MAT | L'acquisition des premiers outils mathématiques | Premiers outils mathématiques |
| TES | Se repérer dans le temps et l'espace | *(identique)* |
| VIV | Découvrir le monde du vivant, de la matière et des objets | Le vivant, les objets et la matière |

Le programme ajoute à ces six domaines :
- l'**éducation à la vie affective et relationnelle (EVAR)**, qui a son propre programme, non inclus dans ce PDF ;
- un enjeu transversal, « **Apprendre ensemble et vivre ensemble** », sans objectifs détaillés.

Ni l'un ni l'autre ne figure dans le référentiel pour l'instant.

### Sous-domaines renommés (nom officiel retenu)

| Liste enseignant | Nom officiel retenu |
|---|---|
| Construire ses équilibres | Construire des équilibres |
| Graphisme | S'exercer au graphisme artistique |
| Compositions plastiques | Réaliser des compositions planes ou en volume |
| Observer et transformer des images | Observer, comprendre et transformer des images |
| Voix et répertoire | Jouer avec sa voix et acquérir un répertoire de comptines et de chansons |
| Instruments et corps sonore | Explorer des instruments, utiliser les sonorités du corps |
| Le spectacle vivant | Le spectacle vivant : pratiquer |
| Se repérer dans le temps / « La chronologie » / La durée | Se repérer dans le temps / S'approprier la notion de chronologie / Construire la notion de durée |
| Se repérer dans l'espace | Faire l'expérience de l'espace |

Dans la liste d'origine, « La chronologie » n'avait pas d'en-tête `---` : c'est maintenant un sous-domaine à part entière.

Le programme regroupe aussi certains sous-domaines en niveaux intermédiaires (par exemple « Acquérir le langage oral », « Passer de l'oral à l'écrit : se préparer à apprendre à lire », « Arts visuels », « Les univers sonores »). Le référentiel n'en garde pas la trace pour rester à deux niveaux : **domaine → sous-domaine**.

## 3. Niveaux

Le programme ne parle pas de PS / MS / GS mais d'âges :

| Programme | Référentiel |
|---|---|
| À aborder avant 4 ans | PS |
| À partir de 4 ans ou dès que les apprentissages précédents ont pu être observés | MS |
| À partir de 5 ans ou dès que les apprentissages précédents ont pu être observés | GS |

### Changements de niveau appliqués (alignement sur le programme)

| Compétence | Avant | Après |
|---|---|---|
| Je connais le nom des lettres de l'alphabet et leur son. | MS | **GS** |
| Je reconnais des mots auditivement proches. | MS | **GS** |
| Je prends plaisir à écouter des chants… en langue étrangère. | MS | **PS** |
| Je participe à des jeux dans une autre langue… | GS | **MS** |
| Je mémorise des chants et des comptines dans une autre langue. | GS | **MS** |
| Je comprends des histoires où l'enchaînement des actions se répète. | PS | **MS** |
| Je commence à comprendre les émotions et les sentiments des personnages. | PS | **MS** |
| Je (commence à) planifie(r) deux activités. | MS | **PS** |
| Je mime la posture et les gestes d'écriture… | PS « Passer de l'oral à l'écrit » | PS « **Produire des écrits** » |

## 4. Reformulations et corrections

**Fautes de frappe corrigées** (sans changement de sens) : découvvre, créé → crée, lesmatières, avecles, j'utiliseavant, obejts, J respecte, planisphere, spectacteur, illustration → illustrations, espaces et ponctuation manquants, majuscules en début de phrase, ligne fusionnée « J'explore la flottabilité.Je choisis… » séparée en deux.

**Reformulations de fond** :

| Avant | Après | Raison |
|---|---|---|
| Je comprends les histoires et je sais les raconter dans support. | Je raconte une histoire connue en entier et sans support. | « dans » → « sans » (programme GS) |
| Je sais ranger. | J'utilise les outils de la classe pour me repérer, m'organiser et ranger. | Trop vague ; formulation du programme |
| Je m'entraîne pour écrire. | Je tiens correctement mon crayon et je m'entraîne à ne pas le lever en écrivant. | Objectif MS du programme (préhension) |
| J'essaie d'écrire. | Je participe à la dictée à l'adulte et je fais des essais d'écriture. | Programme MS |
| Je repère des mots. / Je repère des intrus. | Je repère un mot dans une phrase que l'adulte vient de lire. / Je repère un mot oublié par l'adulte quand il relit ma dictée. | Trop vagues ; exemples de réussite GS |
| Je prends conscience de l'écriture de lettres. | Je trace des signes en sachant que ce sont des lettres et non des dessins. | Programme PS |
| Je prononce mon prénom. | Je prononce mon prénom en scandant les syllabes. | Contexte « syllabes » |
| Je compare les mots. | Je compare les mots selon leur nombre de syllabes. | Précision |
| Je copie mon prénom. | Je copie mon prénom en lettres capitales. | Programme MS |
| J'écris un mot simple avec / sans modèle. (GS) | … en cursive … | Programme GS |
| Je joue avec ma voix. (MS, doublon de PS) | J'explore la richesse de ma voix (sons brefs, tenus, graves, aigus). | Distinguer MS de PS |
| Je connais les saisons. (MS, doublon de GS) | Je nomme l'hiver et l'été et je connais leurs caractéristiques. | Programme MS |
| Je réalise un trajet ou un parcours. (PS et GS) | … simple dans l'école (PS) / … dans un environnement connu (GS) | Distinguer les niveaux |
| Je situe les objets. | Je situe des objets dans un ensemble ordonné. | Programme GS |
| Je les représente. | Je représente quelques parties du corps. | Phrase autonome (sortie de son contexte dans l'appli) |
| Je reconnais / classe les formes de base comme le cube, la boule, le triangle ou le carré. | … des solides (cube, boule, pyramide, cylindre) et des formes planes (triangle, carré, disque). | Liste officielle MS |
| … (carré, triangle, **cercle**, rectangle) / … le carré ou le **cercle**. | … **disque** … | Le programme demande « disque » (le cercle est la ligne) |
| Je sais décrire des solides. | Je sais décrire des solides simples (cube, pavé, boule, pyramide, cylindre, cône). | Liste officielle GS |
| Je compare le **poids** / par ordre de **poids** / même **poids** | … **masse** … | Le programme parle de masse (« confondue à tort avec le poids ») |
| Je peux reproduire des constructions simples avec des blocs ou des pièces. | … (jusqu'à 4 éléments). | Repère chiffré PS |
| J'utilise mes doigts pour résoudre. | J'utilise mes doigts pour résoudre un problème. | Phrase incomplète |
| Je comprends des histoires où l'enchaînement des actions se répète. (PS) | Déplacée en MS ; en PS : « Je comprends des histoires proches de ma vie de tous les jours (le bain, le coucher). » | Progression du programme |
| Je saute un obstacle. / Je cours vite. / Je cours et franchis les obstacles. | … sans élan / … en ligne droite / Je cours vite et je franchis un obstacle. | Précision du programme |
| Je me repère dans les espaces. (PS) | Je me repère dans un espace proche et connu. | Programme PS |
| Je mémorise de plus en plus. | Je mémorise de plus en plus ce que j'entends. | Contexte « sons » |

## 5. Doublons

**Supprimés** :
- « Je nomme les objets que j'utilise, je sais à quoi ils servent. » apparaissait aussi dans « États de la matière » (PS). Il est conservé seulement dans « Objets et matériaux ».
- « J'écris un mot simple sans modèle. » (MS, Produire des écrits) : c'est un objectif GS, déjà présent dans « Geste d'écriture ».
- « Je comprends les émotions et les sentiments des personnages. » (MS) : il faisait doublon avec la version PS remontée en MS (« Je commence à comprendre… ») et avec la version GS.
- « J'explore la flottabilité. » était en double dans la ligne fusionnée.

**Conservés volontairement**, car le programme les répète d'un âge à l'autre (réinvestissement) :
- `LAN-SYL-PS-01` / `LAN-SYL-MS-01` : Je scande les syllabes d'un mot.
- `LAN-SYL-PS-03` / `LAN-SYL-MS-05` : Je dis des comptines courtes avec des sons qui se ressemblent.
- `ART-SPE-MS-02` / `ART-SPE-GS-03` : Je rencontre des artistes.

À trancher : dans l'appli, deux compétences au libellé identique risquent de gêner l'enseignant au moment du choix. Il suffit d'en garder une, ou de préciser le niveau dans le libellé.

## 6. Ajouts depuis le programme (24, marqués ➕)

Objectifs du programme sans équivalent dans la liste :

- **Vocabulaire** : s'appuyer sur un mot connu (MS), synonymes (MS), sens propre / figuré (GS), mot générique ou hyperonyme (GS).
- **Syntaxe** : futur / futur antérieur (GS).
- **Articulation** : imagier GS (chaud/seau, mouche/mousse…).
- **Sons** : distinguer les phonèmes (MS).
- **Son des lettres** : valeur sonore de quelques lettres connues (MS).
- **Textes lus** : reconnaître et nommer un personnage (PS), histoires proches du quotidien (PS).
- **Geste d'écriture** : tracer et enchaîner les lettres cursives (GS).
- **Oral → écrit** : l'écrit encode l'oral (PS), écriture ≠ dessin (PS).
- **Produire des écrits** : chercher des modèles dans la classe (MS), persévérer jusqu'au bout (GS).
- **Arts** : résistance des matériaux (PS), posture d'écoute (PS).
- **Maths** : surcomptage pour ajouter (GS), problèmes de groupements (MS), invariance de la forme (PS).
- **Temps / espace** : saisons observées dans la nature (PS), orienter un livre (PS).
- **Vivant** : changements liés à sa croissance (MS), gel et fonte de l'eau (PS).

**Non repris** (volontairement) :
- les **« exemples de réussite »** : ils sont très nombreux et se trouvent dans le PDF. Ils seront utiles plus tard comme idées d'ateliers (voir section 7).
- « Initier un parcours de lecteur » : ce thème n'a pas d'objectifs par âge.
- EVAR et « Apprendre ensemble et vivre ensemble » (voir section 2).

## 7. Ce que le programme apporte à la conception de Foteli

1. **Le programme cite la photographie comme trace d'apprentissage** :
   - « une trace du travail réalisé est conservée (boîte à histoires, traces sonores, photographies) » (p. 16) ;
   - suivi de croissance d'une plante « à partir d'un suivi d'observation (photographies régulières) » (p. 59) ;
   - étapes de fabrication « photographiées ou dessinées » (p. 61).

   Le principe même de l'appli s'appuie donc directement sur le texte officiel.

2. **Une évaluation positive**. Le programme décrit une évaluation qui « n'est pas un instrument de prédiction ni de sélection », qui doit « permet[tre] à chaque élève d'identifier ses réussites » et « rend[re] explicite pour les parents la progression » (p. 4). Cela conforte l'identité du projet : garder la trace d'une réussite, pas noter l'élève. C'est aussi un argument pour le bilan individuel (V2).

3. **Le niveau reste indicatif**. La formule « ou dès que les apprentissages précédents ont pu être observés » implique que l'appli ne doit **jamais empêcher** d'associer une compétence GS à un élève de MS, ni l'inverse. Le niveau sert à filtrer et à trier, pas à bloquer.

4. **Objectif ↔ compétence, exemple de réussite ↔ atelier**. Le programme présente chaque objectif en deux colonnes : « objectifs d'apprentissage » et « exemples de réussite ». Cette structure correspond au modèle de Foteli :
   - un objectif du programme devient une **compétence** ;
   - un exemple de réussite est souvent un **atelier** tout prêt (« reproduire un assemblage de 4 éléments à partir d'un modèle », « construire la tour la plus haute avec des planchettes »…).

   C'est une bonne source pour les futurs **modèles d'ateliers** (V3).

5. **Une bonne partie des compétences ne se photographie pas** : oral, chant, écoute, phonologie, jeux collectifs… Foteli s'applique surtout aux compétences qui laissent une **production visible** (constructions, puzzles, graphisme, arts plastiques, maths manipulées, sciences). Piste pour plus tard, à ne pas faire en V0 : repérer les compétences « photographiables » pour les proposer en premier quand l'enseignant crée un atelier.

6. **Volume à prévoir pour l'écran « choisir une compétence »**. Avec 493 compétences, l'enseignant ne peut pas parcourir une liste plate. Il faudra une navigation domaine → sous-domaine → niveau, et probablement une recherche par mot. En V0, le référentiel peut être embarqué **en lecture seule**, ce qui évite la saisie libre.

7. **Séparation des données** (cahier des charges, § 18). Ce référentiel est une donnée **pédagogique**, sans aucune donnée d'élève. Il peut être partagé, versionné et importé librement.
