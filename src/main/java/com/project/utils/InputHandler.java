package com.project.utils;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;

/**
 * Manages keyboard and mouse input by registering action/analog mappings with
 * jMonkey's {@link InputManager} and storing per-frame state.
 *
 * <h3>Continuous inputs</h3>
 * <ul>
 *   <li>WASD / Arrow keys — movement flags</li>
 *   <li>Left Mouse Button (held) — fire weapon</li>
 * </ul>
 *
 * <h3>One-shot inputs</h3>
 * <ul>
 *   <li>P / ESC — pause/resume</li>
 *   <li>R — restart</li>
 *   <li>Right Mouse Button — toggle circle/cone vision</li>
 *   <li>Q — toggle fire lock (auto-fire)</li>
 *   <li>E — cycle weapon</li>
 *   <li>1-9 — confirm level-up choice index</li>
 * </ul>
 */
public class InputHandler implements ActionListener {

    // ------------------------------------------------------------------
    // Action-name constants
    // ------------------------------------------------------------------
    private static final String MOVE_LEFT      = "MoveLeft";
    private static final String MOVE_RIGHT     = "MoveRight";
    private static final String MOVE_UP        = "MoveUp";
    private static final String MOVE_DOWN      = "MoveDown";
    private static final String FIRE           = "Fire";
    private static final String VISION_TOGGLE  = "VisionToggle";
    private static final String PAUSE          = "Pause";
    private static final String ESCAPE         = "Escape";
    private static final String RESTART        = "Restart";
    private static final String CYCLE_WEAPON   = "CycleWeapon";
    private static final String FIRE_LOCK      = "FireLock";
    private static final String CHOICE_1       = "Choice1";
    private static final String CHOICE_2       = "Choice2";
    private static final String CHOICE_3       = "Choice3";
    private static final String CHOICE_4       = "Choice4";
    private static final String CHOICE_5       = "Choice5";
    private static final String REROLL         = "Reroll";
    private static final String DELETE         = "Delete";

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveUp;
    private boolean moveDown;
    private boolean fireHeld;      // LMB held
    private boolean lmbJustPressed; // LMB pressed this frame (one-shot, for menu clicks)

    // One-shot flags
    private boolean visionTogglePending;
    private boolean pausePending;
    private boolean escapePending;
    private boolean restartPending;
    private boolean cycleWeaponPending;
    private boolean fireLockPending;
    private int     choicePending = -1;  // -1 = no pending choice
    private boolean rerollPending;
    private boolean deletePending;

