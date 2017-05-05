package cl.mastercode.DamageIndicator;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class CommandHandler implements CommandExecutor {

	String noPermission= ChatColor.DARK_RED + "You don't have permission to do this!";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] strings) {
        if(strings.length>0){
	        if ("reload".equalsIgnoreCase(strings[0])) {
	            if (CheckPermissions(sender,"damageindicator.admin")) {
	            	Main.splugin.reload();
		            sender.sendMessage(ChatColor.GREEN + "Reloaded config!");
		            return true;
	            }
	        }else if ("clear".equalsIgnoreCase(strings[0])) {
	        	if(IsPlayer(sender)&&
	        			(CheckPermissions(sender,"damageindicator.clear")||CheckPermissions(sender,"damageindicator.admin"))
	        			&&CheckArguments(sender, strings.length, 2)){
	        		int range = GetInt(sender, strings[1]);
	        		if(range>=0){
	        			int c =0;
	        			for (Iterator<Entity> iterator = ((Player)sender).getNearbyEntities(range, range, range).iterator(); iterator.hasNext();) {
	        				Entity entity = (Entity) iterator.next();
	        				if(entity instanceof ArmorStand && Main.splugin.isDamageIndicator((ArmorStand)entity)){
	        					entity.remove();
	        					c++;
	        				}
	        			}
	        			sender.sendMessage(ChatColor.GREEN +""+c+" Damage Indicators were removed");
	        			return true;
	        		}
	        	}
	        }else if ("clearall".equalsIgnoreCase(strings[0])) {
	        	if(IsPlayer(sender)&&(CheckPermissions(sender,"damageindicator.admin"))){
	        		int c =0;
	        		for(ArmorStand as : ((Player)sender).getLocation().getWorld().getEntitiesByClass(org.bukkit.entity.ArmorStand.class)){
	        			if(Main.splugin.isDamageIndicator(as)){
	        				as.remove();
        					c++;
        				}
	        		}
	        		sender.sendMessage(ChatColor.GREEN +""+c+" Damage Indicators were removed in "+((Player)sender).getLocation().getWorld().getName());
	        		return true;
	        	}
	        }else if ("debug".equalsIgnoreCase(strings[0])) {
	        	if(IsPlayer(sender)&&
	        			(CheckPermissions(sender,"damageindicator.admin"))
	        			&&CheckArguments(sender, strings.length, 2)){
	        		int range = GetInt(sender, strings[1]);
	        		if(range>=0){
	        			for (Iterator<Entity> iterator = ((Player)sender).getNearbyEntities(range, range, range).iterator(); iterator.hasNext();) {
	        				Entity entity = (Entity) iterator.next();
	        				if(entity instanceof ArmorStand){
	        					ArmorStand as=(ArmorStand)entity;
	        					sender.sendMessage("Dysplay Name: "+as.getCustomName());
	        					sender.sendMessage("isSmall (true) "+as.isSmall());
	        					sender.sendMessage("isCustomNameVisible (true) "+as.isCustomNameVisible());
	        					sender.sendMessage("hasGravity (false) "+as.hasGravity());
	        					sender.sendMessage("isCollidable (false) "+as.isCollidable());
	        					sender.sendMessage("iisMarker (true) "+as.isMarker());
	        					sender.sendMessage("isVisible (false) "+as.isVisible());
	        					sender.sendMessage("isInvulnerable (true) "+as.isInvulnerable());
	        					sender.sendMessage("getRemoveWhenFarAway (true) "+as.getRemoveWhenFarAway());
	        				}
	        			}
	        		}
	        	}
	        }
        }else{
        	sender.sendMessage(ChatColor.DARK_AQUA+"<===== Damage Indicator =====>");
        	sender.sendMessage(ChatColor.DARK_AQUA+"/di reload");
        	sender.sendMessage(ChatColor.DARK_AQUA+"/di clear <range> "+ChatColor.AQUA+"#remove the damage indicators in the range");
        	sender.sendMessage(ChatColor.DARK_AQUA+"/di clearall "+ChatColor.AQUA+"#remove the damage indicators in the world (may cause lag)");
        }
        return true;
	}
	public static boolean IsPlayer(CommandSender sender){
        if (sender instanceof Player) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Only a Player can use this command");
		return false;
	}
	public static boolean CheckPermissions(CommandSender sender,String permission){
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "You don't have permission to use this command this!");
		return false;
	}
	public static boolean CheckArguments(CommandSender sender,int args ,int num){
		if(args!=num){
			sender.sendMessage(ChatColor.RED+"Invalid number of arguments");
			return false;
		}
		return true;
	}
	public static int GetInt(CommandSender sender, String text){
		int amount = -1;
		try{
			amount = Integer.parseInt(text);
			return amount;
		}catch(NumberFormatException e){
			sender.sendMessage(ChatColor.RED+"Invalid given amount");
			return -1;
		}
	}
	
	
}
