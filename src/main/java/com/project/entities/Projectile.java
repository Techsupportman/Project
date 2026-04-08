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
    private float dirX;
    private float dirZ;
    private final float speed;

    // ------------------------------------------------------------------
    // Combat stats
    // ------------------------------------------------------------------
    private final float damage;
    private int   pierceRemaining;
    private int   ricochetRemaining;

    /** Maximum flight distance before the bullet despawns (prevents infinite travel). */
    private static final float MAX_RANGE = 80f;
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
     * @param ricochet      number of enemy bounces allowed
     * @param color         visual colour of the bullet placeholder
     */
    public Projectile(AssetManager assetManager,
                      float x, float z,
                      float dirX, float dirZ,
                      float speed, float damage,
                      float bulletSize,
                      int   pierce, int ricochet,
                      ColorRGBA color) {
        super(x, z, bulletSize);
        this.dirX              = dirX;
        this.dirZ              = dirZ;
        this.speed             = speed;
        this.damage            = damage;
        this.pierceRemaining   = pierce;
        this.ricochetRemaining = ricochet;

        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager, "BULLET", bulletSize, bulletSize, color));
    }

    /**
     * Creates a player bullet with the default bright-yellow colour.
     */
    public Projectile(AssetManager assetManager,
                      float x, float z,
                      float dirX, float dirZ,
                      float speed, float damage,
                      float bulletSize,
                      int   pierce, int ricochet) {
        this(assetManager, x, z, dirX, dirZ, speed, damage, bulletSize, pierce, ricochet,
             new ColorRGBA(1.0f, 1.0f, 0.2f, 1.0f)); // bright yellow
    }

    /**
     * Legacy constructor kept for backward compatibility.
     * The arenaHalfW and arenaHalfH parameters are no longer used (wall-bounce
     * is replaced by enemy ricochet), but the signature is preserved so
     * existing callers compile without change.
     */
    public Projectile(AssetManager assetManager,
                      float x, float z,
                      float dirX, float dirZ,
                      float speed, float damage,
                      float bulletSize,
                      int   pierce, int ricochet,
                      float arenaHalfW, float arenaHalfH) {
        this(assetManager, x, z, dirX, dirZ, speed, damage, bulletSize, pierce, ricochet);
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

        setPosition(newX, newZ);
    }

    // ------------------------------------------------------------------
    // Hit processing
    // ------------------------------------------------------------------
    /**
     * Called when this projectile hits an enemy.
     *
     * <p>If the projectile has ricochet remaining it does <em>not</em> consume
     * pierce — instead the caller is expected to call {@link #applyRicochet}
     * with the reflected direction.  Otherwise, pierce is decremented and the
     * projectile deactivates when pierce runs out.
     *
     * @return damage to deal to the enemy
     */
    public float onHit() {
        if (ricochetRemaining > 0) {
            // Ricochet takes priority — direction will be updated by caller via applyRicochet()
            return damage;
        }
        pierceRemaining--;
        if (pierceRemaining < 0) {
            active = false;
        }
        return damage;
    }

    /**
     * Reflects the projectile's direction and decrements the ricochet counter.
     * The projectile is also nudged forward by one bullet-size so that it
     * immediately separates from the enemy it just bounced off.
     *
     * @param newDirX normalised reflected X direction
     * @param newDirZ normalised reflected Z direction
     */
    public void applyRicochet(float newDirX, float newDirZ) {
        this.dirX = newDirX;
        this.dirZ = newDirZ;
        this.ricochetRemaining--;
        // Nudge forward so the projectile doesn't instantly re-hit the same enemy
        setPosition(position.x + newDirX * size * 2f,
                    position.z + newDirZ * size * 2f);
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float getDirX()  { return dirX; }
    public float getDirZ()  { return dirZ; }
    public float getDamage(){ return damage; }
    public int   getRicochetRemaining() { return ricochetRemaining; }

    // Legacy reflection helpers — kept for any existing callers
    public Vector2D getReflectedDirX_Wall()  { return new Vector2D(-dirX,  dirZ); }
    public Vector2D getReflectedDirZ_Wall()  { return new Vector2D( dirX, -dirZ); }
}
