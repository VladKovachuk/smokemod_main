# Архитектура Smoke Mod

## Технологии

| Слой | Технология |
|------|------------|
| **Платформа** | Minecraft Java Edition 1.20.1 |
| **Загрузчик модов** | Fabric Loader |
| **API** | Fabric API (Loom для сборки) |
| **Язык** | Java 17 |  
| **Сборка** | Gradle (Fabric Loom), разделение `main` / `client` source sets |
| **База данных** | Не используется |
| **«Backend»** | Логический **сервер** Minecraft: обработка тиков, урон, звуки, частицы, сетевые пакеты S2C |
| **«Frontend»** | **Клиент** Minecraft: рендер HUD, приём пакетов, предикаты моделей предметов |

Мод не является веб-приложением: нет HTTP-сервера, SQL и т.п.

## Структура репозитория (важное)

```
smokemod_main/
├── build.gradle, settings.gradle, gradle.properties
├── src/main/java/          # Общая и серверная логика
│   └── com/example/
│       ├── ExampleMod.java           # Точка входа, регистрация предметов/звуков/событий
│       ├── item/CigaretteItem.java   # Предмет сигареты
│       ├── nicotine/NicotineManager.java
│       └── mixin/ExampleMixin.java # Заготовка (почти пустая)
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── smokemod.mixins.json
│   └── assets/smokemod/    # Модели, текстуры, lang, sounds.json
└── src/client/java/        # Только клиент
    └── com/example/
        ├── ExampleModClient.java
        └── mixin/client/ExampleClientMixin.java
```

## Поток данных (никотин)

1. **Сервер** (`NicotineManager`): хранит уровень по UUID, тикает убывание и урон, вызывает `syncToClient`.
2. **Пакет** `smokemod:nicotine_sync` (S2C): один `int` — текущий уровень.
3. **Клиент** (`ExampleModClient`): обновляет `clientNicotineLevel`, HUD рисует строку `Lungs: …%`.

## Зависимости между модулями

- `CigaretteItem` → `ExampleMod` (звуки), `NicotineManager` (пополнение).
- `ExampleMod` → регистрация `NicotineManager`, `ServerPlayConnectionEvents.JOIN`.
- `ExampleModClient` → `NicotineManager` только для `NICOTINE_SYNC_ID` (константа канала).

## Mixins

- `ExampleMixin` / `ExampleClientMixin` — из шаблона Fabric; **функционально мод почти не использует** (пустые/заготовочные инъекции).
