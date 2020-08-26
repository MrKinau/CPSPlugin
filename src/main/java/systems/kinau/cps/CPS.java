package systems.kinau.cps;

import org.bukkit.plugin.java.JavaPlugin;

public class CPS extends JavaPlugin {

    public static final String PREFIX = "§9CPS §7┃ ";

    @Override
    public void onEnable() {
        CPSManager cpsManager = new CPSManager(this);

        getCommand("cps").setExecutor(new CPSCommand(cpsManager, this));
    }

}
