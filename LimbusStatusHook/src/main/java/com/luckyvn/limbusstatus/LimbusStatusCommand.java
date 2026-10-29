package com.luckyvn.limbusstatus;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** /limbusstatus <reload|check|stats> [player] */
public final class LimbusStatusCommand implements TabExecutor {

    private final LimbusStatusHook plugin;
    private final StatusRenderer renderer;

    public LimbusStatusCommand(LimbusStatusHook plugin, StatusRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/" + label + " reload §7- tải lại config");
            sender.sendMessage("§e/" + label + " check [player] §7- giá trị 7 status của người chơi");
            sender.sendMessage("§e/" + label + " stats [player] §7- mọi stat khác 0 (để tìm ID stat)");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            plugin.reloadAll();
            sender.sendMessage("§a[LimbusStatusHook] Đã reload config (" + renderer.config().effects.size()
                    + " status effect).");
            return true;
        }

        if (!sub.equals("check") && !sub.equals("stats")) {
            sender.sendMessage("§cDùng: /" + label + " <reload|check|stats> [player]");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cKhông thấy người chơi online: " + args[1]);
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cConsole cần chỉ rõ người chơi.");
            return true;
        }

        MMOPlayerData data = MMOPlayerData.getOrNull(target);
        if (data == null) {
            sender.sendMessage("§cMythicLib chưa có dữ liệu cho " + target.getName() + ".");
            return true;
        }
        StatMap map = data.getStatMap();

        if (sub.equals("check")) {
            sender.sendMessage("§6Status của §e" + target.getName() + "§6:");
            for (StatusEffect e : renderer.config().effects) {
                double v = renderer.value(map, e);
                sender.sendMessage(" §7- §f" + e.name() + "§7 = §e" + StatusRenderer.number(v)
                        + " §8(stat: " + String.join(", ", e.stats()) + ")");
            }
            return true;
        }

        List<StatInstance> instances = new ArrayList<>(map.getInstances());
        instances.sort(Comparator.comparing(StatInstance::getStat));
        sender.sendMessage("§6Stat khác 0 của §e" + target.getName() + "§6:");
        int shown = 0;
        for (StatInstance inst : instances) {
            double v = inst.getTotal();
            if (Math.abs(v) > 1e-9) {
                sender.sendMessage(" §7- §f" + inst.getStat() + "§7 = §e" + StatusRenderer.number(v));
                shown++;
            }
        }
        if (shown == 0) {
            sender.sendMessage(" §7(không có stat nào)");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("reload", "check", "stats")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(s);
                }
            }
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
