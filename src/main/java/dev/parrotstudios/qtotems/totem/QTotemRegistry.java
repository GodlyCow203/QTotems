package dev.parrotstudios.qtotems.totem;

import dev.parrotstudios.qtotems.QTotems;
import dev.parrotstudios.qtotems.config.ConfigManager;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class QTotemRegistry {
    private static final List<QTotem> qTotems = new ArrayList<>();
    private static BukkitTask TASK;

    private static final HashMap<UUID, QTotem> activePlayerEquips = new HashMap<>();

    public static void startUp(){
        TASK = QTotems.getInstance().getServer().getScheduler().runTaskTimer(QTotems.getInstance(), QTotemRegistry::checkActiveEquips, 0L, 100L);

    }

    public static List<QTotem> getQTotems() {
        return new ArrayList<>(qTotems);
    }

    public static void add(QTotem qTotem) {
        qTotems.add(qTotem);
    }


    public static List<String> getTotemNames() {
        return qTotems.stream().map(QTotem::getName).toList();
    }

    public static Map<UUID, QTotem> getActivePlayerEquips() {
        return Map.copyOf(activePlayerEquips);
    }

    public static boolean isQTotem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        return qTotems.stream().map(QTotem::getKey).anyMatch(pdc::has);
    }


    public static void clearPastEffects(Player player) {
        QTotem active = activePlayerEquips.remove(player.getUniqueId());
        if (active != null) {
            active.removeEquipEffects(player);
        }
    }

    public static void handleEquip(Player player, ItemStack stack) {
        clearPastEffects(player);
        if (!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainerView pdc = stack.getPersistentDataContainer();
            return pdc.has(qTotem1.getKey());
        }).findFirst();
        if (qTotem.isEmpty()) return;

        QTotem totem = qTotem.get();
        totem.provideEquipEffects(player);
        activePlayerEquips.put(player.getUniqueId(), totem);
    }

    public static void handlePop(Player player, ItemStack stack) {
        if (!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainerView pdc = stack.getPersistentDataContainer();
            return pdc.has(qTotem1.getKey());
        }).findFirst();
        if (qTotem.isEmpty()) return;
        QTotems.getInstance().getServer().getScheduler().runTaskLater(QTotems.getInstance(), () ->
                qTotem.get().providePopEffects(player), 1L);
        activePlayerEquips.remove(player.getUniqueId(), qTotem.get());
    }

    public static void handleLeave(Player player) {
        clearPastEffects(player);
    }

    public static void handleJoin(Player player) {
        handleEquip(player, player.getInventory().getItemInOffHand());
    }

    public static void checkActiveEquips() {
        List<UUID> activeUuids = new ArrayList<>(activePlayerEquips.keySet());
        for (UUID uuid : activeUuids) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player == null) continue;

            ItemStack stack = player.getInventory().getItemInOffHand();
            QTotem active = activePlayerEquips.get(uuid);

            if (isQTotem(stack)) {
                Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
                    PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
                    return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
                }).findFirst();

                if (qTotem.isPresent()) {
                    QTotem totem = qTotem.get();
                    if (totem == active) {
                        totem.provideEquipEffects(player);
                    } else {
                        handleEquip(player, stack);
                    }
                } else {
                    clearPastEffects(player);
                }
            } else {
                clearPastEffects(player);
            }
        }
    }

    public static QTotem getTotem(String totemName) {
        return getQTotems().stream().filter(qTotem -> qTotem.getName().equalsIgnoreCase(totemName)).findFirst().orElse(null);
    }

    public static void populate() {
        ConfigManager.getSection("totems").getKeys(false).forEach(qTotem -> {
            try {
                if (!ConfigManager.getBoolean("totems." + qTotem + ".enabled", true)) {
                    return;
                }
                QTotem totem = QTotem.create(qTotem)
                        .displayName(ConfigManager.getString("totems." + qTotem + ".name"))
                        .lore(ConfigManager.getStringList("totems." + qTotem + ".lore"));
                List<String> popEffects = ConfigManager.getStringList("totems." + qTotem + ".popEffects");
                List<String> equipEffects = ConfigManager.getStringList("totems." + qTotem + ".equipEffects");
                popEffects.forEach(effect -> {
                    String[] split = effect.split(";");
                    totem.addPopEffect(split[0].toLowerCase(), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                });
                equipEffects.forEach(effect -> {
                    String[] split = effect.split(";");
                    totem.addEquipEffect(split[0].toLowerCase(), Integer.parseInt(split[1]));
                });
                totem.register();
                QTotems.getInstance().getLogger().info("Registered Qtotem: " + qTotem);
            } catch (Exception e) {
                QTotems.getInstance().getLogger().warning("Invalid configuration for Qtotem: " + qTotem);
                e.printStackTrace();
            }

        });
    }

    public static void handleDisable() {
        if(TASK != null) TASK.cancel();
        qTotems.clear();
        getActivePlayerEquips().forEach((uuid, _) -> {
            Player player = QTotems.getInstance().getServer().getPlayer(uuid);
            if (player != null) {
                clearPastEffects(player);
            }
        });
        activePlayerEquips.clear();
    }

    public static void reload() {
        qTotems.clear();
        populate();
        getActivePlayerEquips().forEach((uuid, _) -> {
            Player player = QTotems.getInstance().getServer().getPlayer(uuid);
            if (player != null) {
                handleEquip(player, player.getInventory().getItemInOffHand());
            }
        });
    }
}
