# MMO Skill System Template (Forge 1.20.1) – Hướng dẫn sử dụng (Tiếng Việt)

Template này được thêm vào dự án của bạn dưới dạng **data-driven** (định nghĩa skill bằng JSON) + một số **FX task** mẫu.
Mục tiêu: **dễ thêm skill mới**, có **hệ (element)**, **damage/on-hit effect**, **âm thanh**, **camera shake**, **flash màn hình**, và **auto gắn skill vào vũ khí** qua JSON.

## 1) Bạn vừa được thêm những gì?

### Code (Java)

- `com.main.slashing.mmo.skill.MmoSkillReloadListener`
  - Load skill từ `data/<modid>/skills/*.json`
  - Khi `/reload` sẽ tự cập nhật lại skill (không cần restart).
- `com.main.slashing.mmo.weapon.WeaponSkillReloadListener`
  - Load mapping **vũ khí -> danh sách skill** từ `data/<modid>/weapon_skills/*.json`.
- `com.main.slashing.mmo.fx.ArcSweepHitFxTask`
  - Mẫu "skill MMO" dạng **arc sweep**: vừa FX vừa hit detection + on-hit FX.
- `com.main.slashing.client.ClientScreenFlash` + packet `S2C_ScreenFlashPacket`
  - Hiệu ứng **flash toàn màn hình**.

### Data (JSON)

- Ví dụ skill: `data/slashing_alphad/skills/mmo_template_ember_sweep.json`
- Ví dụ gắn skill vào vũ khí: `data/slashing_alphad/weapon_skills/template_slash_wand.json`

> Lưu ý: Trong code template, `SkillManager` đã được nâng cấp để **merge** skill từ:
> - Item có implement `IWeaponSkills`
> - Và mapping JSON trong `WeaponSkillRegistry`
>
> Nhờ đó bạn có thể "auto add" skill cho vũ khí **mà không cần sửa class Item**.

---

## 2) Tạo skill mới nhanh nhất (chỉ cần JSON)

### Bước 1 – Tạo file skill JSON

Tạo file mới:

```
src/main/resources/data/slashing_alphad/skills/<ten_skill>.json
```

Ví dụ (tối giản):

```json
{
  "name_key": "skill.slashing_alphad.my_new_skill",
  "cooldown_ticks": 20,
  "element": "lightning",
  "actions": [
    {
      "type": "arc_sweep",
      "duration": 7,
      "hit": { "damage": 6.0 }
    }
  ]
}
```

### Bước 2 – Thêm tên skill vào file ngôn ngữ

- `src/main/resources/assets/slashing_alphad/lang/vi_vn.json`
- `src/main/resources/assets/slashing_alphad/lang/en_us.json`

Ví dụ:

```json
"skill.slashing_alphad.my_new_skill": "Skill Mới - Sét Chém"
```

### Bước 3 – Gắn skill vào vũ khí (auto)

Tạo file:

```
src/main/resources/data/slashing_alphad/weapon_skills/<ten_mapping>.json
```

Ví dụ gắn vào **một item cụ thể**:

```json
{
  "match": {
    "items": ["minecraft:diamond_sword"]
  },
  "skills": [
    "slashing_alphad:my_new_skill"
  ],
  "default": "slashing_alphad:my_new_skill"
}
```

Ví dụ gắn vào **cả một tag item** (rất tiện):

```json
{
  "match": {
    "tags": ["minecraft:swords"]
  },
  "skills": [
    "slashing_alphad:my_new_skill"
  ]
}
```

Sau khi vào game, dùng phím:

- `G` cast
- `R/Z` chuyển skill

---

## 3) Tùy biến: pattern FX, âm thanh, damage, hiệu ứng hit, element, camera shake, flash

### 3.1 Các field quan trọng trong skill JSON

| Field | Ý nghĩa |
|---|---|
| `name_key` | key dịch (lang) |
| `cooldown_ticks` | cooldown theo tick (20 tick = 1s) |
| `element` | hệ: `physical/fire/ice/lightning/water/wind/earth/holy/dark` |
| `cast_sound` | sound khi cast |
| `camera_shake` | rung camera khi cast |
| `screen_flash` | flash màn hình khi cast |
| `actions` | danh sách hành động FX/hit (có thể có `delay`) |

### 3.2 `cast_sound`

```json
"cast_sound": {
  "event": "minecraft:player_attack_sweep",
  "source": "players",
  "volume": 1.0,
  "pitch": 1.0,
  "pitch_random": 0.1
}
```

