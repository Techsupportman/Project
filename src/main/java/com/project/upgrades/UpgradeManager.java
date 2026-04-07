package com.project.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages the full set of upgrade trees and the level-up selection process.
 *
 * <p>On level-up, the manager randomly selects 3–5 upgrades from across all
 * trees (limited to currently unlocked paths) and presents them to the player.
 * The player picks one; the manager applies it and optionally offers a
 * <em>Reroll</em> or <em>Delete</em> action (once per level).
 *
 * <p>Upgrade trees bundled:
 * <ol>
 *   <li>Damage — increases bullet damage</li>
 *   <li>Speed — increases fire rate</li>
 *   <li>Vitality — grants extra hearts</li>
 *   <li>Mobility — increases player movement speed</li>
 *   <li>Piercing — adds pierce / ricochet to bullets</li>
 *   <li>Black Hole — unlocks the Pocket Black Hole replay ability</li>
 * </ol>
 */
public class UpgradeManager {

    private final List<UpgradeTree> trees;
    private final Random random = new Random();

    /** Reroll uses remaining this level (1 per level). */
    private int rerollsLeft = 1;
    /** Delete uses remaining this level (1 per level). */
    private int deletesLeft = 1;

    // ------------------------------------------------------------------
    // Stat accumulators applied to the game at runtime
    // ------------------------------------------------------------------
    private float damageMult    = 1.0f;
    private float fireRateMult  = 1.0f;
    private int   bonusHearts   = 0;
    private float speedMult     = 1.0f;
    private int   bonusPierce   = 0;
    private int   bonusRicochet = 0;

    // ------------------------------------------------------------------
    // Pocket Black Hole state
    // ------------------------------------------------------------------
    private boolean pocketBlackHoleEnabled = false;
    /** Multiplier applied to the spew fire rate (higher = faster replay). */
    private float   blackHoleSpewRateMult  = 1.0f;
    /** Additional recording duration in seconds. */
    private float   blackHoleExtraRecordTime = 0f;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public UpgradeManager() {
        trees = buildDefaultTrees();
    }

    // ------------------------------------------------------------------
    // Level-up flow
    // ------------------------------------------------------------------
    /**
     * Generates a random set of 3–5 upgrade options from unlocked paths.
     * Should be called when the player levels up.
     *
     * @return list of selectable upgrades (3–5 options)
     */
    public List<Upgrade> generateChoices() {
        List<Upgrade> pool = new ArrayList<>();
        for (UpgradeTree tree : trees) {
            pool.addAll(tree.getAvailableUpgrades());
        }
        Collections.shuffle(pool, random);
        int count = Math.min(pool.size(), 3 + random.nextInt(3)); // 3, 4, or 5
        return pool.subList(0, Math.min(count, pool.size()));
    }

    /**
     * Applies the selected upgrade and advances any accumulated stat
     * modifiers.
     *
     * @param upgrade the upgrade the player chose
     */
    public void applyUpgrade(Upgrade upgrade) {
        upgrade.choose();
        applyStats(upgrade);
        rerollsLeft = 1;
        deletesLeft = 1;
    }

    /**
     * Rerolls the current upgrade offer (if rerolls remain).
     *
     * @return a fresh set of choices, or {@code null} if no rerolls left
     */
    public List<Upgrade> reroll() {
        if (rerollsLeft <= 0) return null;
        rerollsLeft--;
        return generateChoices();
    }

    /**
     * Deletes (skips) this level-up without choosing any upgrade
     * (if deletes remain).
     *
     * @return {@code true} if the delete was consumed
     */
    public boolean delete() {
        if (deletesLeft <= 0) return false;
        deletesLeft--;
        return true;
    }

    // ------------------------------------------------------------------
    // Reset (on game restart)
    // ------------------------------------------------------------------
    public void reset() {
        damageMult    = 1.0f;
        fireRateMult  = 1.0f;
        bonusHearts   = 0;
        speedMult     = 1.0f;
        bonusPierce   = 0;
        bonusRicochet = 0;
        rerollsLeft   = 1;
        deletesLeft   = 1;
        pocketBlackHoleEnabled  = false;
        blackHoleSpewRateMult   = 1.0f;
        blackHoleExtraRecordTime = 0f;
        for (UpgradeTree tree : trees) {
            for (Upgrade u : tree.getAllUpgrades()) {
                // Reset chosen state via reflection workaround isn't needed:
                // we just rebuild the trees on reset.
            }
        }
        trees.clear();
        trees.addAll(buildDefaultTrees());
    }

