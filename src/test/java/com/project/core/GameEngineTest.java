package com.project.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
    }

    @Test
    void initialStateIsWaitingForFirstWave() {
        assertTrue(engine.isWaitingForWave());
        assertEquals(0, engine.getCurrentWave());
        assertEquals(0, engine.getScore());
    }

    @Test
    void tickCountsDownAndFiresStartNextWave() {
        // First tick should eventually return START_NEXT_WAVE once countdown elapses
        GameEngine.Event event = GameEngine.Event.NONE;
        float elapsed = 0f;
        while (event != GameEngine.Event.START_NEXT_WAVE && elapsed < 10f) {
            event = engine.tick(0.1f, 0);
            elapsed += 0.1f;
        }
        assertEquals(GameEngine.Event.START_NEXT_WAVE, event);
    }

    @Test
    void advanceWaveIncrementsCounter() {
        engine.advanceWave();
        assertEquals(1, engine.getCurrentWave());
        engine.advanceWave();
        assertEquals(2, engine.getCurrentWave());
    }

    @Test
    void recordKillIncreasesScore() {
        engine.recordKill();
        assertEquals(com.project.utils.Constants.ENEMY_SCORE_VALUE, engine.getScore());
        engine.recordKill();
        assertEquals(com.project.utils.Constants.ENEMY_SCORE_VALUE * 2, engine.getScore());
    }

    @Test
    void resetClearsAllState() {
        engine.advanceWave();
        engine.recordKill();
        engine.reset();

        assertEquals(0, engine.getCurrentWave());
        assertEquals(0, engine.getScore());
        assertTrue(engine.isWaitingForWave());
    }

    @Test
    void enemiesForNextWaveScalesWithWave() {
        // Before any advanceWave, currentWave == 0
        int waveZeroCount = engine.enemiesForNextWave();
        engine.advanceWave();   // wave 1
        int waveOneCount = engine.enemiesForNextWave();
        assertTrue(waveOneCount > waveZeroCount,
                "Enemy count should increase each wave");
    }

    @Test
    void waveClearedEventWhenEnemiesReachZero() {
        // Simulate: advance past START_NEXT_WAVE first
        GameEngine.Event event = GameEngine.Event.NONE;
        while (event != GameEngine.Event.START_NEXT_WAVE) {
            event = engine.tick(0.1f, 0);
        }
        engine.advanceWave();
        // Now wave is active; tick with 0 enemies should trigger WAVE_CLEARED
        event = engine.tick(0.05f, 0);
        assertEquals(GameEngine.Event.WAVE_CLEARED, event);
    }
}