### 3.3 `camera_shake`

```json
"camera_shake": { "intensity": 3.0, "duration": 14, "frequency": 0.9 }
```

### 3.4 `screen_flash`

```json
"screen_flash": { "color": "#FF7A1A", "alpha": 0.25, "duration": 6 }
```

`color` hỗ trợ:

- string `"#RRGGBB"`
- string `"0xRRGGBB"`
- hoặc mảng `[r,g,b]`

### 3.5 `actions` (hiện hỗ trợ 4 loại)

#### A) `arc_sweep` (khuyến nghị: skill MMO có damage + on-hit)

Các field thường dùng:

- `duration` (tick)
- `center_forward`, `center_y`
- `arc_deg` (độ mở cung), `sweep_deg` (độ "quét" theo thời gian)
- `radii` (nhiều lớp cung), `steps` (độ mịn), `sample_stride` (stride càng lớn càng nhẹ)
- `follow_caster` (true = tâm đi theo người chơi, false = khóa ngay lúc cast)
- `add_sparks` (spark accent)

Damage + hiệu ứng:

```json
"hit": {
  "radius": 0.65,
  "damage": 7.0,
  "knockback": 0.65,
  "fire_seconds": 2,
  "effects": [
    { "id": "minecraft:weakness", "duration": 40, "amplifier": 0, "show_particles": false, "show_icon": true }
  ]
}
```

On-hit FX (hit confirm):

```json
"hit_fx": {
  "extra_particles": 10,
  "hit_shake": { "intensity": 0.9, "duration": 4, "frequency": 1.25 },
  "hit_flash": { "color": "#FFFFFF", "alpha": 0.12, "duration": 3 },
  "hit_sound": {
    "event": "minecraft:firecharge_use",
    "source": "players",
    "volume": 0.55,
    "pitch": 1.15,
    "pitch_random": 0.08
  }
}
```

#### B) `arc_fan` (FX-only)

Tạo afterimage / layer phụ bằng delay:

```json
{ "type": "arc_fan", "delay": 3, "duration": 6, "hot": true }
```

#### C) `spiral` (FX-only)

```json
{ "type": "spiral", "duration": 14, "points_per_tick": 38 }
```

#### D) `radial_burst` (FX-only)

```json
{ "type": "radial_burst", "duration": 8 }
```

---

## 4) Tạo pattern/skill mới nâng cao (custom Java)

Khi bạn muốn pattern mới hoàn toàn (ví dụ: projectile curve, kiếm bay, multi-hit combo):

1) Tạo `FxTask` mới (như `ArcSweepHitFxTask`) trong `com.main.slashing.mmo.fx` hoặc `com.main.slashing.fx`.
2) Tạo `MmoAction` mới trong `com.main.slashing.mmo.skill.actions`.
3) Thêm case parse trong `MmoSkillReloadListener.parseAction(...)`.

Nhờ vậy skill mới vẫn **định nghĩa bằng JSON**, chỉ cần thêm 1 type mới.

---

## 5) Tối ưu để skill mượt (rất quan trọng)

### 5.1 Giảm particle/hit cost

- Tăng `sample_stride` (vd: 2 → 3 hoặc 4) để giảm số điểm sample.
- Giảm `steps` nếu không cần quá mịn.
- Tránh `duration` quá dài (FX lâu = tốn tick).

### 5.2 Giảm spam packet

- `hit_fx.hit_flash` và `hit_fx.hit_shake` chỉ gửi cho **caster** và chỉ 1 lần/target.
- Nếu skill multi-hit, bạn nên tự rate-limit (vd: mỗi 3-4 tick mới cho hit confirm).

### 5.3 Đừng tìm entity quá rộng

Trong `ArcSweepHitFxTask`, vùng quét đã được giới hạn bằng `AABB` theo bán kính lớn nhất.
Khi bạn viết task mới, hãy giữ triết lý:

- AABB nhỏ nhất có thể
- Lọc entity sớm (`isAlive`, không allied, không invulnerable)

---

## 6) Kiểm thử nhanh

1) Vào game, cầm `Slash Wand`
2) Dùng `R/Z` chuyển skill
3) Bạn sẽ thấy skill mới: **Template MMO - Ember Sweep**

Nếu không thấy:

- chắc chắn JSON nằm đúng thư mục `data/slashing_alphad/skills/`
- chạy `/reload`
- xem log console (nếu JSON sai format)

---

Chúc bạn build được hệ skill MMO "đã tay" 😄
