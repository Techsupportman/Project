package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.project.core.GameState;
import com.project.entities.Player;
import com.project.weapons.WeaponStats;
import com.project.weapons.WeaponType;

/**
 * Heads-Up Display rendered in 2-D screen space (attached to
 * {@code guiNode}).
 */
public class HUD {

    // Screen dimensions — non-final so onResize() can update them
    private int screenW;
    private int screenH;

    // Left-side HUD
    private final BitmapText heartsText;
    private final BitmapText levelText;
    private final BitmapText expText;
    private final BitmapText creditsText;

    // Right-side HUD
    private final BitmapText timeText;
    private final BitmapText scoreText;
    private final BitmapText blackHoleText;

    // Centre overlays (GAME OVER, boss warning, etc.)
    private final BitmapText centerMessage;
    private final BitmapText subMessage;

    // Bottom hints
    private final BitmapText controlsHint;
    private final BitmapText fireLockText;

    // Difficulty-select panel
    private final BitmapText  difficultyTitle;
    private final ButtonPanel difficultyPanel;

    // Pause menu panel (Resume / Restart / Settings / Quit)
    private final ButtonPanel pausePanel;

    // Settings menu panel (Fullscreen toggle / Back)
    private final BitmapText  settingsTitle;
    private final ButtonPanel settingsPanel;

    // Weapon-select (up to 5 buttons + 2 nav buttons + title)
    private static final int MAX_WEAPON_BUTTONS = 5;
    private final BitmapText weaponSelectTitle;
    private final Button[]   weaponButtons = new Button[MAX_WEAPON_BUTTONS];
    private final Button     weaponNavNext;
    private final Button     weaponNavPrev;

