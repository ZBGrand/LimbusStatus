package com.luckyvn.limbusstatus;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Đọc stat từ MythicLib và dựng chuỗi legacy (&-code) cho phần status. */
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

    /** Giá trị của một status: ID stat đầu tiên có giá trị khác 0, hoặc 0. */
    public double value(StatMap map, StatusEffect effect) {
        for (String id : effect.stats()) {
            double v = map.getStat(id);
            if (Double.isFinite(v) && Math.abs(v) > 1e-9) {
                return v;
            }
        }
        return 0;
    }

    /** Chuỗi status của người chơi, rỗng nếu không có status nào để hiện. */
    public String render(Player player) {
        StatusConfig cfg = this.config;
        MMOPlayerData data = MMOPlayerData.getOrNull(player);
        if (data == null) {
            return "";
        }
        StatMap map = data.getStatMap();
        List<String> parts = new ArrayList<>();
        for (StatusEffect effect : cfg.effects) {
            double v = value(map, effect);
            if (cfg.hideZero && Math.abs(v) <= 1e-9) {
                continue;
            }
            parts.add(effect.format()
                    .replace("{color}", effect.color())
                    .replace("{name}", effect.name())
                    .replace("{value}", number(v)));
        }
        return String.join(cfg.separator, parts);
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
