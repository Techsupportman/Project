package com.project.core;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.project.difficulty.Difficulty;
import com.project.entities.Enemy;
import com.project.entities.EnemyType;
import com.project.entities.Pickup;
import com.project.entities.Player;
import com.project.entities.Projectile;
import com.project.levels.Level1;
import com.project.levels.LevelManager;
import com.project.systems.AISystem;
import com.project.systems.CombatSystem;
import com.project.systems.PhysicsSystem;
import com.project.systems.ProjectileSystem;
import com.project.systems.SpawnManager;
import com.project.systems.VisionSystem;
import com.project.ui.UIManager;
import com.project.upgrades.Upgrade;
import com.project.upgrades.UpgradeManager;
import com.project.utils.Constants;
import com.project.utils.InputHandler;
import com.project.weapons.WeaponStats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main jMonkeyEngine application — top-level coordinator for all game systems.
 *
 * <h3>Key mechanics (Disfigure-inspired)</h3>
 * <ul>
 *   <li>Ranged shooting aimed with the mouse cursor</li>
 *   <li>Circle / Cone vision toggled with the Right Mouse Button</li>
 *   <li>Time-based continuous enemy spawning via {@link SpawnManager}</li>
 *   <li>Integer heart-based HP</li>
 *   <li>EXP pickups and level-up upgrade trees</li>
 *   <li>Boss encounters that seal the arena</li>
 *   <li>Difficulty selection at game start</li>
 * </ul>
 */
public class GameApp extends SimpleApplication {

    // ------------------------------------------------------------------
    // Difficulty
    // ------------------------------------------------------------------
    private Difficulty difficulty = Difficulty.NORMAL;

    // ------------------------------------------------------------------
    // Game entities
    // ------------------------------------------------------------------
    private Player             player;
    private List<Enemy>        enemies;
    private List<Projectile>   projectiles;
    private List<Pickup>       pickups;

    // ------------------------------------------------------------------
    // Systems
    // ------------------------------------------------------------------
    private AISystem         aiSystem;
    private CombatSystem     combatSystem;
    private PhysicsSystem    physicsSystem;
    private ProjectileSystem projectileSystem;
    private SpawnManager     spawnManager;
    private VisionSystem     visionSystem;
    private UpgradeManager   upgradeManager;

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
    private GameEngine     gameEngine;
    private GameState      gameState;
    private List<Upgrade>  currentUpgradeChoices;
    private final Random   random = new Random();

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public GameApp() {
        super();
    }

    // ------------------------------------------------------------------
    // SimpleApplication lifecycle
    // ------------------------------------------------------------------
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        setupCamera();
        setupLighting();

        // Containers
        enemies     = new ArrayList<>();
        projectiles = new ArrayList<>();
        pickups     = new ArrayList<>();

        // Systems (difficulty-independent)
        aiSystem         = new AISystem();
        combatSystem     = new CombatSystem();
        physicsSystem    = new PhysicsSystem();
        projectileSystem = new ProjectileSystem();
        visionSystem     = new VisionSystem();
        upgradeManager   = new UpgradeManager();
        gameEngine       = new GameEngine();

        // Input
        inputHandler = new InputHandler(inputManager);

        // Level
        levelManager = new LevelManager(assetManager, rootNode);
        levelManager.loadLevel(new Level1(assetManager, rootNode));

        // UI
        uiManager = new UIManager(assetManager, guiNode, settings);

