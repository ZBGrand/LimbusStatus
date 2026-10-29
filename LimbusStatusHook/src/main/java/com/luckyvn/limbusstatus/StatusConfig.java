package com.luckyvn.limbusstatus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Cấu hình đã parse từ config.yml. */
public final class StatusConfig {

    public final boolean enabled;
    public final int updateTicks;
    public final String separator;
    public final boolean hideZero;
    public final List<StatusEffect> effects;

    private StatusConfig(boolean enabled, int updateTicks, String separator, boolean hideZero,
                         List<StatusEffect> effects) {
        this.enabled = enabled;
        this.updateTicks = updateTicks;
        this.separator = separator;
        this.hideZero = hideZero;
        this.effects = effects;
    }

    public static StatusConfig load(FileConfiguration cfg) {
        boolean enabled = cfg.getBoolean("action-bar.enabled", true);
        int ticks = Math.max(1, cfg.getInt("action-bar.update-ticks", 10));
        String separator = cfg.getString("action-bar.separator", " &7| ");
        boolean hideZero = cfg.getBoolean("action-bar.hide-zero", true);
        String defaultFormat = cfg.getString("action-bar.default-format", "{color}{name} {value}");

        List<StatusEffect> effects = new ArrayList<>();
        ConfigurationSection section = cfg.getConfigurationSection("status-effects");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection s = section.getConfigurationSection(key);
                if (s == null || !s.getBoolean("enabled", true)) {
                    continue;
                }
                List<String> stats = new ArrayList<>();
                for (String id : s.getStringList("stats")) {
                    if (id != null && !id.isBlank()) {
                        stats.add(id.trim().toUpperCase(Locale.ROOT));
                    }
                }
                if (stats.isEmpty()) {
                    stats.add(key.toUpperCase(Locale.ROOT));
                }
                effects.add(new StatusEffect(
                        key,
                        s.getString("name", key),
                        s.getString("color", "&f"),
                        List.copyOf(stats),
                        s.getString("format", defaultFormat)));
            }
        }
        return new StatusConfig(enabled, ticks, separator, hideZero, List.copyOf(effects));
    }
}
