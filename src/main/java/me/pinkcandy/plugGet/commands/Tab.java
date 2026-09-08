package me.pinkcandy.plugGet.commands;

import me.pinkcandy.plugGet.ConfigManager;
import me.pinkcandy.plugGet.db.DBManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!(sender instanceof Player)) {
            return suggestions;
        }

        if (ConfigManager.tabMode == ConfigManager.TabMode.APT) {
            if (args.length >= 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("-R") || args[0].equalsIgnoreCase("autoremove") || args[0].equalsIgnoreCase("include") || args[0].equalsIgnoreCase("-I") || args[0].equalsIgnoreCase("exclude") || args[0].equalsIgnoreCase("-E"))) {
                List<String> sugs = DBManager.getAllInstalledSlugs();
                StringUtil.copyPartialMatches(args[args.length - 1], sugs, suggestions);
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("-Qs"))) {
                List<String> sugs = Arrays.asList("all", "excluded");
                StringUtil.copyPartialMatches(args[args.length - 1], sugs, suggestions);
            } else if (args.length == 1) {
                List<String> sugs = Arrays.asList("search", "install", "update", "remove","autoremove", "reload", "help", "y", "n", "list", "versions", "release", "include", "exclude");
                StringUtil.copyPartialMatches(args[0], sugs, suggestions);
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("install") || args[0].equalsIgnoreCase("-S")) {

                } else if (args[0].equalsIgnoreCase("search") || args[0].equalsIgnoreCase("-Ss")) {

                } else if (args[0].equalsIgnoreCase("update") || args[0].equalsIgnoreCase("-Syu")) {
                    List<String> sugs = DBManager.getAllInstalledSlugs();
                    StringUtil.copyPartialMatches(args[1], sugs, suggestions);
                } else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("-Qs")) {

                } else if (args[0].equalsIgnoreCase("versions") || args[0].equalsIgnoreCase("-Vs")) {

                }
            }

        } else if (ConfigManager.tabMode == ConfigManager.TabMode.PACMAN) {
            if (args.length >= 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("-R") || args[0].equalsIgnoreCase("-Rs") || args[0].equalsIgnoreCase("include") || args[0].equalsIgnoreCase("-I") || args[0].equalsIgnoreCase("exclude") || args[0].equalsIgnoreCase("-E"))) {
                List<String> sugs = DBManager.getAllInstalledSlugs();
                StringUtil.copyPartialMatches(args[args.length - 1], sugs, suggestions);
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("-Qs"))) {
                List<String> sugs = Arrays.asList("all", "excluded");
                StringUtil.copyPartialMatches(args[args.length - 1], sugs, suggestions);
            } else if (args.length == 1) {
                List<String> sugs = Arrays.asList("-Ss", "-S", "-Vs", "-Syu", "-R", "-Rs", "-h", "-y", "-n", "-Alr", "include", "exclude");
                StringUtil.copyPartialMatches(args[0], sugs, suggestions);
            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("install") || args[0].equalsIgnoreCase("-S")) {

                } else if (args[0].equalsIgnoreCase("search") || args[0].equalsIgnoreCase("-Ss")) {

                } else if (args[0].equalsIgnoreCase("versions") || args[0].equalsIgnoreCase("-Vs")) {

                } else if (args[0].equalsIgnoreCase("update") || args[0].equalsIgnoreCase("-Syu")) {
                    List<String> sugs = DBManager.getAllInstalledSlugs();
                    StringUtil.copyPartialMatches(args[1], sugs, suggestions);
                }
            }
        }
        return suggestions;
    }
}
