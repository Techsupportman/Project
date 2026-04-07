package com.project.weapons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

    @Test
    void pistolIsUnlockedByDefault() {
        Weapon pistol = new Weapon(WeaponType.PISTOL);
        assertTrue(pistol.isUnlocked(), "Pistol (starter weapon) must start unlocked");
    }

    @Test
    void nonStarterWeaponStartsLocked() {
        Weapon sniper = new Weapon(WeaponType.SNIPER);
        assertFalse(sniper.isUnlocked(), "Sniper must start locked");
    }

    @Test
    void lockedWeaponCannotFire() {
        Weapon sniper = new Weapon(WeaponType.SNIPER);
        assertFalse(sniper.tryFire(), "Locked weapon must not fire");
    }

    @Test
    void unlockedWeaponCanFire() {
        Weapon pistol = new Weapon(WeaponType.PISTOL);
        assertTrue(pistol.tryFire(), "Unlocked weapon should fire on first attempt");
    }

    @Test
    void weaponRespectsCooldown() {
        Weapon pistol = new Weapon(WeaponType.PISTOL);
        pistol.tryFire(); // first shot sets cooldown
        assertFalse(pistol.tryFire(), "Second immediate shot should be blocked by cooldown");
    }

    @Test
    void cooldownExpiresAfterSufficientTime() {
        Weapon pistol = new Weapon(WeaponType.PISTOL);
        pistol.tryFire();
        // Advance well past the cooldown duration (1 / fireRate seconds)
        float cooldown = 1f / WeaponType.PISTOL.stats.fireRate + 0.01f;
        pistol.update(cooldown);
        assertTrue(pistol.tryFire(), "Weapon should be ready after cooldown expires");
    }

    @Test
    void unlockMethodEnablesLockedWeapon() {
        Weapon sniper = new Weapon(WeaponType.SNIPER);
        sniper.unlock();
        assertTrue(sniper.isUnlocked());
        assertTrue(sniper.tryFire());
    }

    @Test
    void allWeaponTypesHavePositiveStats() {
        for (WeaponType wt : WeaponType.values()) {
            WeaponStats s = wt.stats;
            assertTrue(s.damage       > 0f, wt + ": damage must be > 0");
            assertTrue(s.fireRate     > 0f, wt + ": fireRate must be > 0");
            assertTrue(s.bulletSpeed  > 0f, wt + ": bulletSpeed must be > 0");
            assertTrue(s.bulletSize   > 0f, wt + ": bulletSize must be > 0");
        }
    }
}
