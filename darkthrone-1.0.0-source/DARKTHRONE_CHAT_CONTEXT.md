# Dark Throne — Codex Project Context

## Target
Minecraft 1.16.5, Forge 36.2.x, Vampirism 1.9.10, Java 8.
Mod ID: `darkthrone`
Java package: `ru.darkthrone`

This file is the working context for Codex. Read it before modifying the project.
Do not claim a feature is implemented or buildable unless it is actually present and tested.

## Project goal
Create a Vampirism addon called Dark Throne with two vampire endgame titles:
- King of Vampires
- Emperor of Darkness

The Emperor storyline leads to the final boss Xard'Rot and the Blood Void.

## King of Vampires
Requirements:
- Vampire Lord level 5.
- All vampire-side Vampirism advancements completed.

Prefer dynamically detecting Vampirism vampire advancements instead of hard-coding an achievement list.

Passive: Royal Fortress
- immune to knockback/displacement where practical;
- immune to fall damage.

Active: Call of the Blood Pack
- summons controlled enhanced wolves with glowing/blood-themed eyes;
- summons bats;
- pack attacks targets attacked by the King;
- pack protects the King;
- use ownership, lifetime and count limits so entities cannot be spawned infinitely.

## Emperor of Darkness
Obtained through the storyline after becoming King.

Passive: Lord of Evil
- hostile mobs normally do not initiate attacks against the Emperor;
- where feasible, account for hostile mobs from compatible third-party mods;
- major storyline bosses remain exceptions.

Passive: Absolute Armor of Night
- sunlight no longer burns the Emperor;
- block Vampirism `sun_blindness` and `sun_weakness`.

Passive: Harvest of the Primordial
- killing a creature heals the Emperor;
- blood is restored to maximum;
- prevent duplicate processing of one death.

## Ultimate abilities

### Fury of the Primordial Dragon
- Ender Dragon roar sound;
- crimson/blood particles;
- radius: 15 blocks;
- huge damage;
- strong knockback;
- Emperor heals for damage dealt;
- server-authoritative;
- cooldown required.

### Blood Eclipse
- instantly sets the relevant world/server to deep night;
- entities within 20 blocks receive Blindness and Slowness V;
- duration: 10 seconds;
- multiplayer semantics should be explicit/configurable.

## Lore

Ancient legend:
The first true vampire was the Primordial Blood Dragon. Modern vampires inherited only fragments
of its power. Weaknesses to sunlight and garlic are remnants of the ancient curse.

### Chapter I
After reaching Lord level 5:
- rebellion against three High Elders;
- defeat them;
- obtain royal regalia;
- perform the ritual;
- become King.

### Chapter II
- search for Blood Tomes;
- travel to the End;
- use the ritual dagger during the Ender Dragon encounter;
- after the dragon dies, obtain the Heart of the Primordial Dragon.

### Chapter III
Ritual monument uses:
- obsidian;
- crying obsidian;
- blocks of pure blood.

Consume/use the Heart.
Display:
`Старый мир содрогнулся. Проклятие солнца разрушено. Приветствуйте Императора Тьмы!`

After the ritual:
- ordinary monsters submit;
- hunters stop attacking;
- sunlight no longer harms the Emperor.

## Xard'Rot
Former Emperor and final boss.

Lore:
- ancient vampire;
- first to absorb the Primordial Blood Dragon's blood;
- became immensely powerful;
- defeated/sealed by High Lords and End mages;
- power sealed in the Dragon's Heart;
- exiled to The Blood Void.

After the Ender Dragon is killed and the Heart is obtained:
`Ты украл мою силу, червь... Но корона держится лишь на той голове, которая способна её удержать. Я иду за своим Сердцем.`

### Boss
5000 HP.

Phase 1, 100–60%:
- ancient vampire form;
- spectral wolves;
- Call of the Blood Pack;
- Dragon Roar;
- teleportation;
- ordinary weapons comparatively ineffective.

