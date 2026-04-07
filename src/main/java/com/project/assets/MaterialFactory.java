package com.project.assets;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;

/**
 * Factory helpers for creating jMonkeyEngine {@link Material} instances.
 *
 * <p>All placeholder geometry uses the built-in {@code Unshaded} material
 * definition so the game looks the same regardless of scene lighting.
 */
public final class MaterialFactory {

    private MaterialFactory() {}

    /**
     * Creates an unshaded, solid-colour {@link Material}.
     *
     * @param assetManager jMonkey asset manager
     * @param color        fill colour
     * @return             a ready-to-use material
     */
    public static Material createColorMaterial(AssetManager assetManager, ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    /**
     * Creates an unshaded wireframe {@link Material} — useful for collision
     * debug visualisations or wall outlines.
     *
     * @param assetManager jMonkey asset manager
     * @param color        line colour
     * @return             a ready-to-use wireframe material
     */
    public static Material createWireframeMaterial(AssetManager assetManager, ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setWireframe(true);
        return mat;
    }

    /**
     * Creates a translucent colour material.  The geometry's render bucket must
     * be set to {@code Transparent} separately if alpha blending is needed.
     *
     * @param assetManager jMonkey asset manager
     * @param color        colour with desired alpha
     * @return             a ready-to-use translucent material
     */
    public static Material createTranslucentMaterial(AssetManager assetManager, ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        return mat;
    }
}
