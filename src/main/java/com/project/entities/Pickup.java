package com.project.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.project.assets.PlaceholderGenerator;

/**
 * A collectible pickup that the player walks over to receive its effect.
 *
 * <p>Pickup types:
 * <ul>
 *   <li>{@link PickupType#HEART}    — Restores 1 HP (or full HP if already at max).</li>
 *   <li>{@link PickupType#EXP}      — Awards experience points toward the next level.</li>
 *   <li>{@link PickupType#MUTATION} — Dropped by bosses; triggers an upgrade pick.</li>
 * </ul>
 *
 * <p>The collection radius is slightly larger than the visual size to allow
 * "magnet"-style collection.
 */
public class Pickup extends GameObject {

    /** The type of this pickup. */
    public enum PickupType { HEART, EXP, MUTATION }

    private static final float HEART_SIZE    = 0.30f;
    private static final float EXP_SIZE      = 0.22f;
    private static final float MUTATION_SIZE = 0.40f;

    /** Radius within which the player auto-collects this pickup. */
    private static final float COLLECT_RADIUS = 0.8f;

    private final PickupType type;
    /** Amount of EXP to award (only relevant for EXP pickups). */
    private final int expAmount;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Pickup(AssetManager assetManager, float x, float z, PickupType type, int expAmount) {
        super(x, z, sizeFor(type));
        this.type      = type;
        this.expAmount = expAmount;

        ColorRGBA color = colorFor(type);
        String    label = labelFor(type);

        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager, label, sizeFor(type), sizeFor(type), color
        ));
    }

    /** Convenience constructor for HEART pickups. */
    public Pickup(AssetManager assetManager, float x, float z) {
        this(assetManager, x, z, PickupType.HEART, 0);
    }

    /** Convenience constructor for EXP pickups. */
    public Pickup(AssetManager assetManager, float x, float z, int expAmount) {
        this(assetManager, x, z, PickupType.EXP, expAmount);
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    @Override
    public void update(float tpf) { /* pickups are static */ }

    // ------------------------------------------------------------------
    // Collection
    // ------------------------------------------------------------------
    /**
     * @param playerX  player X position
     * @param playerZ  player Z position
     * @return {@code true} if the player is close enough to collect this pickup
     */
    public boolean canBeCollectedBy(float playerX, float playerZ) {
        if (!active) return false;
        float dx = position.x - playerX;
        float dz = position.z - playerZ;
        return (dx * dx + dz * dz) <= COLLECT_RADIUS * COLLECT_RADIUS;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private static float sizeFor(PickupType t) {
        return switch (t) {
            case HEART    -> HEART_SIZE;
            case EXP      -> EXP_SIZE;
            case MUTATION -> MUTATION_SIZE;
        };
    }

    private static ColorRGBA colorFor(PickupType t) {
        return switch (t) {
            case HEART    -> new ColorRGBA(1.0f, 0.2f, 0.3f, 1.0f); // red-pink
            case EXP      -> new ColorRGBA(0.3f, 1.0f, 0.4f, 1.0f); // green
            case MUTATION -> new ColorRGBA(0.8f, 0.3f, 1.0f, 1.0f); // purple
        };
    }

    private static String labelFor(PickupType t) {
        return switch (t) {
            case HEART    -> "HEART";
            case EXP      -> "EXP";
            case MUTATION -> "MUTATION";
        };
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public PickupType getPickupType() { return type; }
    public int        getExpAmount()  { return expAmount; }
}
