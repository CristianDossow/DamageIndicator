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
package cl.mastercode.DamageIndicator.storage;

import org.bukkit.entity.Player;

/**
 *
 * @author Beelzebu
 */
public interface StorageProvider {

    /**
     * Check if a player should see a armor stand or not.
     *
     * @param player player to check.
     * @return true if the player can see the armor stand, false otherwhise.
     */
    public boolean showArmorStand(Player player);

    /**
     * Set the armorstand show status for a player.
     *
     * @param player player to set the status.
     * @param status new status.
     */
    public void setShowArmorStand(Player player, boolean status);

}
