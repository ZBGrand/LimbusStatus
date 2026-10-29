# LimbusStatusHook

Hook **OneBlockCore + MythicLib + MMOItems**: hiển thị 7 Keyword Status Effects của Limbus Company
(Burn, Bleed, Tremor, Rupture, Sinking, Poise, Charge) trên Action Bar của OneBlockCore.

Ví dụ: `MÁU 20/20 | GIÁP 5 | Burn 3 | Bleed 2 | Charge 7/20`

## Build
Cần JDK 21 + Maven, có mạng để tải Paper API:

    mvn clean package

Jar ra ở `target/LimbusStatusHook.jar` → bỏ vào `plugins/` (OneBlockCore, MythicLib, MMOItems phải có).
`libs/MythicLib.jar` chỉ dùng để biên dịch, không đóng gói vào plugin.

## Cách hoạt động
- Giá trị lấy từ `MMOPlayerData.getStatMap().getStat(ID)` của MythicLib (MMOItems đẩy stat trang bị vào đó).
- OneBlockCore tự gửi Action Bar mỗi 20 tick và không có API mở rộng. Plugin này dừng task gốc bằng reflection,
  gọi lại `render()` của OneBlockCore để lấy phần MÁU | GIÁP, rồi nối thêm phần status vào cùng một dòng.
  Nếu `/obc reload` khởi động lại task gốc thì plugin dừng lại ở lần cập nhật kế tiếp.
- Khi người chơi đang câu cá (`FishingFeature.ownsActionBar`) plugin không gửi để không đè minigame.
- Khi tắt plugin, quyền gửi Action Bar được trả lại cho OneBlockCore.

## Cấu hình (nằm trong config.yml của OneBlockCore)
Plugin không có config riêng. Mọi thiết lập nằm ở `plugins/OneBlockCore/config.yml`, mục `actionbar.limbus`
(file mẫu `OneBlockCore-config.yml` kèm theo), bật/tắt bằng `actionbar.sections.limbus`.
Sửa xong dùng `/obc reload` (riêng `update-ticks` cần `/limbusstatus reload`).
Nếu config chưa có mục này, plugin dùng giá trị mặc định trong jar.

Mặc định mỗi status thử lần lượt `BURN`, `CUSTOM_BURN`, `LIMBUS_BURN` (tương tự cho các status khác).
Nếu bạn đặt ID khác, sửa `stats:` của status đó.

## Lệnh (quyền `limbusstatus.admin`)
- `/limbusstatus reload` – đọc lại config và khởi động lại task
- `/limbusstatus check [player]` – giá trị 7 status đang đọc được
- `/limbusstatus stats [player]` – mọi stat khác 0 của người chơi (để tìm ID stat thật)
