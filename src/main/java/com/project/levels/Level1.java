package com.project.levels;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.project.assets.MaterialFactory;
import com.project.utils.Constants;

/**
 * The first (and currently only) playable level.
 *
 * <p>The arena consists of:
 * <ul>
 *   <li>A dark floor tile covering the full play area</li>
 *   <li>Visible boundary walls along all four edges</li>
 *   <li>A simple grid pattern (thin dark lines) drawn on the floor to help
 *       gauge distances</li>
 * </ul>
 *
 * <p>All geometry here is placeholder art.  Replace with actual tile-map
 * loading once level assets exist.
 */
public class Level1 extends Level {

    // Colour palette
    private static final ColorRGBA COLOR_FLOOR = new ColorRGBA(0.28f, 0.32f, 0.40f, 1f);
    private static final ColorRGBA COLOR_WALL  = new ColorRGBA(0.55f, 0.58f, 0.70f, 1f);
    private static final ColorRGBA COLOR_GRID  = new ColorRGBA(0.22f, 0.26f, 0.34f, 1f);

    public Level1(AssetManager assetManager, Node rootNode) {
        super(assetManager, rootNode);
    }

    // ------------------------------------------------------------------
    // Level.build()
    // ------------------------------------------------------------------
    @Override
    public void build() {
        buildFloor();
        buildGridLines();
        buildWalls();
        rootNode.attachChild(levelNode);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------
    private void buildFloor() {
        // Main floor — slightly below Y = 0 so entities sit on top
        Box floorMesh = new Box(Constants.LEVEL_HALF_WIDTH, 0.02f, Constants.LEVEL_HALF_HEIGHT);
        Geometry floor = new Geometry("floor", floorMesh);
        floor.setMaterial(MaterialFactory.createColorMaterial(assetManager, COLOR_FLOOR));
        floor.setLocalTranslation(0f, -0.05f, 0f);
        levelNode.attachChild(floor);
    }

    private void buildGridLines() {
        // Horizontal grid lines every 2 world units
        float lineThickness = 0.03f;
        float lineHeight    = 0.01f;

        for (float z = -Constants.LEVEL_HALF_HEIGHT + 2f;
             z < Constants.LEVEL_HALF_HEIGHT;
             z += 2f) {
            Box mesh = new Box(Constants.LEVEL_HALF_WIDTH, lineHeight, lineThickness);
            Geometry line = new Geometry("hline_" + z, mesh);
            line.setMaterial(MaterialFactory.createColorMaterial(assetManager, COLOR_GRID));
            line.setLocalTranslation(0f, -0.03f, z);
            levelNode.attachChild(line);
        }

        // Vertical grid lines every 2 world units
        for (float x = -Constants.LEVEL_HALF_WIDTH + 2f;
             x < Constants.LEVEL_HALF_WIDTH;
             x += 2f) {
            Box mesh = new Box(lineThickness, lineHeight, Constants.LEVEL_HALF_HEIGHT);
            Geometry line = new Geometry("vline_" + x, mesh);
            line.setMaterial(MaterialFactory.createColorMaterial(assetManager, COLOR_GRID));
            line.setLocalTranslation(x, -0.03f, 0f);
            levelNode.attachChild(line);
        }
    }

    private void buildWalls() {
        float hw  = Constants.LEVEL_HALF_WIDTH;
        float hh  = Constants.LEVEL_HALF_HEIGHT;
        float wt  = 0.4f;   // wall thickness
        float wh  = 0.35f;  // wall height (visible bump)

        // Left wall
        addWall("wall_left",   -hw - wt, 0f,  wt,  hh + wt * 2f, wh);
        // Right wall
        addWall("wall_right",   hw + wt, 0f,  wt,  hh + wt * 2f, wh);
        // Top wall
        addWall("wall_top",     0f, -hh - wt, hw + wt * 2f, wt, wh);
        // Bottom wall
        addWall("wall_bottom",  0f,  hh + wt, hw + wt * 2f, wt, wh);
    }

    private void addWall(String name, float x, float z,
                         float halfX, float halfZ, float halfY) {
        Box mesh = new Box(halfX, halfY, halfZ);
        Geometry wall = new Geometry(name, mesh);
        wall.setMaterial(MaterialFactory.createColorMaterial(assetManager, COLOR_WALL));
        wall.setLocalTranslation(x, 0f, z);
        levelNode.attachChild(wall);
    }

    // ------------------------------------------------------------------
    // Level metadata
    // ------------------------------------------------------------------
    @Override
    public String getLevelName() {
        return "Level 1 - The Arena";
    }
}
