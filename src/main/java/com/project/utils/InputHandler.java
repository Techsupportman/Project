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
 *   <li>ESC — pause/resume</li>
 *   <li>Q — toggle fire lock (auto-fire)</li>
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
    private static final String PAUSE          = "Pause";
    private static final String ESCAPE         = "Escape";
    private static final String FIRE_LOCK      = "FireLock";

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
    private boolean pausePending;
    private boolean escapePending;
    private boolean fireLockPending;

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

        // Shooting
        inputManager.addMapping(FIRE,          new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // System
        inputManager.addMapping(PAUSE,   new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping(ESCAPE,  new KeyTrigger(KeyInput.KEY_ESCAPE));

        // Fire lock toggle
        inputManager.addMapping(FIRE_LOCK, new KeyTrigger(KeyInput.KEY_Q));

        inputManager.addListener(this,
                MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN,
                FIRE, PAUSE, ESCAPE, FIRE_LOCK);

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
            case PAUSE         -> { if (isPressed) pausePending         = true; }
            case ESCAPE        -> { if (isPressed) escapePending        = true; }
            case FIRE_LOCK     -> { if (isPressed) fireLockPending      = true; }
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
    public boolean isPausePressed() {
        if (pausePending)   { pausePending   = false; return true; }
        return false;
    }

    public boolean isEscapePressed() {
        if (escapePending)  { escapePending  = false; return true; }
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

    // ------------------------------------------------------------------
    // Legacy: kept so existing tests that call isAttackPressed() compile
    // ------------------------------------------------------------------
    public boolean isAttackPressed() { return false; }
}
