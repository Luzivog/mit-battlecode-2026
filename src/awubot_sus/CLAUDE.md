**when refactoring, always maintain 100% functionality the same as before**

# Infrastructure & Code Organization Guidelines

## Abstraction
- If logic is repeated, extract it.
- Reused across files → helper in `/utils`.
- Reused in one file → local helper.
- If fixing a bug would require multiple edits, abstraction is required.

## `/utils` Usage
Put code in `/utils` only if it is:
- Strategy-agnostic
- Reused in multiple places

Do **not** put:
- Strategy-specific logic
- One-off helpers
- Convenience wrappers

## Strategy Ownership
- General robot / engine classes should contain:
  - Shared mechanics
  - State plumbing
  - Execution hooks
- Do **not** add decision-making or behavior logic to robot base classes. Keep it minimal

## Strategy Files
Strategy code should express **decision logic only**.
Move out:
- Long checks / validation
- Helper-heavy logic
- Orthogonal behavior

If behavior has a different goal → make it a separate strategy.

## Global State
- Do not default to global arrays or registries.
- Use globals only when ownership and lifetime are clear.

## Verbosity
- Long conditionals and defensive checks should be wrapped in helpers.
- Prefer intent-revealing names.

## AI / Vibe Code
- AI-generated or exploratory code must be cleaned before commit.
- No verbose or unclear code in main.

## When Not to Abstract
- If code is unique, linear, and clearer inline, leave it.

**Optimize for bytecode, readability and intent.**




## Movement & Performance Guidelines

### Movement API
- Use `Pathfinder.makeMove(target)` to move toward a target.
  - It internally checks move cooldown — **do not** check cooldown yourself.
- For directional movement:
  - Use `Pathfinder.makeMove(dir)` and `turn(dir)`
  - **Do not** use `rc.move`, `rc.moveForwards`, or `rc.turn`
- `Pathfinder` handles cell tracking automatically when vision range changes.

### Bytecode Efficiency
- Always prefer the custom interfaces in `Util` for better bytecode usage.
- If required functionality is missing:
  - Fall back to standard `rc` methods or Java utilities.
- Optimize for **lowest bytecode cost**

### Code Hygiene
- Ensure all required imports are present in every file.
  - No compile errors should be introduced.
- Avoid redundant checks and unnecessary allocations.

### Strategy Rules
- **Never edit `BabyRat.java`.**
- Implement new behavior by creating new strategy classes only.

