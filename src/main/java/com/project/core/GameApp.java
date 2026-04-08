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
import com.project.ui.UIManager;
import com.project.upgrades.Upgrade;
import com.project.upgrades.UpgradeManager;
import com.project.utils.Constants;
import com.project.utils.InputHandler;
import com.project.weapons.WeaponStats;
import com.project.weapons.WeaponType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main jMonkeyEngine application.
 */
public class GameApp extends SimpleApplication {

    private Difficulty difficulty = Difficulty.NORMAL;

    private static final WeaponType[] ALL_WEAPONS      = WeaponType.values();
    private static final int          WEAPONS_PER_PAGE = 5;
    private static final int          TOTAL_PAGES      =
            (ALL_WEAPONS.length + WEAPONS_PER_PAGE - 1) / WEAPONS_PER_PAGE;

    private WeaponType selectedWeapon   = WeaponType.PISTOL;
    private int        weaponSelectPage = 0;

    private Player           player;
    private List<Enemy>      enemies;
    private List<Projectile> projectiles;
    private List<Projectile> enemyProjectiles;
    private List<Pickup>     pickups;

    private AISystem         aiSystem;
    private CombatSystem     combatSystem;
    private PhysicsSystem    physicsSystem;
    private ProjectileSystem projectileSystem;
    private SpawnManager     spawnManager;
    private UpgradeManager   upgradeManager;

    private UIManager    uiManager;
    private LevelManager levelManager;
    private InputHandler inputHandler;

    private GameEngine     gameEngine;
    private GameState      gameState;
    private List<Upgrade>  currentUpgradeChoices;
    private final Random   random     = new Random();
    private boolean        fireLocked = false;

    private static final float BLACK_HOLE_BASE_SPEW_INTERVAL = 0.15f;

    private boolean                              blackHoleActive      = false;
    private boolean                              blackHoleSpewing     = false;
    private float                                blackHoleRecordTimer = 0f;
    private float                                blackHoleSpewTimer   = 0f;
    private final ArrayDeque<ProjectileSnapshot> blackHoleStack       = new ArrayDeque<>();

    /**
     * Stores a spread angle relative to the player's aim direction at fire time,
     * rather than absolute world-space direction components. This allows the
     * Black Hole spew phase to replay shots relative to wherever the player is
     * aiming at replay time, giving the ability a dynamic feel.
     */
    private static final class ProjectileSnapshot {
        /** Rotation offset from the current aim direction in radians (not absolute). */
        final float spreadAngle;
        final float speed, damage, bulletSize;
        final int   pierce, ricochet;

        ProjectileSnapshot(float spreadAngle, float speed, float damage,
                           float bulletSize, int pierce, int ricochet) {
            this.spreadAngle = spreadAngle;
            this.speed       = speed;
            this.damage      = damage;
            this.bulletSize  = bulletSize;
            this.pierce      = pierce;
            this.ricochet    = ricochet;
        }
    }

    public GameApp() {
        super();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        setupCamera();
        setupLighting();
        viewPort.setBackgroundColor(new ColorRGBA(0.08f, 0.08f, 0.12f, 1f));

        enemies          = new ArrayList<>();
        projectiles      = new ArrayList<>();
        enemyProjectiles = new ArrayList<>();
        pickups          = new ArrayList<>();

        aiSystem         = new AISystem();
        combatSystem     = new CombatSystem();
        physicsSystem    = new PhysicsSystem();
        projectileSystem = new ProjectileSystem();
        upgradeManager   = new UpgradeManager();
        gameEngine       = new GameEngine();

        inputHandler = new InputHandler(inputManager);

        levelManager = new LevelManager(assetManager, rootNode);
        levelManager.loadLevel(new Level1(assetManager, rootNode));

        uiManager = new UIManager(assetManager, guiNode, settings);

        gameState = GameState.DIFFICULTY_SELECT;
        uiManager.showDifficultySelect();
    }

