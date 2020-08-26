package systems.kinau.cps;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;


public class CPSManager implements Listener, Runnable {

    private final Multimap<UUID, Long> leftClicks = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    private final Multimap<UUID, Long> rightClicks = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    private BukkitTask task;

    public CPSManager(CPS plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        enable(plugin);
    }

    public int getLastSec(UUID uuid, ClickType clickType) {
        Multimap<UUID, Long> map = leftClicks;
        if (clickType == ClickType.RIGHT_CLICK)
            map = rightClicks;

        if (!map.containsKey(uuid))
            return 0;
        return Long.valueOf(map.get(uuid).stream()
                .filter(timestamp -> timestamp >= (System.currentTimeMillis() - 1000))
                .count()).intValue();
    }

    private void enable(CPS plugin) {
        if (task != null)
            task.cancel();

        //Delete old values thread
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 40L, 40L); //Just in case let a puffer
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        leftClicks.removeAll(event.getPlayer().getUniqueId());
        rightClicks.removeAll(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            rightClicks.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
            leftClicks.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }


    @Override
    public void run() {
        Set<UUID> uuids = new HashSet<>(leftClicks.keySet());
        for (UUID uuid : uuids) {
            List<Long> clicks = new ArrayList<>(leftClicks.get(uuid));
            clicks.removeIf(value -> value < (System.currentTimeMillis() - 1000));
            leftClicks.removeAll(uuid);
            leftClicks.putAll(uuid, clicks);
        }

        uuids = new HashSet<>(rightClicks.keySet());
        for (UUID uuid : uuids) {
            List<Long> clicks = new ArrayList<>(rightClicks.get(uuid));
            clicks.removeIf(value -> value < (System.currentTimeMillis() - 1000));
            rightClicks.removeAll(uuid);
            rightClicks.putAll(uuid, clicks);
        }
    }
}
