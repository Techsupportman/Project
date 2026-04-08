package com.project.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;

/**
 * A reusable GUI button with a translucent Quad background and a centred
 * BitmapText label, rendered in the guiNode.
 *
 * <p>Z-ordering: background at z=0, label at z=1.
 */
public class Button {

    private static final ColorRGBA COLOR_BG_NORMAL   = new ColorRGBA(0.15f, 0.15f, 0.25f, 0.92f);
    private static final ColorRGBA COLOR_BG_HOVER    = new ColorRGBA(0.35f, 0.35f, 0.60f, 0.97f);
    private static final ColorRGBA COLOR_TEXT_NORMAL = ColorRGBA.White;
    private static final ColorRGBA COLOR_TEXT_HOVER  = new ColorRGBA(1f, 1f, 0.3f, 1f);

    private final Geometry   bgGeo;
    private final BitmapText label;
    private final Material   bgMat;

    // Mutable so setPosition() works
    private float x, y;
    private final float w, h;

    /**
     * Creates a new button and attaches it to {@code guiNode}.
     *
     * @param am      AssetManager used to create the background material
     * @param guiNode scene-graph node to attach button elements to
     * @param font    font used for the label
     * @param text    initial button label text
     * @param x       left edge in screen-space pixels
     * @param y       bottom edge in screen-space pixels (jME GUI: y=0 at bottom)
     * @param w       button width in pixels
     * @param h       button height in pixels
     */
    public Button(AssetManager am, Node guiNode, BitmapFont font,
                  String text, float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        // Background quad (z=0)
        Quad quad = new Quad(w, h);
        bgGeo = new Geometry("btn-bg", quad);
        bgMat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", COLOR_BG_NORMAL.clone());
        bgMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bgGeo.setMaterial(bgMat);
        bgGeo.setQueueBucket(RenderQueue.Bucket.Gui);
        bgGeo.setLocalTranslation(x, y, 0f);
        guiNode.attachChild(bgGeo);

        // Label (z=1)
        label = new BitmapText(font, false);
        label.setSize(font.getCharSet().getRenderedSize() * 1.4f);
        label.setColor(COLOR_TEXT_NORMAL.clone());
        label.setText(text);
        label.setQueueBucket(RenderQueue.Bucket.Gui);
        guiNode.attachChild(label);

        centerLabel();
        hide();
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public void show() {
        bgGeo.setCullHint(Spatial.CullHint.Never);
        label.setCullHint(Spatial.CullHint.Never);
    }

    public void hide() {
        bgGeo.setCullHint(Spatial.CullHint.Always);
        label.setCullHint(Spatial.CullHint.Always);
    }

    public void setHovered(boolean hovered) {
        bgMat.setColor("Color", hovered ? COLOR_BG_HOVER : COLOR_BG_NORMAL);
        label.setColor(hovered ? COLOR_TEXT_HOVER : COLOR_TEXT_NORMAL);
    }

    /** Returns {@code true} when (mx, my) is within the button bounds. */
    public boolean hitTest(float mx, float my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public void setText(String text) {
        label.setText(text);
        centerLabel();
    }

    /** Repositions both the background quad and the label. */
    public void setPosition(float nx, float ny) {
        this.x = nx;
        this.y = ny;
        bgGeo.setLocalTranslation(nx, ny, 0f);
        centerLabel();
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private void centerLabel() {
        float lw = label.getLineWidth();
        float lh = label.getSize();
        // Fallback to a small indent if getLineWidth() returns 0 before first render
        float lx = lw > 0f ? x + (w - lw) * 0.5f : x + 8f;
        float ly = y + h * 0.5f + lh * 0.35f;
        label.setLocalTranslation(lx, ly, 1f);
    }
}