    @Override
    public void simpleUpdate(float tpf) {
        switch (gameState) {
            case DIFFICULTY_SELECT -> handleDifficultySelect();
            case WEAPON_SELECT     -> handleWeaponSelect();
            case PLAYING           -> updatePlaying(tpf);
            case PAUSED            -> handlePausedInput();
            case SETTINGS          -> handleSettingsInput();
            case LEVEL_UP          -> handleLevelUpInput();
            case GAME_OVER         -> {
                Vector2f cursor = inputHandler.getCursorPosition();
                uiManager.updateGameOverHover(cursor.x, cursor.y);
                if (inputHandler.isLmbJustPressed()
                        && uiManager.isGameOverRestartClicked(cursor.x, cursor.y)) {
                    returnToDifficultySelect();
                }
            }
        }
    }

    @Override
    public void reshape(int w, int h) {
        super.reshape(w, h);
        updateCameraFrustum(w, h);
        if (uiManager != null) uiManager.onResize(w, h);
    }

    // -- Difficulty --
    private void handleDifficultySelect() {
        Vector2f cursor = inputHandler.getCursorPosition();
        uiManager.updateDifficultyHover(cursor.x, cursor.y);
        if (inputHandler.isLmbJustPressed()) {
            int clicked = uiManager.getDifficultyClickedOption(cursor.x, cursor.y);
            if (clicked >= 0) { applyDifficultyChoice(clicked); return; }
        }
    }

    private void applyDifficultyChoice(int choice) {
        difficulty = switch (choice) {
            case 0  -> Difficulty.EASY;
            case 1  -> Difficulty.NORMAL;
            case 2  -> Difficulty.HARD;
            case 3  -> Difficulty.NIGHTMARE;
            default -> Difficulty.NORMAL;
        };
        uiManager.hideDifficultySelect();
        enterWeaponSelect();
    }

    // -- Weapon select --
    private void enterWeaponSelect() {
        weaponSelectPage = 0;
        selectedWeapon   = WeaponType.PISTOL;
        gameState = GameState.WEAPON_SELECT;
        showCurrentWeaponPage();
    }

    private void handleWeaponSelect() {
        Vector2f cursor = inputHandler.getCursorPosition();
        uiManager.updateWeaponSelectHover(cursor.x, cursor.y);
        if (inputHandler.isLmbJustPressed()) {
            if (uiManager.isWeaponNavNextClicked(cursor.x, cursor.y)) {
                weaponSelectPage = (weaponSelectPage + 1) % TOTAL_PAGES;
                showCurrentWeaponPage(); return;
            }
            if (uiManager.isWeaponNavPrevClicked(cursor.x, cursor.y)) {
                weaponSelectPage = (weaponSelectPage - 1 + TOTAL_PAGES) % TOTAL_PAGES;
                showCurrentWeaponPage(); return;
            }
            int clicked = uiManager.getWeaponClickedOption(cursor.x, cursor.y);
            if (clicked >= 0) {
                int idx = weaponSelectPage * WEAPONS_PER_PAGE + clicked;
                if (idx < ALL_WEAPONS.length) {
                    selectedWeapon = ALL_WEAPONS[idx];
                    uiManager.hideWeaponSelect();
                    startNewGame(); return;
                }
            }
        }
    }

    private void showCurrentWeaponPage() {
        int start = weaponSelectPage * WEAPONS_PER_PAGE;
        int end   = Math.min(start + WEAPONS_PER_PAGE, ALL_WEAPONS.length);
        WeaponType[] page = Arrays.copyOfRange(ALL_WEAPONS, start, end);
        uiManager.showWeaponSelect(page, weaponSelectPage, TOTAL_PAGES);
    }

    // -- New game --
    private void startNewGame() {
        for (Enemy e : enemies)              rootNode.detachChild(e.getNode());
        for (Projectile p : projectiles)     rootNode.detachChild(p.getNode());
        for (Projectile p : enemyProjectiles) rootNode.detachChild(p.getNode());
        for (Pickup pk : pickups)            rootNode.detachChild(pk.getNode());
        enemies.clear(); projectiles.clear(); enemyProjectiles.clear(); pickups.clear();
        if (player != null) rootNode.detachChild(player.getNode());

        gameEngine.setDifficulty(difficulty);
        gameEngine.reset();
        spawnManager = new SpawnManager(difficulty);
        upgradeManager.reset();

        blackHoleActive = false; blackHoleSpewing = false;
        blackHoleRecordTimer = 0f; blackHoleSpewTimer = 0f;
        blackHoleStack.clear();

        fireLocked = false;
        uiManager.setFireLockStatus(false);

        player = new Player(assetManager, 0f, 0f, difficulty);
        player.setStartingWeapon(selectedWeapon);
        rootNode.attachChild(player.getNode());

        uiManager.reset();
        gameState = GameState.PLAYING;
    }