    // ------------------------------------------------------------------
    // Stat application
    // ------------------------------------------------------------------
    private void applyStats(Upgrade u) {
        switch (u.name) {
            // Damage tree
            case "Damage I", "Damage II", "Damage III",
                 "Crit Boost I", "Crit Boost II", "Overwhelm I", "Overwhelm II"
                    -> damageMult += u.magnitude;
            // Speed tree
            case "Fire Rate I", "Fire Rate II", "Fire Rate III",
                 "Rapid Fire I", "Rapid Fire II", "Burst I", "Burst II"
                    -> fireRateMult += u.magnitude;
            // Vitality tree
            case "Extra Heart I", "Extra Heart II", "Extra Heart III",
                 "Regen I", "Regen II", "Shield I", "Shield II"
                    -> bonusHearts += (int) u.magnitude;
            // Mobility tree
            case "Speed I", "Speed II", "Speed III",
                 "Dash I", "Dash II", "Agility I", "Agility II"
                    -> speedMult += u.magnitude;
            // Piercing tree
            case "Pierce I", "Pierce II", "Pierce III",
                 "Deep Pierce I", "Deep Pierce II", "Ricochet I", "Ricochet II"
                    -> {
                if (u.branch == 2) bonusRicochet += (int) u.magnitude;
                else               bonusPierce   += (int) u.magnitude;
            }
            // Black Hole tree
            case "Pocket Black Hole"     -> pocketBlackHoleEnabled   = true;
            case "Black Hole Accelerate" -> blackHoleSpewRateMult    += u.magnitude;
            case "Black Hole Extend"     -> blackHoleExtraRecordTime += u.magnitude;
            case "Black Hole Overdrive"  -> blackHoleSpewRateMult    += u.magnitude;
            default -> { /* unknown upgrade — no-op */ }
        }
    }