Phase 2, 60–20%:
- dragon-like blood wings;
- Eternal Eclipse;
- player blindness;
- dive attacks;
- every 30 seconds may consume a nearby mob/bat and heal 200 HP.

Phase 3, 20–0%:
- humanoid half-dragon/half-vampire;
- more vulnerable;
- approximately 3x damage behavior;
- blood rain;
- exposed players take damage;
- AoE;
- block destruction must be bounded/configurable.

After defeat:
- remaining Xard'Rot power is absorbed;
- final coronation;
- true Emperor status;
- ultimate abilities fully unlocked.

## Blood Void
Final dimension/arena.
Portal concept:
- crying obsidian;
- Eyes of Ender;
- activated with blood.

Implement with Minecraft 1.16.5 Forge APIs, not modern Minecraft dimension APIs.

## Resources
Use original placeholder artwork rather than copying Vampirism assets.

Required resource concepts:
- King texture
- Emperor texture
- Xard'Rot phase 1/2/3 textures
- Heart of Primordial Dragon
- Ritual Dagger
- King's Regalia
- Emperor's Regalia
- Blood Obelisk
- Blood Void Portal Frame
- Fury effect
- Blood Eclipse effect

## Current scaffold
The original source archive is a starting scaffold, not the finished addon.
It contains Forge entry point, build files, mods.toml, pack.mcmeta, language files,
basic model files and original placeholder PNG assets.

The current Java entry point is only the initial Forge mod bootstrap. Gameplay systems still need implementation.

## Useful Vampirism API notes
Previously inspected:
- `IFactionPlayerHandler`: `getCurrentFactionPlayer()`, `getCurrentLevel()`,
  `getLordLevel()`, `setLordLevel(...)`
- `ILordPlayer`: `getLordLevel()`, `getLordTitle()`, `getLordFaction()`
- `IBloodStats`: `getBloodLevel()`, `getMaxBlood()`, `needsBlood()`
- `IVampire`: `drinkBlood`, `useBlood`, `isGettingSundamage`,
  `isIgnoringSundamage`, `wantsBlood`

The official Vampirism addon example uses ForgeGradle 3.x-era conventions and
`compileOnly fg.deobf(...)` for Vampirism. Verify the exact dependency setup for 1.16.5/1.9.10 before assuming it builds.

## Development priorities
1. Make the Gradle project buildable.
2. Register items, blocks, effects and entities.
3. Add persistent King/Emperor progression.
4. Implement dynamic vampire advancement completion.
5. Implement Lord level 5 -> King.
6. Implement Royal Fortress.
7. Implement Blood Pack.
8. Implement Emperor storyline.
9. Implement Emperor passives.
10. Implement ultimate abilities, cooldowns and actions/keybinds.
11. Implement Ender Dragon -> Heart progression.
12. Implement Emperor ritual.
13. Implement Xard'Rot and phase controller.
14. Implement Blood Void.
15. Implement final encounter.
16. Implement coronation and unlocks.
17. Test dedicated server/multiplayer behavior.
18. Replace placeholder artwork with final art.

## Engineering rules
- Java 8.
- Minecraft 1.16.5 APIs only.
- Server-authoritative progression and combat.
- Keep client-only classes out of dedicated-server execution paths.
- Persist progression and cooldowns correctly.
- Avoid UUID-specific hacks.
- Avoid accidental friendly/owned entity damage.
- Limit destructive boss behavior.
- Prefer configurable values for destructive or server-wide effects.
- Use lowercase resource paths.
- Keep custom resources under `assets/darkthrone/`.
- Never claim a build succeeded unless the build was actually run successfully.
- If dependency resolution prevents testing, report that honestly.

## Workflow
The user intends to work in VS Code/Gradle and may use Codex.
Codex should read this file and inspect the source tree before coding.
The user can run Gradle locally and provide build errors for debugging.
Update this file when major architecture decisions change.
