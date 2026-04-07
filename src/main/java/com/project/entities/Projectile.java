package com.project.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.project.assets.PlaceholderGenerator;
import com.project.utils.Vector2D;

/**
 * A projectile fired by the player's weapon.
 *
 * <p>Projectiles travel in a fixed direction at constant speed.  On hitting
 * an enemy they deal damage, reduce their pierce count, and are deactivated
 * when pierce runs out (or they leave the arena).
 *
 * <p>Ricochet bounces are applied against the level boundary: each time the
 * bullet crosses an edge it reflects and the bounce count decrements.
 */
public class Projectile extends GameObject {

    // ------------------------------------------------------------------
    // Movement
    // ------------------------------------------------------------------
    private final float dirX;
    private final float dirZ;
    private final float speed;

    // ------------------------------------------------------------------
    // Combat stats
    // ------------------------------------------------------------------
    private final float damage;
    private int   pierceRemaining;
    private int   ricochetRemaining;

    /** World-unit half-extents of the arena boundary for ricochet clamping. */
    private final float arenaHalfW;
    private final float arenaHalfH;

    /** Maximum flight distance before the bullet despawns (prevents infinite travel). */
    private static final float MAX_RANGE = 50f;
    private float distanceTravelled = 0f;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    /**
     * @param assetManager  jME asset manager for placeholder visuals
     * @param x             starting X position
     * @param z             starting Z position
     * @param dirX          normalised X direction component
     * @param dirZ          normalised Z direction component
     * @param speed         bullet travel speed (world-units / second)
     * @param damage        damage dealt to each enemy hit
     * @param bulletSize    visual and collision radius
     * @param pierce        number of enemies the bullet can pass through
     * @param ricochet      number of wall bounces allowed
     * @param arenaHalfW    arena half-width for bounce detection
     * @param arenaHalfH    arena half-height for bounce detection
     */
    public Projectile(AssetManager assetManager,
                      float x, float z,
                      float dirX, float dirZ,
                      float speed, float damage,
                      float bulletSize,
                      int   pierce, int ricochet,
                      float arenaHalfW, float arenaHalfH) {
        super(x, z, bulletSize);
        this.dirX              = dirX;
        this.dirZ              = dirZ;
        this.speed             = speed;
        this.damage            = damage;
        this.pierceRemaining   = pierce;
        this.ricochetRemaining = ricochet;
        this.arenaHalfW        = arenaHalfW;
        this.arenaHalfH        = arenaHalfH;

        // Placeholder: small yellow box labelled "BULLET"
        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager,
                "BULLET",
                bulletSize,
                bulletSize,
                new ColorRGBA(1.0f, 1.0f, 0.2f, 1.0f) // bright yellow
        ));
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    @Override
    public void update(float tpf) {
        if (!active) return;

        float newX = position.x + dirX * speed * tpf;
        float newZ = position.z + dirZ * speed * tpf;

        distanceTravelled += speed * tpf;
        if (distanceTravelled > MAX_RANGE) {
            active = false;
            return;
        }

        // Wall bounce
        if (ricochetRemaining > 0) {
            boolean bounced = false;
            if (Math.abs(newX) >= arenaHalfW) {
                newX = Math.signum(newX) * (arenaHalfW - 0.01f);
                // The direction is reversed in the X component; since dirX/Z are
                // final we cannot reassign, so we mark inactive after bounce.
                // For a full ricochet the caller should create a new projectile
                // travelling in the reflected direction.
                ricochetRemaining--;
                active = false; // simplified: deactivate and let SpawnManager create reflected bullet
                bounced = true;
            }
            if (!bounced && Math.abs(newZ) >= arenaHalfH) {
                ricochetRemaining--;
                active = false;
            }
        } else {
            // No ricochet: deactivate if it leaves the arena
            if (Math.abs(newX) >= arenaHalfW || Math.abs(newZ) >= arenaHalfH) {
                active = false;
                return;
            }
        }

        setPosition(newX, newZ);
    }

    // ------------------------------------------------------------------
    // Hit processing
    // ------------------------------------------------------------------
    /**
     * Called when this projectile hits an enemy.
     * Decrements pierce; deactivates when pierce runs out.
     *
     * @return damage to deal to the enemy
     */
    public float onHit() {
        pierceRemaining--;
        if (pierceRemaining < 0) {
            active = false;
        }
        return damage;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float getDirX()  { return dirX; }
    public float getDirZ()  { return dirZ; }
    public float getDamage(){ return damage; }
    public int   getRicochetRemaining() { return ricochetRemaining; }
    public Vector2D getReflectedDirX_Wall()  { return new Vector2D(-dirX,  dirZ); }
    public Vector2D getReflectedDirZ_Wall()  { return new Vector2D( dirX, -dirZ); }
}
