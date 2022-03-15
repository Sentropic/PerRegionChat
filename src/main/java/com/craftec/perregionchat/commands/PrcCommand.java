package com.craftec.perregionchat.commands;

import com.craftec.perregionchat.PerRegionChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PrcCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {
            PerRegionChat.getPlugin().reload();
            sender.sendMessage(ChatColor.GREEN+"PerRegionChat config reloaded.");
            sender.sendMessage(ChatColor.YELLOW+"Properly disabling /teammsg /tm requires to restart, so a server restart is recommended if you changed this config option.");
            return true;
        } else {
            return false;
        }
    }
}
