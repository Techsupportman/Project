package com.project.core;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.project.entities.Enemy;
import com.project.entities.Player;
import com.project.levels.Level1;
import com.project.levels.LevelManager;
import com.project.systems.AISystem;
import com.project.systems.CombatSystem;
import com.project.systems.PhysicsSystem;
import com.project.ui.UIManager;
import com.project.utils.Constants;
import com.project.utils.InputHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main jMonkeyEngine application.  Extends {@link SimpleApplication} and acts
 * as the top-level coordinator for all game systems.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Camera setup (top-down orthographic)</li>
 *   <li>Scene graph initialisation (level, player, enemies)</li>
 *   <li>Forwarding per-frame updates to each system</li>
 *   <li>Game-state transitions (PLAYING ↔ PAUSED, PLAYING → GAME OVER)</li>
 *   <li>Wave spawning</li>
 * </ul>
 *
 * <p>Heavy game-logic (wave counters, score) is delegated to
 * {@link GameEngine}.
 */
public class GameApp extends SimpleApplication {

    // ------------------------------------------------------------------
    // Game entities
    // ------------------------------------------------------------------
    private Player       player;
    private List<Enemy>  enemies;

    // ------------------------------------------------------------------
    // Systems
    // ------------------------------------------------------------------
    private AISystem       aiSystem;
    private CombatSystem   combatSystem;
    private PhysicsSystem  physicsSystem;

    // ------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------
    private UIManager uiManager;

    // ------------------------------------------------------------------
    // Level
    // ------------------------------------------------------------------
    private LevelManager levelManager;

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------
    private InputHandler inputHandler;

    // ------------------------------------------------------------------
    // Game logic
    // ------------------------------------------------------------------
    private GameEngine  gameEngine;
    private GameState   gameState;
    private final Random random = new Random();

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public GameApp() {
        // Pass an empty array to suppress the default FlyCamAppState,
        // DebugKeysAppState and StatsAppState that SimpleApplication adds.
        super(/* no default app-states */);
    }

    // ------------------------------------------------------------------
    // SimpleApplication lifecycle
    // ------------------------------------------------------------------
    @Override
    public void simpleInitApp() {
        // Suppress the built-in fly-cam so it doesn't fight our fixed camera
        flyCam.setEnabled(false);

        setupCamera();
        setupLighting();

        // Initialise containers
        enemies    = new ArrayList<>();
        gameEngine = new GameEngine();

        // Input
        inputHandler = new InputHandler(inputManager);

        // Level
        levelManager = new LevelManager(assetManager, rootNode);
        levelManager.loadLevel(new Level1(assetManager, rootNode));

        // Player
        player = new Player(assetManager, 0f, 0f);
        rootNode.attachChild(player.getNode());

        // Systems
        aiSystem      = new AISystem();
        combatSystem  = new CombatSystem();
        physicsSystem = new PhysicsSystem();

        // UI
        uiManager = new UIManager(assetManager, guiNode, settings);

        // Begin in PLAYING state and kick off the first wave immediately
        gameState = GameState.PLAYING;
        spawnWave();
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Pause toggle is handled regardless of current state
        if (inputHandler.isPausePressed()) {
            togglePause();
            return;
        }

        switch (gameState) {
            case PLAYING  -> updatePlaying(tpf);
            case GAME_OVER -> {
                if (inputHandler.isRestartPressed()) restartGame();
            }
            default -> { /* PAUSED — do nothing */ }
        }
    }

