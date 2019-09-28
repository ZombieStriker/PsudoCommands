package me.zombie_striker.psudocommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("psudoAs")) {
			List<String> names = new ArrayList<String>();
			names.add("Console");
			for (Player player : Bukkit.getOnlinePlayers())
				names.add(player.getName());
			return names;
		}
		return super.onTabComplete(sender, command, alias, args);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label2, String[] args) {
		boolean psudoAs = command.getName().equalsIgnoreCase("psudoas");
		if ((!psudoAs && sender.hasPermission("psudocommand.psudo"))
				|| (psudoAs && sender.hasPermission("psudocommand.psudoas"))) {
			CommandSender[] senders = new CommandSender[1];
			if (args.length <= (psudoAs ? 1 : 0)) {
				sender.sendMessage(ChatColor.GRAY + "[PsudoCommands] Please provide a valid "
						+ ((psudoAs && args.length == 0) ? "player name" : "command") + ".");
				return false;
			}
			if (psudoAs) {
				if (args[0].equalsIgnoreCase("Console"))
					senders[0] = Bukkit.getConsoleSender();
				else if (args[0].contains("@")) {
					senders = CommandUtils.getTargets(sender, args[0]);
				} else {
					@SuppressWarnings("deprecation") /* Not storing player */
							Player tmp = Bukkit.getPlayer(args[0]);
					senders[0] = tmp;
				}
			} else {
				senders[0] = sender;
			}
			if (senders[0] == null) {
				sender.sendMessage("The sender is null. Choose a valid player or \"Console\"");
				return false;
			}
			for (CommandSender issue : senders) {
				int coordCounter = 0;
				List<StringBuilder> cmds = new ArrayList<StringBuilder>();
				cmds.add(new StringBuilder());
				for (int i = (psudoAs ? 1 : 0); i < args.length; i++) {
					List<StringBuilder> temps = new ArrayList<>();
					for (StringBuilder cmd : cmds) {
						if (args[i].startsWith("~") || args[i].startsWith("^")) {
							if (!args[i + 1].startsWith("~") && (coordCounter == 0 || coordCounter >= 3)) {
								cmd.append(issue.getName());
							} else {
								if (coordCounter == 0)
									cmd.append(CommandUtils.getIntRelative(args[i], "x", (Entity) issue));
								if (coordCounter == 1)
									cmd.append(CommandUtils.getIntRelative(args[i], "y", (Entity) issue));
								if (coordCounter == 2)
									cmd.append(CommandUtils.getIntRelative(args[i], "z", (Entity) issue));
								coordCounter++;
							}
						} else if (args[i].startsWith("@")) {
							Entity[] e = CommandUtils.getTargets(issue, args[i]);
							if (e == null)
								continue;
							boolean works = true;
							for (int j = 1; j < e.length; j++) {
								StringBuilder sb = new StringBuilder(cmd.toString());
								if (e[j] == null) {
									works = false;
									break;
								}
								sb.append((e[j].getCustomName() != null ? e[j].getCustomName() : e[j].getName()));
								if (i + 1 < args.length) {
									sb.append(" ");
								}
								temps.add(sb);
							}
							if (!works)
								return false;
							if (e.length == 0 || e[0] == null) {
								return false;
							} else {
								cmd.append(e[0].getCustomName() != null ? e[0].getCustomName() : e[0].getName());
							}
						} else {
							cmd.append(args[i]);
						}
						if (i + 1 < args.length) {
							cmd.append(" ");
						}
					}
					if (temps.size() > 0) {
						cmds.addAll(temps);
						temps.clear();
					}
				}
				boolean atleastOne = false;
				for (StringBuilder cmd : cmds) {
					if (Bukkit.dispatchCommand(issue, cmd.toString())) atleastOne = true;
				}
				return atleastOne;
			}
		}
		return false;
	}
}
