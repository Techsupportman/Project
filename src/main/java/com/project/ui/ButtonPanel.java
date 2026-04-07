package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;

/**
 * A vertically-stacked array of {@link Button}s with a dark, semi-transparent
 * background panel rendered behind them.
 *
 * <p>Layout: buttons are stacked top-to-bottom with {@code spacing} pixels
 * between them and {@code PADDING} pixels between the outer edges and the
 * nearest button edge.
 */
public class ButtonPanel {

    private static final float PADDING = 30f;

    private final Button[]  buttons;
    private final Geometry  bgGeo;

    /**
     * Creates a new ButtonPanel and attaches all elements to {@code guiNode}.
     *
     * @param am      AssetManager for creating materials
     * @param guiNode scene-graph node to attach elements to
     * @param font    font for button labels
     * @param centerX horizontal centre of the panel in screen-space pixels
     * @param centerY vertical centre of the panel in screen-space pixels
     * @param btnW    width of each button in pixels
     * @param btnH    height of each button in pixels
     * @param spacing gap between consecutive buttons in pixels
     * @param labels  button labels (one per button, top to bottom)
     */
    public ButtonPanel(AssetManager am, Node guiNode, BitmapFont font,
                       float centerX, float centerY,
                       float btnW, float btnH, float spacing,
                       String... labels) {
        int n = labels.length;
        buttons = new Button[n];

        float totalH = n * btnH + (n - 1) * spacing + 2f * PADDING;
        float totalW = btnW + 2f * PADDING;

        float panelX = centerX - totalW * 0.5f;
        float panelY = centerY - totalH * 0.5f;

        // Dark semi-transparent background (z=0)
        Quad quad = new Quad(totalW, totalH);
        bgGeo = new Geometry("panel-bg", quad);
        Material bgMat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.05f, 0.05f, 0.10f, 0.85f));
        bgMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bgGeo.setMaterial(bgMat);
        bgGeo.setQueueBucket(RenderQueue.Bucket.Gui);
        bgGeo.setLocalTranslation(panelX, panelY, 0f);
        guiNode.attachChild(bgGeo);

        // Buttons stacked top-to-bottom (highest y first in jME's GUI coords)
        float firstBtnY = panelY + totalH - PADDING - btnH;
        for (int i = 0; i < n; i++) {
            float bx = panelX + PADDING;
            float by = firstBtnY - i * (btnH + spacing);
            buttons[i] = new Button(am, guiNode, font, labels[i], bx, by, btnW, btnH);
        }

        hide();
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public void show() {
        bgGeo.setCullHint(Spatial.CullHint.Never);
        for (Button b : buttons) b.show();
    }

    public void hide() {
        bgGeo.setCullHint(Spatial.CullHint.Always);
        for (Button b : buttons) b.hide();
    }

    /**
     * Updates hover state for all buttons based on the current cursor position.
     *
     * @param mx cursor X in screen-space pixels
     * @param my cursor Y in screen-space pixels
     * @return index of the hovered button, or -1 if none
     */
    public int updateHover(float mx, float my) {
        int hovered = -1;
        for (int i = 0; i < buttons.length; i++) {
            boolean h = buttons[i].hitTest(mx, my);
            buttons[i].setHovered(h);
            if (h) hovered = i;
        }
        return hovered;
    }

    /**
     * Returns the index of the button the cursor is over, or -1 if none.
     * Does not change hover state — use {@link #updateHover} for that.
     */
    public int getClickedButton(float mx, float my) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].hitTest(mx, my)) return i;
        }
        return -1;
    }

    /** Returns the {@link Button} at index {@code i}. */
    public Button getButton(int i) { return buttons[i]; }

    /** Returns the number of buttons in this panel. */
    public int size() { return buttons.length; }
}