    // ------------------------------------------------------------------
    // Per-frame game logic (PLAYING state only)
    // ------------------------------------------------------------------
    private void updatePlaying(float tpf) {

        // --- Player movement ---
        float dx = 0f, dz = 0f;
        if (inputHandler.isMoveLeft())  dx -= 1f;
        if (inputHandler.isMoveRight()) dx += 1f;
        if (inputHandler.isMoveUp())    dz -= 1f;
        if (inputHandler.isMoveDown())  dz += 1f;

        // Normalise diagonal movement so speed is consistent
        if (dx != 0f && dz != 0f) {
            float inv = (float) (1.0 / Math.sqrt(dx * dx + dz * dz));
            dx *= inv;
            dz *= inv;
        }

        player.move(dx, dz, tpf);
        player.update(tpf);

        // --- Player attack ---
        if (inputHandler.isAttackPressed() && player.canAttack()) {
            player.attack();
            combatSystem.playerAttack(player, enemies);
        }

        // --- Enemy updates ---
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isActive()) {
                rootNode.detachChild(e.getNode());
                it.remove();
                gameEngine.recordKill();
                continue;
            }
            e.update(tpf);
            aiSystem.update(e, player, tpf);
            combatSystem.enemyContactDamage(e, player, tpf);
        }

        // --- Separation / collision resolution ---
        physicsSystem.resolveAll(enemies, player);

        // --- Wave / engine tick ---
        GameEngine.Event event = gameEngine.tick(tpf, enemies.size());
        switch (event) {
            case START_NEXT_WAVE -> spawnWave();
            default -> { /* NONE or WAVE_CLEARED — HUD shows countdown */ }
        }

        // --- Game over check ---
        if (player.isDead()) {
            triggerGameOver();
            return;
        }

        // --- HUD update ---
        uiManager.update(
                player,
                gameEngine.getCurrentWave(),
                gameEngine.getScore(),
                gameState,
                gameEngine.getWaveCountdown(),
                gameEngine.isWaitingForWave(),
                tpf
        );
    }

    // ------------------------------------------------------------------
    // Wave spawning
    // ------------------------------------------------------------------
    private void spawnWave() {
        gameEngine.advanceWave();
        int   count = gameEngine.enemiesForNextWave();
        float speed = gameEngine.speedForNextWave();

        for (int i = 0; i < count; i++) {
            spawnEnemy(speed);
        }

        uiManager.showWaveAnnouncement("WAVE " + gameEngine.getCurrentWave());
    }

    private void spawnEnemy(float speed) {
        float hw = Constants.LEVEL_HALF_WIDTH  - 1.2f;
        float hh = Constants.LEVEL_HALF_HEIGHT - 1.2f;
        float x, z;

        // Place enemy along one of the four edges, chosen at random
        switch (random.nextInt(4)) {
            case 0  -> { x = -hw + random.nextFloat() * 2f * hw; z = -hh; } // top
            case 1  -> { x = -hw + random.nextFloat() * 2f * hw; z =  hh; } // bottom
            case 2  -> { x = -hw; z = -hh + random.nextFloat() * 2f * hh; } // left
            default -> { x =  hw; z = -hh + random.nextFloat() * 2f * hh; } // right
        }

        Enemy enemy = new Enemy(assetManager, x, z, speed);
        enemies.add(enemy);
        rootNode.attachChild(enemy.getNode());
    }

    // ------------------------------------------------------------------
    // State transitions
    // ------------------------------------------------------------------
    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            uiManager.showPauseOverlay(true);
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            uiManager.showPauseOverlay(false);
        }
    }

    private void triggerGameOver() {
        gameState = GameState.GAME_OVER;
        uiManager.showGameOverOverlay(gameEngine.getScore());
    }

    private void restartGame() {
        // Remove all enemies from the scene
        for (Enemy e : enemies) {
            rootNode.detachChild(e.getNode());
        }
        enemies.clear();

        // Remove old player and create a fresh one
        rootNode.detachChild(player.getNode());
        player = new Player(assetManager, 0f, 0f);
        rootNode.attachChild(player.getNode());

        // Reset game engine (score, wave, counters)
        gameEngine.reset();

        // Reset UI
        uiManager.reset();

        // Re-enter playing state and start wave 1
        gameState = GameState.PLAYING;
        spawnWave();
    }

    // ------------------------------------------------------------------
    // Scene setup helpers
    // ------------------------------------------------------------------
    private void setupCamera() {
        // Lock camera at a fixed position directly above the origin
        cam.setLocation(new Vector3f(0f, Constants.CAMERA_HEIGHT, 0f));
        cam.lookAtDirection(new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f));
        cam.setParallelProjection(true);

        // Set the orthographic frustum so the level fills the screen nicely
        float aspect     = (float) cam.getWidth() / (float) cam.getHeight();
        float viewHalfH  = Constants.LEVEL_HALF_HEIGHT + 1.5f;
        float viewHalfW  = viewHalfH * aspect;
        cam.setFrustum(-1000f, 1000f, -viewHalfW, viewHalfW, -viewHalfH, viewHalfH);
    }

    private void setupLighting() {
        // Full-brightness ambient light so unshaded materials are visible
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(2f));
        rootNode.addLight(ambient);
    }
}
