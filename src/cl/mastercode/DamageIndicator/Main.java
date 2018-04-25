package cl.mastercode.DamageIndicator;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
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
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private static Main plugin;
    private Map<ArmorStand, Long> armorStands = new HashMap<>();
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();

    public void reload() {
        armorStands.forEach((armor, time) -> armor.remove());
        armorStands = new HashMap<>();
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    public static Main getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        reload();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("damageindicator").setExecutor(new CommandHandler());

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!armorStands.isEmpty()) {
                armorStands.entrySet().forEach(entry -> {
                    if (entry.getValue() + 1500 < System.currentTimeMillis()) {
                        entry.getKey().remove();
                    } else {
                        entry.getKey().teleport(entry.getKey().getLocation().add(0.0, 0.07, 0.0));
                    }
                });
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        armorStands.entrySet().forEach((entry) -> {
            entry.getKey().remove();
        });
        int c = 0;
        for (World world : Bukkit.getWorlds()) {
            c = world.getEntitiesByClass(ArmorStand.class).stream().filter(as -> isDamageIndicator(as)).map(as -> {
                as.remove();
                return as;
            }).map(i -> 1).reduce(c, Integer::sum);
        }
        console.sendMessage("§c" + c + " Damage Indicators were removed in plugin unload" + "");
    }

    public ArmorStand getDefaultArmorStand(Location loc) {
        Location spawnLoc = new Location(loc.getWorld(), loc.getX(), 500, loc.getZ());
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setCustomNameVisible(false);
        as.setInvulnerable(true);
        as.setSmall(true);
        as.setRemoveWhenFarAway(true);
        as.setMetadata("Mastercode-DamageIndicator", new FixedMetadataValue(plugin, 1));
        as.setGravity(false);
        as.setCollidable(false);
        as.setMarker(true);
        as.teleport(loc.add(0.0, 1.6, 0.0));
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            as.setCustomNameVisible(true);
        }, 7);
        return as;
    }

    public boolean isDamageIndicator(ArmorStand as) {
        if (as.hasMetadata("Mastercode-DamageIndicator")) {
            return true;
        }
        return as.isInvulnerable()
                && as.isSmall()
                && !as.hasGravity()
                && as.isMarker()
                && !as.isVisible();
    }

    public boolean isOldDamageIndicator(ArmorStand as) {
        return as.isCustomNameVisible()
                && !as.hasGravity()
                && !as.isVisible()
                && as.getCustomName() != null
                && (as.getCustomName().contains("-") || as.getCustomName().contains("+"));
    }

    @EventHandler()
    public void RemoveArmorStandsOnChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand as = (ArmorStand) entity;
                if (isDamageIndicator(as)) {
                    armorStands.remove(as);
                    as.remove();
                }
            }
        }
    }

    @EventHandler()
    public void RemoveArmorStandsOnChunkload(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand as = (ArmorStand) entity;
                if (isDamageIndicator(as)) {
                    armorStands.remove(as);
                    as.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        ArmorStand as;
        String cfgFormat = getConfig().getString("Format.EntityRegain");
        String displayFormat = cfgFormat.replace("&", "§").replace("%health%", damageFormat(e.getAmount()));
        boolean enablePlayer = getConfig().getBoolean("UseAt.Player");
        boolean enableMonster = getConfig().getBoolean("UseAt.Monster");
        boolean enableAnimal = getConfig().getBoolean("UseAt.Animals");
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
        String cfgFormat = getConfig().getString("Format.EntityDamage");
        String displayFormat = cfgFormat.replace("&", "§").replace("%damage%", damageFormat(e.getFinalDamage()));
        boolean enablePlayer = getConfig().getBoolean("UseAt.Player");
        boolean enableMonster = getConfig().getBoolean("UseAt.Monster");
        boolean enableAnimal = getConfig().getBoolean("UseAt.Animals");
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
            df = new DecimalFormat(getConfig().getString("Format.Decimal"));
        } catch (Exception ex) {
            df = new DecimalFormat("#.#");
        }
        return df.format(o);
    }
}
