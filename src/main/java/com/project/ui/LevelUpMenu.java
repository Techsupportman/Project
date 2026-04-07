package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.project.upgrades.Upgrade;

import java.util.List;

/**
 * Overlay displayed when the player levels up.
 *
 * <p>Shows a centred panel with:
 * <ul>
 *   <li>A "LEVEL UP!" banner.</li>
 *   <li>Up to 5 upgrade options, each with a number key hint.</li>
 *   <li>Reroll and Delete hints at the bottom.</li>
 * </ul>
 *
 * <p>The menu is hidden by default; call {@link #show(int, List, int, int)} to
 * display it and {@link #hide()} when the player has made their choice.
 */
public class LevelUpMenu {

    private static final ColorRGBA COLOR_CHOICE_NORMAL  = ColorRGBA.White;
    private static final ColorRGBA COLOR_CHOICE_HOVER   = new ColorRGBA(1f, 1f, 0.3f, 1f);
    private static final ColorRGBA COLOR_REROLL_NORMAL  = new ColorRGBA(0.4f, 0.9f, 1.0f, 1f);
    private static final ColorRGBA COLOR_REROLL_HOVER   = ColorRGBA.White;
    private static final ColorRGBA COLOR_DELETE_NORMAL  = new ColorRGBA(1.0f, 0.5f, 0.5f, 1f);
    private static final ColorRGBA COLOR_DELETE_HOVER   = ColorRGBA.White;

    private static final int MAX_CHOICES = 5;

    private final int screenW;
    private final int screenH;

    private final BitmapText        headerText;
    private final BitmapText[]      choiceTexts = new BitmapText[MAX_CHOICES];
    private final BitmapText        rerollText;
    private final BitmapText        deleteText;
    private final BitmapText        visionHint;

    private boolean visible = false;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public LevelUpMenu(AssetManager assetManager, Node guiNode, AppSettings settings) {
        this.screenW = settings.getWidth();
        this.screenH = settings.getHeight();

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        headerText = makeText(font, guiNode, 3.2f, new ColorRGBA(1f, 0.9f, 0.2f, 1f));
        for (int i = 0; i < MAX_CHOICES; i++) {
            choiceTexts[i] = makeText(font, guiNode, 1.5f, ColorRGBA.White);
        }
        rerollText  = makeText(font, guiNode, 1.3f, new ColorRGBA(0.4f, 0.9f, 1.0f, 1f));
        deleteText  = makeText(font, guiNode, 1.3f, new ColorRGBA(1.0f, 0.5f, 0.5f, 1f));
        visionHint  = makeText(font, guiNode, 1.1f, new ColorRGBA(0.6f, 0.6f, 0.6f, 1f));

        hide(); // start hidden
    }

    // ------------------------------------------------------------------
    // Show / hide
    // ------------------------------------------------------------------
    /**
     * Displays the level-up menu with the given choices.
     *
     * @param newLevel    the level just reached
     * @param choices     list of upgrade options (3-5 items)
     * @param rerollsLeft remaining rerolls this level (0 or 1)
     * @param deletesLeft remaining deletes this level (0 or 1)
     */
    public void show(int newLevel, List<Upgrade> choices, int rerollsLeft, int deletesLeft) {
        visible = true;

        headerText.setText("LEVEL UP!  →  Level " + newLevel);
        centre(headerText, screenH * 0.80f);

        for (int i = 0; i < MAX_CHOICES; i++) {
            if (i < choices.size()) {
                Upgrade u = choices.get(i);
                choiceTexts[i].setText("[" + (i + 1) + "]  " + u.name
                        + "  —  " + u.description);
                centre(choiceTexts[i], screenH * (0.70f - i * 0.07f));
                choiceTexts[i].setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            } else {
                choiceTexts[i].setText("");
                choiceTexts[i].setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            }
        }

        float bottomY = screenH * (0.70f - choices.size() * 0.07f) - 40f;
        rerollText.setText(rerollsLeft > 0
                ? "[F]  Reroll choices  (" + rerollsLeft + " left)"
                : "[F]  Reroll  (none left)");
        centre(rerollText, bottomY);
        rerollText.setCullHint(com.jme3.scene.Spatial.CullHint.Never);

        deleteText.setText(deletesLeft > 0
                ? "[G]  Skip / Delete  (" + deletesLeft + " left)"
                : "[G]  Skip  (none left)");
        centre(deleteText, bottomY - rerollText.getSize() * 1.5f);
        deleteText.setCullHint(com.jme3.scene.Spatial.CullHint.Never);

        visionHint.setText("Click or press 1-" + choices.size() + " to pick an upgrade");
        centre(visionHint, bottomY - rerollText.getSize() * 3.2f);
        visionHint.setCullHint(com.jme3.scene.Spatial.CullHint.Never);

        headerText.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
    }

