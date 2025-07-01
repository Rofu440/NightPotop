package org.rofu.nightpotop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotopManager {

    private final NightPotop plugin;
    private BukkitTask potopTask;
    private boolean potopActive = false;
    private int currentWaterLevel = 0;
    private final int MAX_WATER_LEVEL = 256;

    private final Map<String, List<Block>> originalBlocks = new HashMap<>();

    public PotopManager(NightPotop plugin) {
        this.plugin = plugin;
    }

    public boolean isPotopActive() {
        return potopActive;
    }

    public void startPotop() {
        if (potopActive) {
            return;
        }

        potopActive = true;
        currentWaterLevel = 0;

        originalBlocks.clear();

        potopTask = Bukkit.getScheduler().runTaskTimer(plugin, this::raiseWaterLevel, 0L, 1200L); // 3 минуты = 3600 тиков
    }

    public void stopPotop() {
        if (!potopActive) {
            return;
        }

        if (potopTask != null) {
            potopTask.cancel();
            potopTask = null;
        }

        potopActive = false;

        restoreOriginalBlocks();
    }

    private void raiseWaterLevel() {
        if (currentWaterLevel >= MAX_WATER_LEVEL) {
            Bukkit.broadcastMessage(ChatColor.RED + "Потоп достиг максимальной высоты!");
            return;
        }

        currentWaterLevel++;

        for (World world : Bukkit.getWorlds()) {
            fillWaterInWorld(world, currentWaterLevel);
        }

        Bukkit.broadcastMessage(ChatColor.BLUE + "Уровень воды поднялся до высоты " + currentWaterLevel);
    }

    private void fillWaterInWorld(World world, int waterLevel) {
        int worldBorderSize = (int) world.getWorldBorder().getSize() / 2;
        int centerX = (int) world.getWorldBorder().getCenter().getX();
        int centerZ = (int) world.getWorldBorder().getCenter().getZ();

        int chunkRadius = Math.min(worldBorderSize, 10000) / 16;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Block> blocksToChange = new ArrayList<>();

            // Перебираем чанки вокруг спавна
            for (int chunkX = -chunkRadius; chunkX <= chunkRadius; chunkX++) {
                for (int chunkZ = -chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
                    if (!world.isChunkLoaded(chunkX + centerX / 16, chunkZ + centerZ / 16)) {
                        continue;
                    }

                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int blockX = (chunkX * 16) + x + centerX - (centerX % 16);
                            int blockZ = (chunkZ * 16) + z + centerZ - (centerZ % 16);

                            Block block = world.getBlockAt(blockX, waterLevel, blockZ);

                            if (block.getType() == Material.AIR) {
                                blocksToChange.add(block);

                                String key = world.getName() + "," + blockX + "," + waterLevel + "," + blockZ;
                                originalBlocks.computeIfAbsent(key, k -> new ArrayList<>()).add(block);
                            }
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Block block : blocksToChange) {
                    block.setType(Material.WATER);
                }
            });
        });
    }

    private void restoreOriginalBlocks() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (List<Block> blocks : originalBlocks.values()) {
                for (Block block : blocks) {
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.AIR);
                    }
                }
            }

            Bukkit.broadcastMessage(ChatColor.GREEN + "Вода вернулась к исходному уровню!");
        });

        originalBlocks.clear();
    }
}
