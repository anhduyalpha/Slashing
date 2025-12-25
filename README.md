# Slashing Alphad RPG MVP

## Build & Run

```bash
gradlew runClient
```

```bash
gradlew build
```

## In-game Test Steps (Acceptance Tests)

1. Run the game/server once. Confirm the folder is created at:
   `config/slashing_alphad/packs/base/` with sample JSON files under `classes/`, `skills/`, and `statuses/`.
2. Run `/rpg reload` (OP only) and verify no errors in the log.
3. Run `/rpg class set @s reaper` (OP only).
4. Look at a mob and run `/rpg cast reaper:reaping_claw`.
   - The mob should be pulled toward you, take damage, and be stunned (movement disabled).
5. Run `/rpg profile` to see stats/resources/statuses.

## JSON Pack Notes

Runtime packs live at:
`<gameDir>/config/slashing_alphad/packs/`

Packs are loaded in alphabetical order; later packs override earlier packs by id.
Each pack contains:
- `classes/*.json`
- `skills/*.json`
- `statuses/*.json`

Use `/rpg reload` to apply JSON changes without restarting.