    /** Hides all menu elements. */
    public void hide() {
        visible = false;
        headerText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        for (BitmapText t : choiceTexts) {
            t.setColor(COLOR_CHOICE_NORMAL);
            t.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        }
        rerollText.setColor(COLOR_REROLL_NORMAL);
        rerollText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        deleteText.setColor(COLOR_DELETE_NORMAL);
        deleteText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        visionHint.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }

    // ------------------------------------------------------------------
    // Mouse interaction
    // ------------------------------------------------------------------

    /**
     * Updates hover highlighting based on the current mouse cursor position.
     * Call every frame while the menu is visible.
     *
     * @param mx           cursor X in screen space
     * @param my           cursor Y in screen space
     * @param activeChoices number of currently displayed upgrade choices
     */
    public void updateHover(float mx, float my, int activeChoices) {
        if (!visible) return;
        for (int i = 0; i < MAX_CHOICES; i++) {
            if (i < activeChoices) {
                choiceTexts[i].setColor(
                        hitTest(choiceTexts[i], mx, my) ? COLOR_CHOICE_HOVER : COLOR_CHOICE_NORMAL);
            }
        }
        rerollText.setColor(hitTest(rerollText, mx, my) ? COLOR_REROLL_HOVER : COLOR_REROLL_NORMAL);
        deleteText.setColor(hitTest(deleteText, mx, my) ? COLOR_DELETE_HOVER : COLOR_DELETE_NORMAL);
    }

    /**
     * Returns the 0-based index of the upgrade choice that the cursor is over,
     * or -1 if no choice is hovered.
     *
     * @param mx           cursor X in screen space
     * @param my           cursor Y in screen space
     * @param activeChoices number of currently displayed upgrade choices
     */
    public int getClickedChoice(float mx, float my, int activeChoices) {
        for (int i = 0; i < activeChoices; i++) {
            if (hitTest(choiceTexts[i], mx, my)) return i;
        }
        return -1;
    }

    /** Returns {@code true} when the cursor is over the Reroll button. */
    public boolean isRerollClicked(float mx, float my) {
        return hitTest(rerollText, mx, my);
    }

    /** Returns {@code true} when the cursor is over the Delete/Skip button. */
    public boolean isDeleteClicked(float mx, float my) {
        return hitTest(deleteText, mx, my);
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

    private void centre(BitmapText t, float y) {
        float x = Math.max(0f, (screenW - t.getLineWidth()) * 0.5f);
        t.setLocalTranslation(x, y, 0f);
    }

    private boolean hitTest(BitmapText text, float mx, float my) {
        if (text.getText().isEmpty()) return false;
        float x   = text.getLocalTranslation().x;
        float y   = text.getLocalTranslation().y;
        float w   = text.getLineWidth();
        float h   = text.getSize() * 1.4f;
        float pad = 8f;
        return mx >= x - pad && mx <= x + w + pad
            && my >= y - pad && my <= y + h + pad;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public boolean isVisible() { return visible; }
}
