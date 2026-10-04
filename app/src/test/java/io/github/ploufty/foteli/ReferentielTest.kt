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
}
