package me.zombie_striker.psudocommands;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

public class CommandUtils {

	/**
	 * Use this if you are unsure if a player provided the "@a" tag. This will allow
	 * multiple entities to be retrieved.
	 * 
	 * This can return a null variable if no tags are included, or if a value for a
	 * tag does not exist (I.e if the tag [type=___] contains an entity that does
	 * not exist in the specified world)
	 * 
	 * The may also be empty or null values at the end of the array. Once a null
	 * value has been reached, you do not need to loop through any of the higher
	 * indexes
	 * 
	 * Currently supports the tags:
	 * 
	 * @p , @a , @e , @r
	 * 
	 *    Currently supports the selectors: [type=] [r=] [rm=] [c=] [w=] [m=]
	 *    [name=] [l=] [lm=] [h=] [hm=] [rx=] [rxm=] [ry=] [rym=] [team=]
	 *    [score_---=] [score_---_min=]
	 * 
	 *    All selectors can be inverted.
	 * 
	 * @param The
	 *            command sender
	 * @param The
	 *            argument to test for
	 * @return The entities that match the criteria
	 */
	public static Entity[] getTargets(CommandSender sender, String arg) {
		Entity[] ents = null;
		Location loc = null;
		if (sender instanceof Player) {
			loc = ((Player) sender).getLocation();
		} else if (sender instanceof BlockCommandSender) {
			loc = ((BlockCommandSender) sender).getBlock().getLocation();
			// Tempfix
			loc.add(0.5, 0, 0.5);
		} else if (sender instanceof CommandMinecart) {
			loc = ((CommandMinecart) sender).getLocation();
		}
		if (arg.startsWith("@s")) {
			ents = new Entity[1];
			if (sender instanceof Player) {
				boolean good = true;
				for (int b = 0; b < getTags(arg).length; b++) {
					if (!canBeAccepted(getTags(arg)[b], (Entity) sender, loc)) {
						good = false;
						break;
					}
				}
				if (good) {
					ents[0] = (Entity) sender;
				}
			} else {

			}
			return ents;
		} else if (arg.startsWith("@a")) {
			// ents = new Entity[maxEnts];
			List<Entity> listOfValidEntities = new ArrayList<>();
			int C = Integer.MAX_VALUE;
			if (hasTag(SelectorType.C, arg))
				for (String s : getTags(arg)) {
					if (hasTag(SelectorType.C, s)) {
						C = getInt(s);
						break;
					}
				}
			World world3 = null;
			boolean usePlayers = true;
			for (String tag : getTags(arg)) {
				if (hasTag(SelectorType.TYPE, tag)) {
					usePlayers = false;
					break;
				}
			}
			for (World w : getAcceptedWorldsFullString(loc, arg)) {
				List<Entity> ea = usePlayers ? new ArrayList<Entity>(w.getPlayers()) : w.getEntities();
				for (Entity e : ea) {
					if (world3 == null || !world3.equals(w)) {
						world3 = w;
					}
					if (listOfValidEntities.size() >= C)
						break;
					boolean good = true;
					for (int b = 0; b < getTags(arg).length; b++) {
						if (!canBeAccepted(getTags(arg)[b], e, loc)) {
							good = false;
							break;
						}
					}
					if (good) {
						listOfValidEntities.add(e);
					}
				}
			}
			ents = listOfValidEntities.toArray(new Entity[listOfValidEntities.size()]);

		} else if (arg.startsWith("@p")) {
			ents = new Entity[1];
			double closestInt = Double.MAX_VALUE;
			Entity closest = null;

			for (World w : getAcceptedWorldsFullString(loc, arg)) {
				for (Player e : w.getPlayers()) {
					if (e == sender)
						continue;
					Location temp = loc;
					if (temp == null)
						temp = e.getWorld().getSpawnLocation();
					double distance = e.getLocation().distanceSquared(temp);
					if (closestInt > distance) {
						boolean good = true;
						for (String tag : getTags(arg)) {
							if (!canBeAccepted(tag, e, temp)) {
								good = false;
								break;
							}
						}
						if (good) {
							closestInt = distance;
							closest = e;
						}
					}
				}
			}
			ents[0] = closest;
		} else if (arg.startsWith("@e")) {
			// ents = new Entity[1];
			List<Entity> entities = new ArrayList<Entity>();
			for (World w : getAcceptedWorldsFullString(loc, arg)) {
				for (Entity e : w.getEntities()) {
					if (e == sender)
						continue;
					boolean good = true;
					for (String tag : getTags(arg)) {
						if (!canBeAccepted(tag, e, loc)) {
							good = false;
							break;
						}
					}
					if (good) {
						// Location check = e.getLocation();
						// if (check.getWorld() != loc.getWorld()) {
						// continue;
						// }
						entities.add(e);
					}
				}
			}
			// ents[0] = closest;
			ents = entities.toArray(new Entity[entities.size()]);
		} else if (arg.startsWith("@r")) {
			Random r = ThreadLocalRandom.current();
			ents = new Entity[1];
			Entity entity = null;
			int tries = 0;
			while (entity == null && tries < 100) {
				tries++;
				if (hasTag(SelectorType.TYPE, arg)) {
					Entity e = loc.getWorld().getEntities().get(r.nextInt(loc.getWorld().getEntities().size()));
					boolean good = true;
					for (String tag : getTags(arg)) {
						if (!canBeAccepted(tag, e, loc)) {
							good = false;
							break;
						}
					}
					if (good)
						entity = e;
				} else {
					List<Player> onl = new ArrayList<Player>(Bukkit.getOnlinePlayers());
					Entity e = onl.get(r.nextInt(onl.size()));
					boolean good = true;
					for (String tag : getTags(arg)) {
						if (!canBeAccepted(tag, e, loc)) {
							good = false;
							break;
						}
					}
					if (good)
						entity = e;
				}
			}
			ents[0] = entity;
		} else {
			ents = new Entity[1];
			ents[0] = Bukkit.getPlayer(arg);
		}
		return ents;
	}

