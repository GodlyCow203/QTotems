package dev.parrotstudios.qtotems.utils;


import dev.parrotstudios.qtotems.QTotems;
import dev.parrotstudios.qtotems.utils.taskwrappers.BukkitQTaskWrapper;
import dev.parrotstudios.qtotems.utils.taskwrappers.FoliaQTaskWrapper;
import dev.parrotstudios.qtotems.utils.taskwrappers.QTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is a manager for all methods and functions related to task schedulers, adding Folia support and removing the need keep using "try catch" statements to use the right scheduler.
 */
@SuppressWarnings("unused")
public final class QSchedulerManager {

    @Getter
    private static final boolean isFolia;
    private static final JavaPlugin plugin;


    private QSchedulerManager() {

    }

    private static boolean checkIfHasFoliaSchedulers() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static{
        plugin = QTotems.getInstance();
        isFolia = checkIfHasFoliaSchedulers();
    }

    private static JavaPlugin getPlugin() {
        return plugin;
    }

    public static QTask run(Runnable task) {
        if (isFolia) {
            return new FoliaQTaskWrapper(Bukkit.getGlobalRegionScheduler().run(getPlugin(), (scheduledTask) -> task.run()));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTask(getPlugin(), task));
        }
    }

    public static QTask runLater(Runnable task, long delay) {
        if (isFolia) {
            return new FoliaQTaskWrapper(Bukkit.getGlobalRegionScheduler().runDelayed(getPlugin(), (scheduledTask) -> task.run(), delay));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay));
        }
    }

    public static QTask runTimer(Runnable task, long delay, long period) {
        if (isFolia) {
            return new FoliaQTaskWrapper(Bukkit.getGlobalRegionScheduler().runAtFixedRate(getPlugin(), (scheduledTask) -> task.run(), delay, period));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTaskTimer(getPlugin(), task, delay, period));
        }
    }

    public static QTask runAsync(Runnable task) {
        if (isFolia) {
            return new FoliaQTaskWrapper(Bukkit.getAsyncScheduler().runNow(getPlugin(), (scheduledTask) -> task.run()));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), task));
        }
    }

    public static QTask runAtLocation(Runnable task, Location location) {
        if (isFolia) {
            return new FoliaQTaskWrapper(Bukkit.getRegionScheduler().run(getPlugin(), location, (scheduledTask) -> task.run()));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTask(getPlugin(), task));
        }
    }

    public static QTask runAtLocationLater(Runnable task, Location location, long delay) {
        if (isFolia) {
            return new FoliaQTaskWrapper(Bukkit.getRegionScheduler().runDelayed(getPlugin(), location, (scheduledTask) -> task.run(), delay));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay));
        }
    }

    public static QTask runAtEntity(Entity entity, Runnable task) {
        if (isFolia) {
            return new FoliaQTaskWrapper(entity.getScheduler().run(getPlugin(), (scheduledTask) -> task.run(), null));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTask(getPlugin(), task));
        }
    }

    public static QTask runAtEntityLater(Entity entity, Runnable task, long delay) {
        if (isFolia) {
            return new FoliaQTaskWrapper(entity.getScheduler().runDelayed(getPlugin(), (scheduledTask) -> task.run(), null, delay));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay));
        }
    }

    public static QTask runAtEntityTimer(Entity entity, Runnable task, long delay, long period) {
        if (isFolia) {
            return new FoliaQTaskWrapper(entity.getScheduler().runAtFixedRate(getPlugin(), (scheduledTask) -> task.run(), null, delay, period));
        } else {
            return new BukkitQTaskWrapper(Bukkit.getScheduler().runTaskTimer(getPlugin(), task, delay, period));
        }
    }
}