    // Mouse cursor position in screen space (updated via AnalogListener)
    private final Vector2f cursorPos = new Vector2f();
    private final InputManager inputManager;

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------
    public InputHandler(InputManager inputManager) {
        this.inputManager = inputManager;

        // Remove default ESC → quit mapping
        if (inputManager.hasMapping("SIMPLEAPP_Exit")) {
            inputManager.deleteMapping("SIMPLEAPP_Exit");
        }

        // Movement
        inputManager.addMapping(MOVE_LEFT,
                new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(MOVE_RIGHT,
                new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(MOVE_UP,
                new KeyTrigger(KeyInput.KEY_W),
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(MOVE_DOWN,
                new KeyTrigger(KeyInput.KEY_S),
                new KeyTrigger(KeyInput.KEY_DOWN));

        // Shooting & vision
        inputManager.addMapping(FIRE,          new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(VISION_TOGGLE, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        // System
        inputManager.addMapping(PAUSE,   new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping(ESCAPE,  new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping(RESTART, new KeyTrigger(KeyInput.KEY_R));

        // Weapon cycling (E only; Q is now fire-lock)
        inputManager.addMapping(CYCLE_WEAPON, new KeyTrigger(KeyInput.KEY_E));

        // Fire lock toggle
        inputManager.addMapping(FIRE_LOCK, new KeyTrigger(KeyInput.KEY_Q));

        // Level-up choices (1-5) and meta-actions
        inputManager.addMapping(CHOICE_1, new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping(CHOICE_2, new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping(CHOICE_3, new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping(CHOICE_4, new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping(CHOICE_5, new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping(REROLL,   new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping(DELETE,   new KeyTrigger(KeyInput.KEY_G));

        inputManager.addListener(this,
                MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN,
                FIRE, VISION_TOGGLE, PAUSE, ESCAPE, RESTART, CYCLE_WEAPON, FIRE_LOCK,
                CHOICE_1, CHOICE_2, CHOICE_3, CHOICE_4, CHOICE_5,
                REROLL, DELETE);

        // Cursor tracking via hardware cursor
        inputManager.setCursorVisible(true);
    }

    // ------------------------------------------------------------------
    // ActionListener implementation
    // ------------------------------------------------------------------
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case MOVE_LEFT     -> moveLeft  = isPressed;
            case MOVE_RIGHT    -> moveRight = isPressed;
            case MOVE_UP       -> moveUp    = isPressed;
            case MOVE_DOWN     -> moveDown  = isPressed;
            case FIRE          -> { fireHeld = isPressed; if (isPressed) lmbJustPressed = true; }
            case VISION_TOGGLE -> { if (isPressed) visionTogglePending = true; }
            case PAUSE         -> { if (isPressed) pausePending         = true; }
            case ESCAPE        -> { if (isPressed) escapePending        = true; }
            case RESTART       -> { if (isPressed) restartPending       = true; }
            case CYCLE_WEAPON  -> { if (isPressed) cycleWeaponPending   = true; }
            case FIRE_LOCK     -> { if (isPressed) fireLockPending      = true; }
            case CHOICE_1      -> { if (isPressed) choicePending = 0; }
            case CHOICE_2      -> { if (isPressed) choicePending = 1; }
            case CHOICE_3      -> { if (isPressed) choicePending = 2; }
            case CHOICE_4      -> { if (isPressed) choicePending = 3; }
            case CHOICE_5      -> { if (isPressed) choicePending = 4; }
            case REROLL        -> { if (isPressed) rerollPending  = true; }
            case DELETE        -> { if (isPressed) deletePending  = true; }
        }
    }

    // ------------------------------------------------------------------
    // Mouse cursor
    // ------------------------------------------------------------------
    /**
     * Returns the current hardware cursor position in screen-space pixels.
     * Call once per frame to read the mouse position.
     */
    public Vector2f getCursorPosition() {
        Vector2f pos = inputManager.getCursorPosition();
        cursorPos.set(pos.x, pos.y);
        return cursorPos;
    }

    // ------------------------------------------------------------------
    // Continuous movement getters
    // ------------------------------------------------------------------
    public boolean isMoveLeft()  { return moveLeft;  }
    public boolean isMoveRight() { return moveRight; }
    public boolean isMoveUp()    { return moveUp;    }
    public boolean isMoveDown()  { return moveDown;  }
    public boolean isFireHeld()  { return fireHeld;  }

    // ------------------------------------------------------------------
    // One-shot action getters (consume the flag on read)
    // ------------------------------------------------------------------
    public boolean isVisionTogglePressed() {
        if (visionTogglePending) { visionTogglePending = false; return true; }
        return false;
    }

    public boolean isPausePressed() {
        if (pausePending)   { pausePending   = false; return true; }
        return false;
    }

    public boolean isEscapePressed() {
        if (escapePending)  { escapePending  = false; return true; }
        return false;
    }

    public boolean isRestartPressed() {
        if (restartPending) { restartPending = false; return true; }
        return false;
    }

    public boolean isCycleWeaponPressed() {
        if (cycleWeaponPending) { cycleWeaponPending = false; return true; }
        return false;
    }

    public boolean isFireLockPressed() {
        if (fireLockPending) { fireLockPending = false; return true; }
        return false;
    }

    /**
     * Returns {@code true} once when the left mouse button was just clicked
     * this frame (one-shot, consumed on read). Used for menu interactions.
     */
    public boolean isLmbJustPressed() {
        if (lmbJustPressed) { lmbJustPressed = false; return true; }
        return false;
    }

    /**
     * Returns the index (0-based) of the level-up choice the player pressed,
     * or -1 if no choice was pressed this frame.
     */
    public int consumeChoicePending() {
        int c = choicePending;
        choicePending = -1;
        return c;
    }

    public boolean isRerollPressed() {
        if (rerollPending) { rerollPending = false; return true; }
        return false;
    }

    public boolean isDeletePressed() {
        if (deletePending) { deletePending = false; return true; }
        return false;
    }

    // ------------------------------------------------------------------
    // Legacy: kept so existing tests that call isAttackPressed() compile
    // ------------------------------------------------------------------
    public boolean isAttackPressed() { return false; }
}
