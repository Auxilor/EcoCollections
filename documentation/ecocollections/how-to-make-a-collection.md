---
title: "How to Make a Collection"
sidebar_position: 1
---

A **collection** rewards a player for repeatedly doing one thing, like mining acacia logs or killing zombies. It counts a libreforge **trigger**, advances the player through **tiers** as their count climbs, and runs **effects** at each tier. This page takes you from an empty file to a working collection.

## Quick start

1. Open `/plugins/EcoCollections/collections/`.
2. Copy `_example.yml` to a new file named after the collection's ID, e.g. `acacia.yml`.
3. Set `name`, point `group` at an existing group, and pick the `gui.icon` and `position`.
4. Set your `tier-requirements` and a `count-methods` trigger that matches the action you want to track.
5. Save, run `/ecocollections reload`, then open `/collections`, find your collection, and perform the action to watch the count rise.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real collection. You can also organise collections into subfolders inside `collections/`, and they'll still load.
:::

## Naming and IDs

The file name (without `.yml`) is the collection's ID. This is the ID you reference in group configs, effects, and placeholders. Item IDs used for `gui.icon` come from the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system/items).

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the collection will not load.
:::

## The structure of a collection

A collection config has seven parts:

| Part | What it controls |
| --- | --- |
| **Display** | The name, group, lore, and when the collection is hidden |
| **GUI** | The icon, slot, and lore shown in the group menu |
| **Tiers** | How many items each tier needs, as a list or a formula |
| **Count methods** | Which trigger and filters add to the count |
| **Manual collect** | Which items players can hand in, when manual collect mode is on |
| **Effects** | What runs on tier-up and on completion |
| **Reward messages and conditions** | The reward lore, and gates on progress or unlocking |

Here is a complete collection with every part in place:

```yaml
# === Display: name, group, and visibility ===
name: "&6Acacia Collection" # Shown in GUIs
group: foraging # ID of the group this collection belongs to
hide-before-tier-1: false # Hide in the GUI until the player reaches tier 1
hide-when-locked: false # Hide in the GUI until the player unlocks it

description: # Reusable lore, inserted elsewhere via %description%
  - "&7Chop acacia logs to progress."
  - "&7Tier: &e%tier_numeral%&7/&e%max_tier_numeral%"
  - "&7Progress: &e%count%&7/&e%required% &8(&e%percent%%&8)"

# === GUI: how it appears in the group menu ===
gui:
  icon: acacia_log # Item ID from the Item Lookup System
  position: # Slot in the group GUI
    row: 2
    column: 5
  lore:
    - "%description%"
    - ""
    - "&8Click to view tiers"

# === Tiers: the count needed for each tier ===
tier-requirements:
  - 50 # Tier 1
  - 100 # Tier 2
  - 250
  - 500
  - 1000
  - 2500
  - 5000
  - 10000
  - 25000
  - 50000

# === Count methods: what adds to the count ===
count-methods:
  - trigger: mine_block
    filters:
      player_placed: false # Ignore blocks the player placed, to stop dupe farming
      blocks:
        - acacia_log

# === Manual collect: items players can hand in ===
# Only used when collections.manual-collect-mode.enabled is true in config.yml
manual-collect:
  items:
    - acacia_log

# === Effects: run on tier-up and completion ===
tier-up-effects:
  - tier: all # "all" runs on every tier, or use a specific tier number
    effects:
      - id: send_message
        args:
          message: "&6Acacia &e%tier_numeral% &freached!"

completion-effects: # Run once, when the final tier is reached
  - id: broadcast
    args:
      message: "&6%player% &fhas maxed the &6Acacia &fcollection!"

# === Reward messages and conditions ===
reward-messages: # Shown in tier-slot lore via %rewards%
  all:
    - " &8» &f+1 Foraging Token"
  5:
    - " &8» &6Acacia Hatchet"
  10:
    - " &8» &6Acacia Master Title"

conditions: [] # Must be met to gain count
unlock-conditions: [] # Must be met to unlock the collection
```

### Display

The display fields set the name shown in GUIs, the group the collection sits under, and when it is visible.

```yaml
name: "&6Acacia Collection" # Shown in GUIs
group: foraging # Must match an existing group ID
hide-before-tier-1: false # Hide until the player reaches tier 1
hide-when-locked: false # Hide until the player unlocks it

description: # Reusable lore block, pulled in elsewhere with %description%
  - "&7Chop acacia logs to progress."
  - "&7Tier: &e%tier_numeral%&7/&e%max_tier_numeral%"
  - "&7Progress: &e%count%&7/&e%required% &8(&e%percent%%&8)"
```

### GUI

The GUI block places the collection in its group menu and styles its icon.

```yaml
gui:
  icon: acacia_log # Item ID from the Item Lookup System
  position:
    row: 2
    column: 5
  lore:
    - "%description%" # Inserts the description block above
    - ""
    - "&8Click to view tiers"
```

### Tiers

Tiers define how many counted items each level needs. Use an explicit list, or a formula for infinite tiers; do not use both.

```yaml
# Option 1: an explicit list, one entry per tier
tier-requirements:
  - 50 # Tier 1
  - 100 # Tier 2
  - 250

# Option 2: a formula for infinite tiers
# tier-formula: (2 ^ %level%) * 25
# max-tier: 100 # Optional; leave out for no cap
```