    // Game-over restart button
    private final Button gameOverRestartBtn;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public HUD(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.screenW = settings.getWidth();
        this.screenH = settings.getHeight();

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        // ---- Left HUD ----
        heartsText   = makeText(font, guiNode, 2.0f, new ColorRGBA(1f, 0.3f, 0.3f, 1f));
        levelText    = makeText(font, guiNode, 1.4f, ColorRGBA.Yellow);
        expText      = makeText(font, guiNode, 1.2f, new ColorRGBA(0.4f, 1f, 0.5f, 1f));
        creditsText  = makeText(font, guiNode, 1.2f, new ColorRGBA(1f, 0.9f, 0.2f, 1f));

        // ---- Right HUD ----
        timeText      = makeText(font, guiNode, 1.4f, ColorRGBA.White);
        scoreText     = makeText(font, guiNode, 1.3f, ColorRGBA.White);
        blackHoleText = makeText(font, guiNode, 1.2f, new ColorRGBA(0.4f, 0.0f, 1.0f, 1f));

        // ---- Centre overlays ----
        centerMessage = makeText(font, guiNode, 3.0f, ColorRGBA.White);
        subMessage    = makeText(font, guiNode, 1.6f, ColorRGBA.LightGray);

        // ---- Bottom hints ----
        controlsHint = makeText(font, guiNode, 1.0f,
                new ColorRGBA(0.55f, 0.55f, 0.55f, 1f));
        fireLockText = makeText(font, guiNode, 1.2f,
                new ColorRGBA(1.0f, 0.6f, 0.0f, 1f));

        controlsHint.setText(
                "WASD: Move  |  LMB: Fire  |  Q: Toggle Fire-Lock  |  ESC: Pause");
        controlsHint.setLocalTranslation(10f, 28f, 0f);
        fireLockText.setText("");
        fireLockText.setLocalTranslation(10f, 50f, 0f);

        // Placeholder text for correct first-frame sizing
        heartsText.setText("♥♥♥");
        levelText.setText("Lv 1");
        expText.setText("EXP: 0 / 100");
        creditsText.setText("Credits: 0");
        timeText.setText("00:00");
        scoreText.setText("Score: 0");
        blackHoleText.setText("");
        centerMessage.setText("");
        subMessage.setText("");

        layoutStaticElements();

        // ---- Difficulty select ----
        difficultyTitle = makeText(font, guiNode, 2.4f,
                new ColorRGBA(1f, 0.9f, 0.2f, 1f));
        difficultyTitle.setText("");
        difficultyTitle.setCullHint(Spatial.CullHint.Always);

        difficultyPanel = new ButtonPanel(
                assetManager, guiNode, font,
                screenW * 0.5f, screenH * 0.38f,
                320f, 52f, 14f,
                "Easy", "Normal", "Hard", "Nightmare");

        // ---- Pause menu ----
        pausePanel = new ButtonPanel(
                assetManager, guiNode, font,
                screenW * 0.5f, screenH * 0.40f,
                300f, 52f, 14f,
                "Resume", "Restart", "Settings", "Quit");

        // ---- Settings menu ----
        settingsTitle = makeText(font, guiNode, 2.4f,
                new ColorRGBA(1f, 0.9f, 0.2f, 1f));
        settingsTitle.setText("");
        settingsTitle.setCullHint(Spatial.CullHint.Always);

        settingsPanel = new ButtonPanel(
                assetManager, guiNode, font,
                screenW * 0.5f, screenH * 0.42f,
                320f, 52f, 14f,
                "Fullscreen: OFF", "Back");

        // ---- Weapon select ----
        weaponSelectTitle = makeText(font, guiNode, 2.2f,
                new ColorRGBA(1f, 0.85f, 0.2f, 1f));
        weaponSelectTitle.setText("");
        weaponSelectTitle.setCullHint(Spatial.CullHint.Always);

        float wBtnW       = 640f;
        float wBtnH       = 52f;
        float wBtnSpacing = 10f;
        float wBtnX       = screenW * 0.5f - wBtnW * 0.5f;
        float wBtnStartY  = screenH * 0.72f;
        for (int i = 0; i < MAX_WEAPON_BUTTONS; i++) {
            float by = wBtnStartY - i * (wBtnH + wBtnSpacing);
            weaponButtons[i] = new Button(assetManager, guiNode, font,
                    "", wBtnX, by, wBtnW, wBtnH);
        }

        float navBtnW = 200f;
        float navBtnH = 44f;
        float navY    = wBtnStartY - MAX_WEAPON_BUTTONS * (wBtnH + wBtnSpacing) - 10f;
        weaponNavPrev = new Button(assetManager, guiNode, font,
                "[G] Prev Page",
                screenW * 0.5f - navBtnW - 10f, navY, navBtnW, navBtnH);
        weaponNavNext = new Button(assetManager, guiNode, font,
                "[F] Next Page",
                screenW * 0.5f + 10f, navY, navBtnW, navBtnH);

        // ---- Game-over restart button ----
        gameOverRestartBtn = new Button(assetManager, guiNode, font,
                "Press R to Restart",
                screenW * 0.5f - 150f, screenH * 0.35f, 300f, 52f);
    }

    // ------------------------------------------------------------------
    // Per-frame update
    // ------------------------------------------------------------------
    public void update(Player player, int score,
                       GameState state, float waveTimer,
                       boolean waitingForWave, float tpf) {

        heartsText.setText(buildHearts(player.getHearts(), player.getMaxHearts()));
        levelText.setText("Lv " + player.getLevel());
        expText.setText("EXP: " + player.getCurrentExp() + " / " + player.getExpToNextLevel()
                + "  " + buildBar(player.getCurrentExp(), player.getExpToNextLevel(), 14));
        creditsText.setText("Credits: " + player.getCredits());
        scoreText.setText("Score: " + score);
        layoutRightColumn();
    }

    /** Refreshes the elapsed-time indicator (call each frame). */
    public void updateElapsedTime(float elapsedSeconds) {
        int totalSec = (int) elapsedSeconds;
        int minutes  = totalSec / 60;
        int seconds  = totalSec % 60;
        timeText.setText(String.format("%02d:%02d", minutes, seconds));
        layoutRightColumn();
    }

    /**
     * Legacy method kept for backward compatibility.
     */
    public void updateTimeAndVision(float elapsedSeconds, boolean circleVision) {
        updateElapsedTime(elapsedSeconds);
    }

