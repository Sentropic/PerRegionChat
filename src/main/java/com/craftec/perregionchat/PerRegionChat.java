package com.craftec.perregionchat;

import com.craftec.perregionchat.commands.PrcCommand;
import com.craftec.perregionchat.commands.TeamMsgCommand;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PerRegionChat extends JavaPlugin implements Listener {
    private static PerRegionChat singleton;

    public static PerRegionChat getPlugin() { return singleton; }

    @Override
    public void onEnable() {
        singleton = this;
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("prc").setExecutor(new PrcCommand());
        if (getConfig().getBoolean("Override vanilla /teammsg and /tm commands")) {
            try {
                Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

                commandMap.register("perregionchat", new TeamMsgCommand());
                commandMap.register("tm", "perregionchat", new TeamMsgCommand());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        singleton = null;
        HandlerList.unregisterAll((Listener) this);

        //Doesn't work
        /*try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
            Field mapField = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
            mapField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) mapField.get(commandMap);
            knownCommands.remove("teammsg");
            knownCommands.remove("tm");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
    }

    public static BooleanFlag restrict_chat;
    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        BooleanFlag flag = new BooleanFlag("restrict-chat");
        registry.register(flag);
        restrict_chat = flag;
    }

    public void reload() {
        reloadConfig();
        onDisable();
        onEnable();
    }

    public static boolean isChatRestricted (Player player){
        ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        Boolean restricted = getPlugin().getConfig().getBoolean("Restrict chat by default");
        if(!set.queryAllValues(localPlayer,PerRegionChat.restrict_chat).isEmpty()){
            restricted = set.queryValue(localPlayer,PerRegionChat.restrict_chat);
        }
        return restricted;
    }
    public static ProtectedRegion getChatRegion(Player player){
        List<ProtectedRegion> restricted_regions = new ArrayList<>();
        boolean defaultRestrict = getPlugin().getConfig().getBoolean("Restrict chat by default");
        for (ProtectedRegion region : WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(player.getLocation()))){
            try {
                if (region.getFlag(PerRegionChat.restrict_chat)) {
                    restricted_regions.add(region);
                }
            } catch (NullPointerException e) {
                System.out.println("prc null");
                if (defaultRestrict) {
                    restricted_regions.add(region);
                }
            }
        }
        int priority = restricted_regions.get(0).getPriority();
        for (ProtectedRegion region : restricted_regions){
            if(region.getPriority()>priority){
                priority=region.getPriority();
            }
        }
        ProtectedRegion main_region =  null;
        for (ProtectedRegion region : restricted_regions){
            if(region.getPriority()==priority){
                main_region=region;
                break;
            }
        }
        return main_region;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("perregionchat.bypass.send")) {
            if(isChatRestricted(player)) {
                Set<Player> recipients = event.getRecipients();
                Set<Player> recipients_aux = recipients.stream().collect(Collectors.toSet());
                recipients.clear();
                ProtectedRegion main_region = getChatRegion(player);
                for(Player recipient : recipients_aux){
                    Location location=recipient.getLocation();
                    if(recipient.hasPermission("perregionchat.bypass.receive") || main_region.contains(location.getBlockX(),location.getBlockY(),location.getBlockZ())){
                        recipients.add(recipient);
                    }
                }
            } else if(!getConfig().getBoolean("Send unrestricted chat to restricted regions")) {
                Set<Player> recipients = event.getRecipients();
                Set<Player> recipients_aux = recipients.stream().collect(Collectors.toSet());
                recipients.clear();
                for(Player recipient : recipients_aux){
                    if(!isChatRestricted(recipient)){
                        recipients.add(recipient);
                    }
                }
            }
        }
    }
}