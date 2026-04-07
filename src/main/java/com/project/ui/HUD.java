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
 *   <li><b>Hearts</b> — top-left, one symbol per HP</li>
 *   <li><b>Level / EXP bar</b> — below hearts</li>
 *   <li><b>Credits</b> — below level</li>
 *   <li><b>Time elapsed</b> — top-right</li>
 *   <li><b>Vision mode indicator</b> — below time (Circle / Cone)</li>
 *   <li><b>Score</b> — below vision mode</li>
 *   <li><b>Centre message</b> — used for PAUSED, GAME OVER banners</li>
 *   <li><b>Controls hint</b> — bottom of screen</li>
 * </ul>
 */
public class HUD {

    private final int screenW;
    private final int screenH;

    // Left-side HUD
    private final BitmapText heartsText;
    private final BitmapText levelText;
    private final BitmapText expText;
    private final BitmapText creditsText;

    // Right-side HUD
    private final BitmapText timeText;
    private final BitmapText visionText;
    private final BitmapText scoreText;

    // Centre overlays
    private final BitmapText centerMessage;
    private final BitmapText subMessage;

    // Bottom hint
    private final BitmapText controlsHint;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public HUD(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.screenW = settings.getWidth();
        this.screenH = settings.getHeight();

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        heartsText   = makeText(font, guiNode, 2.0f,  new ColorRGBA(1f, 0.3f, 0.3f, 1f));
        levelText    = makeText(font, guiNode, 1.4f,  ColorRGBA.Yellow);
        expText      = makeText(font, guiNode, 1.2f,  new ColorRGBA(0.4f, 1f, 0.5f, 1f));
        creditsText  = makeText(font, guiNode, 1.2f,  new ColorRGBA(1f, 0.9f, 0.2f, 1f));

        timeText     = makeText(font, guiNode, 1.4f,  ColorRGBA.White);
        visionText   = makeText(font, guiNode, 1.3f,  new ColorRGBA(0.5f, 0.9f, 1f, 1f));
        scoreText    = makeText(font, guiNode, 1.3f,  ColorRGBA.White);

        centerMessage = makeText(font, guiNode, 3.0f, ColorRGBA.White);
        subMessage    = makeText(font, guiNode, 1.6f, ColorRGBA.LightGray);

        controlsHint  = makeText(font, guiNode, 1.0f,
                new ColorRGBA(0.55f, 0.55f, 0.55f, 1f));

        controlsHint.setText("WASD: Move  |  LMB: Fire  |  RMB: Vision  |  Q/E: Weapon  |  P: Pause");
        controlsHint.setLocalTranslation(10f, 28f, 0f);

        // Initial placeholder text for correct first-frame sizing
        heartsText.setText("♥♥♥");
        levelText.setText("Lv 1");
        expText.setText("EXP: 0 / 100");
        creditsText.setText("Credits: 0");
        timeText.setText("00:00");
        visionText.setText("Vision: Circle");
        scoreText.setText("Score: 0");
        centerMessage.setText("");
        subMessage.setText("");

        layoutStaticElements();
    }

