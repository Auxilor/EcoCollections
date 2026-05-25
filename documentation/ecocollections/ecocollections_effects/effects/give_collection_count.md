# `give_collection_count`
:::infoRequires:
EcoCollections
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Gives a player count toward a collection

## Example Config

```yaml
- id: give_collection_count
  args:
    amount: 10 # The amount of count to give
    collection: diamond # The collection ID
  ...other config (eg triggers, filters, mutators, etc)
```