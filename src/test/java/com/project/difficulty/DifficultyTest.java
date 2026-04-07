package com.project.difficulty;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DifficultyTest {

    @Test
    void easyHasHighestStartingHearts() {
        assertTrue(Difficulty.EASY.startingHearts > Difficulty.NORMAL.startingHearts,
                "Easy should give more starting hearts than Normal");
    }

    @Test
    void nightmareHasHighestScoreMultiplier() {
        assertTrue(Difficulty.NIGHTMARE.scoreMultiplier > Difficulty.HARD.scoreMultiplier);
        assertTrue(Difficulty.HARD.scoreMultiplier > Difficulty.NORMAL.scoreMultiplier);
    }

    @Test
    void easyHasLowestScoreMultiplier() {
        assertTrue(Difficulty.EASY.scoreMultiplier < Difficulty.NORMAL.scoreMultiplier);
    }

    @Test
    void nightmareHasHighestEnemyHealth() {
        assertTrue(Difficulty.NIGHTMARE.enemyHealthMult > Difficulty.NORMAL.enemyHealthMult);
    }

    @Test
    void easyHasLowestSpawnRate() {
        assertTrue(Difficulty.EASY.spawnRateMult < Difficulty.HARD.spawnRateMult);
    }

    @Test
    void allDifficultiesHavePositiveValues() {
        for (Difficulty d : Difficulty.values()) {
            assertTrue(d.startingHearts > 0,    d + ": startingHearts must be > 0");
            assertTrue(d.enemyHealthMult > 0f,  d + ": enemyHealthMult must be > 0");
            assertTrue(d.enemyDamageMult > 0f,  d + ": enemyDamageMult must be > 0");
            assertTrue(d.spawnRateMult > 0f,    d + ": spawnRateMult must be > 0");
            assertTrue(d.scoreMultiplier > 0f,  d + ": scoreMultiplier must be > 0");
            assertTrue(d.expGainMult > 0f,      d + ": expGainMult must be > 0");
        }
    }
}
