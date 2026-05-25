# MatCraft Minigames

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric_Loader-0.18.4-blue?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKM+VkTtIA0EQhr+NihpFwcJCLCIWRjGKYKMYNIIWYtQi3jYXE4W9W3YTIdiKtYWFaOGr8A9Qa2GhFkEFETtxaxXj+E4hEIwDw3x8M/wMA9YkY1X0TA+y+byOxqMRaW5esvxg4Y2OmlWGrmJq+v+3LT/u0bK+D5h6VfPjruoAfkN5AJXxPgBuJTzJAvoAS4DV3J+k5o/GI0+puUPRqPKg2cpaIlHE5YWsvmCYhzucT/GrVjKchH4VNX2WcICsCWkAg1fRygkAv/MUExOh6dw6HJogT3gAfYAT4Bv6Z4Aw9wEAAAAASUVORK5CYII=)
![Minecraft 1.21.11](https://img.shields.io/badge/Minecraft-1.21.11-green?logo=mojangstudios&logoColor=white)
![License: CC0](https://img.shields.io/badge/License-CC0_1.0-lightgrey)

**Server-side Fabric mod powering the MatCraft minigame engine — arena lifecycle management, concurrent game orchestration, and a full duel system with configurable kits.**

*Mod Fabric côté serveur propulsant le moteur de mini-jeux MatCraft — gestion du cycle de vie des arènes, orchestration de parties concurrentes et système de duel complet avec kits configurables.*

---

## Table of Contents / Table des matières

- [Overview](#overview--aperçu)
- [Architecture](#architecture)
- [Design Patterns & Technical Depth](#design-patterns--technical-depth)
- [Features](#features--fonctionnalités)
- [Tech Stack](#tech-stack--stack-technique)
- [Build & Run](#build--run)
- [Author](#author--auteur)

---

## Overview / Aperçu

**[EN]** MatCraft Minigames is a server-side Fabric mod for Minecraft 1.21.11 that implements a production-ready minigame framework. The system manages the full game lifecycle — from arena allocation to player session tracking to post-game cleanup — while supporting multiple concurrent games with automatic resource pooling. The flagship game mode is a 1v1 Duel system with kit management, countdown sequences, cross-server transfers via BungeeCord, and graceful disconnect handling.

**[FR]** MatCraft Minigames est un mod Fabric côté serveur pour Minecraft 1.21.11 qui implémente un framework de mini-jeux prêt pour la production. Le système gère le cycle de vie complet — de l'allocation d'arène au suivi de session joueur jusqu'au nettoyage post-partie — tout en supportant plusieurs parties concurrentes avec un pool de ressources automatique. Le mode phare est un système de Duel 1v1 avec gestion de kits, séquences de compte à rebours, transferts inter-serveurs via BungeeCord et gestion gracieuse des déconnexions.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                      MatMinigames (Entry Point)                  │
│         DedicatedServerModInitializer — Event Registration       │
└───────────┬──────────────┬──────────────┬───────────────┬────────┘
            │              │              │               │
            ▼              ▼              ▼               ▼
   ┌────────────┐  ┌──────────────┐ ┌──────────┐  ┌────────────┐
   │ GameManager│  │ ArenaManager │ │  Config  │  │  Network   │
   │ (Singleton)│  │  (Registry)  │ │ Manager  │  │  Transfer  │
   └─────┬──────┘  └──────┬───────┘ └────┬─────┘  └──────┬─────┘
         │                │              │               │
         ▼                ▼              ▼               │
   ┌───────────┐   ┌───────────┐  ┌──────────┐          │
   │ Minigame  │◄──│   Arena   │  │ JSON     │          │
   │ (Abstract)│   │ (Record)  │  │ Configs  │          │
   └─────┬─────┘   └───────────┘  │ ×4 files │          │
         │                        └──────────┘          │
         ▼                                              │
   ┌───────────┐   ┌───────────┐   ┌────────────┐      │
   │ DuelGame  │──▶│  DuelKit  │   │ BungeeCord │◄─────┘
   │           │   │ (Loadout) │   │  Payload   │
   └─────┬─────┘   └───────────┘   └────────────┘
         │
         ▼
   ┌──────────────┐
   │PlayerManager │
   │(Session Map) │
   └──────────────┘

   Game State Machine:
   ┌─────────┐    ┌──────────┐    ┌─────────┐    ┌────────┐
   │ WAITING │───▶│ STARTING │───▶│ RUNNING │───▶│ ENDING │──▶ cleanup
   └─────────┘    └──────────┘    └─────────┘    └────────┘
      join +       countdown       game logic     delay +
      queue        + teleport      + events       restore
```

### Source Layout

```
src/main/java/qc/mat/minigames/
├── MatMinigames.java             # Entry point, event wiring
├── arena/
│   ├── Arena.java                # Immutable record (id, spawns, dimension)
│   └── ArenaManager.java         # Arena registry & availability queries
├── command/
│   ├── DuelCommand.java          # /duel challenge/accept/deny — Brigadier
│   └── MinigameCommand.java      # /minigame list/join/leave
├── config/
│   ├── ConfigManager.java        # Generic JSON config loader (Gson)
│   ├── ServerConfig.java         # Server mode, concurrency limits
│   ├── DuelConfig.java           # Kit definitions, timers
│   ├── ArenasConfig.java         # Arena list with spawn points
│   └── MessagesConfig.java       # All player-facing strings
├── game/
│   ├── GameState.java            # FSM enum: WAITING→STARTING→RUNNING→ENDING
│   ├── Minigame.java             # Abstract base — lifecycle, player mgmt
│   ├── GameManager.java          # Singleton orchestrator, tick loop
│   └── PlayerManager.java        # UUID→gameId session tracking (ConcurrentHashMap)
├── games/duel/
│   ├── DuelGame.java             # 1v1 PvP with countdown, kill tracking
│   └── DuelKit.java              # Programmatic item/enchantment/potion builder
├── lobby/
│   └── LobbyManager.java         # Lobby vs. Game server mode switch
└── network/
    └── ServerTransfer.java       # BungeeCord plugin messaging (S2C payloads)
```

---

## Design Patterns & Technical Depth

### Finite State Machine (FSM)

The `Minigame` abstract class implements a **tick-driven FSM** with four states (`WAITING`, `STARTING`, `RUNNING`, `ENDING`). Each state has a dedicated tick handler, enabling complex sequenced behavior (countdown timers, teleportation, kit application) without coroutines or threading. The `GameManager` drives all active games from a single server tick callback, ensuring deterministic execution.

### Template Method Pattern

`Minigame` defines the game lifecycle skeleton (`setup → start → tick → end → cleanup`) while concrete implementations (`DuelGame`) override specific phases. Hooks like `onPlayerJoin`, `onPlayerLeave`, `tickStarting`, and `tickRunning` allow game modes to specialize behavior without duplicating lifecycle management.

### Singleton + Registry

`GameManager` is a singleton that maintains a `LinkedHashMap` of active games, supporting O(1) lookup by game ID. `ArenaManager` acts as a registry, loaded from config at startup, providing type-filtered queries to find available arenas not currently in use.

### Challenge System with Expiry

`DuelCommand` implements a **challenge/accept/deny** protocol using a `ConcurrentHashMap<UUID, DuelChallenge>` with tick-based expiry. Challenges are validated against multiple conditions (not self-targeting, neither player in-game, no duplicate pending challenges) before creation, and expired entries are cleaned up lazily.

### Programmatic Item Builder

`DuelKit.buildItem()` constructs `ItemStack` instances programmatically from config data — resolving materials from the Vanilla registry, applying enchantments via `HolderLookup<Enchantment>`, and setting potion contents through `DataComponents`. This allows full kit customization without hardcoded items.

### Death Interception

The mod hooks into Fabric's `ALLOW_DEATH` event to **intercept player death** during duels. Instead of killing the player, it resets their health and triggers the game's `onPlayerKilled` handler — preventing death screens while maintaining combat flow.

### Cross-Server Networking

`ServerTransfer` implements **BungeeCord plugin messaging** via Fabric's custom payload API, enabling seamless player transfers between lobby and game servers in a proxy network.

---

## Features / Fonctionnalites

| Feature | Description EN | Description FR |
|---|---|---|
| **Duel System** | 1v1 PvP with challenge/accept/deny flow, configurable countdown, and automatic win/loss detection | Duel 1v1 avec flux défi/accepter/refuser, compte à rebours configurable et détection automatique victoire/défaite |
| **Arena Pool** | Multiple arenas with dimension-aware spawn points, automatic availability tracking | Pool d'arènes multi-dimension avec spawn points, suivi automatique de disponibilité |
| **Kit Loadout** | Full equipment sets from config: weapons, armor, potions with enchantments | Kits complets depuis la config : armes, armure, potions avec enchantements |
| **Concurrent Games** | Up to N simultaneous games (configurable), each with independent state | Jusqu'à N parties simultanées (configurable), chacune avec état indépendant |
| **Session Tracking** | Thread-safe player-to-game mapping, graceful disconnect handling | Mapping joueur-partie thread-safe, gestion gracieuse des déconnexions |
| **Server Transfer** | BungeeCord channel for lobby↔game server transfers | Canal BungeeCord pour transferts lobby↔serveur de jeu |
| **Hot-Reloadable Config** | 4 JSON config files (server, duel, arenas, messages) with defaults auto-generated | 4 fichiers JSON (serveur, duel, arènes, messages) avec défauts auto-générés |

---

## Tech Stack / Stack technique

| Component | Technology |
|---|---|
| Language | Java 21 |
| Mod Loader | Fabric Loader 0.18.4 |
| Minecraft | 1.21.11 |
| API | Fabric API 0.141.3 |
| Mappings | Official Mojang |
| Build System | Gradle + fabric-loom-remap |
| Serialization | Gson (config files) |
| Concurrency | ConcurrentHashMap, AtomicInteger |
| Commands | Brigadier (Minecraft's command framework) |
| Networking | Fabric Networking v1, BungeeCord plugin messaging |

---

## Build & Run

```bash
# Clone
git clone https://github.com/Maaattqc/matcraft-minigames.git
cd matcraft-minigames

# Build
./gradlew build

# Output JAR → build/libs/matminigames-1.0.0.jar
# Deploy to a Fabric 1.21.11 server's mods/ directory
```

**Requirements:** Java 21, Fabric Loader 0.18.4+, Fabric API

---

## Author / Auteur

**Mathieu Fournier** — [@Maaattqc](https://github.com/Maaattqc)
