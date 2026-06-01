---
title: "Commands and Permissions"
sidebar_position: 5
---

Every EcoCollections command and the permission node it needs. Players open the GUI with `/collections`; the rest are admin commands under `/ecocollections`.

| Command                                                           | Description                             | Permission                           |
|-------------------------------------------------------------------|-----------------------------------------|--------------------------------------|
| `/collections`                                                    | Open the Collections GUI                | `ecocollections.command.collections` |
| `/ecocollections reload`                                          | Reloads the plugin                      | `ecocollections.command.reload`      |
| `/ecocollections give <player> <collection> <amount>`             | Give a player collection count          | `ecocollections.command.give`        |
| `/ecocollections set <count\|tier> <player> <collection> <value>` | Set a player's collection count or tier | `ecocollections.command.set`         |
| `/ecocollections reset <player> <collection\|all>`                | Reset a player's collection progress    | `ecocollections.command.reset`       |
| `/ecocollections unlock <player> <collection>`                    | Force-unlock a collection for a player  | `ecocollections.command.unlock`      |
| `/ecocollections leaderboard refresh [collection]`                | Refresh the collection leaderboard(s)   | `ecocollections.command.leaderboard` |

<hr/>

## Where to go next

- **Make a collection:** [How to Make a Collection](how-to-make-a-collection) is the place to start.
- **Placeholders:** [PlaceholderAPI](placeholderapi) lists every exposed placeholder.