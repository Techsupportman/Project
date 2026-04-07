package com.project.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector2DTest {

    @Test
    void lengthOfUnitVector() {
        Vector2D v = new Vector2D(1f, 0f);
        assertEquals(1f, v.length(), 1e-6f);
    }

    @Test
    void normalizeProducesUnitVector() {
        Vector2D v = new Vector2D(3f, 4f);
        Vector2D n = v.normalize();
        assertEquals(1f, n.length(), 1e-6f);
    }

    @Test
    void normalizeZeroVectorReturnsZero() {
        Vector2D v = Vector2D.zero().normalize();
        assertEquals(0f, v.x);
        assertEquals(0f, v.z);
    }

    @Test
    void distanceTo() {
        Vector2D a = new Vector2D(0f, 0f);
        Vector2D b = new Vector2D(3f, 4f);
        assertEquals(5f, a.distanceTo(b), 1e-6f);
    }

    @Test
    void addTwoVectors() {
        Vector2D a = new Vector2D(1f, 2f);
        Vector2D b = new Vector2D(3f, 4f);
        Vector2D sum = a.add(b);
        assertEquals(4f, sum.x, 1e-6f);
        assertEquals(6f, sum.z, 1e-6f);
    }

    @Test
    void scaleVector() {
        Vector2D v = new Vector2D(2f, 3f);
        Vector2D scaled = v.scale(2f);
        assertEquals(4f, scaled.x, 1e-6f);
        assertEquals(6f, scaled.z, 1e-6f);
    }
}
