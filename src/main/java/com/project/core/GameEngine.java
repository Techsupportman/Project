package com.project.core;

import com.project.difficulty.Difficulty;
import com.project.entities.EnemyType;
import com.project.utils.Constants;

/**
 * Encapsulates high-level game-logic state: time, score, level, credits,
 * and wave/spawn bookkeeping.
 *
 * <h3>Changes from original MVP</h3>
 * <ul>
 *   <li>Score is scaled by the chosen {@link Difficulty#scoreMultiplier}.</li>
 *   <li>Credits accumulate as {@code floor(score × 0.1)}.</li>
 *   <li>Elapsed game-time is tracked so the HUD can display it and
 *       {@link com.project.systems.SpawnManager} can schedule bosses.</li>
 *   <li>The wave-based event system is retained for backward compatibility
 *       with existing tests, but the main game now uses
 *       {@link com.project.systems.SpawnManager} for actual spawning.</li>
 * </ul>
 */
public class GameEngine {

    // ------------------------------------------------------------------
    // Difficulty
    // ------------------------------------------------------------------
    private Difficulty difficulty = Difficulty.NORMAL;

    // ------------------------------------------------------------------
    // Wave state (legacy — kept for backward compatibility)
    // ------------------------------------------------------------------
    private int   currentWave       = 0;
    private float waveCountdown     = 0f;
    private boolean waitingForWave  = true;

    // ------------------------------------------------------------------
    // Score & Credits
    // ------------------------------------------------------------------
    private int   score   = 0;
    private int   credits = 0;

    // ------------------------------------------------------------------
    // Time
    // ------------------------------------------------------------------
    private float elapsedSeconds = 0f;

    // ------------------------------------------------------------------
    // Events returned from tick()
    // ------------------------------------------------------------------
    public enum Event {
        /** Nothing significant happened this frame. */
        NONE,
        /** All enemies in the current wave have been killed — start countdown. */
        WAVE_CLEARED,
        /** The countdown has elapsed — spawn the next wave. */
        START_NEXT_WAVE
    }

    // ------------------------------------------------------------------
    // Per-frame update
    // ------------------------------------------------------------------
    /**
     * Advances the engine by one frame.
     *
     * @param tpf         seconds elapsed since last frame
     * @param enemiesLeft number of currently active enemies in the scene
     * @return an {@link Event} the caller should act upon
     */
    public Event tick(float tpf, int enemiesLeft) {
        elapsedSeconds += tpf;

        if (waitingForWave) {
            waveCountdown -= tpf;
            if (waveCountdown <= 0f) {
                waitingForWave = false;
                return Event.START_NEXT_WAVE;
            }
        } else {
            if (enemiesLeft == 0 && currentWave > 0) {
                waitingForWave = true;
                waveCountdown  = Constants.WAVE_DELAY;
                return Event.WAVE_CLEARED;
            }
        }
        return Event.NONE;
    }

    // ------------------------------------------------------------------
    // Wave management (legacy)
    // ------------------------------------------------------------------
    public void advanceWave() { currentWave++; }

    public int enemiesForNextWave() {
        return Constants.BASE_ENEMIES_PER_WAVE
                + currentWave * Constants.ENEMIES_INCREASE_PER_WAVE;
    }

    public float speedForNextWave() {
        return Constants.ENEMY_SPEED_BASE
                + currentWave * Constants.SPEED_INCREASE_PER_WAVE;
    }

    // ------------------------------------------------------------------
    // Score & Credits
    // ------------------------------------------------------------------
    /**
     * Records a kill, awarding score and credits based on the enemy type and
     * the current difficulty multiplier.
     *
     * @param type enemy type killed
     */
    public void recordKill(EnemyType type) {
        int raw = (int) (type.scoreValue * difficulty.scoreMultiplier);
        score   += raw;
        credits  = (int) (score * Constants.CREDITS_PER_SCORE);
    }

    /** Awards the legacy fixed score value (used by tests). */
    public void recordKill() {
        int raw = (int) (Constants.ENEMY_SCORE_VALUE * difficulty.scoreMultiplier);
        score   += raw;
        credits  = (int) (score * Constants.CREDITS_PER_SCORE);
    }

    // ------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------
    public void reset() {
        currentWave    = 0;
        waveCountdown  = 0f;
        waitingForWave = true;
        score          = 0;
        credits        = 0;
        elapsedSeconds = 0f;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public int        getCurrentWave()   { return currentWave;    }
    public int        getScore()         { return score;          }
    public int        getCredits()       { return credits;        }
    public float      getWaveCountdown() { return waveCountdown;  }
    public boolean    isWaitingForWave() { return waitingForWave; }
    public float      getElapsedSeconds(){ return elapsedSeconds; }
    public Difficulty getDifficulty()    { return difficulty;     }

    public void setDifficulty(Difficulty d) { this.difficulty = d; }
}
