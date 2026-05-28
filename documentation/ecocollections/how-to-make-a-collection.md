---
title: "How to make a Collection"
sidebar_position: 1
---

Collection configs are the star of the show. This is where you define how many items are needed per-tier, how many tiers the collection has, and what rewards the player receives.

EcoCollections gives you the freedom to create collections for any item, mob, or resource, each with its own tier requirements, count methods, and tier-up effects.

## How to add collections

Each collection is its own config file, placed in the `/collections/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the Collection is the file name. This is what you would use in your group configs and effects.\
ID's must be lowercase letters, numbers, and underscores only.

## Example Collection Config

```yaml
name: "&6Acacia Collection"

group: foraging
hide-before-tier-1: false
hide-when-locked: false

description:
  - "&7Chop acacia logs to progress."
  - "&7Tier: &e%tier_numeral%&7/&e%max_tier_numeral%"
  - "&7Progress: &e%count%&7/&e%required% &8(&e%percent%%&8)"

gui:
  icon: acacia_log
  position:
    row: 2
    column: 5
  lore:
    - "%description%"
    - ""
    - "&8Click to view tiers"

tier-requirements:
  - 50
  - 100
  - 250
  - 500
  - 1000
  - 2500
  - 5000
  - 10000
  - 25000
  - 50000

count-methods:
  - trigger: mine_block
    filters:
      player_placed: false
      blocks:
        - acacia_log

tier-up-effects:
  - tier: all
    effects:
      - id: send_message
        args:
          message: "&6Acacia &e%tier_numeral% &freached!"

completion-effects:
  - id: broadcast
    args:
      message: "&6%player% &fhas maxed the &6Acacia &fcollection!"

reward-messages:
  all:
    - " &8» &f+1 Foraging Token"
  5:
    - " &8» &6Acacia Hatchet"
  10:
    - " &8» &6Acacia Master Title"

conditions: []
unlock-conditions: []
```

## Understanding the Sections

#### The Collection Configuration Section

```yaml
name: "&6Acacia Collection" # The name of the collection, to display in GUIs.

# The group this collection belongs to.
group: foraging

# If true, the collection is hidden in the GUI until the player reaches tier 1.
hide-before-tier-1: false

# If true, the collection is hidden in the GUI until it is unlocked.
hide-when-locked: false

# The description lore shown on the collection icon.
description:
  - "&7Chop acacia logs to progress."
  - "&7Tier: &e%tier_numeral%&7/&e%max_tier_numeral%"
  - "&7Progress: &e%count%&7/&e%required% &8(&e%percent%%&8)"
```

#### The GUI Section
```yaml
# GUI settings for this collection in the Group GUI.
gui:
  icon: acacia_log
  position:
    row: 2
    column: 5
  lore:
    - "%description%"
    - ""
    - "&8Click to view tiers"
```

#### The Tier Requirements Section

```yaml
tier-requirements: # A list of the required counts to reach each tier.
  - 50 # Tier 1
  - 100 # Tier 2
  - 250
  - 500
  - 1000
```

#### The Effects Section
:::danger Effects Section

The effects section is the core functionality of the collection. You can configure effects, conditions, and filters to run when the collection is completed or the player tiers up.

Check out [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) to understand how to configure this section correctly.

For more advanced users or setups, you can configure chains in this section to string together different effects under one trigger. Check out [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain) for more info.

:::
```yaml
# Effects run when a player tiers up. Use "all" to run on every tier, or a specific tier number.
tier-up-effects:
  - tier: all
    effects:
      - id: send_message
        args:
          message: "&6Acacia &e%tier_numeral% &freached!"

# Effects run once when a player reaches the final tier.
completion-effects:
  - id: broadcast
    args:
      message: "&6%player% &fhas maxed the &6Acacia &fcollection!"
```

#### The Reward Messages Section

```yaml
# Reward messages shown in the tier slot lore via %rewards%.
# "all" applies to every tier, numbered keys apply only to that specific tier.
reward-messages:
  all:
    - " &8» &f+1 Foraging Token"
  5:
    - " &8» &6Acacia Hatchet"
  10:
    - " &8» &6Acacia Master Title"
```

#### The Conditions Section

```yaml
# Conditions that must be met to progress the collection.
# If unmet, players won't gain count but can still see the collection in the GUI.
conditions: []

# Conditions that must be met to unlock the collection.
unlock-conditions: []
```

## Internal Placeholders

| Placeholder          | Value                                               |
| -------------------- | --------------------------------------------------- |
| `%tier%`             | The player's current collection tier                |
| `%tier_numeral%`     | The player's current collection tier as Roman numerals |
| `%max_tier%`         | The max tier of the collection                      |
| `%max_tier_numeral%` | The max tier as Roman numerals                      |

<hr/>

## Default Configs
The default configs can be found [here](https://github.com/Auxilor/EcoCollections/tree/master/eco-core/core-plugin/src/main/resources/collections).