	/**
	 * Returns one entity. Use this if you know the player will not provide the '@a'
	 * tag.
	 * 
	 * This can return a null variable if no tags are included, or if a value for a
	 * tag does not exist (I.e if the tag [type=___] contains an entity that does
	 * not exist in the specified world)
	 * 
	 * @param Who
	 *            sent the command
	 * @param The
	 *            argument
	 * @return The first entity retrieved.
	 */
	public static Entity getTarget(CommandSender sender, String arg) {
		Entity[] e = getTargets(sender, arg);
		if (e.length == 0)
			return null;
		return e[0];
	}

	/**
	 * Returns an integer. Use this to support "~" by providing what it will mean.
	 * 
	 * E.g. rel="x" when ~ should be turn into the entity's X coord.
	 * 
	 * Currently supports "x", "y" and "z".
	 * 
	 * 
	 * @param The
	 *            argument
	 * @param What
	 *            the int should represent
	 * @param The
	 *            entity to get the relative int from.
	 * @return the int
	 */
	public static int getIntRelative(String arg, String rel, Entity e) {
		int relInt = 0;
		if (arg.startsWith("~")) {
			switch (rel.toLowerCase()) {
			case "x":
				relInt = e.getLocation().getBlockX();
				break;
			case "y":
				relInt = e.getLocation().getBlockY();
				break;
			case "z":
				relInt = e.getLocation().getBlockZ();
				break;
			}
			return mathIt(arg, relInt);
		} else if (arg.startsWith("^")) {
			// TODO: Fix code. The currently just acts the same as ~. This should move the
			// entity relative to what its looking at.

			switch (rel.toLowerCase()) {
			case "x":
				relInt = e.getLocation().getBlockX();
				break;
			case "y":
				relInt = e.getLocation().getBlockY();
				break;
			case "z":
				relInt = e.getLocation().getBlockZ();
				break;
			}
			return mathIt(arg, relInt);
		}
		return 0;
	}

