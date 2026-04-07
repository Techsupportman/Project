package com.project.utils;

/**
 * Lightweight 2-component vector used for positions and directions in the XZ plane.
 * jMonkeyEngine uses the Y axis as "up", so the game world lies in the XZ plane
 * when viewed from a top-down orthographic camera.
 */
public class Vector2D {

    public float x;
    public float z;

    public Vector2D(float x, float z) {
        this.x = x;
        this.z = z;
    }

    /** Returns a copy of this vector added to {@code other}. */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, z + other.z);
    }

    /** Returns a copy scaled by {@code scalar}. */
    public Vector2D scale(float scalar) {
        return new Vector2D(x * scalar, z * scalar);
    }

    /** Euclidean length. */
    public float length() {
        return (float) Math.sqrt(x * x + z * z);
    }

    /**
     * Returns a unit-length copy.  Returns (0, 0) for a zero vector to avoid
     * division by zero.
     */
    public Vector2D normalize() {
        float len = length();
        if (len == 0f) return new Vector2D(0f, 0f);
        return new Vector2D(x / len, z / len);
    }

    /** Distance to another 2D point. */
    public float distanceTo(Vector2D other) {
        float dx = x - other.x;
        float dz = z - other.z;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    /** Mutates this vector in-place and returns {@code this} for chaining. */
    public Vector2D set(float x, float z) {
        this.x = x;
        this.z = z;
        return this;
    }

    /** Convenience factory: zero vector. */
    public static Vector2D zero() {
        return new Vector2D(0f, 0f);
    }

    @Override
    public String toString() {
        return "Vector2D(" + x + ", " + z + ")";
    }
}
