package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.project.core.GameState;
import com.project.entities.Player;

/**
 * Heads-Up Display rendered in 2-D screen space (attached to
 * {@code guiNode}).
 *
 * <p>Elements displayed:
 * <ul>
 *   <li><b>Health bar</b> — top-left, colour changes red → yellow → green</li>
 *   <li><b>Wave counter</b> — below health</li>
 *   <li><b>Score</b> — below wave counter</li>
 *   <li><b>Centre message</b> — used for WAVE N, PAUSED, GAME OVER banners</li>
 *   <li><b>Controls hint</b> — bottom of screen</li>
 * </ul>
 */
public class HUD {

    // Screen dimensions cached for layout calculations
    private final int screenW;
    private final int screenH;

    // HUD elements
    private final BitmapText healthText;
    private final BitmapText waveText;
    private final BitmapText scoreText;
    private final BitmapText centerMessage;
    private final BitmapText subMessage;
    private final BitmapText controlsHint;

    // Transient wave-announcement timer (shows "WAVE N" for a few seconds)
    private float waveMessageTimer = 0f;
    private static final float WAVE_MSG_DURATION = 2f;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public HUD(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.screenW = settings.getWidth();
        this.screenH = settings.getHeight();

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        healthText   = makeText(font, guiNode, 1.6f, ColorRGBA.Green);
        waveText     = makeText(font, guiNode, 1.4f, ColorRGBA.Yellow);
        scoreText    = makeText(font, guiNode, 1.4f, ColorRGBA.White);
        centerMessage = makeText(font, guiNode, 3.0f, ColorRGBA.White);
        subMessage   = makeText(font, guiNode, 1.6f, ColorRGBA.LightGray);
        controlsHint = makeText(font, guiNode, 1.0f, new ColorRGBA(0.6f, 0.6f, 0.6f, 1f));

        // Static controls hint at the bottom of the screen
        controlsHint.setText("WASD: Move  |  SPACE: Attack  |  P: Pause  |  R: Restart");
        positionBottomLeft(controlsHint, 10f, 30f);

        // Initial placeholder text so elements render at their correct size
        healthText.setText("HP: 100/100");
        waveText.setText("Wave: 1");
        scoreText.setText("Score: 0");
        centerMessage.setText("");
        subMessage.setText("");

        layoutStaticElements();
    }

    // ------------------------------------------------------------------
    // Per-frame update (call from GameApp.simpleUpdate)
    // ------------------------------------------------------------------
    /**
     * Refreshes all dynamic HUD elements.
     *
     * @param player           player entity (source of health data)
     * @param wave             current wave number
     * @param score            current player score
     * @param state            game state (for conditional display)
     * @param waveTimer        countdown in seconds until next wave starts
     * @param waitingForWave   {@code true} while between waves
     * @param tpf              seconds since last frame (for timer animations)
     */
    public void update(Player player, int wave, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {

        // --- Health ---
        float hp    = player.getHealth();
        float maxHp = player.getMaxHealth();
        healthText.setText("HP: " + (int) hp + " / " + (int) maxHp
                + "  [" + buildBar(hp, maxHp, 16) + "]");

        float hpPct = hp / maxHp;
        if      (hpPct > 0.60f) healthText.setColor(ColorRGBA.Green);
        else if (hpPct > 0.30f) healthText.setColor(ColorRGBA.Yellow);
        else                     healthText.setColor(ColorRGBA.Red);

        // --- Wave / Score ---
        waveText.setText("Wave: " + wave);
        scoreText.setText("Score: " + score);

        // --- Centre message ---
        if (state == GameState.PLAYING) {
            waveMessageTimer -= tpf;
            if (waveMessageTimer > 0f) {
                // "WAVE N" announcement is already set — leave it
            } else if (waitingForWave) {
                int secs = Math.max(1, (int) Math.ceil(waveTimer));
                centerMessage.setText("Next wave in " + secs + "...");
                subMessage.setText("");
                centerMessage.setColor(ColorRGBA.Yellow);
                recenterMessage();
            } else {
                centerMessage.setText("");
                subMessage.setText("");
            }
        }
    }

    // ------------------------------------------------------------------
    // One-shot state methods
    // ------------------------------------------------------------------
    /** Flash a large "WAVE N" banner for a brief time. */
    public void showWaveAnnouncement(String text) {
        centerMessage.setColor(ColorRGBA.Cyan);
        centerMessage.setText(text);
        subMessage.setText("");
        waveMessageTimer = WAVE_MSG_DURATION;
        recenterMessage();
    }

    /** Shows or hides the PAUSED overlay. */
    public void showPauseOverlay(boolean show) {
        if (show) {
            centerMessage.setColor(ColorRGBA.White);
            centerMessage.setText("PAUSED");
            subMessage.setText("Press P to Resume");
        } else {
            centerMessage.setText("");
            subMessage.setText("");
        }
        recenterMessage();
    }

    /** Shows the GAME OVER overlay. */
    public void showGameOverOverlay(int finalScore) {
        centerMessage.setColor(ColorRGBA.Red);
        centerMessage.setText("GAME OVER");
        subMessage.setText("Final Score: " + finalScore + "   |   Press R to Restart");
        recenterMessage();
    }

    /** Clears all overlay messages — called on restart. */
    public void reset() {
        centerMessage.setText("");
        subMessage.setText("");
        centerMessage.setColor(ColorRGBA.White);
        waveMessageTimer = 0f;
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------
    private BitmapText makeText(BitmapFont font, Node guiNode,
                                float sizeMultiplier, ColorRGBA color) {
        BitmapText t = new BitmapText(font, false);
        t.setSize(font.getCharSet().getRenderedSize() * sizeMultiplier);
        t.setColor(color);
        guiNode.attachChild(t);
        return t;
    }

    private void layoutStaticElements() {
        float lineSpacing = healthText.getSize() * 1.5f;
        // Top-left column
        healthText.setLocalTranslation(12f, screenH - 12f, 0f);
        waveText.setLocalTranslation(12f, screenH - 12f - lineSpacing, 0f);
        scoreText.setLocalTranslation(12f, screenH - 12f - lineSpacing * 2f, 0f);
    }

    private void positionBottomLeft(BitmapText t, float marginX, float marginY) {
        t.setLocalTranslation(marginX, marginY, 0f);
    }

    /**
     * Centres the current text of {@link #centerMessage} horizontally, and
     * positions it roughly at two-thirds screen height.
     * {@link #subMessage} is placed just below it.
     */
    private void recenterMessage() {
        float cx = Math.max(0f, (screenW - centerMessage.getLineWidth()) * 0.5f);
        float cy = screenH * 0.65f;
        centerMessage.setLocalTranslation(cx, cy, 0f);

        // Sub-message centred below the main banner
        if (!subMessage.getText().isEmpty()) {
            float sx = Math.max(0f, (screenW - subMessage.getLineWidth()) * 0.5f);
            subMessage.setLocalTranslation(sx, cy - centerMessage.getSize() * 1.4f, 0f);
        } else {
            subMessage.setLocalTranslation(0f, 0f, 0f);
        }
    }

    /** Builds a simple ASCII progress bar, e.g. {@code "########----"}. */
    private String buildBar(float value, float max, int width) {
        int filled = Math.round((value / max) * width);
        filled = Math.max(0, Math.min(width, filled));
        return "#".repeat(filled) + "-".repeat(width - filled);
    }
}
