package com.project.systems;

import com.jme3.asset.AssetManager;
import com.project.difficulty.Difficulty;
import com.project.entities.Enemy;
import com.project.entities.EnemyType;
import com.project.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Time-based enemy spawn manager.
 *
 * <p>Unlike the original wave system, enemies spawn <em>continuously</em>,
 * with rate and type determined by elapsed game time.  The schedule is:
 *
 * <ul>
 *   <li>Base spawn interval decreases as time passes (enemies arrive faster).</li>
 *   <li>New {@link EnemyType} variants unlock at the time thresholds defined
 *       in each type's {@code spawnAfterSeconds} field.</li>
 *   <li><b>Mini-bosses</b> spawn occasionally instead of a normal cluster.</li>
 *   <li><b>Bosses</b> spawn at 5-minute intervals (300 s, 600 s, …).
 *       The final boss replaces the 20-minute or 30-minute marker.</li>
 * </ul>
 *
 * <p>During a boss encounter ({@link #isBossActive()}) the arena is "sealed"
 * and no standard enemies spawn until the boss is defeated.
 */
public class SpawnManager {

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------
    private float elapsedSeconds   = 0f;
    private float spawnTimer       = 0f;
    private int   bossIndex        = 0;        // which boss is next
    private boolean bossActive     = false;
    private final Random random    = new Random();
    private final Difficulty difficulty;

    /** Boss spawn times in real seconds (every 5 minutes). */
    private static final float[] BOSS_SPAWN_TIMES = { 300f, 600f, 900f, 1200f, 1800f };

    /** Mini-boss: every ~90 seconds on average. */
    private static final float MINI_BOSS_INTERVAL = 90f;
    private float nextMiniBossTime = MINI_BOSS_INTERVAL;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SpawnManager(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    // ------------------------------------------------------------------
    // Per-frame update
    // ------------------------------------------------------------------
    /**
     * Advances the spawn timers and returns any enemies that should be added
     * to the scene this frame.
     *
     * @param tpf          seconds since last frame
     * @param assetManager jME asset manager for enemy construction
     * @param currentCount number of enemies currently alive (throttle cap)
     * @return list of newly spawned enemies (may be empty)
     */
    public List<Enemy> update(float tpf, AssetManager assetManager, int currentCount) {
        List<Enemy> spawned = new ArrayList<>();
        elapsedSeconds += tpf;

        // Don't spawn standard enemies while a boss is alive
        if (bossActive) return spawned;

        // --- Boss check ---
        if (bossIndex < BOSS_SPAWN_TIMES.length
                && elapsedSeconds >= BOSS_SPAWN_TIMES[bossIndex]) {
            EnemyType bossType = bossTypeAt(bossIndex);
            spawned.add(spawnAt(assetManager, bossType, 0f, 0f)); // boss spawns at center
            bossActive = true;
            bossIndex++;
            return spawned; // only spawn the boss this tick
        }

        // --- Mini-boss check ---
        if (elapsedSeconds >= nextMiniBossTime) {
            nextMiniBossTime = elapsedSeconds + MINI_BOSS_INTERVAL;
            EnemyType mbType = random.nextBoolean()
                    ? EnemyType.MINI_BOSS_ALPHA
                    : EnemyType.MINI_BOSS_BETA;
            if (elapsedSeconds >= mbType.spawnAfterSeconds) {
                float[] pos = randomEdgePosition();
                spawned.add(spawnAt(assetManager, mbType, pos[0], pos[1]));
                return spawned;
            }
        }

        // --- Standard spawning ---
        if (currentCount >= Constants.MAX_ENEMIES_ALIVE) return spawned;

        float baseInterval = currentSpawnInterval();
        spawnTimer += tpf;
        while (spawnTimer >= baseInterval && currentCount + spawned.size() < Constants.MAX_ENEMIES_ALIVE) {
            spawnTimer -= baseInterval;
            EnemyType type = randomEnemyType();
            float[] pos = randomEdgePosition();
            spawned.add(spawnAt(assetManager, type, pos[0], pos[1]));
        }

        return spawned;
    }

    /**
     * Called when the current boss is defeated.  Resumes standard spawning.
     */
    public void onBossDefeated() {
        bossActive = false;
    }

    // ------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------
    public void reset() {
        elapsedSeconds   = 0f;
        spawnTimer       = 0f;
        bossIndex        = 0;
        bossActive       = false;
        nextMiniBossTime = MINI_BOSS_INTERVAL;
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------
    /**
     * Current base spawn interval in seconds.  Starts at
     * {@link Constants#SPAWN_INTERVAL_INITIAL} and shrinks toward
     * {@link Constants#SPAWN_INTERVAL_MIN} over time.
     */
    private float currentSpawnInterval() {
        float t = elapsedSeconds / Constants.SPAWN_RAMP_DURATION;
        t = Math.min(1f, t);
        float interval = Constants.SPAWN_INTERVAL_INITIAL
                - t * (Constants.SPAWN_INTERVAL_INITIAL - Constants.SPAWN_INTERVAL_MIN);
        return interval / difficulty.spawnRateMult;
    }

    /** Picks a random standard enemy type that has unlocked by the current time. */
    private EnemyType randomEnemyType() {
        EnemyType[] standards = {
            EnemyType.BASIC, EnemyType.RUNNER, EnemyType.TANK,
            EnemyType.SHOOTER, EnemyType.SWARM, EnemyType.BRUISER,
            EnemyType.SPECTER, EnemyType.ARTILLERY
        };
        List<EnemyType> available = new ArrayList<>();
        for (EnemyType t : standards) {
            if (elapsedSeconds >= t.spawnAfterSeconds) {
                available.add(t);
            }
        }
        if (available.isEmpty()) return EnemyType.BASIC;
        return available.get(random.nextInt(available.size()));
    }

    /** Returns the {@link EnemyType} for boss number {@code index} (0-based). */
    private EnemyType bossTypeAt(int index) {
        EnemyType[] bosses = {
            EnemyType.BOSS_1, EnemyType.BOSS_2, EnemyType.BOSS_3,
            EnemyType.BOSS_4, EnemyType.FINAL_BOSS
        };
        return index < bosses.length ? bosses[index] : EnemyType.FINAL_BOSS;
    }

    private Enemy spawnAt(AssetManager assetManager, EnemyType type, float x, float z) {
        return new Enemy(assetManager, x, z, type, difficulty);
    }

    /** Returns a random position along one of the four arena edges. */
    private float[] randomEdgePosition() {
        float hw = Constants.LEVEL_HALF_WIDTH  - 0.5f;
        float hh = Constants.LEVEL_HALF_HEIGHT - 0.5f;
        float x, z;
        switch (random.nextInt(4)) {
            case 0  -> { x = -hw + random.nextFloat() * 2f * hw; z = -hh; }
            case 1  -> { x = -hw + random.nextFloat() * 2f * hw; z =  hh; }
            case 2  -> { x = -hw; z = -hh + random.nextFloat() * 2f * hh; }
            default -> { x =  hw; z = -hh + random.nextFloat() * 2f * hh; }
        }
        return new float[]{ x, z };
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float   getElapsedSeconds() { return elapsedSeconds; }
    public boolean isBossActive()      { return bossActive; }
    public int     getBossIndex()      { return bossIndex; }
}
