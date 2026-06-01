---
title: "Plugin Config"
sidebar_position: 8
---

`config.yml` holds the server-wide settings: where collections are disabled, how the GUIs are laid out, the leaderboard behaviour, and the tier-up, completion, and unlock messages. It lives at `/plugins/EcoCollections/config.yml`. Edit it, then run `/ecocollections reload` to apply your changes.

## Default config.yml

```yaml
collections:
  # Worlds where collections are disabled (count is not gained, GUI still works)
  disabled-worlds: []

  # If true, count is not gained while AFK (requires eco AFK detection)
  prevent-while-afk: true

  # If true, creative/spectator mode players do not gain count
  prevent-while-creative: true

  # On plugin load, scan every count-method and log a warning if mine_block or
  # break_block is used without an explicit `player_placed` filter. The plugin
  # itself does not enforce filters; this only emits warnings.
  warn-on-missing-dupe-filter: true

# GUI
gui:
  cache-ttl: 5000 # Milliseconds rendered lore is cached before re-rendering

  collections:
    title: "&8Collections"
    rows: 6
    mask:
      materials:
        - black_stained_glass_pane
      pattern:
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
    close:
      material: "barrier"
      name: "&cClose"
      location:
        row: 6
        column: 5
    custom-slots: []

  group:
    title: "&8%group_name%"
    rows: 6
    mask:
      materials:
        - black_stained_glass_pane
      pattern:
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
    back:
      material: "arrow"
      name: "&fBack"
      location:
        row: 6
        column: 1
    custom-slots: []

  detail:
    title: "&8%collection_name% Collection"
    rows: 6
    mask:
      materials:
        - black_stained_glass_pane
      pattern:
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
        - "111111111"
    info-icon:
      location:
        row: 1
        column: 5
    # Progression slots define where tier indicators appear and how they look.
    # Pattern: 0 = empty, 1-9 then a-z define the order tiers are placed (left-to-right, top-to-bottom).
    progression-slots:
      pattern:
        - "000000000"
        - "012345670"
        - "089abcde0"
        - "0fghijkl0"
        - "0mnopqrs0"
        - "000000000"
      # Max tier reached
      completed:
        item: gold_block
        name: "&6Tier %tier_numeral% &8- &6&lMAXED"
        lore:
          - "&7Requires: &e%required% &7items"
          - "&6&lCollection Complete!"
          - ""
          - "&7Rewards:"
          - "%rewards%"
      # Tier reached (but not max)
      reached:
        item: lime_stained_glass_pane
        name: "&aTier %tier_numeral%"
        lore:
          - "&7Requires: &e%required% &7items"
          - "&a&lCompleted"
          - ""
          - "&7Rewards:"
          - "%rewards%"
      # The next tier the player is working towards
      in-progress:
        item: yellow_stained_glass_pane
        name: "&eTier %tier_numeral%"
        lore:
          - "&7Requires: &e%required% &7items"
          - "&7Progress: &e%count%&7/&e%required% &8(&e%percent%%&8)"
          - ""
          - "&7Rewards:"
          - "%rewards%"
      # Tiers not yet reachable
      locked:
        item: red_stained_glass_pane
        name: "&7Tier %tier_numeral%"
        lore:
          - "&7Requires: &e%required% &7items"
          - ""
          - "&7Rewards:"
          - "%rewards%"
    buttons:
      prev-page:
        material: "arrow"
        name: "&fPrevious Page"
        location:
          row: 6
          column: 4
      next-page:
        material: "arrow"
        name: "&fNext Page"
        location:
          row: 6
          column: 6
      back:
        material: "barrier"
        name: "&fCollections"
        location:
          row: 6
          column: 1
      rank:
        enabled: true
        material: "player_head"
        name: "&7Your Ranking"
        location:
          row: 6
          column: 9
    custom-slots: []

  # Locked-collection rendering (used when a collection has unmet unlock-conditions
  # AND the per-collection `hide-when-locked` is false)
  locked:
    # Globally show locked collections in the GUI. If false, locked collections
    # are hidden regardless of the per-collection hide-when-locked setting.
    show-locked-collections: true
    # The icon used in place of the collection's normal icon when locked.
    icon:
      material: "iron_bars" # Any item ID eco's parser accepts
      name: "&7&l? ? ?"
      lore:
        - "&8Locked"
        - ""
        - "&7Unlock requirements:"
        - "%unlock_requirements%"   # rendered from collection.unlockConditions.descriptions
        - ""
        - "&cYou have not unlocked this collection."
    # Sound played when a player clicks a locked collection
    click-sound:
      enabled: true
      sound: block.note_block.bass
      pitch: 1.0
      volume: 1.0

# Leaderboard
leaderboard:
  enabled: true
  cache-lifetime: 300 # Seconds before the leaderboard is recomputed

leaderboards:
  refresh-interval: 6h
  exact-rank-cutoff: 1000 # Set to 0 to always return raw integer ranks (disables Top X% bucketing)
  percent-decimal-places: 1
  show-in-group-gui: true

# Messages on tier-up / completion / unlock (in addition to whatever the rewards do)
messages:
  tier-up:
    enabled: true
    chat: true
    title: true
    sound:
      enabled: true
      sound: entity_player_levelup
      pitch: 1.0
      volume: 1.0
  complete:
    enabled: true
    chat: true
    title: true
    sound:
      enabled: true
      sound: ui_toast_challenge_complete
      pitch: 1.0
      volume: 1.0
    broadcast: true
  unlock:
    enabled: true
    chat: true
    title: true
    sound:
      enabled: true
      sound: entity_experience_orb_pickup
      pitch: 1.0
      volume: 1.0

```

<hr/>

## Where to go next

- **Make a collection:** [How to Make a Collection](how-to-make-a-collection) covers the per-collection config.
- **Group your collections:** [How to Make a Group](how-to-make-a-group) covers the categories players browse.
- **Configure effects:** [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) covers the shared effects system.