    // ------------------------------------------------------------------
    // Per-frame update
    // ------------------------------------------------------------------
    /**
     * Refreshes all dynamic HUD elements.
     *
     * @param player         player entity
     * @param score          current score
     * @param state          current game state
     * @param waveTimer      wave countdown seconds (legacy)
     * @param waitingForWave legacy flag
     * @param tpf            delta time (for animation timers)
     */
    public void update(Player player, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {

        // --- Hearts ---
        heartsText.setText(buildHearts(player.getHearts(), player.getMaxHearts()));

        // --- Level & EXP ---
        levelText.setText("Lv " + player.getLevel());
        expText.setText("EXP: " + player.getCurrentExp() + " / " + player.getExpToNextLevel()
                + "  " + buildBar(player.getCurrentExp(), player.getExpToNextLevel(), 14));

        // --- Credits ---
        creditsText.setText("Credits: " + player.getCredits());

        // --- Score ---
        scoreText.setText("Score: " + score);

        // --- Re-position right column (they may have changed size) ---
        layoutRightColumn();
    }

    /** Refreshes time and vision-mode indicators (call each frame). */
    public void updateTimeAndVision(float elapsedSeconds, boolean circleVision) {
        int totalSec = (int) elapsedSeconds;
        int minutes  = totalSec / 60;
        int seconds  = totalSec % 60;
        timeText.setText(String.format("%02d:%02d", minutes, seconds));

        visionText.setText("Vision: " + (circleVision ? "◎ Circle" : "▷ Cone"));
        visionText.setColor(circleVision
                ? new ColorRGBA(0.4f, 1f, 1f, 1f)
                : new ColorRGBA(1f, 0.7f, 0.3f, 1f));

        layoutRightColumn();
    }

    // ------------------------------------------------------------------
    // One-shot state methods
    // ------------------------------------------------------------------
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

    /** Shows a difficulty-select prompt. */
    public void showDifficultySelect() {
        centerMessage.setColor(new ColorRGBA(1f, 0.9f, 0.2f, 1f));
        centerMessage.setText("SELECT DIFFICULTY");
        subMessage.setText("[1] Easy   [2] Normal   [3] Hard   [4] Nightmare");
        recenterMessage();
    }

    /** Hides difficulty-select and main overlays. */
    public void hideDifficultySelect() {
        centerMessage.setText("");
        subMessage.setText("");
    }

    /** Shows a boss-warning banner. */
    public void showBossWarning(String bossName) {
        centerMessage.setColor(new ColorRGBA(1f, 0.2f, 0.2f, 1f));
        centerMessage.setText("⚠ BOSS — " + bossName + " ⚠");
        subMessage.setText("Defeat the boss to resume normal spawning!");
        recenterMessage();
    }

    /** Clears all overlay messages — called on restart. */
    public void reset() {
        centerMessage.setText("");
        subMessage.setText("");
        centerMessage.setColor(ColorRGBA.White);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------
    private BitmapText makeText(BitmapFont font, Node guiNode,
                                float sizeMult, ColorRGBA color) {
        BitmapText t = new BitmapText(font, false);
        t.setSize(font.getCharSet().getRenderedSize() * sizeMult);
        t.setColor(color);
        guiNode.attachChild(t);
        return t;
    }

    private void layoutStaticElements() {
        float lineH = heartsText.getSize() * 1.6f;
        float startY = screenH - 14f;
        heartsText.setLocalTranslation(12f, startY, 0f);
        levelText.setLocalTranslation(12f, startY - lineH, 0f);
        expText.setLocalTranslation(12f, startY - lineH * 1.8f, 0f);
        creditsText.setLocalTranslation(12f, startY - lineH * 2.6f, 0f);
        layoutRightColumn();
    }

    private void layoutRightColumn() {
        float startY = screenH - 14f;
        float lineH  = timeText.getSize() * 1.7f;

        float tW = timeText.getLineWidth();
        timeText.setLocalTranslation(screenW - tW - 12f, startY, 0f);

        float vW = visionText.getLineWidth();
        visionText.setLocalTranslation(screenW - vW - 12f, startY - lineH, 0f);

        float sW = scoreText.getLineWidth();
        scoreText.setLocalTranslation(screenW - sW - 12f, startY - lineH * 2f, 0f);
    }

    private void recenterMessage() {
        float cx = Math.max(0f, (screenW - centerMessage.getLineWidth()) * 0.5f);
        float cy = screenH * 0.65f;
        centerMessage.setLocalTranslation(cx, cy, 0f);
        if (!subMessage.getText().isEmpty()) {
            float sx = Math.max(0f, (screenW - subMessage.getLineWidth()) * 0.5f);
            subMessage.setLocalTranslation(sx, cy - centerMessage.getSize() * 1.4f, 0f);
        } else {
            subMessage.setLocalTranslation(0f, 0f, 0f);
        }
    }

    /** Heart symbols — filled ♥ for current HP, empty ♡ for lost. */
    private String buildHearts(int current, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            sb.append(i < current ? "♥" : "♡");
        }
        return sb.toString();
    }

    private String buildBar(float value, float max, int width) {
        if (max <= 0) return "";
        int filled = Math.round((value / max) * width);
        filled = Math.max(0, Math.min(width, filled));
        return "[" + "#".repeat(filled) + "-".repeat(width - filled) + "]";
    }

    // ------------------------------------------------------------------
    // Legacy update signature kept for backward compatibility
    // ------------------------------------------------------------------
    public void update(Player player, int wave, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {
        update(player, score, state, waveTimer, waitingForWave, tpf);
    }
}