    public void setBlackHoleStatus(String statusText) {
        blackHoleText.setText(statusText);
        layoutRightColumn();
    }

    // ------------------------------------------------------------------
    // Resize support
    // ------------------------------------------------------------------
    public void onResize(int w, int h) {
        this.screenW = w;
        this.screenH = h;
        layoutStaticElements();
        if (!difficultyTitle.getText().isEmpty()) {
            centreText(difficultyTitle, screenH * 0.62f);
        }
        if (!settingsTitle.getText().isEmpty()) {
            centreText(settingsTitle, screenH * 0.68f);
        }
        if (!weaponSelectTitle.getText().isEmpty()) {
            centreText(weaponSelectTitle, screenH * 0.85f);
        }
        recenterMessage();
    }

    // ------------------------------------------------------------------
    // Difficulty select
    // ------------------------------------------------------------------
    public void showDifficultySelect() {
        difficultyTitle.setText("SELECT DIFFICULTY");
        centreText(difficultyTitle, screenH * 0.62f);
        difficultyTitle.setCullHint(Spatial.CullHint.Never);
        difficultyPanel.show();
    }

    public void hideDifficultySelect() {
        difficultyTitle.setText("");
        difficultyTitle.setCullHint(Spatial.CullHint.Always);
        difficultyPanel.hide();
    }

    public int getDifficultyClickedOption(float mx, float my) {
        return difficultyPanel.getClickedButton(mx, my);
    }

    public void updateDifficultyHover(float mx, float my) {
        difficultyPanel.updateHover(mx, my);
    }

