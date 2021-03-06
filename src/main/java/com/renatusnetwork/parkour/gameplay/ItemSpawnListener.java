package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.EventType;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemSpawnListener implements Listener {

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {

        if (Parkour.getEventManager().isEventRunning() &&
            Parkour.getEventManager().getEventType() == EventType.FALLING_ANVIL &&
            (event.getEntity() != null &&
            event.getEntity().getItemStack() != null &&
            event.getEntity().getItemStack().getType() == Material.ANVIL))
            event.setCancelled(true);
    }
}
