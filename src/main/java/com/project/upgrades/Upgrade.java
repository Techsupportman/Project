package com.project.upgrades;

/**
 * Represents a single node in an {@link UpgradeTree}.
 *
 * <p>Each upgrade has:
 * <ul>
 *   <li>A human-readable name and description shown in the level-up menu.</li>
 *   <li>A parent ID (0 = root) indicating which upgrade must be chosen first.</li>
 *   <li>A branch ID (1 or 2) when the tree forks after the root.</li>
 *   <li>Whether the upgrade has already been selected by the player.</li>
 * </ul>
 */
public class Upgrade {

    /** Unique identifier within a tree (1-based; 0 = none/root sentinel). */
    public final int    id;
    /** Id of the required parent upgrade (0 = always available). */
    public final int    parentId;
    /** Branch this upgrade belongs to (0 = root, 1 = branch A, 2 = branch B). */
    public final int    branch;

    public final String name;
    public final String description;

    /** Stat modifier — meaning depends on the tree type. */
    public final float  magnitude;

    private boolean chosen = false;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Upgrade(int id, int parentId, int branch,
                   String name, String description, float magnitude) {
        this.id          = id;
        this.parentId    = parentId;
        this.branch      = branch;
        this.name        = name;
        this.description = description;
        this.magnitude   = magnitude;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public boolean isChosen() { return chosen; }
    public void    choose()   { chosen = true; }

    @Override
    public String toString() {
        return name + " — " + description;
    }
}
