package com.luckyvn.limbusstatus;

import org.bukkit.plugin.java.JavaPlugin;

public final class LimbusStatusHook extends JavaPlugin {

    private StatusRenderer renderer;
    private LimbusExpansion expansion;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        renderer = new StatusRenderer(StatusConfig.load(getConfig()));

        expansion = new LimbusExpansion(this, renderer);
        if (!expansion.register()) {
            getLogger().warning("Không đăng ký được expansion 'limbus' với PlaceholderAPI.");
        }

        LimbusStatusCommand command = new LimbusStatusCommand(this, renderer);
        if (getCommand("limbusstatus") != null) {
            getCommand("limbusstatus").setExecutor(command);
            getCommand("limbusstatus").setTabCompleter(command);
        }

        getLogger().info("Đã hook MythicLib + MMOItems, đăng ký %limbus_*% với " + renderer.config().effects.size()
                + " status effect.");
    }

    @Override
    public void onDisable() {
        if (expansion != null) {
            expansion.unregister();
        }
    }

    public void reloadAll() {
        reloadConfig();
        renderer.setConfig(StatusConfig.load(getConfig()));
    }
}
