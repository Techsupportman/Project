package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.EnemyType;
import com.project.entities.Player;
import com.project.difficulty.Difficulty;
import com.project.utils.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisionSystemTest {

    private final VisionSystem vision = new VisionSystem();

    // --- Circle vision tests ---

    @Test
    void circleVisionRevealsNearbyTarget() {
        // Target is clearly within VISION_CIRCLE_RADIUS
        boolean vis = vision.isVisible(
                1f, 1f,    // target
                0f, 0f,    // player
                1f, 0f,    // aim (irrelevant for circle)
                true       // circle mode
        );
        assertTrue(vis, "Target within circle radius should be visible");
    }

    @Test
    void circleVisionHidesDistantTarget() {
        float farDist = Constants.VISION_CIRCLE_RADIUS + 2f;
        boolean vis = vision.isVisible(
                farDist, 0f,
                0f, 0f,
                1f, 0f,
                true
        );
        assertFalse(vis, "Target beyond circle radius should be invisible");
    }

    // --- Cone vision tests ---

    @Test
    void coneVisionRevealsTargetInsideCone() {
        // Target is directly ahead, within cone range
        boolean vis = vision.isVisible(
                5f, 0f,   // target ahead on X axis
                0f, 0f,   // player at origin
                1f, 0f,   // aim: right
                false     // cone mode
        );
        assertTrue(vis, "Target directly ahead in cone should be visible");
    }

    @Test
    void coneVisionHidesTargetBehindPlayer() {
        // Target is directly behind the player (opposite aim direction)
        boolean vis = vision.isVisible(
                -8f, 0f,  // target behind
                0f, 0f,
                1f, 0f,   // aim: right
                false
        );
        assertFalse(vis, "Target behind player outside near radius should be hidden");
    }

    @Test
    void coneVisionNearCircleAlwaysReveals() {
        // Target is close but behind the player — still within near circle
        float nearDist = Constants.VISION_CONE_NEAR_RADIUS - 0.5f;
        boolean vis = vision.isVisible(
                -nearDist, 0f, // directly behind but very close
                0f, 0f,
                1f, 0f,
                false
        );
        assertTrue(vis, "Target within cone near-radius should always be visible");
    }

    @Test
    void coneVisionHidesTargetBeyondMaxRange() {
        float farDist = Constants.VISION_CONE_RANGE + 2f;
        boolean vis = vision.isVisible(
                farDist, 0f,
                0f, 0f,
                1f, 0f,
                false
        );
        assertFalse(vis, "Target beyond cone max range should be invisible");
    }
}
