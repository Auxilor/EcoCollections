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

  # Manual collect mode: players submit items in the GUI instead of gaining count automatically
  manual-collect-mode:
    # If true, players do not gain collection count automatically, and the
    # `count-methods` option in every collection file is ignored. Players instead
    # right-click a collection in the group GUI to consume matching items from
    # their inventory and gain count.
    enabled: false
    # If true, players cannot submit more items than the collection's final tier requires.
    prevent-over-count: true

# GUI
gui:
  cache-ttl: 5000 # Milliseconds rendered lore is cached before re-rendering

  collections:
    title: "&8Collections (%page%/%max_page%)" # Supports %page% and %max_page%
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
    prev-page:
      item: arrow name:"&fPrevious Page"
      item-inactive: gray_stained_glass_pane name:"&7Previous Page" # Shown on the first page
      location:
        row: 6
        column: 4
    next-page:
      item: arrow name:"&fNext Page"
      item-inactive: gray_stained_glass_pane name:"&7Next Page" # Shown on the last page
      location:
        row: 6
        column: 6
    page-change-sound:
      enabled: true
      sound: ui.button.click
      pitch: 1.0
      volume: 1.0
    custom-slots: []

  group:
    title: "&8%group_name% (%page%/%max_page%)" # Supports %group_name%, %page% and %max_page%
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
    prev-page:
      item: arrow name:"&fPrevious Page"
      item-inactive: gray_stained_glass_pane name:"&7Previous Page"
      location:
        row: 6
        column: 4
    next-page:
      item: arrow name:"&fNext Page"
      item-inactive: gray_stained_glass_pane name:"&7Next Page"
      location:
        row: 6
        column: 6
    # Only shown when collections.manual-collect-mode.enabled is true.
    # Submits every matching item for every unlocked collection in the group at once.
    collect-all:
      material: "chest"
      name: "&aCollect All"
      location:
        row: 6
        column: 5
    page-change-sound:
      enabled: true
      sound: ui.button.click
      pitch: 1.0
      volume: 1.0
    custom-slots: []

  detail:
    title: "&8%collection_name% Collection (%page%/%max_page%)" # Supports %collection_name%, %page% and %max_page%
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
        item: arrow name:"&fPrevious Page"
        item-inactive: gray_stained_glass_pane name:"&7Previous Page"
        location:
          row: 6
          column: 4
      next-page:
        item: arrow name:"&fNext Page"
        item-inactive: gray_stained_glass_pane name:"&7Next Page"
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
      page-change-sound:
        enabled: true
        sound: ui.button.click
        pitch: 1.0
        volume: 1.0
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
  # Sent every time a player gains count, including outside manual collect mode.
  # Supports %amount% in lang.yml.
  count-up:
    enabled: false
    chat: true
    title: true
    sound:
      enabled: true
      sound: entity_experience_orb_pickup
      pitch: 1.0
      volume: 1.0
  # Sent when a manual collect attempt is refused (disabled world, creative/spectator,
  # AFK, or unmet collection `conditions`)
  manual-collect-denied:
    enabled: false
    chat: true
    title: true
    sound:
      enabled: true
      sound: entity_villager_no
      pitch: 1.0
      volume: 1.0

```

## Manual collect mode

With `collections.manual-collect-mode.enabled: false` (the default), collections count automatically from their `count-methods` triggers. Set it to `true` and that flips: triggers are ignored server-wide, and players gain count by submitting items in the group GUI.

The switch is global — it applies to every collection at once, and there is no per-collection override. What each collection accepts is set per-collection with `manual-collect.items`, covered in [How to Make a Collection](how-to-make-a-collection#manual-collect-items).

| Setting | What it does |
| --- | --- |
| `enabled` | Turns manual collect mode on. While on, `count-methods` is ignored for every collection and the dupe-filter warning is skipped |
| `prevent-over-count` | Caps submissions at the final tier's requirement, so players cannot hand in items past a maxed collection |

### How players submit items

In the group GUI, an unlocked collection responds to:

| Click | Result |
| --- | --- |
| Left-click | Opens the collection's detail GUI, same as normal |
| Right-click | Submits one matching item |
| Shift + right-click | Submits every matching item in the inventory |
| Left-click the **Collect All** button | Submits every matching item for every unlocked collection in the group |

Items are taken from the main inventory only, and the GUI reopens on the same page after a successful submission.

:::warning Submissions are refused, not queued
A player gains nothing if they are in a `disabled-world`, in creative or spectator, AFK (with `prevent-while-afk`), or fail the collection's `conditions`. Enable `messages.manual-collect-denied` so they get told why instead of seeing a click do nothing.
:::

### Related lang.yml keys

Manual collect mode adds these to `lang.yml`:

```yaml
messages:
  count-up:
    chat: "&aAdded &e%amount% &ato &e%collection_name%&a."
    title: "&e%collection_name%"
    subtitle: "&a+%amount%"
  manual-collect-denied:
    chat: "&cYou can't collect items right now."
    title: "&cCollect Denied"
    subtitle: "&7Failed to meet conditions"

lore:
  # Appended to each collection's group-GUI lore while manual collect mode is on
  manual-collect-one:
    - "&7Right Click: Collect One"
  manual-collect-all:
    - "&7Shift + Right Click: Collect All"
```

<hr/>

## Where to go next

- **Make a collection:** [How to Make a Collection](how-to-make-a-collection) covers the per-collection config.
- **Group your collections:** [How to Make a Group](how-to-make-a-group) covers the categories players browse.
- **Configure effects:** [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) covers the shared effects system.
