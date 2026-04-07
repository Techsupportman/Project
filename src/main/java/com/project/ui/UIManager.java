package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.project.core.GameState;
import com.project.entities.Player;

/**
 * Thin orchestration layer between {@link com.project.core.GameApp} and the
 * individual UI components.
 *
 * <p>Currently it wraps just {@link HUD}, but additional screens (main menu,
 * settings, level-complete overlay, etc.) can be registered here without
 * changing any code in {@code GameApp}.
 */
public class UIManager {

    private final HUD hud;

    public UIManager(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.hud = new HUD(assetManager, guiNode, settings);
    }

    // ------------------------------------------------------------------
    // Delegates to HUD
    // ------------------------------------------------------------------

    /**
     * Refreshes all dynamic UI elements for the current frame.
     *
     * @see HUD#update(Player, int, int, GameState, float, boolean, float)
     */
    public void update(Player player, int wave, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {
        hud.update(player, wave, score, state, waveTimer, waitingForWave, tpf);
    }

    /** @see HUD#showWaveAnnouncement(String) */
    public void showWaveAnnouncement(String text) {
        hud.showWaveAnnouncement(text);
    }

    /** @see HUD#showPauseOverlay(boolean) */
    public void showPauseOverlay(boolean show) {
        hud.showPauseOverlay(show);
    }

    /** @see HUD#showGameOverOverlay(int) */
    public void showGameOverOverlay(int finalScore) {
        hud.showGameOverOverlay(finalScore);
    }

    /** @see HUD#reset() */
    public void reset() {
        hud.reset();
    }

    /** Direct access to the underlying HUD if fine-grained control is needed. */
    public HUD getHud() {
        return hud;
    }
}
