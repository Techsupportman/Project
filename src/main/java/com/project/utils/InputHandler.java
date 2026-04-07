package com.project.utils;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

/**
 * Manages keyboard input by registering action mappings with jMonkey's
 * {@link InputManager} and storing per-frame state.
 *
 * <p>Movement keys (WASD / arrow keys) are continuous: {@code isMoveLeft()} etc.
 * return {@code true} while the key is held down.
 *
 * <p>Action keys (SPACE, P, R) are one-shot: the flag is cleared after it is
 * consumed by the first call to the corresponding {@code is*Pressed()} getter.
 */
public class InputHandler implements ActionListener {

    // ------------------------------------------------------------------
    // Action-name constants
    // ------------------------------------------------------------------
    private static final String MOVE_LEFT  = "MoveLeft";
    private static final String MOVE_RIGHT = "MoveRight";
    private static final String MOVE_UP    = "MoveUp";
    private static final String MOVE_DOWN  = "MoveDown";
    private static final String ATTACK     = "Attack";
    private static final String PAUSE      = "Pause";
    private static final String RESTART    = "Restart";

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveUp;
    private boolean moveDown;

    // One-shot flags: consumed on first read
    private boolean attackPending;
    private boolean pausePending;
    private boolean restartPending;

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------
    public InputHandler(InputManager inputManager) {
        // Remove default ESC → quit mapping so we can use it freely
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

        // Actions
        inputManager.addMapping(ATTACK,  new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(PAUSE,   new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping(RESTART, new KeyTrigger(KeyInput.KEY_R));

        inputManager.addListener(this,
                MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN,
                ATTACK, PAUSE, RESTART);
    }

    // ------------------------------------------------------------------
    // ActionListener implementation
    // ------------------------------------------------------------------
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case MOVE_LEFT  -> moveLeft  = isPressed;
            case MOVE_RIGHT -> moveRight = isPressed;
            case MOVE_UP    -> moveUp    = isPressed;
            case MOVE_DOWN  -> moveDown  = isPressed;
            case ATTACK     -> { if (isPressed) attackPending  = true; }
            case PAUSE      -> { if (isPressed) pausePending   = true; }
            case RESTART    -> { if (isPressed) restartPending = true; }
        }
    }

    // ------------------------------------------------------------------
    // Continuous movement getters
    // ------------------------------------------------------------------
    public boolean isMoveLeft()  { return moveLeft;  }
    public boolean isMoveRight() { return moveRight; }
    public boolean isMoveUp()    { return moveUp;    }
    public boolean isMoveDown()  { return moveDown;  }

    // ------------------------------------------------------------------
    // One-shot action getters (consume the flag on read)
    // ------------------------------------------------------------------
    public boolean isAttackPressed() {
        if (attackPending)  { attackPending  = false; return true; }
        return false;
    }

    public boolean isPausePressed() {
        if (pausePending)   { pausePending   = false; return true; }
        return false;
    }

    public boolean isRestartPressed() {
        if (restartPending) { restartPending = false; return true; }
        return false;
    }
}
