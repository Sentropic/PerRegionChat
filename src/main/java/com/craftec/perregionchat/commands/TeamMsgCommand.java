package com.craftec.perregionchat.commands;

import com.craftec.perregionchat.PerRegionChat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TeamMsgCommand extends Command {
    private Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    public TeamMsgCommand() {
        super("teammsg",
                "Overrides the vanilla commands /teammsg and /tm, allowing region restriction. Enabling this command in the config.yml will leave the vanilla commands still accesible through /minecraft:tammsg and /minecraft:tm, so you might want to remove the permission minecraft.command.teammsg from players.",
                "/<command> <message>",
                new ArrayList<>(Collections.singleton("tm")));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Color.RED+"An entity is required to run this command here");
            return false;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("perregionchat.command.teammsg")) {
            player.sendMessage(Color.RED + "No permission to run this command");
            return false;
        }

        if (args.length < 1) { return false; }
        StringBuilder messageBuilder = new StringBuilder(args[0]);
        for (String arg : Arrays.copyOfRange(args, 1, args.length)) { messageBuilder.append(" ").append(arg); }


        Team team = scoreboard.getEntryTeam(player.getName());
        String message = "-> [" + team.getDisplayName() + "] <" + player.getDisplayName() + "> " + messageBuilder.toString();
        Set<Player> recipients = new HashSet<>();

        if (player.hasPermission("perregionchat.bypass.send")) {
            recipients.addAll(Bukkit.getOnlinePlayers());
        } else if (PerRegionChat.isChatRestricted(player)) {
            ProtectedRegion mainRegion = PerRegionChat.getChatRegion(player);
            for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
                Location location = recipient.getLocation();
                if (
                        team.hasEntry(recipient.getName())
                                && (recipient.hasPermission("perregionchat.bypass.receive") || mainRegion.contains(location.getBlockX(),location.getBlockY(),location.getBlockZ()))
                ) { recipients.add(recipient); }
            }
        } else if(!PerRegionChat.getPlugin().getConfig().getBoolean("Send unrestricted chat to restricted regions")) {
            for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
                if (
                        team.hasEntry(recipient.getName())
                                && !PerRegionChat.isChatRestricted(recipient)
                ) { recipients.add(recipient); }
            }
        }
        recipients.add(player);
        recipients.forEach(recipient -> recipient.sendMessage(message));
        return true;
    }
}
