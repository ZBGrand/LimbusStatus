package com.luckyvn.limbusstatus;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Cấu hình đã parse từ config.yml. */
public final class StatusConfig {

    public final boolean hideZero;
    public final String separator;
    public final String empty;
    /** Theo thứ tự trong config, khoá là id viết thường. */
    public final Map<String, StatusEffect> effects;

    private StatusConfig(boolean hideZero, String separator, String empty, Map<String, StatusEffect> effects) {
        this.hideZero = hideZero;
        this.separator = separator;
        this.empty = empty;
        this.effects = effects;
    }

    public static StatusConfig load(ConfigurationSection cfg) {
        boolean hideZero = cfg.getBoolean("placeholders.hide-zero", true);
        String separator = cfg.getString("placeholders.separator", " &7| ");
        String empty = cfg.getString("placeholders.empty", "");
        String defaultFormat = cfg.getString("placeholders.format", "{color}{name} {value}");

        Map<String, StatusEffect> effects = new LinkedHashMap<>();
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
                String id = key.toLowerCase(Locale.ROOT);
                effects.put(id, new StatusEffect(
                        id,
                        s.getString("name", key),
                        s.getString("color", "&f"),
                        List.copyOf(stats),
                        s.getString("format", defaultFormat)));
            }
        }
        return new StatusConfig(hideZero, separator, empty, java.util.Collections.unmodifiableMap(effects));
    }
}
