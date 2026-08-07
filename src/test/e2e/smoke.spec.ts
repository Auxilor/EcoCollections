import { expect, test } from '@drownek/plugwright';

// Wide blast radius on purpose: passing proves the server booted, eco loaded,
// EcoCollections loaded, the bundled groups and collections registered, and the
// root GUI rendered its config-driven icon and lore.
test('collections GUI opens with the Mining group', async ({ player }) => {
  player.chat('/collections');

  const gui = await player.gui({ title: /Collections/ });
  const mining = gui.locator(item => item.name === 'diamond_pickaxe');

  await expect(mining).toHaveLore('Mine ores');
});
