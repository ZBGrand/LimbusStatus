package com.luckyvn.limbusstatus;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class LimbusStatusHook extends JavaPlugin {

    /** Hỗ trợ &a và hex &#RRGGBB. */
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private StatusRenderer renderer;
    private ObcActionBarBridge bridge;
    private BukkitTask task;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin obc = Bukkit.getPluginManager().getPlugin("OneBlockCore");
        if (obc == null) {
            getLogger().severe("Không tìm thấy OneBlockCore, tắt plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        renderer = new StatusRenderer(StatusConfig.load(getConfig()));
        bridge = new ObcActionBarBridge(obc, getLogger());

        LimbusStatusCommand command = new LimbusStatusCommand(this, renderer);
        if (getCommand("limbusstatus") != null) {
            getCommand("limbusstatus").setExecutor(command);
            getCommand("limbusstatus").setTabCompleter(command);
        }

        startTask();
        getLogger().info("Đã hook OneBlockCore + MythicLib + MMOItems. " + renderer.config().effects.size()
                + " status effect, Action Bar OBC: " + (bridge.isAvailable() ? "đã tiếp quản" : "không hook được"));
    }

    @Override
    public void onDisable() {
        stopTask();
        if (bridge != null) {
            bridge.release();
        }
    }

    public void reloadAll() {
        reloadConfig();
        renderer.setConfig(StatusConfig.load(getConfig()));
        stopTask();
        startTask();
    }

    private void startTask() {
        stopTask();
        // Delay 1 tick: chạy trước lần gửi đầu tiên (tick 20) của OneBlockCore và dừng nó lại.
        task = Bukkit.getScheduler().runTaskTimer(this, this::tick, 1L, renderer.config().updateTicks);
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        // Luôn claim, kể cả khi tắt phần status, để Action Bar không bị gửi từ hai nguồn.
        Object bar = bridge.claim();
        StatusConfig cfg = renderer.config();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (bridge.fishingOwns(player)) {
                continue;
            }
            String base = bridge.renderBase(bar, player);
            String status = cfg.enabled ? renderer.render(player) : "";

            String line;
            if (base.isEmpty()) {
                line = status;
            } else if (status.isEmpty()) {
                line = base;
            } else {
                line = base + cfg.separator + status;
            }
            if (!line.isEmpty()) {
                player.sendActionBar(SERIALIZER.deserialize(line));
            }
        }
    }
}
