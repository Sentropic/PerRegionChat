# PerRegionChat
Minecraft Bukkit plugin that allows you to configure a WorldGuard region to have a separate chat from the rest of the world.

Requires WorldGuard: https://dev.bukkit.org/projects/worldguard

## Features

### Choose whether a determined region should have its chat separated from the rest of the world
Adds a custom WorldGuard region flag ('restric-chat'), to let you choose whether a determined region should have a restricted chat.

### Chat messages send from within such region will only be visible to players in the same region
That way, you get a localized game chat, instead of a global one! Check the config and permissions sections for more customization options.

### Allows the region restriction of team chat though/teammsg and /tm commands
Enabling this option will add plugin commands named like the vanilla ones. These will normally be used instead of the vanilla, but the vanilla ones are still accesible through /minecraft:teamms and /minecraft:tm. To avoid this, remove the permission node minecraft.command.teammsg from players.

### Compatible with chat formatting plugins
The plugin only changes the list of message recipients, so the format of the message won't be altered (i.e. If you use Essentials Chat, the messages will still have the format you set with that plugin).

## config.yml
```
Restrict chat by default: false
Send unrestricted chat to restricted regions: false
Override vanilla /teammsg and /tm commands: false
```

## Permissions
- perregionchat.bypass.send

Let's a player bypass chat sending restrictions. Chat send will be received by players regardless of restrictions.
- perregionchat.bypass.receive

Let's a player bypass chat receiving restrictions. The player will be able to receive chat from all regions, regardless of restrictions.
- perregionchat.command.reload

Allows usage of the command '/prc reload', which reloads the config.yml.
- perregionchat.command.teammsg

Allows usage of the commands '/teammsg' and '/tm', which overrides the vanilla team chat commands
