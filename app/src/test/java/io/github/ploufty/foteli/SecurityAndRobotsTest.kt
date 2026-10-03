package io.github.ploufty.foteli

import io.github.ploufty.foteli.security.PinRules
import io.github.ploufty.foteli.security.RescueCode
import io.github.ploufty.foteli.security.Secrets
import io.github.ploufty.foteli.ui.Robots
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndRobotsTest {

    @Test
    fun pinHashIsVerifiableButNeverPlain() {
        val stored = Secrets.hash("5731")
        assertFalse(stored.contains("5731"))
        assertTrue(Secrets.verify("5731", stored))
        assertFalse(Secrets.verify("5732", stored))
        // Deux empreintes du même code diffèrent (sel aléatoire).
        assertNotEquals(stored, Secrets.hash("5731"))
    }

    @Test
    fun weakPinsAreRefused() {
        listOf("0000", "1111", "1234", "4321", "6789", "9876", "1212", "2580").forEach {
            assertTrue("$it devrait être refusé", PinRules.isWeak(it))
        }
        listOf("5731", "2468", "8150").forEach {
            assertFalse("$it devrait être accepté", PinRules.isWeak(it))
        }
        assertFalse(PinRules.isWellFormed("12a4"))
        assertFalse(PinRules.isWellFormed("123"))
    }

    @Test
    fun lockoutGrowsEveryFiveFailures() {
        assertEquals(0L, PinRules.lockDurationMillis(4))
        assertEquals(60_000L, PinRules.lockDurationMillis(5))
        assertEquals(0L, PinRules.lockDurationMillis(6))
        assertEquals(5 * 60_000L, PinRules.lockDurationMillis(10))
        assertEquals(15 * 60_000L, PinRules.lockDurationMillis(15))
        assertEquals(15 * 60_000L, PinRules.lockDurationMillis(40))
    }

    @Test
    fun rescueCodeIsEightDigitsInTwoGroups() {
        repeat(50) {
            val code = RescueCode.generate()
            assertTrue(code, Regex("^\\d{4} \\d{4}$").matches(code))
        }
        val code = RescueCode.generate()
        assertEquals(RescueCode.LENGTH, RescueCode.normalize(code).length)
        val stored = Secrets.hash(RescueCode.normalize(code))
        assertTrue(Secrets.verify(RescueCode.normalize(code.replace(" ", "")), stored))
    }

    @Test
    fun thirtySixRobotsAreAllDifferent() {
        val specs = (0 until Robots.COUNT).map { Robots.spec(it) }
        assertEquals(Robots.COUNT, specs.toSet().size)
        // Couleur + forme de tête suffisent déjà à les distinguer.
        assertEquals(Robots.COUNT, specs.map { it.color to it.head }.toSet().size)
    }
}
