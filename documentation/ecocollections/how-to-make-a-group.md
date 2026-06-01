---
title: "How to Make a Group"
sidebar_position: 2
---

A **group** is a category that holds collections together, like Mining or Combat. Players pick a group from the main Collections menu, then browse the collections inside it. A group is a small config: a **display name**, an optional **permission**, and a **GUI** icon. This page covers building one.

## Quick start

1. Open `/plugins/EcoCollections/groups/`.
2. Copy `_example.yml` to a new file named after the group's ID, e.g. `combat.yml`.
3. Set `name`, pick a `gui.icon` and `position`, and write its `lore`.
4. Point your collections at this group by setting their `group` field to this file's ID.
5. Save, run `/ecocollections reload`, then open `/collections` and confirm the group appears.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real group. You can also organise groups into subfolders inside `groups/`, and they'll still load.
:::

## Naming and IDs

The file name (without `.yml`) is the group's ID. This is the ID a collection puts in its `group` field. Item IDs used for `gui.icon` come from the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system/items).

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the group will not load.
:::

## The structure of a group

A group config has two parts:

| Part | What it controls |
| --- | --- |
| **Display** | The name shown in the menu and an optional access permission |
| **GUI** | The icon, slot, and lore in the Collections menu |

Here is a complete group:

```yaml
# === Display: name and access ===
name: "&cCombat" # Shown in the Collections menu
permission: "" # Permission to see and open the group; blank for no restriction

# === GUI: how it appears in the Collections menu ===
gui:
  icon: diamond_sword # Item ID from the Item Lookup System
  position: # Slot in the Collections menu
    row: 2
    column: 6
  lore:
    - "&7Slay monsters and hostile mobs."
    - ""
    - "&8Click to view collections"
```

### Display

The display fields set the group's name and who can access it.

```yaml
name: "&cCombat" # Shown in the Collections menu
permission: "" # Leave blank for no restriction, or set a node like "ecocollections.group.combat"
```

### GUI

The GUI block places the group in the main Collections menu and styles its icon.

```yaml
gui:
  icon: diamond_sword # Item ID from the Item Lookup System
  position:
    row: 2
    column: 6
  lore:
    - "&7Slay monsters and hostile mobs."
    - ""
    - "&8Click to view collections"
```

:::tip Troubleshooting
- **Group not showing up?** Check the file is in `groups/`, the ID has no capitals or hyphens, and you ran `/ecocollections reload`.
- **Group is empty?** A group only shows collections whose `group` field matches its ID. Confirm at least one collection points at it.
- **Players can't see the group?** Clear the `permission` field, or grant players the node you set.
:::

<hr/>

## Where to go next

- **Fill the group:** [How to Make a Collection](how-to-make-a-collection) builds the collections that live inside it.
- **Style the menus:** [Plugin Config](plugin-config) is the full annotated `config.yml` for the GUIs.
- **Default examples:** the shipped group configs live [here](https://github.com/Auxilor/EcoCollections/tree/master/eco-core/core-plugin/src/main/resources/groups).