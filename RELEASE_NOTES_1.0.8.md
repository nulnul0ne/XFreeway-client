# XFreeway Client 1.0.8

Релиз XFreeway Client для Android с очисткой лишних разрешений и неиспользуемых функций.

## Что изменилось

- Удалён неиспользуемый QR-сканер.
- Убраны входы в QR-сканер из интерфейса и launcher shortcuts.
- Удалены CameraX-зависимости.
- Убраны лишние permissions:
  - `android.permission.CAMERA`
  - `android.permission.RECORD_AUDIO`
  - `android.permission.MANAGE_EXTERNAL_STORAGE`
  - `android.permission.READ_EXTERNAL_STORAGE`
  - `android.permission.WRITE_EXTERNAL_STORAGE`
- Убрана camera hardware feature из manifest.
- Сохранена генерация QR-кодов для шаринга/экспорта конфигов.
- Собран новый signed release APK.

## Версия

- Version name: `1.0.8`
- Version code: `108`

## Attribution

XFreeway Client is based on XrayFA by Q7DF1:

https://github.com/Q7DF1/XrayFA

Original project and this derivative work are distributed under the Apache License 2.0.
