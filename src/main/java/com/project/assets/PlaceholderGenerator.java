package com.project.assets;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Creates labelled placeholder assets (flat coloured boxes with a text label
 * facing upward) so that every visual element has a clear "replace me" marker
 * while real sprites are not yet available.
 *
 * <p>The text is rotated −90 ° around the X axis so it lies flat in the XZ
 * plane and is fully readable from the top-down orthographic camera.
 *
 * <h3>Replacement guide</h3>
 * When real art is ready, replace the call to
 * {@link #createPlaceholderNode} in the relevant entity class with actual
 * model / texture loading.  The {@code label} string indicates which asset
 * belongs in each slot.
 */
public final class PlaceholderGenerator {

    private PlaceholderGenerator() {}

    /**
     * Creates a {@link Node} containing:
     * <ul>
     *   <li>A flat {@link Box} geometry with the given colour</li>
     *   <li>A {@link BitmapText} label rotated to face the top-down camera</li>
     * </ul>
     *
     * @param assetManager jMonkey asset manager
     * @param label        text shown on the placeholder (e.g. {@code "PLAYER_SPRITE"})
     * @param halfX        half-extent on the X axis
     * @param halfZ        half-extent on the Z axis
     * @param color        fill colour for the box
     * @return             a scene {@link Node} ready to attach to the root node
     */
    public static Node createPlaceholderNode(AssetManager assetManager,
                                             String label,
                                             float halfX,
                                             float halfZ,
                                             ColorRGBA color) {
        Node node = new Node("placeholder_" + label);

        // Flat coloured box (thin in Y so it looks 2-D from above)
        Box box = new Box(halfX, 0.05f, halfZ);
        Geometry geo = new Geometry("box_" + label, box);
        geo.setMaterial(MaterialFactory.createColorMaterial(assetManager, color));
        node.attachChild(geo);

        // Text label rotated to face upward so it is visible from the top-down camera
        BitmapFont font   = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText text   = new BitmapText(font, false);
        text.setSize(0.22f);
        text.setText(label);
        text.setColor(ColorRGBA.White);

        // −90 ° around X: text normal flips from +Z to +Y (facing the sky / camera)
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);
        text.setLocalRotation(rot);

        // Nudge slightly above the box surface so it is not z-fighting
        text.setLocalTranslation(-halfX * 0.8f, 0.07f, halfZ * 0.5f);

        node.attachChild(text);
        return node;
    }
}
