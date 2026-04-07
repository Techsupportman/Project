package com.project.levels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 * Abstract base for every level / map in the game.
 *
 * <p>A level is responsible for:
 * <ul>
 *   <li>Building its own scene geometry and attaching it to the root node</li>
 *   <li>Exposing spawn-point information used by the wave manager</li>
 * </ul>
 *
 * <p>To add a new level, extend this class, implement {@link #build()}, and
 * pass an instance to {@link LevelManager#loadLevel(Level)}.
 */
public abstract class Level {

    /** Scene node owned by this level — all level geometry is attached here. */
    protected final Node levelNode;

    protected final AssetManager assetManager;
    protected final Node rootNode;

    protected Level(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode     = rootNode;
        this.levelNode    = new Node("level_" + getClass().getSimpleName());
    }

    /**
     * Constructs all scene geometry and attaches it to {@link #levelNode}.
     * Called by {@link LevelManager#loadLevel(Level)}.
     */
    public abstract void build();

    /**
     * Removes all level geometry from the root scene.
     * Called by {@link LevelManager#unloadCurrent()}.
     */
    public void unload() {
        rootNode.detachChild(levelNode);
    }

    /** @return the name displayed in the HUD or used for debugging. */
    public abstract String getLevelName();
}
