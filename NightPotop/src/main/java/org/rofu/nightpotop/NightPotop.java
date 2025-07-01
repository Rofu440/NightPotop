package org.rofu.nightpotop;

import org.bukkit.plugin.java.JavaPlugin;

public class NightPotop extends JavaPlugin {

    private PotopManager potopManager;

    @Override
    public void onEnable() {
        potopManager = new PotopManager(this);
        getCommand("np").setExecutor(new PotopCommand(this, potopManager));

        getLogger().info("Плагин NightPotop успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (potopManager != null && potopManager.isPotopActive()) {
            potopManager.stopPotop();
        }

        getLogger().info("Плагин NightPotop выключен!");
    }
}