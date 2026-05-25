# `unlock_collection`
:::infoRequires:
EcoCollections
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Unlocks a collection for the player

## Example Config

```yaml
- id: unlock_collection
  args:
    collection: diamond # The collection ID
  ...other config (eg triggers, filters, mutators, etc)
```