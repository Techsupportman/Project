package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.project.core.GameState;
import com.project.entities.Player;
import com.project.upgrades.Upgrade;
import com.project.weapons.WeaponType;

import java.util.List;

/**
 * Thin orchestration layer between {@link com.project.core.GameApp} and the
 * individual UI components.
 *
 * <p>Wraps {@link HUD} and {@link LevelUpMenu}.  Additional overlays (settings,
 * leaderboard, etc.) can be registered here without changing {@code GameApp}.
 */
public class UIManager {

    private final HUD         hud;
    private final LevelUpMenu levelUpMenu;

    public UIManager(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.hud         = new HUD(assetManager, guiNode, settings);
        this.levelUpMenu = new LevelUpMenu(assetManager, guiNode, settings);
    }

    // ------------------------------------------------------------------
    // HUD delegates
    // ------------------------------------------------------------------

    public void update(Player player, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {
        hud.update(player, score, state, waveTimer, waitingForWave, tpf);
    }

    /** @see HUD#update(Player, int, int, GameState, float, boolean, float) */
    public void update(Player player, int wave, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {
        hud.update(player, wave, score, state, waveTimer, waitingForWave, tpf);
    }

    /** Updates the elapsed-time display. Ignores the circleVision param (vision system removed). */
    public void updateTimeAndVision(float elapsedSeconds, boolean circleVision) {
        hud.updateElapsedTime(elapsedSeconds);
    }

    /** @see HUD#setBlackHoleStatus(String) */
    public void setBlackHoleStatus(String statusText) {
        hud.setBlackHoleStatus(statusText);
    }

    /** @see HUD#showPauseOverlay(boolean) */
    public void showPauseOverlay(boolean show) {
        hud.showPauseOverlay(show);
    }

    /** @see HUD#getPauseMenuClickedOption(float, float) */
    public int getPauseMenuClickedOption(float mx, float my) {
        return hud.getPauseMenuClickedOption(mx, my);
    }

    /** @see HUD#updatePauseMenuHover(float, float) */
    public void updatePauseMenuHover(float mx, float my) {
        hud.updatePauseMenuHover(mx, my);
    }

    /** @see HUD#setFireLockStatus(boolean) */
    public void setFireLockStatus(boolean locked) {
        hud.setFireLockStatus(locked);
    }

    /** @see HUD#showGameOverOverlay(int) */
    public void showGameOverOverlay(int finalScore) {
        hud.showGameOverOverlay(finalScore);
    }

    /** @see HUD#showDifficultySelect() */
    public void showDifficultySelect() {
        hud.showDifficultySelect();
    }

    /** @see HUD#hideDifficultySelect() */
    public void hideDifficultySelect() {
        hud.hideDifficultySelect();
    }

    /** @see HUD#showBossWarning(String) */
    public void showBossWarning(String bossName) {
        hud.showBossWarning(bossName);
    }

    /**
     * Shows the weapon selection overlay for a specific page of weapons.
     *
     * @param pageWeapons weapons displayed on this page (up to 5)
     * @param page        0-based page index
     * @param totalPages  total number of pages
     */
    public void showWeaponSelect(WeaponType[] pageWeapons, int page, int totalPages) {
        hud.showWeaponSelect(pageWeapons, page, totalPages);
    }

    /** Hides the weapon selection overlay. */
    public void hideWeaponSelect() {
        hud.hideWeaponSelect();
    }

    /** @see HUD#reset() */
    public void reset() {
        hud.reset();
        levelUpMenu.hide();
    }

    // ------------------------------------------------------------------
    // LevelUpMenu delegates
    // ------------------------------------------------------------------

    /**
     * Shows the level-up upgrade selection menu.
     *
     * @param newLevel    level just reached
     * @param choices     list of upgrade choices (3-5)
     * @param rerollsLeft remaining rerolls this level
     * @param deletesLeft remaining deletes this level
     */
    public void showLevelUpMenu(int newLevel, List<Upgrade> choices,
                                int rerollsLeft, int deletesLeft) {
        levelUpMenu.show(newLevel, choices, rerollsLeft, deletesLeft);
    }

    /** Hides the level-up menu. */
    public void hideLevelUpMenu() {
        levelUpMenu.hide();
    }

    /** @return {@code true} when the level-up menu is currently displayed. */
    public boolean isLevelUpMenuVisible() {
        return levelUpMenu.isVisible();
    }

    /**
     * Updates hover-highlighting on the level-up menu.
     * Call every frame while LEVEL_UP state is active.
     */
    public void updateLevelUpHover(float mx, float my, int activeChoices) {
        levelUpMenu.updateHover(mx, my, activeChoices);
    }

    /**
     * Returns the 0-based index of the upgrade choice the cursor is over,
     * or -1 if none.
     */
    public int getLevelUpClickedChoice(float mx, float my, int activeChoices) {
        return levelUpMenu.getClickedChoice(mx, my, activeChoices);
    }

    /** Returns {@code true} when the cursor is over the Reroll button. */
    public boolean isLevelUpRerollClicked(float mx, float my) {
        return levelUpMenu.isRerollClicked(mx, my);
    }

    /** Returns {@code true} when the cursor is over the Delete/Skip button. */
    public boolean isLevelUpDeleteClicked(float mx, float my) {
        return levelUpMenu.isDeleteClicked(mx, my);
    }

    // ------------------------------------------------------------------
    // Direct access
    // ------------------------------------------------------------------
    public HUD         getHud()         { return hud; }
    public LevelUpMenu getLevelUpMenu() { return levelUpMenu; }

    // ------------------------------------------------------------------
    // Legacy stub kept for backward compatibility
    // ------------------------------------------------------------------
    public void showWaveAnnouncement(String text) { /* no-op in new system */ }
}
