package io.github.ploufty.foteli.data

import android.content.Context

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

    companion object {
        private const val ASSET = "referentiel-competences.csv"

        @Volatile
        private var cached: Referentiel? = null

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
