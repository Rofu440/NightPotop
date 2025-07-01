package org.rofu.nightpotop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PotopCommand implements CommandExecutor {

    private final NightPotop plugin;
    private final PotopManager potopManager;

    public PotopCommand(NightPotop plugin, PotopManager potopManager) {
        this.plugin = plugin;
        this.potopManager = potopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("np.potop")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
            return true;
        }

        if (potopManager.isPotopActive()) {
            potopManager.stopPotop();
            sender.sendMessage(ChatColor.RED + "Потоп остановлен! Вода возвращается на прежний уровень...");
        } else {
            potopManager.startPotop();
            sender.sendMessage(ChatColor.GREEN + "Потоп активирован! Вода будет подниматься каждые 3 минуты.");
        }

        return true;
    }
}
