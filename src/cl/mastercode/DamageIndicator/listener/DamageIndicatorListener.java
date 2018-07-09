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
package cl.mastercode.DamageIndicator.listener;

import cl.mastercode.DamageIndicator.DIMain;
import cl.mastercode.DamageIndicator.util.EntityHider;
import cl.mastercode.DamageIndicator.util.EntityHider.Policy;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 *
 * @author YitanTribal, Beelzebu
 */
public class DamageIndicatorListener implements Listener {

    private final DIMain plugin;
    @Getter
    private final LinkedHashMap<ArmorStand, Long> armorStands = new LinkedHashMap<>();
    private final boolean enablePlayer, enableMonster, enableAnimal, protocollib;
    private EntityHider hider;

    public DamageIndicatorListener(DIMain plugin) {
        this.plugin = plugin;
        enablePlayer = plugin.getConfig().getBoolean("Damage Indicator.Player");
        enableMonster = plugin.getConfig().getBoolean("Damage Indicator.Monster");
        enableAnimal = plugin.getConfig().getBoolean("Damage Indicator.Animals");
        protocollib = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
        if (protocollib) {
            hider = new EntityHider(plugin, Policy.BLACKLIST);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand as = (ArmorStand) entity;
                if (plugin.isDamageIndicator(as)) {
                    armorStands.remove(as);
                    as.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkload(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand as = (ArmorStand) entity;
                if (plugin.isDamageIndicator(as)) {
                    armorStands.remove(as);
                    as.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (!e.isCancelled()) {
            handleArmorStand((LivingEntity) e.getEntity(), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Damage Indicator.Format.EntityRegain").replace("%health%", damageFormat(e.getAmount()))));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (!e.isCancelled()) {
            handleArmorStand((LivingEntity) e.getEntity(), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Damage Indicator.Format.EntityDamage").replace("%damage%", damageFormat(e.getFinalDamage()))));
        }
    }

    private String damageFormat(double damage) {
        DecimalFormat df;
        try {
            df = new DecimalFormat(plugin.getConfig().getString("Damage Indicator.Format.Decimal", "#.##"));
        } catch (Exception ex) {
            df = new DecimalFormat("#.##");
        }
        return df.format(damage);
    }

    private void handleArmorStand(LivingEntity entity, String format) {
        if (entity.hasMetadata("NPC")) {
            return;
        }
        if (entity instanceof ArmorStand) {
            return;
        }
        if (entity instanceof Player && !enablePlayer) {
            return;
        }
        if ((entity instanceof Monster || entity instanceof Slime || entity instanceof MagmaCube) && !enableMonster) {
            return;
        }
        if (entity instanceof Animals && !enableAnimal) {
            return;
        }
        armorStands.put(getDefaultArmorStand(is18() ? entity.getEyeLocation() : entity.getLocation(), format), System.currentTimeMillis());
    }

    public ArmorStand getDefaultArmorStand(Location loc, String name) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(new Location(loc.getWorld(), loc.getX(), 200, loc.getZ()), EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setCustomNameVisible(false);
        as.setSmall(true);
        as.setRemoveWhenFarAway(true);
        as.setMetadata("Mastercode-DamageIndicator", new FixedMetadataValue(plugin, 1));
        as.setGravity(false);
        if (!is18()) {
            as.setCollidable(false);
            as.setInvulnerable(true);
        }
        as.setMarker(true);
        as.teleport(is18() ? loc.add(0, 2, 0) : loc.add(0, 1.6, 0));
        as.setCustomName(name);
        as.setCustomNameVisible(true);
        Bukkit.getOnlinePlayers().stream().filter(op -> !plugin.getStorageProvider().showArmorStand(op)).forEach(op -> hider.hideEntity(op, as));
        return as;
    }

    private boolean is18() {
        try {
            ArmorStand.class.getMethod("setCollidable", boolean.class);
            return false;
        } catch (NoSuchMethodError | NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
            return true;
        }
    }
}
