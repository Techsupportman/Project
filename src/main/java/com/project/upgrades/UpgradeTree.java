package com.project.upgrades;

import java.util.ArrayList;
import java.util.List;

/**
 * A 7-node upgrade tree with the following shape:
 * <pre>
 *              [Root]
 *             /      \
 *          [A1]      [B1]
 *          /  \      /  \
 *        [A2][A3]  [B2][B3]
 * </pre>
 *
 * <p>The player can only pick upgrades on a branch they have already started.
 * Specifically:
 * <ul>
 *   <li>Root (id=1) is always available.</li>
 *   <li>A1 (id=2) and B1 (id=3) require the root to be chosen.</li>
 *   <li>A2/A3 (id=4,5) require A1; B2/B3 (id=6,7) require B1.</li>
 *   <li>Once A1 is chosen the player <em>cannot</em> pick from the B branch,
 *       and vice-versa (exclusive branching).</li>
 * </ul>
 */
public class UpgradeTree {

    public final String     treeName;
    private final List<Upgrade> upgrades;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    /**
     * @param treeName  display name of this upgrade category
     * @param upgrades  exactly 7 {@link Upgrade} nodes forming the tree
     */
    public UpgradeTree(String treeName, List<Upgrade> upgrades) {
        this.treeName = treeName;
        this.upgrades = new ArrayList<>(upgrades);
    }

    // ------------------------------------------------------------------
    // Query helpers
    // ------------------------------------------------------------------
    /** Returns all upgrades available for selection right now. */
    public List<Upgrade> getAvailableUpgrades() {
        List<Upgrade> available = new ArrayList<>();
        for (Upgrade u : upgrades) {
            if (u.isChosen()) continue;
            if (isUnlocked(u)) {
                available.add(u);
            }
        }
        return available;
    }

    /**
     * Determines whether an upgrade's prerequisite has been met and the
     * opposite branch has not been locked out.
     */
    private boolean isUnlocked(Upgrade u) {
        // Root is always available
        if (u.parentId == 0) return true;

        // Check parent was chosen
        Upgrade parent = findById(u.parentId);
        if (parent == null || !parent.isChosen()) return false;

        // Branch lock-out: if the other tier-1 branch is chosen, block this one
        if (u.branch == 1 && isBranchChosen(2)) return false;
        if (u.branch == 2 && isBranchChosen(1)) return false;

        return true;
    }

    /** @return {@code true} if any upgrade on the given branch has been chosen. */
    private boolean isBranchChosen(int branch) {
        for (Upgrade u : upgrades) {
            if (u.branch == branch && u.isChosen()) return true;
        }
        return false;
    }

    private Upgrade findById(int id) {
        for (Upgrade u : upgrades) {
            if (u.id == id) return u;
        }
        return null;
    }

    /** Selects the upgrade with the given id. */
    public void choose(int id) {
        Upgrade u = findById(id);
        if (u != null) u.choose();
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public List<Upgrade> getAllUpgrades() { return upgrades; }
}
