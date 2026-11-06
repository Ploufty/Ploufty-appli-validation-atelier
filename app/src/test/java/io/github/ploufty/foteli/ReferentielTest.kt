package io.github.ploufty.foteli

import io.github.ploufty.foteli.data.Referentiel
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferentielTest {

    private val real = Referentiel.parse(File("../references/referentiel-competences.csv").readText(Charsets.UTF_8))

    @Test
    fun theWholeReferentialIsRead() {
        assertEquals(493, real.competencies.size)
        assertEquals(493, real.competencies.map { it.id }.toSet().size)
        assertEquals(6, real.domains.size)
        assertEquals(listOf("LAN", "EPS", "ART", "MAT", "TES", "VIV"), real.domains.map { it.first })
    }

    @Test
    fun quotedLabelWithSemicolonsIsKeptWhole() {
        val c = real["LAN-NLE-GS-03"]
        assertNotNull(c)
        assertTrue(c!!.text, c.text.contains("majuscule lettre capitale ; minuscules scriptes ; cursives"))
    }

    @Test
    fun filtersByDomainSubdomainAndLevel() {
        val list = real.list("MAT", "FOR", "MS")
        assertTrue(list.isNotEmpty())
        assertTrue(list.all { it.id.startsWith("MAT-FOR-MS-") })
        assertTrue(real.subdomains("TES").any { it.first == "ESP" })
    }

    private val all = setOf("PS", "MS", "GS")

    @Test
    fun searchIgnoresCaseAccentsAndPlurals() {
        assertTrue(real.search("PUZZLES", all).any { it.id == "MAT-FOR-MS-04" })
        assertTrue(real.search("écrire", all).isNotEmpty())
        assertTrue(real.search("ecrire", all).map { it.id } == real.search("écrire", all).map { it.id })
        assertTrue(real.search("", all).isEmpty())
        assertTrue(real.search("zzzz", all).isEmpty())
    }

    @Test
    fun searchMatchesWordStartsOnly() {
        // « vélo » ne doit pas trouver « développe ».
        assertTrue(real.search("velo", all).none { it.subdomainCode == "SYN" })
        assertTrue(real.search("formes trier", all).all { it.domainCode == "MAT" })
    }

    @Test
    fun everydayWordsFindOfficialCompetencies() {
        assertTrue(real.search("kapla", all).take(3).any { it.id == "MAT-FOR-GS-05" || it.id == "MAT-FOR-PS-03" })
        assertTrue(real.search("perles", all).first().id == "MAT-MOT-MS-04")
        assertTrue(real.search("robot", all).all { it.domainCode == "TES" })
    }

    @Test
    fun searchKeepsOnlyChosenLevels() {
        val ms = real.search("perles", setOf("MS"))
        assertTrue(ms.isNotEmpty())
        assertTrue(ms.all { it.level == "MS" })
    }
}
