package com.project.levels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 * Manages the active level: loading, transitioning, and unloading.
 *
 * <p>For the current MVP there is only one level ({@link Level1}), but this
 * class is structured so that additional levels can be swapped in without
 * changing any calling code.
 */
public class LevelManager {

    private final AssetManager assetManager;
    private final Node rootNode;
    private Level currentLevel;

    public LevelManager(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode     = rootNode;
    }

    /**
     * Builds and makes the given level active.  Any previously loaded level
     * is unloaded first.
     *
     * @param level the level to load
     */
    public void loadLevel(Level level) {
        unloadCurrent();
        currentLevel = level;
        currentLevel.build();
    }

    /** Removes the current level's geometry from the scene (if any). */
    public void unloadCurrent() {
        if (currentLevel != null) {
            currentLevel.unload();
            currentLevel = null;
        }
    }

    /**
     * Reloads the current level from scratch (re-runs {@link Level#build()}).
     * Useful for restarting the game without needing to recreate the manager.
     */
    public void reloadCurrent() {
        if (currentLevel != null) {
            currentLevel.unload();
            currentLevel.build();
        }
    }

    /** @return the currently active level, or {@code null} if none is loaded. */
    public Level getCurrentLevel() {
        return currentLevel;
    }
}