        // Start at difficulty selection
        gameState = GameState.DIFFICULTY_SELECT;
        uiManager.showDifficultySelect();
    }

    @Override
    public void simpleUpdate(float tpf) {
        switch (gameState) {
            case DIFFICULTY_SELECT -> handleDifficultySelect();
            case PLAYING           -> updatePlaying(tpf);
            case PAUSED            -> {
                if (inputHandler.isPausePressed()) togglePause();
            }
            case LEVEL_UP          -> handleLevelUpInput();
            case GAME_OVER         -> {
                if (inputHandler.isRestartPressed()) returnToDifficultySelect();
            }
        }
    }

    // ------------------------------------------------------------------
    // Difficulty selection
    // ------------------------------------------------------------------
    private void handleDifficultySelect() {
        int choice = inputHandler.consumeChoicePending();
        if (choice < 0) return;
        difficulty = switch (choice) {
            case 0  -> Difficulty.EASY;
            case 1  -> Difficulty.NORMAL;
            case 2  -> Difficulty.HARD;
            case 3  -> Difficulty.NIGHTMARE;
            default -> Difficulty.NORMAL;
        };
        startNewGame();
    }

    private void startNewGame() {
        // Remove any lingering entities
        for (Enemy e : enemies)       rootNode.detachChild(e.getNode());
        for (Projectile p : projectiles) rootNode.detachChild(p.getNode());
        for (Pickup pk : pickups)     rootNode.detachChild(pk.getNode());
        enemies.clear();
        projectiles.clear();
        pickups.clear();

        if (player != null) rootNode.detachChild(player.getNode());

        // Set difficulty on engine & spawn manager
        gameEngine.setDifficulty(difficulty);
        gameEngine.reset();
        spawnManager = new SpawnManager(difficulty);
        upgradeManager.reset();

        // Create fresh player
        player = new Player(assetManager, 0f, 0f, difficulty);
        rootNode.attachChild(player.getNode());

        uiManager.hideDifficultySelect();
        uiManager.reset();

        gameState = GameState.PLAYING;
    }

    // ------------------------------------------------------------------
    // Per-frame game logic (PLAYING state only)
    // ------------------------------------------------------------------
    private void updatePlaying(float tpf) {
        if (inputHandler.isPausePressed()) {
            togglePause();
            return;
        }

        // --- Weapon cycling ---
        if (inputHandler.isCycleWeaponPressed()) {
            player.cycleWeapon();
        }

        // --- Vision toggle ---
        if (inputHandler.isVisionTogglePressed()) {
            player.toggleVisionMode();
        }

        // --- Player movement ---
        float dx = 0f, dz = 0f;
        if (inputHandler.isMoveLeft())  dx -= 1f;
        if (inputHandler.isMoveRight()) dx += 1f;
        if (inputHandler.isMoveUp())    dz -= 1f;
        if (inputHandler.isMoveDown())  dz += 1f;

        if (dx != 0f && dz != 0f) {
            float inv = (float) (1.0 / Math.sqrt(dx * dx + dz * dz));
            dx *= inv;
            dz *= inv;
        }

        player.move(dx, dz, tpf);

        // --- Mouse aim ---
        Vector2f screenCursor = inputHandler.getCursorPosition();
        float[] worldCursor = screenToWorld(screenCursor.x, screenCursor.y);
        player.updateAim(worldCursor[0], worldCursor[1]);

        player.update(tpf);

        // --- Shooting ---
        if (inputHandler.isFireHeld()) {
            if (player.tryFire()) {
                spawnProjectiles();
            }
        }

        // --- Projectiles ---
        List<Enemy> deadEnemies = new ArrayList<>();
        projectileSystem.update(projectiles, enemies, tpf);

        // --- Enemy spawning (time-based) ---
        List<Enemy> newEnemies = spawnManager.update(tpf, assetManager, enemies.size());
        for (Enemy e : newEnemies) {
            enemies.add(e);
            rootNode.attachChild(e.getNode());
            if (e.getEnemyType().isBoss) {
                uiManager.showBossWarning(e.getEnemyType().displayName);
            }
        }

        // --- Vision system ---
        for (Enemy e : enemies) {
            visionSystem.updateVisibility(e, player);
        }
        // EXP pickups also follow vision rules
        for (Pickup pk : pickups) {
            if (pk.getPickupType() == Pickup.PickupType.EXP) {
                boolean vis = visionSystem.isVisible(
                        pk.getPosition().x, pk.getPosition().z,
                        player.getPosition().x, player.getPosition().z,
                        player.getAimDirX(), player.getAimDirZ(),
                        player.isCircleVision());
                pk.getNode().setCullHint(vis
                        ? com.jme3.scene.Spatial.CullHint.Never
                        : com.jme3.scene.Spatial.CullHint.Always);
            }
        }

        // --- Enemy updates ---
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isActive()) {
                rootNode.detachChild(e.getNode());
                it.remove();
                onEnemyKilled(e);
                if (e.getEnemyType().isBoss) {
                    spawnManager.onBossDefeated();
                    // Drop heart + mutation pickup
                    spawnPickup(e.getPosition().x, e.getPosition().z, Pickup.PickupType.HEART, 0);
                    spawnPickup(e.getPosition().x + 0.8f, e.getPosition().z, Pickup.PickupType.MUTATION, 0);
                }
                continue;
            }
            e.update(tpf);
            aiSystem.update(e, player, tpf);
            combatSystem.enemyContactDamage(e, player, tpf);
        }

        // --- Physics / separation ---
        physicsSystem.resolveAll(enemies, player);

        // --- Pickups ---
        Iterator<Pickup> pit = pickups.iterator();
        while (pit.hasNext()) {
            Pickup pk = pit.next();
            if (!pk.isActive()) {
                rootNode.detachChild(pk.getNode());
                pit.remove();
                continue;
            }
            if (pk.canBeCollectedBy(player.getPosition().x, player.getPosition().z)) {
                applyPickup(pk);
                pk.setActive(false);
            }
        }

        // --- Game over check ---
        if (player.isDead()) {
            triggerGameOver();
            return;
        }

        // --- Level up check ---
        if (player.consumeLevelUp()) {
            triggerLevelUp();
            return;
        }

        // --- HUD ---
        uiManager.update(player, gameEngine.getScore(), gameState,
                gameEngine.getWaveCountdown(), gameEngine.isWaitingForWave(), tpf);
        uiManager.updateTimeAndVision(
                spawnManager.getElapsedSeconds(), player.isCircleVision());
    }

    // ------------------------------------------------------------------
    // Level-up flow
    // ------------------------------------------------------------------
    private void triggerLevelUp() {
        currentUpgradeChoices = upgradeManager.generateChoices();
        if (currentUpgradeChoices.isEmpty()) {
            // No upgrades available — just continue
            return;
        }
        gameState = GameState.LEVEL_UP;
        uiManager.showLevelUpMenu(
                player.getLevel(),
                currentUpgradeChoices,
                upgradeManager.getRerollsLeft(),
                upgradeManager.getDeletesLeft()
        );
    }

    private void handleLevelUpInput() {
        int choice = inputHandler.consumeChoicePending();
        if (choice >= 0 && choice < currentUpgradeChoices.size()) {
            upgradeManager.applyUpgrade(currentUpgradeChoices.get(choice));
            uiManager.hideLevelUpMenu();
            gameState = GameState.PLAYING;
            return;
        }

        if (inputHandler.isRerollPressed()) {
            List<Upgrade> rerolled = upgradeManager.reroll();
            if (rerolled != null) {
                currentUpgradeChoices = rerolled;
                uiManager.showLevelUpMenu(
                        player.getLevel(), currentUpgradeChoices,
                        upgradeManager.getRerollsLeft(),
                        upgradeManager.getDeletesLeft());
            }
        }

        if (inputHandler.isDeletePressed()) {
            if (upgradeManager.delete()) {
                uiManager.hideLevelUpMenu();
                gameState = GameState.PLAYING;
            }
        }
    }

    // ------------------------------------------------------------------
    // Shooting helpers
    // ------------------------------------------------------------------
    /**
     * Spawns one or more projectiles from the player's current weapon position
     * toward the aim direction, applying spread for multi-pellet weapons.
     */
    private void spawnProjectiles() {
        WeaponStats stats  = player.getActiveWeapon().getStats();
        float       baseAX = player.getAimDirX();
        float       baseAZ = player.getAimDirZ();
        float       startX = player.getPosition().x;
        float       startZ = player.getPosition().z;

        // Apply upgrade multipliers
        float finalDamage   = stats.damage   * upgradeManager.getDamageMult();
        float finalFireRate = stats.fireRate  * upgradeManager.getFireRateMult();
        int   finalPierce   = stats.pierce    + upgradeManager.getBonusPierce();
        int   finalRicochet = stats.ricochet  + upgradeManager.getBonusRicochet();

        int pellets = stats.pelletsPerShot;
        for (int i = 0; i < pellets; i++) {
            float angle = 0f;
            if (pellets > 1) {
                angle = -stats.spreadAngle + (2f * stats.spreadAngle / (pellets - 1)) * i;
            }
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float ax  = baseAX * cos - baseAZ * sin;
            float az  = baseAX * sin + baseAZ * cos;

            Projectile proj = new Projectile(
                    assetManager,
                    startX, startZ,
                    ax, az,
                    stats.bulletSpeed,
                    finalDamage,
                    stats.bulletSize,
                    finalPierce,
                    finalRicochet,
                    Constants.LEVEL_HALF_WIDTH,
                    Constants.LEVEL_HALF_HEIGHT
            );
            projectiles.add(proj);
            rootNode.attachChild(proj.getNode());
        }
    }

    // ------------------------------------------------------------------
    // Kill / pickup helpers
    // ------------------------------------------------------------------
    private void onEnemyKilled(Enemy e) {
        gameEngine.recordKill(e.getEnemyType());
        int creditsEarned = (int) (e.getEnemyType().scoreValue
                * Constants.CREDITS_PER_SCORE * difficulty.scoreMultiplier);
        player.addCredits(creditsEarned);
        // Award EXP
        player.addExp(e.getEnemyType().expValue, difficulty.expGainMult);
        // Drop EXP orb
        spawnPickup(e.getPosition().x, e.getPosition().z,
                Pickup.PickupType.EXP, e.getEnemyType().expValue);
    }

    private void spawnPickup(float x, float z, Pickup.PickupType type, int expAmount) {
        Pickup pk = new Pickup(assetManager, x, z, type, expAmount);
        pickups.add(pk);
        rootNode.attachChild(pk.getNode());
    }

    private void applyPickup(Pickup pk) {
        switch (pk.getPickupType()) {
            case HEART    -> player.heal();
            case EXP      -> player.addExp(pk.getExpAmount(), difficulty.expGainMult);
            case MUTATION -> triggerLevelUp(); // boss mutation triggers an extra level-up
        }
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

    private void returnToDifficultySelect() {
        gameState = GameState.DIFFICULTY_SELECT;
        uiManager.reset();
        uiManager.showDifficultySelect();
    }

    // ------------------------------------------------------------------
    // Screen-to-world projection helper
    // ------------------------------------------------------------------
    /**
     * Projects a screen-space pixel coordinate onto the Y=0 world plane using
     * the orthographic camera, returning [worldX, worldZ].
     */
    private float[] screenToWorld(float screenX, float screenY) {
        // In an orthographic top-down setup the projection is linear.
        float aspect    = (float) cam.getWidth() / cam.getHeight();
        float viewHalfH = Constants.LEVEL_HALF_HEIGHT + 1.5f;
        float viewHalfW = viewHalfH * aspect;

        float ndcX = (screenX / cam.getWidth())  * 2f - 1f;
        float ndcY = (screenY / cam.getHeight()) * 2f - 1f;

        float worldX = ndcX * viewHalfW;
        float worldZ = -ndcY * viewHalfH; // Y flipped (screen Y-down vs world Y-up)
        return new float[]{ worldX, worldZ };
    }

    // ------------------------------------------------------------------
    // Scene setup helpers
    // ------------------------------------------------------------------
    private void setupCamera() {
        cam.setLocation(new Vector3f(0f, Constants.CAMERA_HEIGHT, 0f));
        cam.lookAtDirection(new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f));
        cam.setParallelProjection(true);

        float aspect    = (float) cam.getWidth() / (float) cam.getHeight();
        float viewHalfH = Constants.LEVEL_HALF_HEIGHT + 1.5f;
        float viewHalfW = viewHalfH * aspect;
        cam.setFrustum(-1000f, 1000f, -viewHalfW, viewHalfW, -viewHalfH, viewHalfH);
    }

    private void setupLighting() {
        // Dim ambient light to suggest darkness outside vision range
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.4f, 0.4f, 0.45f, 1f));
        rootNode.addLight(ambient);
    }
}