:::info Formula values are expressions
`tier-formula` is a math expression evaluated with `%level%` set to the tier being calculated.
:::

### Count methods

Count methods decide what adds to the player's count. Each takes a libreforge **trigger** and optional **filters** to narrow what qualifies.

```yaml
count-methods:
  - trigger: mine_block # Any libreforge trigger
    filters:
      player_placed: false # Ignore player-placed blocks, to stop dupe farming
      blocks:
        - acacia_log
```

:::warning Always filter block triggers
For `mine_block` and `break_block`, set `player_placed: false`, or players can place and break the same block to farm count. The plugin warns on load when this filter is missing but does not enforce it.
:::

:::info Ignored in manual collect mode
If `collections.manual-collect-mode.enabled` is `true` in [config.yml](plugin-config#manual-collect-mode), `count-methods` is ignored for every collection: no triggers are bound, and the dupe-filter warning is skipped. Count then comes only from items players hand in, set with `manual-collect.items` below.
:::

### Manual collect items

`manual-collect.items` lists the items this collection accepts when the server runs in [manual collect mode](plugin-config#manual-collect-mode). Players right-click the collection in its group menu to hand in one item, or shift + right-click to hand in every matching item in their inventory. Each submitted item is worth 1 count.

```yaml
manual-collect:
  items:
    - acacia_log # Item IDs from the Item Lookup System
    - acacia_wood
```

Leaving this out, or leaving it empty, means the collection accepts nothing and can never be progressed while manual collect mode is on.

:::warning Items must match exactly
An item in the player's inventory only counts if it matches the configured item exactly, meta included. A renamed, enchanted, or otherwise modified acacia log will **not** match plain `acacia_log`. To accept a custom item, list its own ID (ItemsAdder, Oraxen, Nexo, and CustomModelData items all work through the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system/items)).
:::

### Effects

Effects run when the player tiers up (`tier-up-effects`) and once when they max the collection (`completion-effects`).

```yaml
tier-up-effects:
  - tier: all # "all" for every tier, or a specific tier number
    effects:
      - id: send_message
        args:
          message: "&6Acacia &e%tier_numeral% &freached!"

completion-effects:
  - id: broadcast
    args:
      message: "&6%player% &fhas maxed the &6Acacia &fcollection!"
```

:::danger Effects are their own system
Effects, conditions, and filters are a shared libreforge system documented separately. To configure them:

- [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect)
- [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain)
:::

### Reward messages and conditions

`reward-messages` is display-only lore shown in tier slots via `%rewards%`; the actual rewards come from your effects. `conditions` gate gaining count, and `unlock-conditions` gate unlocking the collection at all.

```yaml
reward-messages:
  all: # Shown on every tier
    - " &8» &f+1 Foraging Token"
  5: # Shown only on tier 5
    - " &8» &6Acacia Hatchet"

conditions: [] # Unmet: the player keeps the collection visible but gains no count
unlock-conditions: [] # Unmet: the collection is locked
```

Both gates apply in manual collect mode too: unmet `conditions` refuse the submission and consume nothing, and a locked collection cannot be handed items at all.

## Internal placeholders

These placeholders work inside this config's lore and messages.

| Placeholder | Value |
| --- | --- |
| `%tier%` | The player's current tier |
| `%tier_numeral%` | The current tier as Roman numerals |
| `%max_tier%` | The collection's max tier |
| `%max_tier_numeral%` | The max tier as Roman numerals |
| `%count%` | The player's current count |
| `%required%` | The count needed for the next tier |
| `%percent%` | Progress toward the next tier, as a percentage |
| `%description%` | The collection's `description` block |
| `%rewards%` | The `reward-messages` for the tier being rendered |
| `%player%` | The player's name |

:::tip Troubleshooting
- **Collection not showing up?** Check the file is in `collections/`, the ID has no capitals or hyphens, and `group` matches an existing group ID.
- **Count not rising?** Make sure your `count-methods` trigger matches the action, and that no `conditions` are blocking progress. AFK and creative players are ignored by default (see [Plugin Config](plugin-config)).
- **Players farming count?** Add `player_placed: false` to block triggers so placed-then-broken blocks don't count.
- **Triggers doing nothing?** Check whether `collections.manual-collect-mode.enabled` is on in `config.yml`; it disables `count-methods` server-wide.
- **Right-click not collecting?** The collection needs `manual-collect.items`, the items must match exactly (no rename or enchants), and the player must be unlocked, out of creative, and passing `conditions`. Turn on `messages.manual-collect-denied` to see the refusal.
- **Collect stops early?** `collections.manual-collect-mode.prevent-over-count` caps submissions at the final tier's requirement.
- **Changes not applying?** Run `/ecocollections reload` after editing the file.
:::

<hr/>

## Where to go next

- **Group your collections:** [How to Make a Group](how-to-make-a-group) builds the categories players browse.
- **Configure effects:** [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) covers the shared effects system.
- **Default examples:** the shipped collection configs live [here](https://github.com/Auxilor/EcoCollections/tree/master/eco-core/core-plugin/src/main/resources/collections).