# LimbusStatusHook

Hook **MMOItems + MythicLib**, cung cấp **PlaceholderAPI** (`%limbus_...%`) cho 7 Keyword Status Effects
của Limbus Company: Burn, Bleed, Tremor, Rupture, Sinking, Poise, Charge.
Không còn hook OneBlockCore; bạn tự dùng placeholder để dựng bảng (scoreboard, TAB, DeluxeMenus, hologram...).

## Build
JDK 21 + Maven, có mạng để tải Paper API và PlaceholderAPI:

    mvn clean package

Jar ra ở `target/LimbusStatusHook.jar` → bỏ vào `plugins/` (cần MythicLib, MMOItems, PlaceholderAPI).
`libs/MythicLib.jar` chỉ dùng để biên dịch.

## Placeholder (identifier `limbus`)
`<id>` = burn, bleed, tremor, rupture, sinking, poise, charge (khoá trong `status-effects` của config.yml).

| Placeholder | Kết quả |
|---|---|
| `%limbus_<id>%` | giá trị (3, 2.5...), 0 nếu không có |
| `%limbus_<id>_int%` | giá trị làm tròn |
| `%limbus_<id>_name%` | tên hiển thị |
| `%limbus_<id>_color%` | mã màu |
| `%limbus_<id>_display%` | chuỗi đã định dạng, rỗng nếu = 0 |
| `%limbus_<id>_active%` | true / false |
| `%limbus_all%` | mọi status đang có, nối bằng separator |
| `%limbus_count%` | số status đang có |
| `%limbus_stat_<STAT>%` | giá trị bất kỳ stat MythicLib |

## Cấu hình
`plugins/LimbusStatusHook/config.yml`: mỗi status có `name`, `color`, `stats` (ID stat thử lần lượt),
`format`, `enabled`. Mặc định thử `BURN`, `CUSTOM_BURN`, `LIMBUS_BURN`... Sửa cho khớp stat bạn đã tạo.

## Lệnh (quyền `limbusstatus.admin`)
- `/limbusstatus reload` – tải lại config
- `/limbusstatus check [player]` – giá trị 7 status đang đọc được
- `/limbusstatus stats [player]` – mọi stat khác 0 (để tìm ID stat thật)
