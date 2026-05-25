---
title: "How to make a Group"
sidebar_position: 2
---

Groups organise collections into categories, making them easy to browse in the GUI. Each group acts as a top-level container that players select before viewing its individual collections.

## How to add groups

Each group is its own config file, placed in the `/groups/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the Group is the file name. This is what you use in your collection configs.\
ID's must be lowercase letters, numbers, and underscores only.

## Example Group Config

```yaml
name: "&cCombat"
permission: ""

gui:
  icon: diamond_sword
  position:
    row: 2
    column: 6
  lore:
    - "&7Slay monsters and hostile mobs."
    - ""
    - "&8Click to view collections"
```

## Understanding the Sections

#### The Display Options

```yaml
# The display name of the group, shown in the Collections GUI.
name: "&cCombat"
# Optional permission required to see and access this group.
# Leave blank for no restriction.
permission: ""
```

#### The GUI Section

```yaml
# GUI display settings for this group in the Collections GUI.
gui:
  icon: diamond_sword
  position:
    row: 2
    column: 6
  lore:
    - "&7Slay monsters and hostile mobs."
    - ""
    - "&8Click to view collections"
```

<hr/>

## Default Configs
The default configs can be found [here](https://github.com/Auxilor/EcoCollections/tree/master/eco-core/core-plugin/src/main/resources/groups).