    // ------------------------------------------------------------------
    // Weapon select
    // ------------------------------------------------------------------
    public void showWeaponSelect(WeaponType[] pageWeapons, int page, int totalPages) {
        weaponSelectTitle.setText(
                "SELECT YOUR STARTING WEAPON  (Page " + (page + 1) + " / " + totalPages + ")");
        centreText(weaponSelectTitle, screenH * 0.85f);
        weaponSelectTitle.setCullHint(Spatial.CullHint.Never);

        for (int i = 0; i < MAX_WEAPON_BUTTONS; i++) {
            if (i < pageWeapons.length) {
                WeaponType wt = pageWeapons[i];
                WeaponStats s = wt.stats;
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("[%d] %-20s  DMG:%-6.0f  RATE:%-5.1f  SPD:%.0f",
                        i + 1, wt.displayName, s.damage, s.fireRate, s.bulletSpeed));
                if (s.pelletsPerShot > 1) {
                    sb.append(String.format("  x%d pellets", s.pelletsPerShot));
                }
                if (s.pierce > 0) {
                    sb.append(String.format("  pierce:%d", s.pierce));
                }
                weaponButtons[i].setText(sb.toString());
                weaponButtons[i].show();
            } else {
                weaponButtons[i].setText("");
                weaponButtons[i].hide();
            }
        }
        weaponNavNext.show();
        weaponNavPrev.show();
    }

    public void hideWeaponSelect() {
        weaponSelectTitle.setText("");
        weaponSelectTitle.setCullHint(Spatial.CullHint.Always);
        for (Button b : weaponButtons) b.hide();
        weaponNavNext.hide();
        weaponNavPrev.hide();
    }

    public int getWeaponClickedOption(float mx, float my) {
        for (int i = 0; i < MAX_WEAPON_BUTTONS; i++) {
            if (weaponButtons[i].hitTest(mx, my)) return i;
        }
        return -1;
    }

    /** Returns {@code true} when (mx, my) lands on the "Next Page" nav button. */
    public boolean isNavNextClicked(float mx, float my) {
        return weaponNavNext.hitTest(mx, my);
    }

    /** Returns {@code true} when (mx, my) lands on the "Prev Page" nav button. */
    public boolean isNavPrevClicked(float mx, float my) {
        return weaponNavPrev.hitTest(mx, my);
    }

    public void updateWeaponSelectHover(float mx, float my) {
        for (Button b : weaponButtons) b.setHovered(b.hitTest(mx, my));
        weaponNavNext.setHovered(weaponNavNext.hitTest(mx, my));
        weaponNavPrev.setHovered(weaponNavPrev.hitTest(mx, my));
    }

    // ------------------------------------------------------------------
    // Pause menu
    // ------------------------------------------------------------------
    public void showPauseOverlay(boolean show) {
        if (show) {
            centerMessage.setColor(ColorRGBA.White);
            centerMessage.setText("PAUSED");
            subMessage.setText("P / ESC to Resume");
            recenterMessage();
            pausePanel.show();
        } else {
            centerMessage.setText("");
            subMessage.setText("");
            pausePanel.hide();
        }
    }

    /** Returns 0=Resume, 1=Restart, 2=Settings, 3=Quit, or -1. */
    public int getPauseMenuClickedOption(float mx, float my) {
        return pausePanel.getClickedButton(mx, my);
    }

    public void updatePauseMenuHover(float mx, float my) {
        pausePanel.updateHover(mx, my);
    }

    // ------------------------------------------------------------------
    // Settings menu
    // ------------------------------------------------------------------
    public void showSettingsMenu() {
        settingsTitle.setText("SETTINGS");
        centreText(settingsTitle, screenH * 0.68f);
        settingsTitle.setCullHint(Spatial.CullHint.Never);
        settingsPanel.show();
    }

    public void hideSettingsMenu() {
        settingsTitle.setText("");
        settingsTitle.setCullHint(Spatial.CullHint.Always);
        settingsPanel.hide();
    }

    /** Returns 0=Fullscreen toggle, 1=Back, or -1. */
    public int getSettingsClickedOption(float mx, float my) {
        return settingsPanel.getClickedButton(mx, my);
    }

    public void updateSettingsHover(float mx, float my) {
        settingsPanel.updateHover(mx, my);
    }

    public void setFullscreenLabel(boolean isFullscreen) {
        settingsPanel.getButton(0).setText(
                isFullscreen ? "Fullscreen: ON" : "Fullscreen: OFF");
    }

    // ------------------------------------------------------------------
    // One-shot state methods
    // ------------------------------------------------------------------
    public void setFireLockStatus(boolean locked) {
        fireLockText.setText(locked ? "[Q] FIRE LOCKED" : "");
    }

    public void showGameOverOverlay(int finalScore) {
        centerMessage.setColor(ColorRGBA.Red);
        centerMessage.setText("GAME OVER");
        subMessage.setText("Final Score: " + finalScore);
        recenterMessage();
        gameOverRestartBtn.show();
    }

    public void showBossWarning(String bossName) {
        centerMessage.setColor(new ColorRGBA(1f, 0.2f, 0.2f, 1f));
        centerMessage.setText("!! BOSS -- " + bossName + " !!");
        subMessage.setText("Defeat the boss to resume normal spawning!");
        recenterMessage();
    }

    public void reset() {
        centerMessage.setText("");
        subMessage.setText("");
        centerMessage.setColor(ColorRGBA.White);
        blackHoleText.setText("");
        fireLockText.setText("");
        pausePanel.hide();
        difficultyPanel.hide();
        difficultyTitle.setText("");
        difficultyTitle.setCullHint(Spatial.CullHint.Always);
        hideSettingsMenu();
        hideWeaponSelect();
        gameOverRestartBtn.hide();
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
        float lineH  = heartsText.getSize() * 1.6f;
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

        float sW = scoreText.getLineWidth();
        scoreText.setLocalTranslation(screenW - sW - 12f, startY - lineH, 0f);

        float bW = blackHoleText.getLineWidth();
        blackHoleText.setLocalTranslation(screenW - bW - 12f, startY - lineH * 2f, 0f);
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

    private void centreText(BitmapText t, float y) {
        float lw = t.getLineWidth();
        float x  = lw > 0f ? Math.max(0f, (screenW - lw) * 0.5f) : screenW * 0.5f - 100f;
        t.setLocalTranslation(x, y, 0f);
    }

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
