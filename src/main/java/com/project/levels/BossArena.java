package com.project.levels;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import com.project.assets.MaterialFactory;

/**
 * Seals the play area during a boss encounter.
 *
 * <p>When a full boss spawns, a {@code BossArena} is created centred on the
 * player's current position. It adds three visual layers to the scene:
 * <ol>
 *   <li>A large <em>danger-zone</em> red floor covering everything outside the
 *       ring (200 × 200 world units).</li>
 *   <li>A circular <em>safe-zone</em> floor matching the normal level colour
 *       inside the ring.</li>
 *   <li>A polygon of bright-red box segments forming the visible ring
 *       wall.</li>
 * </ol>
 *
 * <p>The player's movement must be clamped to {@link #ARENA_RADIUS} by the
 * caller (see {@link #clampToArena(float, float)}).
 *
 * <p>Call {@link #destroy(Node)} when the boss is defeated to remove all
 * arena geometry.
 */
public class BossArena {

    /** Radius in world units of the playable boss arena. */
    public static final float ARENA_RADIUS = 22f;

    private static final int   RING_SEGMENTS   = 36;
    private static final float RING_THICKNESS  = 0.5f;
    private static final float RING_HEIGHT     = 0.5f;

    private static final ColorRGBA COLOR_DANGER = new ColorRGBA(0.48f, 0.04f, 0.04f, 1f);
    private static final ColorRGBA COLOR_SAFE   = new ColorRGBA(0.28f, 0.32f, 0.40f, 1f);
    private static final ColorRGBA COLOR_RING   = new ColorRGBA(1.0f,  0.15f, 0.15f, 1f);

    private final Node  arenaNode;
    private final float centerX;
    private final float centerZ;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public BossArena(AssetManager assetManager, Node rootNode, float centerX, float centerZ) {
        this.centerX   = centerX;
        this.centerZ   = centerZ;
        this.arenaNode = new Node("bossArena");

        buildDangerFloor(assetManager);
        buildSafeFloor(assetManager);
        buildRingBorder(assetManager);

        rootNode.attachChild(arenaNode);
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float getCenterX() { return centerX; }
    public float getCenterZ() { return centerZ; }
    public float getRadius()  { return ARENA_RADIUS; }

    /**
     * Clamps a world-space position to within the arena circle.
     * A small inward margin keeps the player from touching the ring wall.
     *
     * @param x candidate X position
     * @param z candidate Z position
     * @return  clamped {@code [x, z]} array
     */
    public float[] clampToArena(float x, float z) {
        float dx   = x - centerX;
        float dz   = z - centerZ;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        float limit = ARENA_RADIUS - 1.2f;          // 1.2 world-unit margin from ring
        if (dist > limit) {
            float scale = limit / dist;
            x = centerX + dx * scale;
            z = centerZ + dz * scale;
        }
        return new float[]{x, z};
    }

    /**
     * Removes all arena geometry from the scene graph.
     * Call this when the boss is defeated.
     */
    public void destroy(Node rootNode) {
        rootNode.detachChild(arenaNode);
    }

    // ------------------------------------------------------------------
    // Geometry builders
    // ------------------------------------------------------------------

    /** Large red plane covering the danger zone outside the ring. */
    private void buildDangerFloor(AssetManager am) {
        // Size is capped to 10× ARENA_RADIUS to stay proportional while still
        // covering the full visible area (camera view is much smaller than the arena).
        float dangerHalf = ARENA_RADIUS * 10f;
        Box dangerMesh = new Box(dangerHalf, 0.01f, dangerHalf);
        Geometry danger = new Geometry("dangerFloor", dangerMesh);
        danger.setMaterial(MaterialFactory.createColorMaterial(am, COLOR_DANGER));
        danger.setLocalTranslation(centerX, -0.04f, centerZ);
        arenaNode.attachChild(danger);
    }

    /**
     * Normal-coloured circular floor covering the safe zone inside the ring.
     * Built as a triangle-fan mesh to approximate a circle.
     */
    private void buildSafeFloor(AssetManager am) {
        int segs = 48;
        Geometry circle = createCircleGeometry(am, "safeFloor", ARENA_RADIUS, segs,
                -0.03f, COLOR_SAFE);
        circle.setLocalTranslation(centerX, 0f, centerZ);
        arenaNode.attachChild(circle);
    }

    /**
     * Polygon of thin box segments arranged in a circle to form the ring wall.
     * Each segment is rotated to be tangential to the ring.
     */
    private void buildRingBorder(AssetManager am) {
        float segArc = FastMath.TWO_PI / RING_SEGMENTS;
        // slight overlap prevents gaps between segments
        float segLen = 2f * ARENA_RADIUS * FastMath.sin(segArc / 2f) + 0.08f;

        for (int i = 0; i < RING_SEGMENTS; i++) {
            float midAngle = i * segArc + segArc / 2f;
            float bx = (float) Math.cos(midAngle) * ARENA_RADIUS;
            float bz = (float) Math.sin(midAngle) * ARENA_RADIUS;

            Box segMesh = new Box(RING_THICKNESS / 2f, RING_HEIGHT / 2f, segLen / 2f);
            Geometry seg = new Geometry("ringSegment_" + i, segMesh);
            seg.setMaterial(MaterialFactory.createColorMaterial(am, COLOR_RING));

            // Rotate so each segment is tangential (its local Z faces along the ring)
            Quaternion rot = new Quaternion();
            rot.fromAngleAxis(midAngle, Vector3f.UNIT_Y);
            seg.setLocalRotation(rot);
            seg.setLocalTranslation(centerX + bx, RING_HEIGHT / 2f, centerZ + bz);

            arenaNode.attachChild(seg);
        }
    }

    // ------------------------------------------------------------------
    // Mesh helpers
    // ------------------------------------------------------------------

    /**
     * Creates a filled-circle (triangle-fan) {@link Geometry} lying in the XZ plane.
     *
     * @param am       asset manager
     * @param name     geometry name
     * @param radius   circle radius
     * @param segments number of triangle slices (higher = smoother)
     * @param yOffset  Y offset of all vertices in local space
     * @param color    fill colour
     * @return         ready-to-use geometry (local translation not set)
     */
    private static Geometry createCircleGeometry(AssetManager am, String name,
                                                  float radius, int segments,
                                                  float yOffset, ColorRGBA color) {
        int vtxCount = segments + 1;          // centre + ring vertices
        float[] verts = new float[vtxCount * 3];
        int[]   idx   = new int[segments * 3];

        // Centre vertex
        verts[0] = 0f; verts[1] = yOffset; verts[2] = 0f;

        for (int i = 0; i < segments; i++) {
            float angle = (float) i / segments * FastMath.TWO_PI;
            verts[(i + 1) * 3]     = (float) Math.cos(angle) * radius;
            verts[(i + 1) * 3 + 1] = yOffset;
            verts[(i + 1) * 3 + 2] = (float) Math.sin(angle) * radius;

            idx[i * 3]     = 0;                        // centre
            idx[i * 3 + 1] = i + 1;
            idx[i * 3 + 2] = (i + 1) % segments + 1;  // wrap last triangle
        }

        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(verts));
        mesh.setBuffer(VertexBuffer.Type.Index,    3, BufferUtils.createIntBuffer(idx));
        mesh.updateBound();

        Geometry geo = new Geometry(name, mesh);
        geo.setMaterial(MaterialFactory.createColorMaterial(am, color));
        return geo;
    }
}