	private static boolean canBeAccepted(String arg, Entity e, Location loc) {
		if (hasTag(SelectorType.TYPE, arg) && isType(arg, e))
			return true;
		if (hasTag(SelectorType.NAME, arg) && isName(arg, e))
			return true;
		if (hasTag(SelectorType.TEAM, arg) && isTeam(arg, e))
			return true;
		if (hasTag(SelectorType.SCORE_FULL, arg) && isScore(arg, e))
			return true;
		if (hasTag(SelectorType.SCORE_MIN, arg) && isScoreMin(arg, e))
			return true;
		if (hasTag(SelectorType.SCORE_13, arg) && isScoreWithin(arg, e))
			return true;
		if (hasTag(SelectorType.DISTANCE, arg) && isWithinDistance(arg, loc, e))
			return true;
		if (hasTag(SelectorType.LEVEL, arg) && isWithinLevel(arg, e))
			return true;
		if (hasTag(SelectorType.TAG, arg) && isHasTags(arg, e))
			return true;
		if (hasTag(SelectorType.RYM, arg) && isRYM(arg, e))
			return true;
		if (hasTag(SelectorType.RXM, arg) && isRXM(arg, e))
			return true;
		if (hasTag(SelectorType.HM, arg) && isHM(arg, e))
			return true;
		if (hasTag(SelectorType.RY, arg) && isRY(arg, e))
			return true;
		if (hasTag(SelectorType.RX, arg) && isRX(arg, e))
			return true;
		if (hasTag(SelectorType.RM, arg) && isRM(arg, loc, e))
			return true;
		if (hasTag(SelectorType.LMax, arg) && isLM(arg, e))
			return true;
		if (hasTag(SelectorType.L, arg) && isL(arg, e))
			return true;
		if (hasTag(SelectorType.m, arg) && isM(arg, e))
			return true;
		if (hasTag(SelectorType.H, arg) && isH(arg, e))
			return true;
		if (hasTag(SelectorType.World, arg) && isW(arg, loc, e))
			return true;
		if (hasTag(SelectorType.R, arg) && isR(arg, loc, e))
			return true;
		return false;
	}

	private static String[] getTags(String arg) {
		if (!arg.contains("["))
			return new String[0];
		String tags = arg.split("\\[")[1].split("\\]")[0];
		return tags.split(",");
	}

	private static int mathIt(String args, int relInt) {
		int total = 0;
		short mode = 0;
		String arg = args.replace("~", String.valueOf(relInt));
		String intString = "";
		for (int i = 0; i < arg.length(); i++) {
			if (arg.charAt(i) == '+' || arg.charAt(i) == '-' || arg.charAt(i) == '*' || arg.charAt(i) == '/') {
				try {
					switch (mode) {
					case 0:
						total = total + Integer.parseInt(intString);
						break;
					case 1:
						total = total - Integer.parseInt(intString);
						break;
					case 2:
						total = total * Integer.parseInt(intString);
						break;
					case 3:
						total = total / Integer.parseInt(intString);
						break;
					}
					mode = (short) ((arg.charAt(i) == '+') ? 0
							: ((arg.charAt(i) == '-') ? 1
									: ((arg.charAt(i) == '*') ? 2 : ((arg.charAt(i) == '/') ? 3 : -1))));
				} catch (Exception e) {
					Bukkit.getLogger().severe("There has been an issue with a plugin using the CommandUtils class!");
				}

			} else if (args.length() == i || arg.charAt(i) == ' ' || arg.charAt(i) == ',' || arg.charAt(i) == ']') {
				try {
					switch (mode) {
					case 0:
						total = total + Integer.parseInt(intString);
						break;
					case 1:
						total = total - Integer.parseInt(intString);
						break;
					case 2:
						total = total * Integer.parseInt(intString);
						break;
					case 3:
						total = total / Integer.parseInt(intString);
						break;
					}
				} catch (Exception e) {
					Bukkit.getLogger().severe("There has been an issue with a plugin using the CommandUtils class!");
				}
				break;
			} else {
				intString += arg.charAt(i);
			}
		}
		return total;
	}

	private static String getType(String arg) {
		if (hasTag(SelectorType.TYPE, arg))
			return arg.toLowerCase().split("=")[1].replace("!", "");
		return "Player";
	}

	private static String getName(String arg) {
		String reparg = arg.replace(" ", "_");
		return reparg.replace("!", "").split("=")[1];
	}

	private static World getW(String arg) {
		return Bukkit.getWorld(getString(arg)/* arg.replace("!", "").split("=")[1] */);
	}

	private static String getScoreMinName(String arg) {
		return arg.split("=")[0].substring(0, arg.split("=")[0].length() - 1 - 4).replace("score_", "");
	}

	private static String getScoreName(String arg) {
		return arg.split("=")[0].replace("score_", "");
	}

	private static String getTeam(String arg) {
		return arg.toLowerCase().replace("!", "").split("=")[1];
	}

