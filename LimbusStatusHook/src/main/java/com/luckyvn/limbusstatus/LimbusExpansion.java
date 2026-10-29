package com.luckyvn.limbusstatus;

import io.lumine.mythic.lib.api.stat.StatMap;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;

/** PlaceholderAPI expansion, identifier "limbus". Danh sách placeholder: xem config.yml. */
public final class LimbusExpansion extends PlaceholderExpansion {

    private final LimbusStatusHook plugin;
    private final StatusRenderer renderer;

    public LimbusExpansion(LimbusStatusHook plugin, StatusRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
    }

    @Override
    public String getIdentifier() {
        return "limbus";
    }

    @Override
    public String getAuthor() {
        return "LimbusStatusHook";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offline, String params) {
        Player player = offline == null ? null : offline.getPlayer();
        if (player == null) {
            return "";
        }
        String p = params.toLowerCase(Locale.ROOT);
        StatMap map = renderer.statMap(player);

        switch (p) {
            case "all":
                return renderer.all(map);
            case "count":
                return Integer.toString(renderer.count(map));
            default:
                break;
        }

        if (p.startsWith("stat_")) {
            return StatusRenderer.number(renderer.rawStat(map, params.substring(5)));
        }

        // <id> hoặc <id>_<suffix>
        String id = p;
        String suffix = "";
        StatusEffect effect = renderer.config().effects.get(p);
        if (effect == null) {
            int i = p.lastIndexOf('_');
            if (i > 0) {
                id = p.substring(0, i);
                suffix = p.substring(i + 1);
                effect = renderer.config().effects.get(id);
            }
        }
        if (effect == null) {
            return null; // placeholder không tồn tại
        }

        double value = renderer.value(map, effect);
        switch (suffix) {
            case "":
                return StatusRenderer.number(value);
            case "int":
                return Long.toString(Math.round(value));
            case "name":
                return effect.name();
            case "color":
                return effect.color();
            case "display":
                return renderer.display(effect, value);
            case "active":
                return Boolean.toString(Math.abs(value) > 1e-9);
            default:
                return null;
        }
    }
}
