package com.project.difficulty;

/**
 * Game difficulty settings that control player starting HP, enemy strength,
 * spawn rates, and score multipliers — matching the Disfigure difficulty tiers.
 *
 * <ul>
 *   <li><b>EASY</b>     — Start with 6 HP, weaker enemies, 0.5× score</li>
 *   <li><b>NORMAL</b>   — Base difficulty, 3 HP, 1× score</li>
 *   <li><b>HARD</b>     — Faster spawns, stronger bosses, 2× score</li>
 *   <li><b>NIGHTMARE</b>— Very hard bosses, reduced EXP gain, 3× score</li>
 * </ul>
 */
public enum Difficulty {

    EASY(
            "Easy",
            6,          // startingHearts
            0.7f,       // enemyHealthMult
            0.7f,       // enemyDamageMult
            0.8f,       // spawnRateMult  (lower = slower spawns)
            0.5f,       // scoreMultiplier
            1.2f        // expGainMult
    ),
    NORMAL(
            "Normal",
            3,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f
    ),
    HARD(
            "Hard",
            3,
            1.3f,
            1.3f,
            1.4f,       // faster spawns
            2.0f,
            0.85f
    ),
    NIGHTMARE(
            "Nightmare",
            3,
            1.8f,
            1.8f,
            1.6f,
            3.0f,
            0.6f        // reduced EXP gain
    );

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    public final String displayName;
    /** Number of hearts the player starts with. */
    public final int    startingHearts;
    /** Multiplier applied to all enemy base health values. */
    public final float  enemyHealthMult;
    /** Multiplier applied to all enemy damage values. */
    public final float  enemyDamageMult;
    /** Multiplier applied to enemy spawn rates (>1 = more frequent). */
    public final float  spawnRateMult;
    /** Final score is multiplied by this value. */
    public final float  scoreMultiplier;
    /** Multiplier applied to EXP gain per kill. */
    public final float  expGainMult;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    Difficulty(String displayName,
               int startingHearts,
               float enemyHealthMult,
               float enemyDamageMult,
               float spawnRateMult,
               float scoreMultiplier,
               float expGainMult) {
        this.displayName     = displayName;
        this.startingHearts  = startingHearts;
        this.enemyHealthMult = enemyHealthMult;
        this.enemyDamageMult = enemyDamageMult;
        this.spawnRateMult   = spawnRateMult;
        this.scoreMultiplier = scoreMultiplier;
        this.expGainMult     = expGainMult;
    }
}
