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
 */
public class UIManager {

    private final HUD         hud;
    private final LevelUpMenu levelUpMenu;

    public UIManager(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.hud         = new HUD(assetManager, guiNode, settings);
        this.levelUpMenu = new LevelUpMenu(assetManager, guiNode, settings);
    }

    // ------------------------------------------------------------------
    // Resize
    // ------------------------------------------------------------------
    public void onResize(int w, int h) {
        hud.onResize(w, h);
    }

    // ------------------------------------------------------------------
    // HUD delegates
    // ------------------------------------------------------------------
    public void update(Player player, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {
        hud.update(player, score, state, waveTimer, waitingForWave, tpf);
    }

    public void update(Player player, int wave, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {
        hud.update(player, wave, score, state, waveTimer, waitingForWave, tpf);
    }

    public void updateTimeAndVision(float elapsedSeconds, boolean circleVision) {
        hud.updateElapsedTime(elapsedSeconds);
    }

    public void setBlackHoleStatus(String statusText) {
        hud.setBlackHoleStatus(statusText);
    }

    // ------------------------------------------------------------------
    // Pause menu
    // ------------------------------------------------------------------
    public void showPauseOverlay(boolean show) {
        hud.showPauseOverlay(show);
    }

    /** Returns 0=Resume, 1=Restart, 2=Settings, 3=Quit, or -1. */
    public int getPauseMenuClickedOption(float mx, float my) {
        return hud.getPauseMenuClickedOption(mx, my);
    }

    public void updatePauseMenuHover(float mx, float my) {
        hud.updatePauseMenuHover(mx, my);
    }

    // ------------------------------------------------------------------
    // Settings menu
    // ------------------------------------------------------------------
    public void showSettingsMenu() {
        hud.showSettingsMenu();
    }

    public void hideSettingsMenu() {
        hud.hideSettingsMenu();
    }

    public int getSettingsClickedOption(float mx, float my) {
        return hud.getSettingsClickedOption(mx, my);
    }

    public void updateSettingsHover(float mx, float my) {
        hud.updateSettingsHover(mx, my);
    }

    /**
     * Adjusts the displayed volume by {@code delta} percent (±10) and returns
     * the new 0–1 float volume for the audio listener.
     */
    public float adjustVolume(int delta) {
        return hud.adjustVolume(delta);
    }

    /** Returns the current master volume as a 0–1 float. */
    public float getMasterVolume() { return hud.getMasterVolume(); }

    /** @deprecated Fullscreen is no longer supported via settings. */
    @Deprecated
    public void setFullscreenLabel(boolean isFullscreen) { /* no-op */ }

    // ------------------------------------------------------------------
    // Fire lock
    // ------------------------------------------------------------------
    public void setFireLockStatus(boolean locked) {
        hud.setFireLockStatus(locked);
    }

    // ------------------------------------------------------------------
    // Game over
    // ------------------------------------------------------------------
    public void showGameOverOverlay(int finalScore) {
        hud.showGameOverOverlay(finalScore);
    }

    public boolean isGameOverRestartClicked(float mx, float my) {
        return hud.isGameOverRestartClicked(mx, my);
    }

    public void updateGameOverHover(float mx, float my) {
        hud.updateGameOverHover(mx, my);
    }

    // ------------------------------------------------------------------
    // Difficulty select
    // ------------------------------------------------------------------
    public void showDifficultySelect() {
        hud.showDifficultySelect();
    }

    public void hideDifficultySelect() {
        hud.hideDifficultySelect();
    }

    public int getDifficultyClickedOption(float mx, float my) {
        return hud.getDifficultyClickedOption(mx, my);
    }

    public void updateDifficultyHover(float mx, float my) {
        hud.updateDifficultyHover(mx, my);
    }

    // ------------------------------------------------------------------
    // Weapon select
    // ------------------------------------------------------------------
    public void showWeaponSelect(WeaponType[] pageWeapons, int page, int totalPages) {
        hud.showWeaponSelect(pageWeapons, page, totalPages);
    }

    public void hideWeaponSelect() {
        hud.hideWeaponSelect();
    }

    public int getWeaponClickedOption(float mx, float my) {
        return hud.getWeaponClickedOption(mx, my);
    }

    public boolean isWeaponNavNextClicked(float mx, float my) {
        return hud.isNavNextClicked(mx, my);
    }

    public boolean isWeaponNavPrevClicked(float mx, float my) {
        return hud.isNavPrevClicked(mx, my);
    }

    public void updateWeaponSelectHover(float mx, float my) {
        hud.updateWeaponSelectHover(mx, my);
    }

    /** Updates the crosshair position to track the mouse cursor (screen-space). */
    public void updateCrosshair(float mx, float my) {
        hud.updateCrosshair(mx, my);
    }

    // ------------------------------------------------------------------
    // Boss warning
    // ------------------------------------------------------------------
    public void showBossWarning(String bossName) {
        hud.showBossWarning(bossName);
    }

    /** Clears the boss warning overlay after the boss has been defeated. */
    public void clearBossWarning() {
        hud.clearBossWarning();
    }

    // ------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------
    public void reset() {
        hud.reset();
        levelUpMenu.hide();
    }

    // ------------------------------------------------------------------
    // LevelUpMenu delegates
    // ------------------------------------------------------------------
    public void showLevelUpMenu(int newLevel, List<Upgrade> choices,
                                int rerollsLeft, int deletesLeft) {
        levelUpMenu.show(newLevel, choices, rerollsLeft, deletesLeft);
    }

    public void hideLevelUpMenu() {
        levelUpMenu.hide();
    }

    public boolean isLevelUpMenuVisible() {
        return levelUpMenu.isVisible();
    }

    public void updateLevelUpHover(float mx, float my, int activeChoices) {
        levelUpMenu.updateHover(mx, my, activeChoices);
    }

    public int getLevelUpClickedChoice(float mx, float my, int activeChoices) {
        return levelUpMenu.getClickedChoice(mx, my, activeChoices);
    }

    public boolean isLevelUpRerollClicked(float mx, float my) {
        return levelUpMenu.isRerollClicked(mx, my);
    }

    public boolean isLevelUpDeleteClicked(float mx, float my) {
        return levelUpMenu.isDeleteClicked(mx, my);
    }

    // ------------------------------------------------------------------
    // Direct access
    // ------------------------------------------------------------------
    public HUD         getHud()         { return hud; }
    public LevelUpMenu getLevelUpMenu() { return levelUpMenu; }

    // ------------------------------------------------------------------
    // Legacy stub
    // ------------------------------------------------------------------
    public void showWaveAnnouncement(String text) { /* no-op */ }
}