    // ------------------------------------------------------------------
    // Default tree definitions
    // ------------------------------------------------------------------
    private List<UpgradeTree> buildDefaultTrees() {
        List<UpgradeTree> result = new ArrayList<>();

        // --- Damage tree ---
        result.add(new UpgradeTree("Damage", List.of(
            new Upgrade(1, 0, 0, "Damage I",      "+10% bullet damage",            0.10f),
            new Upgrade(2, 1, 1, "Damage II",      "+15% bullet damage",            0.15f),
            new Upgrade(3, 1, 2, "Crit Boost I",   "Bullets sometimes crit +25%",   0.25f),
            new Upgrade(4, 2, 1, "Damage III",     "+20% bullet damage",            0.20f),
            new Upgrade(5, 2, 1, "Overwhelm I",    "Stun chance on hit",            0.10f),
            new Upgrade(6, 3, 2, "Crit Boost II",  "Crit chance +15%",              0.15f),
            new Upgrade(7, 3, 2, "Overwhelm II",   "Stun duration +0.3s",           0.30f)
        )));

        // --- Fire Rate tree ---
        result.add(new UpgradeTree("Fire Rate", List.of(
            new Upgrade(1, 0, 0, "Fire Rate I",    "+10% fire rate",                0.10f),
            new Upgrade(2, 1, 1, "Fire Rate II",   "+15% fire rate",                0.15f),
            new Upgrade(3, 1, 2, "Burst I",        "Fire in 3-shot bursts",         3f),
            new Upgrade(4, 2, 1, "Fire Rate III",  "+20% fire rate",                0.20f),
            new Upgrade(5, 2, 1, "Rapid Fire I",   "Hold fire for +5% rate",        0.05f),
            new Upgrade(6, 3, 2, "Burst II",       "Burst size +1",                 1f),
            new Upgrade(7, 3, 2, "Rapid Fire II",  "Hold fire for extra +5% rate",  0.05f)
        )));

        // --- Vitality tree ---
        result.add(new UpgradeTree("Vitality", List.of(
            new Upgrade(1, 0, 0, "Extra Heart I",  "+1 max heart",                  1f),
            new Upgrade(2, 1, 1, "Extra Heart II", "+1 max heart",                  1f),
            new Upgrade(3, 1, 2, "Regen I",        "Slow HP regen",                 1f),
            new Upgrade(4, 2, 1, "Extra Heart III","+1 max heart",                  1f),
            new Upgrade(5, 2, 1, "Shield I",       "Short invincibility on damage", 0.5f),
            new Upgrade(6, 3, 2, "Regen II",       "Faster HP regen",               2f),
            new Upgrade(7, 3, 2, "Shield II",      "Longer invincibility",          0.5f)
        )));

        // --- Mobility tree ---
        result.add(new UpgradeTree("Mobility", List.of(
            new Upgrade(1, 0, 0, "Speed I",        "+10% move speed",               0.10f),
            new Upgrade(2, 1, 1, "Speed II",       "+15% move speed",               0.15f),
            new Upgrade(3, 1, 2, "Agility I",      "Reduce enemy knockback",        1f),
            new Upgrade(4, 2, 1, "Speed III",      "+20% move speed",               0.20f),
            new Upgrade(5, 2, 1, "Dash I",         "Dash on double-tap",            1f),
            new Upgrade(6, 3, 2, "Agility II",     "+10% dodge window",             0.10f),
            new Upgrade(7, 3, 2, "Dash II",        "Dash cooldown −0.5s",          -0.5f)
        )));

        // --- Piercing tree ---
        result.add(new UpgradeTree("Piercing", List.of(
            new Upgrade(1, 0, 0, "Pierce I",       "+1 bullet pierce",              1f),
            new Upgrade(2, 1, 1, "Pierce II",      "+1 bullet pierce",              1f),
            new Upgrade(3, 1, 2, "Ricochet I",     "+1 wall bounce",                1f),
            new Upgrade(4, 2, 1, "Pierce III",     "+2 bullet pierce",              2f),
            new Upgrade(5, 2, 1, "Deep Pierce I",  "+10% pierce damage",            0.10f),
            new Upgrade(6, 3, 2, "Ricochet II",    "+1 wall bounce",                1f),
            new Upgrade(7, 3, 2, "Deep Pierce II", "Bounced bullets deal +15% dmg", 0.15f)
        )));

        // --- Pocket Black Hole tree ---
        result.add(new UpgradeTree("Black Hole", List.of(
            new Upgrade(1, 0, 0, "Pocket Black Hole",     "Record 10s of shots & replay them!",    1f),
            new Upgrade(2, 1, 1, "Black Hole Accelerate", "Replay fire rate +50%",                 0.50f),
            new Upgrade(3, 1, 2, "Black Hole Extend",     "Record for +5 extra seconds",           5f),
            new Upgrade(4, 2, 1, "Black Hole Overdrive",  "Replay fire rate +50% more",            0.50f),
            new Upgrade(5, 2, 1, "Black Hole Depth",      "+1 pierce on replayed shots",           1f),
            new Upgrade(6, 3, 2, "Black Hole Extend II",  "Record for +5 extra seconds",           5f),
            new Upgrade(7, 3, 2, "Black Hole Surge",      "Replay fire rate +75% more",            0.75f)
        )));

        return result;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float getDamageMult()   { return damageMult;    }
    public float getFireRateMult() { return fireRateMult;  }
    public int   getBonusHearts()  { return bonusHearts;   }
    public float getSpeedMult()    { return speedMult;     }
    public int   getBonusPierce()  { return bonusPierce;   }
    public int   getBonusRicochet(){ return bonusRicochet; }
    public int   getRerollsLeft()  { return rerollsLeft;   }
    public int   getDeletesLeft()  { return deletesLeft;   }
    public List<UpgradeTree> getTrees() { return trees; }

    // ------------------------------------------------------------------
    // Pocket Black Hole accessors
    // ------------------------------------------------------------------
    /** @return {@code true} once the Pocket Black Hole upgrade has been picked. */
    public boolean hasPocketBlackHole()      { return pocketBlackHoleEnabled;  }
    /** @return the total recording window (base 10s + any extensions). */
    public float   getBlackHoleRecordTime()  { return 10f + blackHoleExtraRecordTime; }
    /** @return multiplier applied to the replay spew rate (>1 = faster). */
    public float   getBlackHoleSpewRateMult(){ return blackHoleSpewRateMult; }
}
