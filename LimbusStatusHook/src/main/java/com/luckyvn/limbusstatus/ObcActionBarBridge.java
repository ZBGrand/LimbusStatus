package com.luckyvn.limbusstatus;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Cầu nối tới StatsActionBar của OneBlockCore.
 *
 * OneBlockCore tự gửi Action Bar (MÁU | GIÁP) mỗi 20 tick và không có API mở rộng, nên hai nguồn gửi
 * sẽ đè nhau. Bridge này dùng reflection để:
 *  - dừng task Action Bar gốc (và dừng lại nếu /obc reload khởi động lại nó),
 *  - gọi render(Player) của chính OneBlockCore để lấy phần MÁU | GIÁP gốc,
 *  - hỏi FishingFeature.ownsActionBar để không đè lên minigame câu cá.
 * Nhờ vậy plugin chỉ cần nối thêm phần status vào cùng một dòng Action Bar.
 */
public final class ObcActionBarBridge {

    private final Plugin obc;
    private final Logger log;

    private Field barField;        // OneBlockCore.statsActionBar
    private Field taskField;       // StatsActionBar.task
    private Method renderMethod;   // StatsActionBar.render(Player)
    private Method stopMethod;     // StatsActionBar.stop()
    private Method startMethod;    // StatsActionBar.start()
    private Method fishingGetter;  // OneBlockCore.getFishingFeature()
    private Method ownsActionBar;  // FishingFeature.ownsActionBar(Player)
    private boolean available;
    private boolean fishingWarned;

    public ObcActionBarBridge(Plugin obc, Logger log) {
        this.obc = obc;
        this.log = log;
        try {
            barField = obc.getClass().getDeclaredField("statsActionBar");
            barField.setAccessible(true);
            fishingGetter = obc.getClass().getMethod("getFishingFeature");

            Class<?> barClass = Class.forName("com.luckyvn.oneblockcore.actionbar.StatsActionBar",
                    true, obc.getClass().getClassLoader());
            taskField = barClass.getDeclaredField("task");
            taskField.setAccessible(true);
            renderMethod = barClass.getDeclaredMethod("render", Player.class);
            renderMethod.setAccessible(true);
            stopMethod = barClass.getDeclaredMethod("stop");
            stopMethod.setAccessible(true);
            startMethod = barClass.getDeclaredMethod("start");
            startMethod.setAccessible(true);
            available = true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            log.warning("Không hook được StatsActionBar của OneBlockCore (" + e + "). "
                    + "Chỉ hiển thị phần status, không có MÁU/GIÁP gốc.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /** Lấy StatsActionBar hiện tại và dừng task gốc của nó nếu đang chạy. Gọi mỗi lần cập nhật. */
    public Object claim() {
        if (!available) {
            return null;
        }
        try {
            Object bar = barField.get(obc);
            if (bar != null && taskField.get(bar) != null) {
                stopMethod.invoke(bar);
            }
            return bar;
        } catch (ReflectiveOperationException e) {
            disable(e);
            return null;
        }
    }

    /** Trả quyền gửi Action Bar về cho OneBlockCore (khi tắt plugin này). */
    public void release() {
        if (!available) {
            return;
        }
        try {
            Object bar = barField.get(obc);
            if (bar != null && taskField.get(bar) == null && obc.isEnabled()) {
                startMethod.invoke(bar);
            }
        } catch (ReflectiveOperationException e) {
            log.warning("Không trả được Action Bar cho OneBlockCore: " + e);
        }
    }

    /** Phần MÁU | GIÁP gốc của OneBlockCore, rỗng nếu không lấy được. */
    public String renderBase(Object bar, Player player) {
        if (!available || bar == null) {
            return "";
        }
        try {
            Object s = renderMethod.invoke(bar, player);
            return s == null ? "" : s.toString();
        } catch (ReflectiveOperationException e) {
            disable(e);
            return "";
        }
    }

    /** true nếu minigame câu cá đang chiếm Action Bar của người chơi. */
    public boolean fishingOwns(Player player) {
        if (fishingGetter == null) {
            return false;
        }
        try {
            Object feature = fishingGetter.invoke(obc);
            if (feature == null) {
                return false;
            }
            if (ownsActionBar == null) {
                ownsActionBar = feature.getClass().getMethod("ownsActionBar", Player.class);
            }
            return (boolean) ownsActionBar.invoke(feature, player);
        } catch (ReflectiveOperationException | RuntimeException e) {
            if (!fishingWarned) {
                fishingWarned = true;
                log.warning("Không kiểm tra được ownsActionBar của câu cá: " + e);
            }
            return false;
        }
    }

    private void disable(Exception e) {
        available = false;
        log.warning("Hook Action Bar OneBlockCore bị lỗi, chuyển sang chỉ hiển thị status: " + e);
    }
}
