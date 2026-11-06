package io.github.ploufty.foteli.data

import android.content.Context
import java.text.Normalizer

/** Une compétence du référentiel officiel (references/referentiel-competences.csv), en lecture seule. */
data class Competency(
    val id: String,
    val domainCode: String,
    val domain: String,
    val subdomainCode: String,
    val subdomain: String,
    val level: String,
    val text: String,
)

/** Le référentiel embarqué dans l'appli : 6 domaines, 493 compétences, identifiants stables. */
class Referentiel(val competencies: List<Competency>) {
    private val byId = competencies.associateBy { it.id }

    operator fun get(id: String): Competency? = byId[id]

    /** Domaines dans l'ordre du programme : code → nom. */
    val domains: List<Pair<String, String>> =
        competencies.map { it.domainCode to it.domain }.distinct()

    fun subdomains(domainCode: String): List<Pair<String, String>> =
        competencies.filter { it.domainCode == domainCode }.map { it.subdomainCode to it.subdomain }.distinct()

    fun list(domainCode: String, subdomainCode: String, level: String): List<Competency> =
        competencies.filter { it.domainCode == domainCode && it.subdomainCode == subdomainCode && it.level == level }

    fun domainName(code: String): String = domains.firstOrNull { it.first == code }?.second ?: code

    fun list(domainCode: String, subdomainCode: String, levels: Set<String>): List<Competency> =
        competencies.filter { it.domainCode == domainCode && it.subdomainCode == subdomainCode && it.level in levels }

    /** Mots de chaque compétence (libellé + sous-domaine), sans accent : la recherche porte sur leurs débuts. */
    private val words: Map<String, List<String>> by lazy {
        competencies.associate { it.id to words("${it.text} ${it.subdomain}") }
    }

    /**
     * Recherche de l'enseignant : chaque mot tapé doit être le début d'un mot de la compétence,
     * ou un mot du quotidien traduit par [Synonymes]. Majuscules et accents ne comptent pas.
     * Les compétences qui contiennent le mot tapé passent devant celles trouvées par synonyme.
     */
    fun search(query: String, levels: Set<String>): List<Competency> {
        val tokens = searchTokens(query)
        if (tokens.isEmpty()) return emptyList()
        val scored = mutableListOf<Pair<Competency, Int>>()
        for (c in competencies) {
            if (c.level !in levels) continue
            val w = words.getValue(c.id)
            var score = 0
            val all = tokens.all { t ->
                if (w.any { it.startsWith(t) }) {
                    score += 10
                    true
                } else {
                    val hits = Synonymes.termes(t).count { term -> w.any { it.startsWith(term) } }
                    score += hits
                    hits > 0
                }
            }
            if (all) scored.add(c to score)
        }
        return scored.sortedByDescending { it.second }.map { it.first }
    }

    companion object {
        private const val ASSET = "referentiel-competences.csv"
        private val STOP = setOf("le", "la", "les", "de", "des", "du", "un", "une", "et", "a", "au", "aux", "en", "je", "j", "l", "d", "s")

        @Volatile
        private var cached: Referentiel? = null

        fun normalize(text: String): String =
            Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

        fun words(text: String): List<String> = normalize(text).split(Regex("[^a-z0-9œæ]+")).filter { it.isNotEmpty() }

        /** Mots utiles d'une recherche (mots vides retirés, pluriel simple ramené au singulier). */
        fun searchTokens(query: String): List<String> =
            words(query).filter { it !in STOP }.map { if (it.length > 3 && it.endsWith("s")) it.dropLast(1) else it }.distinct()

        /** Débuts de mots à mettre en évidence dans les résultats (mots tapés et leurs synonymes). */
        fun highlightTerms(query: String): List<String> = searchTokens(query).flatMap { listOf(it) + Synonymes.termes(it) }

        fun get(context: Context): Referentiel =
            cached ?: synchronized(this) {
                cached ?: parse(context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).readText()).also { cached = it }
            }

        /** CSV séparé par « ; », guillemets possibles (un libellé contient des « ; »). */
        fun parse(csv: String): Referentiel {
            val rows = csvRows(csv.removePrefix("﻿")).drop(1).filter { it.size >= 7 }
            return Referentiel(rows.map { Competency(it[0], it[1], it[2], it[3], it[4], it[5], it[6]) })
        }

        private fun csvRows(text: String): List<List<String>> {
            val rows = mutableListOf<List<String>>()
            var row = mutableListOf<String>()
            val cell = StringBuilder()
            var quoted = false
            var i = 0
            while (i < text.length) {
                val c = text[i]
                when {
                    quoted && c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { cell.append('"'); i++ }
                    c == '"' -> quoted = !quoted
                    !quoted && c == ';' -> { row.add(cell.toString()); cell.clear() }
                    !quoted && (c == '\n' || c == '\r') -> {
                        if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                        row.add(cell.toString()); cell.clear()
                        if (row.any { it.isNotEmpty() }) rows.add(row)
                        row = mutableListOf()
                    }
                    else -> cell.append(c)
                }
                i++
            }
            if (cell.isNotEmpty() || row.isNotEmpty()) {
                row.add(cell.toString())
                rows.add(row)
            }
            return rows
        }
    }
}
