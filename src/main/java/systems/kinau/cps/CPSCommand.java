package systems.kinau.cps;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CPSCommand implements CommandExecutor, Listener {

    private final CPSManager cpsManager;
    private final CPS plugin;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public CPSCommand(CPSManager cpsManager, CPS plugin) {
        this.cpsManager = cpsManager;
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBrudi, du musst schon nen' Spieler sein. Die CPS der Konsole sind nicht messbar. Sad life :/");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("off")) {
                if (!tasks.containsKey(player.getUniqueId())) {
                    player.sendMessage(CPS.PREFIX + "§cDu überprüfst aktuell keine CPS");
                    return true;
                }
                tasks.get(player.getUniqueId()).cancel();
                tasks.remove(player.getUniqueId());
                player.sendMessage(CPS.PREFIX + "§aCPS-Tracking beendet!");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(CPS.PREFIX + "§cDer Spieler §4" + args[0] + " §cist nicht online!");
                return true;
            }

            if (tasks.containsKey(player.getUniqueId())) {
                tasks.get(player.getUniqueId()).cancel();
                tasks.remove(player.getUniqueId());
            }

            tasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                int ping = ((CraftPlayer)target).getHandle().ping;
                int leftClicks = cpsManager.getLastSec(target.getUniqueId(), ClickType.LEFT_CLICK);
                int rightClicks = cpsManager.getLastSec(target.getUniqueId(), ClickType.RIGHT_CLICK);
                player.sendMessage(CPS.PREFIX + "§3Ping: " + colorizePing(ping) + " §7- §3Links: §e" + leftClicks + " §7- §3Rechts: §e" + rightClicks);
            }, 0, 20L));

            return true;
        } else
            player.sendMessage(CPS.PREFIX + "§c/cps <Spieler>");

        return true;
    }

    private String colorizePing(int ping) {
        if (ping <= 10)
            return "§2" + ping;
        else if (ping <= 30)
            return "§a" + ping;
        else if (ping <= 75)
            return "§e" + ping;
        else if (ping <= 120)
            return "§c" + ping;
        return "§4" + ping;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (tasks.containsKey(event.getPlayer().getUniqueId()))
            tasks.get(event.getPlayer().getUniqueId()).cancel();
        tasks.remove(event.getPlayer().getUniqueId());
    }

}
