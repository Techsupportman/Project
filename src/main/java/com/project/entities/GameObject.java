package com.project.entities;

import com.jme3.scene.Node;
import com.project.utils.Vector2D;

/**
 * Base class for every object that exists in the game world (player, enemies,
 * projectiles, pick-ups, etc.).
 *
 * <p>A {@code GameObject} owns a jMonkeyEngine {@link Node} that represents its
 * visual presence in the scene graph, and a {@link Vector2D} position that maps
 * to the XZ plane of the 3-D world (Y = 0).
 *
 * <p>Subclasses implement {@link #update(float)} to advance their own logic
 * each game tick.
 */
public abstract class GameObject {

    /** Scene-graph node — attach child geometries/nodes to this. */
    protected final Node node;

    /** World position in the XZ plane. */
    protected final Vector2D position;

    /** Collision half-extent (square AABB side = 2 × size). */
    protected final float size;

    /** Whether this object should still be processed and rendered. */
    protected boolean active;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    protected GameObject(float x, float z, float size) {
        this.position = new Vector2D(x, z);
        this.size     = size;
        this.active   = true;
        this.node     = new Node(getClass().getSimpleName());
        node.setLocalTranslation(x, 0f, z);
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    /** Called once per frame while the game is in the PLAYING state. */
    public abstract void update(float tpf);

    // ------------------------------------------------------------------
    // Position
    // ------------------------------------------------------------------
    /**
     * Moves the object to the given XZ coordinates and synchronises the
     * scene-graph node translation.
     */
    public void setPosition(float x, float z) {
        position.set(x, z);
        node.setLocalTranslation(x, 0f, z);
    }

    // ------------------------------------------------------------------
    // Collision
    // ------------------------------------------------------------------
    /**
     * Axis-aligned bounding-box collision test (square, in the XZ plane).
     *
     * @param other the other game object to test against
     * @return      {@code true} if the two AABBs overlap
     */
    public boolean collidesWith(GameObject other) {
        float dx      = Math.abs(position.x - other.position.x);
        float dz      = Math.abs(position.z - other.position.z);
        float minDist = size + other.size;
        return dx < minDist && dz < minDist;
    }

    // ------------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------------
    public Node     getNode()     { return node; }
    public Vector2D getPosition() { return position; }
    public float    getSize()     { return size; }
    public boolean  isActive()    { return active; }
    public void     setActive(boolean active) { this.active = active; }
}
