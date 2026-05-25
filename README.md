# MatCraft Minigames ⚔️

> Server-side Fabric mod for Minecraft minigames — arena management, duels, and game state machines.
>
> Mod Fabric côté serveur pour les mini-jeux Minecraft — gestion d'arènes, duels et machines à états.

## 🚀 Overview / Aperçu

**[EN]** A custom Fabric mod for Minecraft 1.21.11 that powers the minigame system on the MatCraft server. Features a complete game state machine, arena management, player tracking, and a duel system with customizable kits. Designed for a live multiplayer environment with concurrent games and player queues.

**[FR]** Un mod Fabric sur mesure pour Minecraft 1.21.11 qui propulse le système de mini-jeux du serveur MatCraft. Inclut une machine à états complète, gestion d'arènes, suivi des joueurs et un système de duels avec kits personnalisables. Conçu pour un environnement multijoueur en direct avec parties concurrentes et files d'attente.

## 🛠️ Tech Stack

- **Language:** Java
- **Framework:** Fabric Mod Loader (1.21.11)
- **Mappings:** Official Mojang mappings
- **API:** Fabric API 0.141.3
- **Build:** Gradle + fabric-loom-remap
- **Minecraft:** 1.21.11

## 🧠 Technical Highlights / Défis Techniques

- **State machine architecture** — `GameState` enum driving game lifecycle (waiting → starting → playing → ending) with clean transitions
- **Arena management system** — `ArenaManager` handles multiple concurrent arenas with player capacity, spawn points, and availability tracking
- **Player session tracking** — `PlayerManager` manages player states across games, handling joins, leaves, disconnects, and spectating
- **Command framework** — `/duel` and `/minigame` commands with argument parsing and permission checks
- **Modular game design** — `Minigame` base class extended by game types (e.g., `DuelGame` with `DuelKit` system)
- **Server-side only** — no client-side dependencies, works with any vanilla or modded client

## ✨ Features / Fonctionnalités

- ⚔️ **Duel system** — 1v1 PvP with customizable kits
- 🏟️ **Arena management** — multiple arenas with spawn points and capacity
- 🎮 **Game state machine** — clean lifecycle management
- 👥 **Player tracking** — join, leave, disconnect, spectate handling
- 🔧 **Admin commands** — `/minigame` and `/duel` with full control
- 🔌 **Extensible** — base `Minigame` class for adding new game types

## 📦 Build & Deploy

```bash
# Build the mod
./gradlew build

# Output JAR
build/libs/matminigames-1.0.0.jar

# Deploy to server
# Upload JAR to server mods/ directory and restart
```

## 📁 Architecture

```
src/main/java/qc/mat/minigames/
├── game/
│   ├── Minigame.java        # Base game class
│   ├── GameState.java        # State machine enum
│   ├── GameManager.java      # Orchestrates all games
│   └── PlayerManager.java    # Player session tracking
├── games/
│   └── duel/
│       ├── DuelGame.java     # 1v1 PvP implementation
│       └── DuelKit.java      # Kit loadout system
├── arena/
│   ├── Arena.java            # Arena definition
│   └── ArenaManager.java     # Arena pool management
└── command/
    ├── MinigameCommand.java  # Admin commands
    └── DuelCommand.java      # Duel commands
```

## 👤 Author / Auteur

**Mathieu Fournier** — [@Maaattqc](https://github.com/Maaattqc)
