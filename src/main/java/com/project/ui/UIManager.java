package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.project.core.GameState;
import com.project.entities.Player;
import com.project.upgrades.Upgrade;

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

    public void updateTimeAndVision(float elapsedSeconds, boolean circleVision) {
        hud.updateTimeAndVision(elapsedSeconds, circleVision);
    }

    /** @see HUD#showPauseOverlay(boolean) */
    public void showPauseOverlay(boolean show) {
        hud.showPauseOverlay(show);
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
