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

import cl.mastercode.DamageIndicator.Main;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
@RequiredArgsConstructor
public class DamageIndicatorListener implements Listener {

    private final Main plugin;
    @Getter
    private final LinkedHashMap<ArmorStand, Long> armorStands = new LinkedHashMap<>();

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
        ArmorStand as;
        String cfgFormat = plugin.getConfig().getString("Format.EntityRegain");
        String displayFormat = cfgFormat.replace("&", "§").replace("%health%", damageFormat(e.getAmount()));
        boolean enablePlayer = plugin.getConfig().getBoolean("UseAt.Player");
        boolean enableMonster = plugin.getConfig().getBoolean("UseAt.Monster");
        boolean enableAnimal = plugin.getConfig().getBoolean("UseAt.Animals");
        if (e.getEntity() instanceof Player && enablePlayer && !e.isCancelled() && e.getAmount() < 1.0) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            as = getDefaultArmorStand(e.getEntity().getLocation());
            as.setCustomName("" + displayFormat);
            armorStands.put(as, System.currentTimeMillis());
        }
        if (e.getEntity() instanceof Monster || e.getEntity() instanceof Slime || e.getEntity() instanceof MagmaCube) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            if (enableMonster) {
                as = getDefaultArmorStand(e.getEntity().getLocation());
                as.setCustomName("" + displayFormat);
                armorStands.put(as, System.currentTimeMillis());
            }
        }
        if (e.getEntity() instanceof Animals && enableAnimal) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            as = getDefaultArmorStand(e.getEntity().getLocation());
            as.setCustomName("" + displayFormat);
            armorStands.put(as, System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        ArmorStand as;
        String cfgFormat = plugin.getConfig().getString("Format.EntityDamage");
        String displayFormat = cfgFormat.replace("&", "§").replace("%damage%", damageFormat(e.getFinalDamage()));
        boolean enablePlayer = plugin.getConfig().getBoolean("UseAt.Player");
        boolean enableMonster = plugin.getConfig().getBoolean("UseAt.Monster");
        boolean enableAnimal = plugin.getConfig().getBoolean("UseAt.Animals");
        if (!e.isCancelled() && e.getDamage() < 1.0 && (e.getEntity().getType() != EntityType.ARMOR_STAND || e.getEntity().getType() != EntityType.PAINTING || e.getEntity().getType() != EntityType.ITEM_FRAME) && (e.getEntity() instanceof Player || e.getEntity() instanceof Monster || e.getEntity() instanceof Animals || e.getEntity() instanceof Slime || e.getEntity() instanceof MagmaCube)) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0.0, 1.0, 0.0), Effect.STEP_SOUND, 152);
        }
        if (e.getEntity() instanceof Player && enablePlayer) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            as = getDefaultArmorStand(e.getEntity().getLocation());
            as.setCustomName("" + displayFormat);
            armorStands.put(as, System.currentTimeMillis());
        }
        if (e.getEntity() instanceof Monster || e.getEntity() instanceof Slime || e.getEntity() instanceof MagmaCube) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            if (enableMonster) {
                as = getDefaultArmorStand(e.getEntity().getLocation());
                as.setCustomName("" + displayFormat);
                armorStands.put(as, System.currentTimeMillis());
            }
        }
        if (e.getEntity() instanceof Animals && enableAnimal) {
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }
            if (e.getEntity() instanceof ArmorStand) {
                return;
            }
            as = getDefaultArmorStand(e.getEntity().getLocation());
            as.setCustomName("" + displayFormat);
            armorStands.put(as, System.currentTimeMillis());
        }
    }

    private String damageFormat(Object o) {
        DecimalFormat df;
        try {
            df = new DecimalFormat(plugin.getConfig().getString("Format.Decimal"));
        } catch (Exception ex) {
            df = new DecimalFormat("#.#");
        }
        return df.format(o);
    }

    public ArmorStand getDefaultArmorStand(Location loc) {
        Location spawnLoc = new Location(loc.getWorld(), loc.getX(), 500, loc.getZ());
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setCustomNameVisible(false);
        as.setSmall(true);
        as.setRemoveWhenFarAway(true);
        as.setMetadata("Mastercode-DamageIndicator", new FixedMetadataValue(plugin, 1));
        as.setGravity(false);
        try {
            as.setCollidable(false);
            as.setInvulnerable(true);
        } catch (Exception oldVersion) {
        }
        as.setMarker(true);
        as.teleport(loc.add(0.0, 1.6, 0.0));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            as.setCustomNameVisible(true);
        }, 7);
        return as;
    }
}
