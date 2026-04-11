# 003 — Приведение fabric.mod.json и иконки к модулю smokemod

## Goal

Убрать несоответствия шаблона Fabric Example Mod: описание, контакты, путь иконки, чтобы мод выглядел цельно.

## Requirements

- `id` остаётся `smokemod` (как в коде `ExampleMod.MOD_ID`).
- Иконка: либо положить файл по пути из `fabric.mod.json`, либо исправить путь под существующий ресурс.
- Не менять версии зависимостей без необходимости.

## Files involved

- `src/main/resources/fabric.mod.json`
- `src/main/resources/assets/` — наличие иконки (сейчас в json указано `assets/modid/icon.png`)

## Definition of Done

- Игра не ругается на отсутствующую иконку при загрузке мода.
- Описание отражает Smoke Mod, а не placeholder-текст (минимально — одно короткое предложение).
- Сборка успешна.
