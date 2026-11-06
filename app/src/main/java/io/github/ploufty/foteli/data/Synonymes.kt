package io.github.ploufty.foteli.data

/**
 * Mots du quotidien de la classe → débuts de mots du programme officiel.
 * « kapla » n'est pas dans le programme : la recherche le traduit en « assemblages », « encastrer », « solides »…
 * Clés et termes sans accent, en minuscules, au singulier. À compléter librement.
 */
object Synonymes {
    private val groupes: List<Pair<List<String>, List<String>>> = listOf(
        listOf("kapla", "lego", "construction", "construire", "brique", "empiler", "duplo") to
            listOf("assemblag", "encastr", "solide"),
        listOf("puzzle", "encastrement") to listOf("puzzle", "pavage", "assemblag", "encastr"),
        listOf("tangram", "mosaique", "pavage") to listOf("pavage", "puzzle", "forme"),
        listOf("perle", "collier", "enfiler", "enfilage", "algorithme") to listOf("motif", "suite", "rythm", "motricit"),
        listOf("pate", "modelage", "argile", "plastiline") to listOf("modeler", "volume", "materiau"),
        listOf("peinture", "peindre", "gouache", "pinceau", "aquarelle") to listOf("couleur", "composit", "materiau", "trac"),
        listOf("ciseaux", "decoupage", "decouper") to listOf("decoup", "motricit", "materiau"),
        listOf("collage", "coller") to listOf("composit", "materiau"),
        listOf("loto", "memory", "domino", "societe", "plateau") to listOf("quantit", "nombre", "collection", "regle", "jeu"),
        listOf("compter", "comptage", "denombrer", "denombrement") to listOf("quantit", "nombre", "collection"),
        listOf("lecture", "lire", "conte", "album") to listOf("album", "livre", "histoire", "texte", "personnage"),
        listOf("trier", "classer", "classement", "ranger", "rangement") to listOf("tri", "class", "ordonn"),
        listOf("peser", "pesee", "poids") to listOf("masse", "balance"),
        listOf("mesurer", "mesure", "toise") to listOf("longueur", "masse"),
        listOf("plantation", "planter", "graine", "jardin", "jardinage", "semis", "elevage") to
            listOf("plante", "vegeta", "soin", "cycle", "animal"),
        listOf("cuisine", "gateau", "recette") to listOf("recette", "melang", "matiere", "fiche"),
        listOf("eau", "sable", "semoule", "transvaser", "transvasement") to listOf("transvas", "melang", "matiere", "liquide"),
        listOf("aimant") to listOf("materiau", "objet"),
        listOf("motricite", "gym", "gymnastique", "cerceau", "velo", "trottinette", "porteur", "tricycle") to
            listOf("parcours", "deplac", "equilibr", "roul"),
        listOf("ballon", "balle") to listOf("lanc", "roul"),
        listOf("danser", "danse") to listOf("danse", "mime", "corps", "geste"),
        listOf("musique", "chanson", "chanter") to listOf("chant", "comptine", "instrument", "rythm", "voix"),
        listOf("calendrier", "date", "meteo") to listOf("jour", "semaine", "mois", "saison"),
        listOf("robot", "beebot", "codage", "programmation") to listOf("quadrillage", "trajet", "parcours", "orient"),
        listOf("photo", "appareil") to listOf("image"),
        listOf("marionnette") to listOf("marotte", "histoire"),
        listOf("dinette", "imitation", "deguisement", "theatre") to listOf("role", "imit", "mime", "spectacle"),
        listOf("alphabet") to listOf("lettre"),
    )

    private val index: Map<String, List<String>> =
        groupes.flatMap { (mots, termes) -> mots.map { it to termes } }.toMap()

    /** Termes du programme pour un mot de la recherche (déjà normalisé), ou une liste vide. */
    fun termes(mot: String): List<String> = index[mot] ?: index[mot.removeSuffix("s")] ?: emptyList()
}