	private static int getScoreMin(String arg) {
		return Integer.parseInt(arg.replace("!", "").split("=")[1]);
	}

	private static int getScore(String arg) {
		return Integer.parseInt(arg.replace("!", "").split("=")[1]);
	}

	private static GameMode getM(String arg) {
		String[] split = arg.replace("!", "").toLowerCase().split("=");
		String returnType = split[1];
		if (returnType.equalsIgnoreCase("0") || returnType.equalsIgnoreCase("s")
				|| returnType.equalsIgnoreCase("survival"))
			return GameMode.SURVIVAL;
		if (returnType.equalsIgnoreCase("1") || returnType.equalsIgnoreCase("c")
				|| returnType.equalsIgnoreCase("creative"))
			return GameMode.CREATIVE;
		if (returnType.equalsIgnoreCase("2") || returnType.equalsIgnoreCase("a")
				|| returnType.equalsIgnoreCase("adventure"))
			return GameMode.ADVENTURE;
		if (returnType.equalsIgnoreCase("3") || returnType.equalsIgnoreCase("sp")
				|| returnType.equalsIgnoreCase("spectator"))
			return GameMode.SPECTATOR;
		return null;
	}

	private static List<World> getAcceptedWorldsFullString(Location loc, String fullString) {
		String string = null;
		for (String tag : getTags(fullString)) {
			if (hasTag(SelectorType.World, tag)) {
				string = tag;
				break;
			}
		}
		if (string == null) {
			List<World> worlds = new ArrayList<World>();
			if (loc == null || loc.getWorld() == null) {
				worlds.addAll(Bukkit.getWorlds());
			} else {
				worlds.add(loc.getWorld());
			}
			return worlds;
		}
		return getAcceptedWorlds(string);
	}

	private static List<World> getAcceptedWorlds(String string) {
		List<World> worlds = new ArrayList<World>(Bukkit.getWorlds());
		if (isInverted(string)) {
			worlds.remove(getW(string));
		} else {
			worlds.clear();
			worlds.add(getW(string));
		}
		return worlds;
	}

