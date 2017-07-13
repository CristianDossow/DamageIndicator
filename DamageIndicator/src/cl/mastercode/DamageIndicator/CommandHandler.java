package cl.mastercode.DamageIndicator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public final class CommandHandler implements CommandExecutor {

    String noPermission = ChatColor.DARK_RED + "You don't have permission to do this!";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] strings) {
        if (strings.length > 0) {
            if ("reload".equalsIgnoreCase(strings[0])) {
                if (CheckPermissions(sender, "damageindicator.admin")) {
                    Main.splugin.reload();
                    sender.sendMessage(ChatColor.GREEN + "Reloaded config!");
                    return true;
                }
            } else if ("clear".equalsIgnoreCase(strings[0])) {
                if (IsPlayer(sender)
                        && (CheckPermissions(sender, "damageindicator.clear") || CheckPermissions(sender, "damageindicator.admin"))
                        && CheckArguments(sender, strings.length, 2)) {
                    int range = GetInt(sender, strings[1]);
                    if (range >= 0) {
                        int c = 0;
                        c = ((Player) sender).getNearbyEntities(range, range, range).stream().filter((entity) -> (entity instanceof ArmorStand && Main.splugin.isDamageIndicator((ArmorStand) entity))).map((entity) -> {
                            entity.remove();
                            return entity;
                        }).map((_item) -> 1).reduce(c, Integer::sum);
                        sender.sendMessage(ChatColor.GREEN + "" + c + " Damage Indicators were removed");
                        return true;
                    }
                }
            } else if ("clearall".equalsIgnoreCase(strings[0])) {
                if (IsPlayer(sender) && (CheckPermissions(sender, "damageindicator.admin"))) {
                    int c = 0;
                    c = ((Player) sender).getLocation().getWorld().getEntitiesByClass(org.bukkit.entity.ArmorStand.class).stream().filter((as) -> (Main.splugin.isDamageIndicator(as))).map((as) -> {
                        as.remove();
                        return as;
                    }).map((_item) -> 1).reduce(c, Integer::sum);
                    sender.sendMessage(ChatColor.GREEN + "" + c + " Damage Indicators were removed in " + ((Player) sender).getLocation().getWorld().getName());
                    return true;
                }
            } else if ("debug".equalsIgnoreCase(strings[0])) {
                if (IsPlayer(sender)
                        && (CheckPermissions(sender, "damageindicator.admin"))
                        && CheckArguments(sender, strings.length, 2)) {
                    int range = GetInt(sender, strings[1]);
                    if (range >= 0) {
                        ((Player) sender).getNearbyEntities(range, range, range).stream().filter((entity) -> (entity instanceof ArmorStand)).map((entity) -> (ArmorStand) entity).map((as) -> {
                            sender.sendMessage("Dysplay Name: " + as.getCustomName());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("isSmall (true) " + as.isSmall());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("isCustomNameVisible (true) " + as.isCustomNameVisible());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("hasGravity (false) " + as.hasGravity());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("isCollidable (false) " + as.isCollidable());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("iisMarker (true) " + as.isMarker());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("isVisible (false) " + as.isVisible());
                            return as;
                        }).map((as) -> {
                            sender.sendMessage("isInvulnerable (true) " + as.isInvulnerable());
                            return as;
                        }).forEachOrdered((as) -> {
                            sender.sendMessage("getRemoveWhenFarAway (true) " + as.getRemoveWhenFarAway());
                        });
                    }
                }
            }
        } else {
            String version = Bukkit.getServer().getPluginManager().getPlugin("DamageIndicator").getDescription().getVersion();
            sender.sendMessage(ChatColor.DARK_AQUA + "<===== Damage Indicator " + version + " =====>");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di reload");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di clear <range> " + ChatColor.AQUA + "#remove the damage indicators in the range");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di clearall " + ChatColor.AQUA + "#remove the damage indicators in the world (may cause lag)");
        }
        return true;
    }

    public static boolean IsPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Only a Player can use this command");
        return false;
    }

    public static boolean CheckPermissions(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "You don't have permission to use this command this!");
        return false;
    }

    public static boolean CheckArguments(CommandSender sender, int args, int num) {
        if (args != num) {
            sender.sendMessage(ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        return true;
    }

    public static int GetInt(CommandSender sender, String text) {
        int amount;
        try {
            amount = Integer.parseInt(text);
            return amount;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid given amount");
            return -1;
        }
    }

}
