# Hướng dẫn RPG dữ liệu (JSON)

## Cách chạy

```bash
gradlew runClient
```

```bash
gradlew build
```

## Thư mục pack runtime

```
<gameDir>/config/slashing_alphad/packs/
```

- Mỗi pack là một thư mục con (ví dụ: `base`, `customPack`).
- Các pack được load theo thứ tự alphabet; pack sau ghi đè pack trước theo `id`.
- Mỗi pack có các thư mục:
  - `classes/*.json`
  - `skills/*.json`
  - `statuses/*.json`

## Lệnh kiểm tra nhanh

1. Lần chạy đầu sẽ tự tạo `config/slashing_alphad/packs/base/` với JSON mẫu.
2. `/rpg reload` để reload JSON.
3. `/rpg class set @s reaper` để gán class.
4. `/rpg cast reaper:reaping_claw` khi nhìn vào mob để test kéo + damage + stun.
5. `/rpg profile` để xem stats/resources/statuses.
