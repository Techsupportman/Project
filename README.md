# Project – Top-Down 2D Game

A top-down 2D action game inspired by [Disfigure](https://disfigure.wiki.gg/), built with
[jMonkeyEngine](https://jmonkeyengine.org/) (3.6.1-stable / targeting 3.8).

---

## 📁 Project Structure

```
src/main/java/com/project/
├── core/
│   ├── Main.java          – Entry point; configures AppSettings and starts GameApp
│   ├── GameApp.java       – jMonkey SimpleApplication; owns all systems and the game loop
│   ├── GameEngine.java    – Pure-logic wave manager: wave progression, score, events
│   └── GameState.java     – Enum: PLAYING | PAUSED | GAME_OVER
├── entities/
│   ├── GameObject.java    – Abstract base: position, size, AABB collision, scene Node
│   ├── Player.java        – Player character: health, movement, melee attack
│   └── Enemy.java         – Enemy: health, speed, contact damage
├── systems/
│   ├── AISystem.java      – Enemy direct-pursuit pathfinding + separation
│   ├── CombatSystem.java  – Player AoE attack & enemy contact damage
│   └── PhysicsSystem.java – AABB overlap resolution for all entity pairs
├── ui/
│   ├── HUD.java           – 2D health bar, wave count, score, overlay banners
│   └── UIManager.java     – Thin facade over HUD (extensible for future screens)
├── levels/
│   ├── Level.java         – Abstract base for all levels
│   ├── LevelManager.java  – Load / unload / reload levels
│   └── Level1.java        – The arena: dark floor, grid lines, boundary walls
├── utils/
│   ├── Constants.java     – All tunable numbers in one place
│   ├── InputHandler.java  – Keyboard state (held keys + one-shot action flags)
│   └── Vector2D.java      – Lightweight XZ-plane vector maths
└── assets/
    ├── PlaceholderGenerator.java – Labelled coloured boxes (replace with real art later)
    └── MaterialFactory.java      – Unshaded material helpers
```

---

## ✨ Features

| Feature | Details |
|---|---|
| **Top-down orthographic camera** | Positioned directly above the arena, parallel projection |
| **Player movement** | WASD / arrow keys, normalised for diagonals, clamped to arena bounds |
| **Melee attack** | SPACE – area-of-effect around the player; cooldown-gated |
| **Enemy AI** | Direct pursuit + separation force to prevent pile-ups |
| **Wave system** | Enemies spawn in waves along the arena edges; count and speed increase each wave |
| **Collision detection** | AABB overlap resolution for player–enemy and enemy–enemy pairs |
| **Health system** | Player and enemies each have health; HUD bar changes colour (green → red) |
| **Pause / Resume** | P key toggles; game loop freezes cleanly |
| **Game Over / Restart** | Player death shows overlay; R resets all state cleanly |
| **Placeholder assets** | Every visual element is a coloured flat box with a text label indicating the real asset to substitute |
| **Unit tests** | Pure-logic tests for `Vector2D` and `GameEngine` (13 tests, no display required) |

---

## 🎮 Controls

| Key | Action |
|---|---|
| W / ↑ | Move up |
| S / ↓ | Move down |
| A / ← | Move left |
| D / → | Move right |
| SPACE | Attack (melee AoE) |
| P | Pause / Resume |
| R | Restart (when Game Over) |

---

## 🖼️ Asset Replacement Guide

All visuals are placeholder flat boxes with a BitmapText label.  To swap in real art:

| Label on box | Where to replace | Suggested real asset |
|---|---|---|
| `PLAYER_SPRITE` | `Player.java` constructor | Player sprite sheet / model |
| `ENEMY_SPRITE` | `Enemy.java` constructor | Enemy sprite sheet / model |
| Floor / walls | `Level1.java` | Tile-map or environment model |

---

## 🔧 Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Run from source
```bash
mvn compile exec:java -Dexec.mainClass=com.project.core.Main
```

### Build a fat JAR
```bash
mvn package
java -jar target/project-game-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run tests
```bash
mvn test
```

---

## 📝 Notes

* The project targets jMonkeyEngine **3.8** (declared in `pom.xml`).  The
  current dependency resolves to **3.6.1-stable**, the latest stable release
  at the time of writing.  Update `<jme.version>` in `pom.xml` once 3.8 is
  published to the JMonkeyEngine Maven repository.
* All game constants (speed, health, wave sizes, etc.) live in
  `com.project.utils.Constants` — edit that file to tune balance.
