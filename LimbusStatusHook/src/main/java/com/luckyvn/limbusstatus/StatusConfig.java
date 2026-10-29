package com.luckyvn.limbusstatus;

import org.bukkit.configuration.ConfigurationSection;

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

    /**
     * @param actionbar mục {@code actionbar} trong config.yml của OneBlockCore (có thể null)
     * @param defaults  mục mặc định nằm trong jar, dùng khi OneBlockCore chưa có {@code actionbar.limbus}
     */
    public static StatusConfig load(ConfigurationSection actionbar, ConfigurationSection defaults) {
        ConfigurationSection limbus = actionbar == null ? null : actionbar.getConfigurationSection("limbus");
        if (limbus == null) {
            limbus = defaults.getConfigurationSection("limbus");
        }
        boolean enabled = actionbar == null || actionbar.getBoolean("sections.limbus", true);
        String separator = actionbar == null ? " &7| " : actionbar.getString("separator", " &7| ");
        int ticks = Math.max(1, limbus.getInt("update-ticks", 10));
        boolean hideZero = limbus.getBoolean("hide-zero", true);
        String defaultFormat = limbus.getString("format", "{color}{name} {value}");

        List<StatusEffect> effects = new ArrayList<>();
        ConfigurationSection section = limbus.getConfigurationSection("effects");
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