    // -- Playing --
    private void updatePlaying(float tpf) {
        if (inputHandler.isPausePressed() || inputHandler.isEscapePressed()) {
            togglePause(); return;
        }
        if (inputHandler.isFireLockPressed()) {
            fireLocked = !fireLocked;
            uiManager.setFireLockStatus(fireLocked);
        }

        float dx = 0f, dz = 0f;
        if (inputHandler.isMoveLeft())  dx -= 1f;
        if (inputHandler.isMoveRight()) dx += 1f;
        if (inputHandler.isMoveUp())    dz -= 1f;
        if (inputHandler.isMoveDown())  dz += 1f;
        if (dx != 0f && dz != 0f) {
            float inv = (float)(1.0/Math.sqrt(dx*dx+dz*dz));
            dx *= inv; dz *= inv;
        }
        player.move(dx, dz, tpf);

        // Camera follows player
        cam.setLocation(new Vector3f(
                player.getPosition().x, Constants.CAMERA_HEIGHT, player.getPosition().z));

        Vector2f screenCursor = inputHandler.getCursorPosition();
        float[] wc = screenToWorld(screenCursor.x, screenCursor.y);
        player.updateAim(wc[0], wc[1]);
        player.update(tpf);

        if (inputHandler.isFireHeld() || fireLocked) {
            if (player.tryFire()) spawnProjectiles();
        }

        // Player projectiles vs enemies
        projectileSystem.update(projectiles, enemies, tpf);

        // Enemy projectiles vs player
        updateEnemyProjectiles(tpf);

        List<Enemy> newEnemies = spawnManager.update(tpf, assetManager, enemies.size(),
                player.getPosition().x, player.getPosition().z);
        for (Enemy e : newEnemies) {
            enemies.add(e);
            rootNode.attachChild(e.getNode());
            if (e.getEnemyType().isBoss) uiManager.showBossWarning(e.getEnemyType().displayName);
        }

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isActive()) {
                rootNode.detachChild(e.getNode()); it.remove();
                onEnemyKilled(e);
                if (e.getEnemyType().isBoss) {
                    spawnManager.onBossDefeated();
                    uiManager.clearBossWarning();
                    spawnPickup(e.getPosition().x, e.getPosition().z, Pickup.PickupType.HEART, 0);
                    spawnPickup(e.getPosition().x+0.8f, e.getPosition().z, Pickup.PickupType.MUTATION, 0);
                }
                continue;
            }
            e.update(tpf);
            aiSystem.update(e, player, tpf);
            combatSystem.enemyContactDamage(e, player, tpf);

