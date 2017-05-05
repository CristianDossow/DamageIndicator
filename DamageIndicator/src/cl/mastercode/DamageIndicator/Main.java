package cl.mastercode.DamageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main
extends JavaPlugin
implements Listener {
	
    public Main plugin;
    static public Main splugin;
    File file;
    YamlConfiguration cfg;
    static public Map<ArmorStand, Long> armorStands;
    public static final ConsoleCommandSender console = Bukkit.getConsoleSender();
    
    public void reload() {
        this.file = new File(this.getDataFolder() + "/", "settings.yml");
        this.cfg = YamlConfiguration.loadConfiguration((File)this.file);
        armorStands = new HashMap<>();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        if (!this.file.exists()) {
            this.saveResource("settings.yml", false);
        }
    	System.out.println("Load Complete");
    }
    
    @Override
    public void onEnable() {
        this.plugin = this;
        splugin= this;
        
        reload();
        
        PluginCommand command1 = getCommand("DamageIndicator");
        PluginCommand command2 = getCommand("damageindicator");
        PluginCommand command3 = getCommand("di");
        command1.setExecutor(new CommandHandler());
        command2.setExecutor(new CommandHandler());
        command3.setExecutor(new CommandHandler());
        
        Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            @Override
            public void run() {
            	if(armorStands.size()>0){
            		List<ArmorStand> asl = new ArrayList<ArmorStand>();
            		for(Entry<ArmorStand, Long> entry : armorStands.entrySet()) {
            			if(entry.getValue()+1500 < System.currentTimeMillis()){
            				entry.getKey().remove();
            				asl.add(entry.getKey());
            			}
            			else{
            				entry.getKey().teleport(entry.getKey().getLocation().add(0.0, 0.07, 0.0));
            			}
            			
            		}
            		for(ArmorStand as : asl) {
            			armorStands.remove(as);
            		}
            	}
            }
        }, 1,1);
    }
    
    @Override
    public void onDisable() {
		for(Entry<ArmorStand, Long> entry : armorStands.entrySet()) {
			entry.getKey().remove();
		}
		int c =0;
		for(World world: Bukkit.getWorlds()){
    		for(ArmorStand as : world.getEntitiesByClass(org.bukkit.entity.ArmorStand.class)){
    			if(Main.splugin.isDamageIndicator(as)){
    				as.remove();
    				c++;
				}
    		}
		}
		console.sendMessage("§c"+c+" Damage Indicators were removed in plugin unload"+"");
    }
    
    public static ArmorStand getDefaultArmorStand(Location loc) {
    	ArmorStand as;
        as = (ArmorStand)loc.getWorld().spawnEntity(loc.add(0.0, 1.6, 0.0), EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setInvulnerable(true);
        as.setSmall(true);
        as.setRemoveWhenFarAway(true);
        as.setMetadata("Mastercode-DamageIndicator",new FixedMetadataValue(splugin, 1));
        as.setCustomNameVisible(true);
        as.setGravity(false);
        as.setCollidable(false);
        as.setMarker(true);
		return as;
    }
    public boolean isDamageIndicator(ArmorStand as){
    	if(as.hasMetadata("Mastercode-DamageIndicator")){
    		return true;
    	}
    	if(as.isInvulnerable()
    			&&as.isSmall()
    			&&as.isCustomNameVisible()
    			&&!as.hasGravity()
    			&&as.isMarker()
    			&&!as.isVisible()){
    		return true;
    	}
    	return false;
    }
    public boolean isOldDamageIndicator(ArmorStand as){
    	if(as.isCustomNameVisible()
    			&&!as.hasGravity()
    			&&!as.isVisible()
    			&&as.getCustomName()!=null
    			&&(as.getCustomName().contains("-")||as.getCustomName().contains("+")))
    			{
    		return true;
    	}
    	return false;
    }
    
    @EventHandler()
    public void RemoveArmorStandsOnChunkUnload(ChunkUnloadEvent event) {
    	for(Entity entity : event.getChunk().getEntities()){
        	if(entity.equals(EntityType.ARMOR_STAND)){
        		ArmorStand as = (ArmorStand)entity;
        		if(isDamageIndicator(as)){
        			armorStands.remove(as);
        			as.remove();
        		}
        	}
    	}
    }
    @EventHandler()
    public void RemoveArmorStandsOnChunkload(ChunkLoadEvent event) {
    	for(Entity entity : event.getChunk().getEntities()){
        	if(entity.equals(EntityType.ARMOR_STAND)){
        		ArmorStand as = (ArmorStand)entity;
        		if(isDamageIndicator(as)){
        			armorStands.remove(as);
        			as.remove();
        		}
        	}
    	}
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityRegenrateHealth(EntityRegainHealthEvent e) {
        ArmorStand as;
        String cfgFormat = this.cfg.getString("Format.EntityRegain");
        String displayFormat = cfgFormat.replace("&", "\u00a7").replace("%health%", "" + (int)e.getAmount());
        boolean enablePlayer = this.cfg.getBoolean("UseAt.Player");
        boolean enableMonster = this.cfg.getBoolean("UseAt.Monster");
        boolean enableAnimal = this.cfg.getBoolean("UseAt.Animals");
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

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getDamage()<1 || e.isCancelled()) {
            return;
        }
        ArmorStand as;
        String cfgFormat = this.cfg.getString("Format.EntityDamage");
        String displayFormat = cfgFormat.replace("&", "\u00a7").replace("%damage%", "" + (int)e.getDamage());
        boolean enablePlayer = this.cfg.getBoolean("UseAt.Player");
        boolean enableMonster = this.cfg.getBoolean("UseAt.Monster");
        boolean enableAnimal = this.cfg.getBoolean("UseAt.Animals");
        boolean enableBlood = this.cfg.getBoolean("BloodEffect");
        if (enableBlood && !e.isCancelled() && e.getDamage() < 1.0 && (e.getEntity().getType() != EntityType.ARMOR_STAND || e.getEntity().getType() != EntityType.PAINTING || e.getEntity().getType() != EntityType.ITEM_FRAME) && (e.getEntity() instanceof Player || e.getEntity() instanceof Monster || e.getEntity() instanceof Animals || e.getEntity() instanceof Slime || e.getEntity() instanceof MagmaCube)) {
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
}

