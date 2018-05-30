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
package cl.mastercode.DamageIndicator;

import cl.mastercode.DamageIndicator.command.CommandHandler;
import cl.mastercode.DamageIndicator.listener.BloodListener;
import cl.mastercode.DamageIndicator.listener.DamageIndicatorListener;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author YitanTribal, Beelzebu
 */
public class Main extends JavaPlugin {

    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private DamageIndicatorListener damageIndicatorListener;
    private BloodListener bloodListener;

    public void reload() {
        if (damageIndicatorListener != null) {
            damageIndicatorListener.getArmorStands().forEach((armor, time) -> armor.remove());
            damageIndicatorListener.getArmorStands().clear();
        }
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    @Override
    public void onEnable() {
        reload();
        damageIndicatorListener = new DamageIndicatorListener(this);
        bloodListener = new BloodListener(this);
        Bukkit.getPluginManager().registerEvents(damageIndicatorListener, this);
        Bukkit.getPluginManager().registerEvents(bloodListener, this);
        getCommand("damageindicator").setExecutor(new CommandHandler(this));

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Iterator<Map.Entry<ArmorStand, Long>> asit = damageIndicatorListener.getArmorStands().entrySet().iterator();
            while (asit.hasNext()) {
                Map.Entry<ArmorStand, Long> ent = asit.next();
                if (ent.getValue() + 1500 <= System.currentTimeMillis()) {
                    ent.getKey().remove();
                    asit.remove();
                } else {
                    ent.getKey().teleport(ent.getKey().getLocation().clone().add(0.0, 0.07, 0.0));
                }
            }
            Iterator<Map.Entry<Item, Long>> bit = bloodListener.getBloodItems().entrySet().iterator();
            while (bit.hasNext()) {
                Map.Entry<Item, Long> ent = bit.next();
                if (ent.getValue() + 2000 <= System.currentTimeMillis()) {
                    ent.getKey().remove();
                    bit.remove();
                }
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        damageIndicatorListener.getArmorStands().forEach((armor, time) -> armor.remove());
        bloodListener.getBloodItems().forEach((item, time) -> item.remove());
    }

    public boolean isDamageIndicator(ArmorStand as) {
        return as.hasMetadata("Mastercode-DamageIndicator");
    }
}
