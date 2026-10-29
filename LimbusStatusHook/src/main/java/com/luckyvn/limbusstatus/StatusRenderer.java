package com.luckyvn.limbusstatus;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Đọc stat từ MythicLib và dựng chuỗi hiển thị cho PlaceholderAPI. */
public final class StatusRenderer {

    private volatile StatusConfig config;

    public StatusRenderer(StatusConfig config) {
        this.config = config;
    }

    public void setConfig(StatusConfig config) {
        this.config = config;
    }

    public StatusConfig config() {
        return config;
    }

    /** StatMap của người chơi, hoặc null nếu MythicLib chưa có dữ liệu. */
    public StatMap statMap(Player player) {
        MMOPlayerData data = MMOPlayerData.getOrNull(player);
        return data == null ? null : data.getStatMap();
    }

    /** Giá trị của một status: ID stat đầu tiên có giá trị khác 0, hoặc 0. */
    public double value(StatMap map, StatusEffect effect) {
        if (map == null) {
            return 0;
        }
        for (String id : effect.stats()) {
            double v = map.getStat(id);
            if (Double.isFinite(v) && Math.abs(v) > 1e-9) {
                return v;
            }
        }
        return 0;
    }

    /** Giá trị của một stat MythicLib bất kỳ. */
    public double rawStat(StatMap map, String statId) {
        if (map == null) {
            return 0;
        }
        double v = map.getStat(statId.toUpperCase(Locale.ROOT));
        return Double.isFinite(v) ? v : 0;
    }

    /** Chuỗi đã định dạng; rỗng nếu hide-zero và giá trị = 0. */
    public String display(StatusEffect effect, double value) {
        if (config.hideZero && Math.abs(value) <= 1e-9) {
            return "";
        }
        return effect.format()
                .replace("{color}", effect.color())
                .replace("{name}", effect.name())
                .replace("{value}", number(value));
    }

    /** Mọi status đang có, nối bằng separator. */
    public String all(StatMap map) {
        List<String> parts = new ArrayList<>();
        for (StatusEffect effect : config.effects.values()) {
            double v = value(map, effect);
            if (Math.abs(v) <= 1e-9) {
                continue;
            }
            parts.add(display(effect, v));
        }
        return parts.isEmpty() ? config.empty : String.join(config.separator, parts);
    }

    public int count(StatMap map) {
        int n = 0;
        for (StatusEffect effect : config.effects.values()) {
            if (Math.abs(value(map, effect)) > 1e-9) {
                n++;
            }
        }
        return n;
    }

    /** Số nguyên nếu tròn, ngược lại tối đa 2 chữ số thập phân. */
    public static String number(double v) {
        if (!Double.isFinite(v)) {
            return "0";
        }
        if (Math.abs(v - Math.rint(v)) < 1e-9) {
            return Long.toString(Math.round(v));
        }
        String s = String.format(Locale.US, "%.2f", v);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }
}
