package com.project.upgrades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpgradeManagerTest {

    private UpgradeManager manager;

    @BeforeEach
    void setUp() {
        manager = new UpgradeManager();
    }

    @Test
    void generateChoicesReturnsAtLeastOneOption() {
        List<Upgrade> choices = manager.generateChoices();
        assertFalse(choices.isEmpty(), "Should always offer at least one upgrade");
    }

    @Test
    void generateChoicesNeverExceedsFive() {
        List<Upgrade> choices = manager.generateChoices();
        assertTrue(choices.size() <= 5, "Must not offer more than 5 upgrades");
    }

    @Test
    void applyUpgradeIncreasesDamageMult() {
        float before = manager.getDamageMult();
        // Find a damage upgrade and apply it
        List<Upgrade> choices = manager.generateChoices();
        Upgrade dmgUpgrade = choices.stream()
                .filter(u -> u.name.contains("Damage") || u.name.contains("Crit"))
                .findFirst()
                .orElse(null);
        if (dmgUpgrade != null) {
            manager.applyUpgrade(dmgUpgrade);
            assertTrue(manager.getDamageMult() > before,
                    "Damage multiplier should increase after a damage upgrade");
        }
    }

    @Test
    void rerollConsumesOneRerollPerLevel() {
        assertEquals(1, manager.getRerollsLeft());
        List<Upgrade> rerolled = manager.reroll();
        assertNotNull(rerolled);
        assertEquals(0, manager.getRerollsLeft());
        // Second reroll this level should return null
        assertNull(manager.reroll());
    }

    @Test
    void deleteConsumesOneDeletePerLevel() {
        assertEquals(1, manager.getDeletesLeft());
        assertTrue(manager.delete());
        assertEquals(0, manager.getDeletesLeft());
        assertFalse(manager.delete());
    }

    @Test
    void resetRestoresDefaultState() {
        manager.reroll();
        manager.delete();
        manager.reset();
        assertEquals(1, manager.getRerollsLeft());
        assertEquals(1, manager.getDeletesLeft());
        assertEquals(1.0f, manager.getDamageMult(), 1e-6f);
        assertEquals(1.0f, manager.getFireRateMult(), 1e-6f);
    }

    @Test
    void rerollsAndDeletesReplenishAfterApply() {
        manager.reroll();
        // Apply an upgrade — rerolls should be refilled for next level
        List<Upgrade> choices = manager.generateChoices();
        if (!choices.isEmpty()) {
            manager.applyUpgrade(choices.get(0));
            assertEquals(1, manager.getRerollsLeft(),
                    "Rerolls should refresh after applying an upgrade");
        }
    }
}