	private static boolean isTeam(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
			if ((t.getName().equalsIgnoreCase(getTeam(arg)) != isInverted(arg))) {
				if ((t.getEntries().contains(((Player) e).getName()) != isInverted(arg)))
					return true;
			}
		}
		return false;
	}

	private static boolean isWithinDistance(String arg, Location start, Entity e) {
		double distanceMin = 0;
		double distanceMax = Double.MAX_VALUE;
		String distance = arg.split("=")[1];
		if (e.getLocation().getWorld() != start.getWorld())
			return false;
		if (distance.contains("..")) {
			String[] temp = distance.split("\\.\\.");
			if (!temp[0].isEmpty()) {
				distanceMin = Integer.parseInt(temp[0]);
			}
			if (temp.length > 1 && !temp[1].isEmpty()) {
				distanceMax = Double.parseDouble(temp[1]);
			}
			double actDis = start.distanceSquared(e.getLocation());
			return actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis;
		} else {
			int mult = Integer.parseInt(distance);
			mult *= mult;
			return ((int) start.distanceSquared(e.getLocation())) == mult;
		}
	}

	private static boolean isWithinLevel(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		double distanceMin = 0;
		double distanceMax = Double.MAX_VALUE;
		String distance = arg.split("=")[1];
		if (distance.contains("..")) {
			String[] temp = distance.split("..");
			if (!temp[0].isEmpty()) {
				distanceMin = Integer.parseInt(temp[0]);
			}
			if (temp[1] != null && !temp[1].isEmpty()) {
				distanceMax = Double.parseDouble(temp[1]);
			}
			double actDis = ((Player) e).getExpToLevel();
			return actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis;
		} else {
			return ((Player) e).getExpToLevel() == Integer.parseInt(distance);
		}
	}

	private static boolean isScore(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
			if (o.getName().equalsIgnoreCase(getScoreName(arg))) {
				if ((o.getScore(((Player) e).getName()).getScore() <= getScore(arg) != isInverted(arg)))
					return true;
			}
		}
		return false;
	}

	private static boolean isScoreWithin(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		String[] scores = arg.split("{")[1].split("}")[0].split(",");
		for (int i = 0; i < scores.length; i++) {
			String[] s = scores[i].split("=");
			String name = s[0];

			for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
				if (o.getName().equalsIgnoreCase(name)) {
					if (!isWithinIntValue(arg, o.getScore(e.getName()).getScore()))
						return false;
				}
			}
		}
		return true;

	}

	private static boolean isHasTags(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		return isInverted(arg) != e.getScoreboardTags().contains(getString(arg));

	}

	private static boolean isScoreMin(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
			if (o.getName().equalsIgnoreCase(getScoreMinName(arg))) {
				if ((o.getScore(((Player) e).getName()).getScore() >= getScoreMin(arg) != isInverted(arg)))
					return true;
			}
		}
		return false;
	}

	private static boolean isRM(String arg, Location loc, Entity e) {
		if (loc.getWorld() != e.getWorld())
			return false;
		return isGreaterThan(arg, loc.distance(e.getLocation()));
	}

	private static boolean isR(String arg, Location loc, Entity e) {
		if (loc.getWorld() != e.getWorld())
			return false;
		return isLessThan(arg, loc.distance(e.getLocation()));

	}

	private static boolean isRXM(String arg, Entity e) {
		return isLessThan(arg, e.getLocation().getYaw());
	}

	private static boolean isRX(String arg, Entity e) {
		return isGreaterThan(arg, e.getLocation().getYaw());
	}

	private static boolean isRYM(String arg, Entity e) {
		return isLessThan(arg, e.getLocation().getPitch());
	}

	private static boolean isRY(String arg, Entity e) {
		return isGreaterThan(arg, e.getLocation().getPitch());
	}

	private static boolean isL(String arg, Entity e) {
		if (e instanceof Player) {
			isLessThan(arg, ((Player) e).getTotalExperience());
		}
		return false;
	}

	private static boolean isLM(String arg, Entity e) {
		if (e instanceof Player) {
			return isGreaterThan(arg, ((Player) e).getTotalExperience());

		}
		return false;
	}

	private static boolean isH(String arg, Entity e) {
		if (e instanceof Damageable)
			return isGreaterThan(arg, ((Damageable) e).getHealth());
		return false;
	}

	private static boolean isHM(String arg, Entity e) {
		if (e instanceof Damageable)
			return isLessThan(arg, ((Damageable) e).getHealth());
		return false;
	}

	private static boolean isM(String arg, Entity e) {
		if (getM(arg) == null)
			return true;
		if (e instanceof HumanEntity) {
			if ((isInverted(arg) != (getM(arg) == ((HumanEntity) e).getGameMode())))
				return true;
		}
		return false;
	}

	private static boolean isW(String arg, Location loc, Entity e) {
		if (getW(arg) == null) {
			return true;
		} else if ((isInverted(arg) != getAcceptedWorlds(arg).contains(getW(arg))))
			return true;
		return false;
	}

	private static boolean isName(String arg, Entity e) {
		if (getName(arg) == null)
			return true;
		if ((isInverted(arg) != (e.getCustomName() != null) && isInverted(arg) != (getName(arg)
				.equals(e.getCustomName().replace(" ", "_"))
				|| (e instanceof Player && ((Player) e).getName().replace(" ", "_").equalsIgnoreCase(getName(arg))))))
			return true;
		return false;
	}

	private static boolean isType(String arg, Entity e) {
		boolean invert = isInverted(arg);
		String type = getType(arg);
		if (invert != e.getType().name().equalsIgnoreCase(type))
			return true;
		return false;

	}

	/*
	 * private static boolean hasR(String arg) { return
	 * arg.toLowerCase().startsWith("r="); }
	 * 
	 * private static boolean hasScoreMin(String arg) { boolean startW =
	 * arg.startsWith("score_"); if (!startW) return false; String[] split =
	 * arg.split("="); if (split[0].endsWith("_min")) return true; return false; }
	 * 
	 * private static boolean hasScore(String arg) { boolean startW =
	 * arg.startsWith("score_"); if (!startW) return false; String[] split =
	 * arg.split("="); if (!split[0].endsWith("_min")) return true; return false; }
	 * 
	 * private static boolean hasRX(String arg) { return
	 * arg.toLowerCase().startsWith("rx="); }
	 * 
	 * private static boolean hasRXM(String arg) { return
	 * arg.toLowerCase().startsWith("rxm="); }
	 * 
	 * private static boolean hasRY(String arg) { return
	 * arg.toLowerCase().startsWith("ry="); }
	 * 
	 * private static boolean hasRYM(String arg) { return
	 * arg.toLowerCase().startsWith("rym="); }
	 * 
	 * private static boolean hasRM(String arg) { return
	 * arg.toLowerCase().startsWith("rm="); }
	 * 
	 * private static boolean hasH(String arg) { return
	 * arg.toLowerCase().startsWith("h="); }
	 * 
	 * private static boolean hasHM(String arg) { return
	 * arg.toLowerCase().startsWith("hm="); }
	 * 
	 * private static boolean hasC(String arg) { return
	 * arg.toLowerCase().startsWith("c="); }
	 * 
	 * private static boolean hasM(String arg) { return
	 * arg.toLowerCase().startsWith("m="); }
	 * 
	 * private static boolean hasW(String arg) { return
	 * arg.toLowerCase().startsWith("w="); }
	 * 
	 * private static boolean hasL(String arg) { return
	 * arg.toLowerCase().startsWith("l="); }
	 * 
	 * private static boolean hasLM(String arg) { return
	 * arg.toLowerCase().startsWith("lm="); }
	 * 
	 * private static boolean hasName(String arg) { return
	 * arg.toLowerCase().startsWith("name="); }
	 * 
	 * private static boolean hasTeam(String arg) { return
	 * (arg.toLowerCase().startsWith("team=")); }
	 * 
	 * private static boolean hasType(String arg) { return
	 * arg.toLowerCase().startsWith("type="); }
	 * 
	 * private static boolean hasDistance(String arg) { return
	 * arg.toLowerCase().startsWith("distance="); }
	 * 
	 * private static boolean hasLevel(String arg) { return
	 * arg.toLowerCase().startsWith("level="); }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * private static int getC(String arg) { return getInt(arg); if (!hasC(arg))
	 * return Integer.MAX_VALUE; return
	 * Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]); }
	 * 
	 * private static int getR(String arg) { if (!hasR(arg)) return
	 * Integer.MAX_VALUE; return Integer.parseInt(arg.toLowerCase().replace("!",
	 * "").split("=")[1]); }
	 * 
	 * private static int getRM(String arg) { if (!hasRM(arg)) return 1; return
	 * Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]); }
	 * 
	 * private static int getRX(String arg) { if (!hasRX(arg)) return
	 * -Integer.MAX_VALUE; return Integer.parseInt(arg.toLowerCase().replace("!",
	 * "").split("=")[1]); }
	 * 
	 * private static int getRXM(String arg) { if (!hasRXM(arg)) return -8; return
	 * Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]); }
	 * 
	 * private static int getRY(String arg) { if (!hasRY(arg)) return
	 * -Integer.MAX_VALUE; return Integer.parseInt(arg.toLowerCase().replace("!",
	 * "").split("=")[1]); }
	 * 
	 * private static int getRYM(String arg) { if (!hasRYM(arg)) return -8; return
	 * Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]); }
	 * 
	 * private static int getH(String arg) { if (!hasH(arg)) return -1; return
	 * Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]); }
	 * 
	 * private static int getHM(String arg) { if (!hasHM(arg)) return
	 * Integer.MAX_VALUE; return Integer.parseInt(arg.toLowerCase().replace("!",
	 * "").split("=")[1]); }
	 * 
	 * private static int getL(String arg) { if (!hasL(arg)) return -1; return
	 * Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]); }
	 * 
	 * private static int getLM(String arg) { if (!hasLM(arg)) return
	 * Integer.MAX_VALUE; return Integer.parseInt(arg.toLowerCase().replace("!",
	 * "").split("=")[1]); }
	 * 
	 * private static String getName(String arg) { String reparg = arg.replace(" ",
	 * "_"); if (!hasName(reparg)) return null; return reparg.replace("!",
	 * "").split("=")[1]; }
	 * 
	 * private static World getW(String arg) { if (!hasW(arg)) return null; return
	 * Bukkit.getWorld(arg.replace("!", "").split("=")[1]); }
	 * 
	 * private static String getScoreMinName(String arg) { if (!hasScoreMin(arg))
	 * return null; return arg.split("=")[0].substring(0, arg.split("=")[0].length()
	 * - 1 - 4).replace("score_", ""); }
	 * 
	 * private static String getScoreName(String arg) { if (!hasScore(arg)) return
	 * null; return arg.split("=")[0].replace("score_", ""); }
	 * 
	 * private static String getTeam(String arg) { if (!hasTeam(arg)) return null;
	 * return arg.toLowerCase().replace("!", "").split("=")[1]; }
	 * 
	 * private static int getScoreMin(String arg) { if (!hasScoreMin(arg)) return
	 * -8; return Integer.parseInt(arg.replace("!", "").split("=")[1]); }
	 * 
	 * private static int getScore(String arg) { if (!hasScore(arg)) return
	 * Integer.MAX_VALUE; return Integer.parseInt(arg.replace("!",
	 * "").split("=")[1]); }
	 * 
	 * private static GameMode getM(String arg) { if (!hasM(arg)) return null;
	 * String[] split = arg.replace("!", "").toLowerCase().split("="); String
	 * returnType = split[1]; if (returnType.equalsIgnoreCase("0") ||
	 * returnType.equalsIgnoreCase("s") || returnType.equalsIgnoreCase("survival"))
	 * return GameMode.SURVIVAL; if (returnType.equalsIgnoreCase("1") ||
	 * returnType.equalsIgnoreCase("c") || returnType.equalsIgnoreCase("creative"))
	 * return GameMode.CREATIVE; if (returnType.equalsIgnoreCase("2") ||
	 * returnType.equalsIgnoreCase("a") || returnType.equalsIgnoreCase("adventure"))
	 * return GameMode.ADVENTURE; if (returnType.equalsIgnoreCase("3") ||
	 * returnType.equalsIgnoreCase("sp") ||
	 * returnType.equalsIgnoreCase("spectator")) return GameMode.SPECTATOR; return
	 * null; }
	 * 
	 * private static List<World> getAcceptedWorlds(Location loc, String string) {
	 * List<World> worlds = new ArrayList<World>(Bukkit.getWorlds()); if
	 * (!hasW(string)) { } else if (isInverted(string)) {
	 * worlds.remove(getW(string)); } else { worlds.clear();
	 * worlds.add(getW(string)); } return worlds; }
	 */

	private static boolean isInverted(String arg) {
		return arg.toLowerCase().split("!").length != 1;
	}

	private static int getInt(String arg) {
		int mult = Integer.parseInt(arg.split("=")[1]);
		return mult;
	}

	public static String getString(String arg) {
		return arg.split("=")[1].replaceAll("!", "");
	}
	/*
	 * private static double getDouble(String arg) { double mult =
	 * Double.parseDouble(arg.split("=")[1]); return mult; }
	 */

	private static boolean isLessThan(String arg, double value) {
		boolean inverted = isInverted(arg);
		double mult = Double.parseDouble(arg.split("=")[1]);
		return (value < mult) != inverted;
	}

	private static boolean isGreaterThan(String arg, double value) {
		boolean inverted = isInverted(arg);
		double mult = Double.parseDouble(arg.split("=")[1]);
		return (value > mult) != inverted;
	}

	private static boolean isWithinIntValue(String arg, double value) {
		boolean inverted = isInverted(arg);
		double min = -Double.MAX_VALUE;
		double max = Double.MAX_VALUE;
		if (arg.contains("..")) {
			String[] temp = arg.split("\\.\\.");
			if (!temp[0].isEmpty()) {
				min = Integer.parseInt(temp[0]);
			}
			if (temp.length > 1 && !temp[1].isEmpty()) {
				max = Double.parseDouble(temp[1]);
			}
			return (value <= max * max && min * min <= value) != inverted;
		} else {
			double mult = Double.parseDouble(arg.split("=")[1]);
			return (value == mult) != inverted;
		}
	}

	private static boolean hasTag(SelectorType type, String arg) {
		return arg.toLowerCase().startsWith(type.getName());
	}

	enum SelectorType {
		LEVEL("level="), DISTANCE("distance="), TYPE("type="), NAME("name="), TEAM("team="), LMax("lm="), L(
				"l="), World("w="), m("m="), C("c="), HM("hm="), H("h="), RM("rm="), RYM("rym="), RX("rx="), SCORE_FULL(
						"score="), SCORE_MIN(
								"score_min"), SCORE_13("scores="), R("r="), RXM("rxm="), RY("ry="), TAG("tag=")

		;
		String name;

		SelectorType(String s) {
			this.name = s;
		}

		public String getName() {
			return name;
		}
	}
}