            // Ranged enemy shot
            AISystem.EnemyShot shot = aiSystem.tryRangedShot(e, player, tpf);
            if (shot != null) spawnEnemyProjectile(shot);
        }

        physicsSystem.resolveAll(enemies, player);

        Iterator<Pickup> pit = pickups.iterator();
        while (pit.hasNext()) {
            Pickup pk = pit.next();
            if (!pk.isActive()) { rootNode.detachChild(pk.getNode()); pit.remove(); continue; }
            if (pk.canBeCollectedBy(player.getPosition().x, player.getPosition().z)) {
                applyPickup(pk); pk.setActive(false);
            }
        }

        syncBlackHoleState();
        if (blackHoleActive) updateBlackHole(tpf);

        if (player.isDead()) { triggerGameOver(); return; }
        if (player.consumeLevelUp()) { triggerLevelUp(); return; }

        uiManager.update(player, gameEngine.getScore(), gameState,
                gameEngine.getWaveCountdown(), gameEngine.isWaitingForWave(), tpf);
        uiManager.updateTimeAndVision(spawnManager.getElapsedSeconds(), false);
        updateBlackHoleHud();
    }

    /** Moves enemy projectiles and checks for collision with the player. */
    private void updateEnemyProjectiles(float tpf) {
        Iterator<Projectile> epit = enemyProjectiles.iterator();
        while (epit.hasNext()) {
            Projectile proj = epit.next();
            if (!proj.isActive()) { rootNode.detachChild(proj.getNode()); epit.remove(); continue; }
            proj.update(tpf);
            if (!proj.isActive()) { rootNode.detachChild(proj.getNode()); epit.remove(); continue; }

            float pdx = proj.getPosition().x - player.getPosition().x;
            float pdz = proj.getPosition().z - player.getPosition().z;
            float dist = (float) Math.sqrt(pdx * pdx + pdz * pdz);
            if (dist <= proj.getSize() + Constants.PLAYER_SIZE) {
                // Damage is stored as half-hearts; take one full heart if ≥ 1, else half
                player.takeDamage();
                proj.onHit(); // deactivates it
                rootNode.detachChild(proj.getNode());
                epit.remove();
            }
        }
    }

    /** Creates an enemy-fired projectile and adds it to the scene. */
    private void spawnEnemyProjectile(AISystem.EnemyShot shot) {
        Projectile proj = new Projectile(assetManager,
                shot.x, shot.z, shot.dirX, shot.dirZ,
                shot.speed, shot.damage, shot.size, 0, 0,
                new ColorRGBA(1.0f, 0.4f, 0.1f, 1.0f)); // orange-red
        enemyProjectiles.add(proj);
        rootNode.attachChild(proj.getNode());
    }

    // -- Pause --
    private void handlePausedInput() {
        Vector2f cursor = inputHandler.getCursorPosition();
        uiManager.updatePauseMenuHover(cursor.x, cursor.y);
        if (inputHandler.isPausePressed() || inputHandler.isEscapePressed()) {
            togglePause(); return;
        }
        if (inputHandler.isLmbJustPressed()) {
            int clicked = uiManager.getPauseMenuClickedOption(cursor.x, cursor.y);
            switch (clicked) {
                case 0 -> togglePause();
                case 1 -> { uiManager.showPauseOverlay(false); returnToDifficultySelect(); }
                case 2 -> { uiManager.showPauseOverlay(false); enterSettings(); }
                case 3 -> stop();
            }
        }
    }

    private void enterSettings() {
        gameState = GameState.SETTINGS;
        uiManager.showSettingsMenu();
    }

    private void handleSettingsInput() {
        Vector2f cursor = inputHandler.getCursorPosition();
        uiManager.updateSettingsHover(cursor.x, cursor.y);
        if (inputHandler.isEscapePressed()) { leaveSettings(); return; }
        if (inputHandler.isLmbJustPressed()) {
            int clicked = uiManager.getSettingsClickedOption(cursor.x, cursor.y);
            switch (clicked) {
                case 0 -> { float vol = uiManager.adjustVolume(-10); getListener().setVolume(vol); }
                case 1 -> { float vol = uiManager.adjustVolume(+10); getListener().setVolume(vol); }
                case 2 -> leaveSettings();
            }
        }
    }

    private void leaveSettings() {
        uiManager.hideSettingsMenu();
        gameState = GameState.PAUSED;
        uiManager.showPauseOverlay(true);
    }

    // -- Level up --
    private void triggerLevelUp() {
        currentUpgradeChoices = upgradeManager.generateChoices();
        if (currentUpgradeChoices.isEmpty()) return;
        gameState = GameState.LEVEL_UP;
        uiManager.showLevelUpMenu(player.getLevel(), currentUpgradeChoices,
                upgradeManager.getRerollsLeft(), upgradeManager.getDeletesLeft());
    }

    private void handleLevelUpInput() {
        Vector2f cursor = inputHandler.getCursorPosition();
        uiManager.updateLevelUpHover(cursor.x, cursor.y, currentUpgradeChoices.size());
        if (inputHandler.isLmbJustPressed()) {
            int clicked = uiManager.getLevelUpClickedChoice(
                    cursor.x, cursor.y, currentUpgradeChoices.size());
            if (clicked >= 0 && clicked < currentUpgradeChoices.size()) {
                upgradeManager.applyUpgrade(currentUpgradeChoices.get(clicked));
                uiManager.hideLevelUpMenu(); gameState = GameState.PLAYING; return;
            }
            if (uiManager.isLevelUpRerollClicked(cursor.x, cursor.y)) {
                List<Upgrade> r = upgradeManager.reroll();
                if (r != null) { currentUpgradeChoices = r;
                    uiManager.showLevelUpMenu(player.getLevel(), r,
                            upgradeManager.getRerollsLeft(), upgradeManager.getDeletesLeft()); }
                return;
            }
            if (uiManager.isLevelUpDeleteClicked(cursor.x, cursor.y)) {
                if (upgradeManager.delete()) { uiManager.hideLevelUpMenu(); gameState = GameState.PLAYING; }
                return;
            }
        }
    }

    // -- Shooting --
    private void spawnProjectiles() {
        WeaponStats stats  = player.getActiveWeapon().getStats();
        float baseAX = player.getAimDirX();
        float baseAZ = player.getAimDirZ();
        float startX = player.getPosition().x;
        float startZ = player.getPosition().z;

        float finalDamage   = stats.damage   * upgradeManager.getDamageMult();
        int   finalPierce   = stats.pierce   + upgradeManager.getBonusPierce();
        int   finalRicochet = stats.ricochet + upgradeManager.getBonusRicochet();

        int pellets = stats.pelletsPerShot;
        for (int i = 0; i < pellets; i++) {
            float angle = 0f;
            if (pellets > 1) angle = -stats.spreadAngle + (2f*stats.spreadAngle/(pellets-1))*i;
            float cos=(float)Math.cos(angle), sin=(float)Math.sin(angle);
            float ax=baseAX*cos-baseAZ*sin, az=baseAX*sin+baseAZ*cos;

            Projectile proj = new Projectile(assetManager, startX, startZ, ax, az,
                    stats.bulletSpeed, finalDamage, stats.bulletSize,
                    finalPierce, finalRicochet,
                    Constants.LEVEL_HALF_WIDTH, Constants.LEVEL_HALF_HEIGHT);
            projectiles.add(proj);
            rootNode.attachChild(proj.getNode());

            if (blackHoleActive && !blackHoleSpewing) {
                blackHoleStack.add(new ProjectileSnapshot(angle,
                        stats.bulletSpeed, finalDamage, stats.bulletSize,
                        finalPierce, finalRicochet));
            }
        }
    }

    // -- Black Hole --
    private void syncBlackHoleState() {
        if (!blackHoleActive && upgradeManager.hasPocketBlackHole()) {
            blackHoleActive=true; blackHoleSpewing=false;
            blackHoleRecordTimer=0f; blackHoleStack.clear();
        }
    }

    private void updateBlackHole(float tpf) {
        if (!blackHoleSpewing) {
            blackHoleRecordTimer += tpf;
            float recordDuration = upgradeManager.getBlackHoleRecordTime();
            if (blackHoleRecordTimer >= recordDuration) {
                if (!blackHoleStack.isEmpty()) { blackHoleSpewing=true; blackHoleSpewTimer=0f; }
                else blackHoleRecordTimer=0f;
            }
        } else {
            blackHoleSpewTimer -= tpf;
            float spewInterval = BLACK_HOLE_BASE_SPEW_INTERVAL/upgradeManager.getBlackHoleSpewRateMult();
            while (blackHoleSpewTimer<=0f && !blackHoleStack.isEmpty()) {
                ProjectileSnapshot snap = blackHoleStack.pollFirst();
                float baseAX=player.getAimDirX(), baseAZ=player.getAimDirZ();
                float cos=(float)Math.cos(snap.spreadAngle), sin=(float)Math.sin(snap.spreadAngle);
                float ax=baseAX*cos-baseAZ*sin, az=baseAX*sin+baseAZ*cos;
                int replayPierce=snap.pierce+upgradeManager.getBlackHoleExtraPierce();
                Projectile proj=new Projectile(assetManager,
                        player.getPosition().x, player.getPosition().z,
                        ax, az, snap.speed, snap.damage, snap.bulletSize,
                        replayPierce, snap.ricochet,
                        Constants.LEVEL_HALF_WIDTH, Constants.LEVEL_HALF_HEIGHT);
                projectiles.add(proj); rootNode.attachChild(proj.getNode());
                blackHoleSpewTimer+=spewInterval;
            }
            if (blackHoleStack.isEmpty()) { blackHoleSpewing=false; blackHoleRecordTimer=0f; }
        }
    }

    private void updateBlackHoleHud() {
        if (!blackHoleActive) { uiManager.setBlackHoleStatus(""); return; }
        if (blackHoleSpewing) {
            uiManager.setBlackHoleStatus("O BLACK HOLE: SPEWING ["+blackHoleStack.size()+" left]");
        } else {
            float rd=upgradeManager.getBlackHoleRecordTime();
            uiManager.setBlackHoleStatus(String.format(
                    "O BLACK HOLE: RECORDING [%.0f/%.0fs | %d shots]",
                    blackHoleRecordTimer, rd, blackHoleStack.size()));
        }
    }

    // -- Kill/pickup --
    private void onEnemyKilled(Enemy e) {
        gameEngine.recordKill(e.getEnemyType());
        int credits=(int)(e.getEnemyType().scoreValue*Constants.CREDITS_PER_SCORE*difficulty.scoreMultiplier);
        player.addCredits(credits);
        player.addExp(e.getEnemyType().expValue, difficulty.expGainMult);
        spawnPickup(e.getPosition().x, e.getPosition().z, Pickup.PickupType.EXP, e.getEnemyType().expValue);
    }

    private void spawnPickup(float x, float z, Pickup.PickupType type, int expAmount) {
        Pickup pk=new Pickup(assetManager, x, z, type, expAmount);
        pickups.add(pk); rootNode.attachChild(pk.getNode());
    }

    private void applyPickup(Pickup pk) {
        switch (pk.getPickupType()) {
            case HEART    -> player.heal();
            case EXP      -> player.addExp(pk.getExpAmount(), difficulty.expGainMult);
            case MUTATION -> triggerLevelUp();
        }
    }

    // -- State transitions --
    private void togglePause() {
        if (gameState==GameState.PLAYING) { gameState=GameState.PAUSED; uiManager.showPauseOverlay(true); }
        else if (gameState==GameState.PAUSED) { gameState=GameState.PLAYING; uiManager.showPauseOverlay(false); }
    }

    private void triggerGameOver() {
        gameState=GameState.GAME_OVER;
        uiManager.showGameOverOverlay(gameEngine.getScore());
    }

    private void returnToDifficultySelect() {
        fireLocked=false; uiManager.setFireLockStatus(false);
        gameState=GameState.DIFFICULTY_SELECT;
        uiManager.reset(); uiManager.showDifficultySelect();
    }

    // -- Screen to world --
    private float[] screenToWorld(float screenX, float screenY) {
        float w=settings.getWidth(), h=settings.getHeight();
        float aspect=w/h, viewHalfH=Constants.LEVEL_HALF_HEIGHT+1.5f, viewHalfW=viewHalfH*aspect;
        float ndcX=(screenX/w)*2f-1f, ndcY=(screenY/h)*2f-1f;
        // Add camera offset so cursor world position follows the camera (which follows the player)
        float camX = cam.getLocation().x;
        float camZ = cam.getLocation().z;
        return new float[]{camX + ndcX*viewHalfW, camZ - ndcY*viewHalfH};
    }

    // -- Scene setup --
    private void setupCamera() {
        cam.setLocation(new Vector3f(0f, Constants.CAMERA_HEIGHT, 0f));
        cam.lookAtDirection(new Vector3f(0f,-1f,0f), new Vector3f(0f,0f,-1f));
        cam.setParallelProjection(true);
        updateCameraFrustum(settings.getWidth(), settings.getHeight());
    }

    private void updateCameraFrustum(int w, int h) {
        if (cam == null) return;
        float aspect=((float)w)/h, viewHalfH=Constants.LEVEL_HALF_HEIGHT+1.5f, viewHalfW=viewHalfH*aspect;
        cam.setFrustum(-1000f, 1000f, -viewHalfW, viewHalfW, viewHalfH, -viewHalfH);
    }

    private void setupLighting() {
        AmbientLight ambient=new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
    }
}
