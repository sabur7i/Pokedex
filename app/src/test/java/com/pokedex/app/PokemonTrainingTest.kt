package com.pokedex.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonTrainingTest {
    private val dratini = findPokemonById("dratini")!!

    @Test
    fun train_withoutReachingNextLevel_onlyAddsExperience() {
        // Nível 5 pede 20 + 5 * 5 = 45 de XP
        val trained = dratini.copy(level = 5, experience = 0).train(30)
        assertEquals(5, trained.level)
        assertEquals(30, trained.experience)
    }

    @Test
    fun train_withEnoughExperience_levelsUpAndKeepsLeftover() {
        val trained = dratini.copy(level = 5, experience = 40).train(10)
        assertEquals(6, trained.level)
        assertEquals(5, trained.experience)
    }

    @Test
    fun train_withLotsOfExperience_levelsUpMoreThanOnce() {
        // 45 (nível 5) + 50 (nível 6) = 95
        val trained = dratini.copy(level = 5, experience = 0).train(100)
        assertEquals(7, trained.level)
        assertEquals(5, trained.experience)
    }

    @Test
    fun train_atMaxLevel_staysAtMaxWithZeroExperience() {
        val trained = dratini.copy(level = 99, experience = 0).train(10_000)
        assertEquals(100, trained.level)
        assertEquals(0, trained.experience)
        assertTrue(trained.isMaxLevel)
    }

    @Test
    fun getNextLevel_atMaxLevel_returnsNull() {
        assertNull(dratini.copy(level = 100).getNextLevel())
    }

    @Test
    fun stats_growWithLevel() {
        val low = dratini.copy(level = 5).stats
        val high = dratini.copy(level = 50).stats
        assertTrue(high.hp > low.hp)
        assertTrue(high.attack > low.attack)
        // Fórmula: 2 * 41 * 50 / 100 + 50 + 10 = 101
        assertEquals(101, high.hp)
    }

    @Test
    fun canEvolve_onlyFromEvolutionLevel() {
        assertFalse(dratini.copy(level = 29).canEvolve)
        assertTrue(dratini.copy(level = 30).canEvolve)
        assertFalse(findPokemonById("dragonite")!!.copy(level = 100).canEvolve)
    }
}
