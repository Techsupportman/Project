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
│   └── GameState.java         – Enum: DIFFICULTY_SELECT | PLAYING | PAUSED | LEVEL_UP | GAME_OVER
├── difficulty/
│   └── Difficulty.java        – Easy / Normal / Hard / Nightmare with per-tier stat multipliers
├── entities/
│   ├── GameObject.java        – Abstract base: position, size, AABB collision, scene Node
│   ├── Player.java            – Heart HP, ranged weapon, aim direction, EXP/level, credits
│   ├── Enemy.java             – Multiple EnemyType variants; vision-based visibility
│   ├── EnemyType.java         – 8 standard + 2 mini-bosses + 5 bosses with unlock times
│   ├── Projectile.java        – Bullet: direction, speed, pierce, ricochet
│   └── Pickup.java            – Heart / EXP / Mutation collectible
├── weapons/
│   ├── WeaponType.java        – 18 weapon types (Pistol → Rocket) with stat definitions
│   ├── WeaponStats.java       – Damage, fire rate, bullet speed/size, pierce, ricochet, pellets
│   └── Weapon.java            – Active weapon state: unlock status, fire-rate cooldown
├── systems/
│   ├── AISystem.java          – Enemy direct-pursuit pathfinding + separation
│   ├── CombatSystem.java      – Enemy contact damage (1 heart per hit)
│   ├── PhysicsSystem.java     – AABB overlap resolution
│   ├── ProjectileSystem.java  – Move bullets, test against enemies, apply pierce
│   ├── SpawnManager.java      – Time-based continuous spawning: standard → mini-boss → boss
│   └── VisionSystem.java      – Circle / Cone visibility per enemy and pickup
├── upgrades/
│   ├── Upgrade.java           – Single upgrade node (id, parent, branch, name, magnitude)
│   ├── UpgradeTree.java       – 7-node tree with exclusive A/B branching
│   └── UpgradeManager.java    – 5 trees, generate choices, apply, reroll, delete
├── ui/
│   ├── HUD.java               – Hearts, level/EXP, credits, time, vision mode, score
│   ├── UIManager.java         – Facade over HUD + LevelUpMenu
│   └── LevelUpMenu.java       – Level-up overlay: 3-5 upgrade options, reroll, delete
├── levels/
│   ├── Level.java             – Abstract base for all levels
│   ├── LevelManager.java      – Load / unload / reload levels
│   └── Level1.java            – The arena: dark floor, grid lines, boundary walls
├── utils/
│   ├── Constants.java         – All tunable numbers in one place
│   ├── InputHandler.java      – Keyboard + mouse state (movement, fire, vision toggle, choices)
│   └── Vector2D.java          – Lightweight XZ-plane vector maths
└── assets/
    ├── PlaceholderGenerator.java – Labelled coloured boxes (replace with real art later)
    └── MaterialFactory.java      – Unshaded material helpers
```

---

## ✨ Features

| Feature | Details |
|---|---|
| **Difficulty selection** | Easy (6 HP) / Normal (3 HP) / Hard / Nightmare — stat multipliers applied to all systems |
| **Ranged combat** | Left-click to fire toward the mouse cursor; weapon auto-fires while held |
| **18 Weapon types** | Pistol → Rocket; unlock with Credits; each has unique damage/fire rate/pierce/ricochet/spread |
| **Vision modes** | **Circle** — reveals all nearby enemies in 360°; **Cone** — reveals a directional area farther away; toggle with **RMB** |
| **Fog of darkness** | Enemies and EXP are invisible outside vision range (cull-hint based) |
| **Continuous spawning** | Time-based spawn manager: faster spawns over time, new enemy types unlock at timed thresholds |
| **8 standard enemy types** | Basic, Runner, Tank, Shooter, Swarm, Bruiser, Specter, Artillery |
| **Mini-bosses** | Alpha / Beta mini-bosses spawn every ~90 seconds |
| **5 Bosses** | Colossus → Oblivion — one every 5 real minutes; arena sealed during fight |
| **Heart-based HP** | Integer hearts displayed on HUD (♥); 1 heart lost per contact hit |
| **Pickups** | EXP orbs (auto-collected), Hearts, Mutations (boss drops) |
| **EXP & Levelling** | Kill enemies to earn EXP; level up to choose upgrades |
| **5 Upgrade trees** | Damage, Fire Rate, Vitality, Mobility, Piercing — 7 nodes each with exclusive A/B branches |
| **Level-up menu** | 3–5 random options; one Reroll and one Delete available per level |
| **Credits** | Accumulated as Score × 0.1; spend to unlock weapons |
| **HUD** | Hearts, Lv/EXP bar, Credits, MM:SS timer, Vision indicator, Score |
| **Pause / Resume** | P key |
| **Game Over / Restart** | Returns to difficulty select on R |
| **Unit tests** | 40 tests covering engine, vision, weapons, upgrades, difficulty, vector maths |

---

## 🎮 Controls

| Input | Action |
|---|---|
| W / A / S / D | Move |
| **Left Mouse Button** (hold) | Fire active weapon toward cursor |
| **Right Mouse Button** | Toggle Circle / Cone vision |
| Q / E | Cycle to next unlocked weapon |
| P | Pause / Resume |
| R | Restart (when Game Over) |
| 1 – 5 | Choose upgrade in level-up menu |
| F | Reroll upgrade choices (once per level) |
| G | Skip / Delete upgrade (once per level) |

---

## 🖼️ Asset Replacement Guide

All visuals are placeholder flat boxes with a BitmapText label.  To swap in real art:

| Label on box | Where to replace | Suggested real asset |
|---|---|---|
| `PLAYER` | `Player.java` constructor | Player sprite sheet / model |
| `BASIC`, `RUNNER`, etc. | `Enemy.java` `colorFor()` | Enemy sprite variants |
| `BULLET` | `Projectile.java` constructor | Bullet sprites per weapon type |
| `HEART`, `EXP`, `MUTATION` | `Pickup.java` constructor | Pickup sprites |
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

* The project uses jMonkeyEngine **3.8.1-stable** (see `pom.xml`).
  Dependencies are resolved from Maven Central.  To open in the
  [jMonkeyEngine SDK](https://github.com/jMonkeyEngine/sdk/releases), open
  the project as a *Maven project* — the `nbactions.xml` file provides the
  Run / Debug / Test action bindings required by the SDK.
* All game constants (speed, health, vision radii, spawn intervals, etc.) live in
  `com.project.utils.Constants` — edit that file to tune balance.
* Weapon unlock costs and all 18 weapon stat definitions are in
  `com.project.weapons.WeaponType`.

