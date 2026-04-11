# 005 — Mixins: удалить заготовки или ввести реальное использование

## Goal

Либо **удалить** неиспользуемые `ExampleMixin` / `ExampleClientMixin` и записи в `*.mixins.json`, либо **задействовать** их под конкретную задачу (с обоснованием).

## Requirements

- После изменений проект компилируется, игра запускается с модом.
- Не оставлять пустые инъекции «ради миксина» без цели.
- Если миксины удалены — обновить `smokemod.mixins.json` и `modid.client.mixins.json`.

## Files involved

- `src/main/java/com/example/mixin/ExampleMixin.java`
- `src/client/java/com/example/mixin/client/ExampleClientMixin.java`
- `src/main/resources/smokemod.mixins.json`
- `src/client/resources/modid.client.mixins.json`

## Definition of Done

- В конфигах миксинов нет ссылок на удалённые классы.
- Документация в `AI/FILE_MAP.md` обновлена, если менялась роль миксинов (по запросу).
