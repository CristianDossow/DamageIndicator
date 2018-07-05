/**
 * Copyright 2018 YitanTribal & Beelzebu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cl.mastercode.DamageIndicator.command;

import cl.mastercode.DamageIndicator.DIMain;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * @author YitanTribal
 */
@RequiredArgsConstructor
public final class CommandHandler implements CommandExecutor {

    private final DIMain plugin;

    private boolean isPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Only a Player can use this command");
        return false;
    }

    private boolean checkPermissions(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "You don't have permission to use this command this!");
        return false;
    }

    private boolean checkArguments(CommandSender sender, int args, int num) {
        if (args != num) {
            sender.sendMessage(ChatColor.RED + "Invalid number of arguments");
            return false;
        }
        return true;
    }

    private int getInt(CommandSender sender, String text) {
        int amount;
        try {
            amount = Integer.parseInt(text);
            return amount;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid given amount");
            return -1;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] strings) {
        if (strings.length > 0) {
            if ("reload".equalsIgnoreCase(strings[0])) {
                if (checkPermissions(sender, "damageindicator.admin")) {
                    plugin.reload();
                    sender.sendMessage(ChatColor.GREEN + "Reloaded config!");
                    return true;
                }
            } else if ("clear".equalsIgnoreCase(strings[0])) {
                if (isPlayer(sender) && (checkPermissions(sender, "damageindicator.clear") || checkPermissions(sender, "damageindicator.admin")) && checkArguments(sender, strings.length, 2)) {
                    int range = getInt(sender, strings[1]);
                    if (range >= 0) {
                        int c = 0;
                        c = ((Player) sender).getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof ArmorStand && plugin.isDamageIndicator(entity, false)).peek(Entity::remove).map(_i -> 1).reduce(c, Integer::sum);
                        sender.sendMessage(ChatColor.GREEN + "" + c + " Damage Indicators were removed");
                        return true;
                    }
                }
            } else if ("clearall".equalsIgnoreCase(strings[0])) {
                if (isPlayer(sender) && (checkPermissions(sender, "damageindicator.admin"))) {
                    int c = 0;
                    c = ((Player) sender).getLocation().getWorld().getEntitiesByClass(ArmorStand.class).stream().filter(plugin::isDamageIndicator).peek(Entity::remove).map(_i -> 1).reduce(c, Integer::sum);
                    sender.sendMessage(ChatColor.GREEN + "" + c + " Damage Indicators were removed in " + ((Player) sender).getLocation().getWorld().getName());
                    return true;
                }
            } else if ("toggle".equalsIgnoreCase(strings[0])) {
                if (isPlayer(sender)) {
                    boolean status = !plugin.getStorageProvider().showArmorStand((Player) sender);
                    plugin.getStorageProvider().setShowArmorStand((Player) sender, status);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', status ? plugin.getConfig().getString("Messages.Damage Indicator.Enabled", "") : plugin.getConfig().getString("Messages.Damage Indicator.Disabled", "")));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis command can't be executed from console."));
                }
            }
        } else {
            sender.sendMessage(ChatColor.DARK_AQUA + "<===== Damage Indicator " + Bukkit.getServer().getPluginManager().getPlugin("DamageIndicator").getDescription().getVersion() + " =====>");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di reload");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di clear <range> " + ChatColor.AQUA + "#remove the damage indicators in the range");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di clearall " + ChatColor.AQUA + "#remove the damage indicators in the world (may cause lag)");
            sender.sendMessage(ChatColor.DARK_AQUA + "/di toggle " + ChatColor.AQUA + "#enable/disable damage indicators for you");
        }
        return true;
    }

}
