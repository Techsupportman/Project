# Project – Top-Down 2D Game (Disfigure-inspired)

A top-down 2D action game inspired by [Disfigure](https://disfigure.wiki.gg/), built with
[jMonkeyEngine 3.8.1-stable](https://github.com/jMonkeyEngine/jmonkeyengine/releases/tag/v3.8.1-stable).

---

## 📁 Project Structure

```
src/main/java/com/project/
├── core/
│   ├── Main.java              – Entry point; configures AppSettings and starts GameApp
│   ├── GameApp.java           – jMonkey SimpleApplication; owns all systems and the game loop
│   ├── GameEngine.java        – Score, credits, time tracking, difficulty-scaled kills
│   └── GameState.java         – Enum: DIFFICULTY_SELECT | WEAPON_SELECT | PLAYING | PAUSED | SETTINGS | LEVEL_UP | GAME_OVER
├── difficulty/
│   └── Difficulty.java        – Easy / Normal / Hard / Nightmare with per-tier stat multipliers
├── entities/
│   ├── GameObject.java        – Abstract base: position, size, AABB collision, scene Node
│   ├── Player.java            – Heart HP, ranged weapon, aim direction, EXP/level, credits
│   ├── Enemy.java             – Multiple EnemyType variants; melee and ranged attack cooldowns
│   ├── EnemyType.java         – 8 standard + 2 mini-bosses + 5 bosses with unlock times
│   ├── Projectile.java        – Bullet: mutable direction, speed, pierce, enemy-ricochet
│   └── Pickup.java            – Heart / EXP / Mutation collectible
├── weapons/
│   ├── WeaponType.java        – 18 weapon types (Pistol → Rocket) with stat definitions
│   ├── WeaponStats.java       – Damage, fire rate, bullet speed/size, pierce, ricochet, pellets
│   └── Weapon.java            – Active weapon state: unlock status, fire-rate cooldown
├── systems/
│   ├── AISystem.java          – Enemy pursuit + separation; SHOOTER/ARTILLERY fire ranged shots
│   ├── CombatSystem.java      – Enemy contact damage (1 heart per hit)
│   ├── PhysicsSystem.java     – AABB overlap resolution
│   ├── ProjectileSystem.java  – Move bullets, enemy collision, ricochet bounce toward next enemy
│   └── SpawnManager.java      – Time-based continuous spawning around the player; standard → mini-boss → boss
├── upgrades/
│   ├── Upgrade.java           – Single upgrade node (id, parent, branch, name, magnitude)
│   ├── UpgradeTree.java       – 7-node tree with exclusive A/B branching
│   └── UpgradeManager.java    – 5 trees, generate choices, apply, reroll, delete
├── ui/
│   ├── Button.java            – Reusable button with translucent background + visible border outline
│   ├── ButtonPanel.java       – Vertically-stacked array of Buttons with a panel background
│   ├── HUD.java               – Hearts, level/EXP, credits, time, score; all menus and overlays
│   ├── UIManager.java         – Facade over HUD + LevelUpMenu
│   └── LevelUpMenu.java       – Level-up overlay: 3-5 upgrade options, reroll, delete
├── levels/
│   ├── Level.java             – Abstract base for all levels
│   ├── LevelManager.java      – Load / unload / reload levels
│   └── Level1.java            – The arena: dark floor and grid lines
├── utils/
│   ├── Constants.java         – All tunable numbers in one place
│   ├── InputHandler.java      – Keyboard + mouse state (movement, fire, pause, fire-lock)
│   └── Vector2D.java          – Lightweight XZ-plane vector maths
└── assets/
    ├── PlaceholderGenerator.java – Labelled coloured boxes (replace with real art later)
    └── MaterialFactory.java      – Unshaded material helpers
```

---

## ✨ Features

| Feature | Details |
|---|---|
| **Difficulty selection** | Easy / Normal / Hard / Nightmare — stat multipliers applied to all systems |
| **Weapon selection** | Choose from all 18 weapons before starting; paginated mouse-driven menu |
| **Ranged combat** | Left-click to fire toward the mouse cursor; weapon auto-fires while held |
| **18 Weapon types** | Pistol → Rocket; each with unique damage / fire rate / pierce / ricochet / spread |
| **Camera follows player** | Orthographic top-down camera tracks the player; no arena boundary clamp |
| **Continuous spawning** | Time-based spawn manager: enemies appear around the player; spawn rate ramps up over time |
| **8 standard enemy types** | Basic, Runner, Tank, Shooter, Swarm, Bruiser, Specter, Artillery |
| **Ranged enemies** | SHOOTER fires projectiles every 1.8 s within 8 units; ARTILLERY fires every 3 s within 14 units |
| **Mini-bosses** | Alpha / Beta mini-bosses spawn every ~90 seconds |
| **5 Bosses** | Colossus → Oblivion — one every 5 real minutes; normal spawning resumes after defeat |
| **Heart-based HP** | Integer hearts on HUD (♥); 1 heart lost per contact or ranged hit |
| **Ricochet** | Bullets with ricochet bounce off hit enemies toward the nearest other enemy |
| **Pickups** | EXP orbs (auto-collected), Hearts, Mutations (boss drops) |
| **EXP & Levelling** | Kill enemies to earn EXP; level up to choose upgrades |
| **5 Upgrade trees** | Damage, Fire Rate, Vitality, Mobility, Piercing — 7 nodes each with exclusive A/B branches |
| **Level-up menu** | 3–5 random options; one Reroll and one Delete available per level |
| **Credits** | Accumulated as Score × 0.1; spend to unlock weapons |
| **HUD** | Hearts, Lv/EXP bar, Credits, MM:SS timer, Score |
| **Pause menu** | Resume / Restart / Settings / Quit |
| **Settings** | Master volume slider (Vol − / Vol +) applied in real time |
| **Button outlines** | All UI buttons have a visible border (silver → gold on hover) |
| **Game Over / Restart** | Click "Restart" to return to difficulty select |
| **Unit tests** | 34 tests covering engine, weapons, upgrades, difficulty, vector maths |

---

## 🎮 Controls

| Input | Action |
|---|---|
| W / A / S / D | Move |
| **Left Mouse Button** (hold) | Fire active weapon toward cursor |
| Q | Toggle Fire-Lock (auto-fire) |
| ESC | Pause / Resume |
| **Mouse click** | All menu interactions (difficulty, weapon, pause, level-up, game over) |

> Number keys 1–5, F, G, E, K are no longer used — all menus are fully mouse-driven.

---

## 🖼️ Asset Replacement Guide

All visuals are placeholder flat boxes with a BitmapText label.  To swap in real art:

| Label on box | Where to replace | Suggested real asset |
|---|---|---|
| `PLAYER` | `Player.java` constructor | Player sprite sheet / model |
| `BASIC`, `RUNNER`, etc. | `Enemy.java` `colorFor()` | Enemy sprite variants |
| `BULLET` | `Projectile.java` constructor (yellow = player, orange-red = enemy) | Bullet sprites per weapon type |
| `HEART`, `EXP`, `MUTATION` | `Pickup.java` constructor | Pickup sprites |
| Floor | `Level1.java` | Tile-map or environment model |

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

* The project uses jMonkeyEngine **3.8.1-stable** (see `pom.xml`).
  Dependencies are resolved from Maven Central.  To open in the
  [jMonkeyEngine SDK](https://github.com/jMonkeyEngine/sdk/releases), open
  the project as a *Maven project* — the `nbactions.xml` file provides the
  Run / Debug / Test action bindings required by the SDK.
* All game constants (speed, health, spawn intervals, enemy fire ranges, etc.) live in
  `com.project.utils.Constants` — edit that file to tune balance.
* Weapon unlock costs and all 18 weapon stat definitions are in
  `com.project.weapons.WeaponType`.
* There are no arena boundary walls — the camera follows the player freely.
  Enemies spawn off-screen around the player's current position.

