# Карта важных файлов

## Точки входа

| Файл | Назначение |
|------|------------|
| `src/main/java/com/example/ExampleMod.java` | `ModInitializer`: id мода, регистрация звуков и предмета, креатив, `NicotineManager`, событие входа игрока. |
| `src/client/java/com/example/ExampleModClient.java` | `ClientModInitializer`: предикат модели `lit`, приём `nicotine_sync`, HUD. |

## Игровая логика

| Файл | Назначение |
|------|------------|
| `src/main/java/com/example/item/CigaretteItem.java` | Предмет сигареты: использование, звуки, дым, никотин, прочность, кулдаун, экипировка головы. |
| `src/main/java/com/example/nicotine/NicotineManager.java` | Уровень «лёгких» (0–100), тик убывания/урона, синхронизация с клиентом. |

## Mixins (заготовки)

| Файл | Назначение |
|------|------------|
| `src/main/java/com/example/mixin/ExampleMixin.java` | Инъекция в `MinecraftServer.loadWorld` — пустая заготовка. |
| `src/client/java/com/example/mixin/client/ExampleClientMixin.java` | Клиентский миксин из шаблона. |

## Ресурсы

| Файл / папка | Назначение |
|--------------|------------|
| `src/main/resources/fabric.mod.json` | Метаданные мода, entrypoints, mixins, зависимости. |
| `src/main/resources/assets/smokemod/lang/*.json` | Переводы предметов, субтитров звуков. |
| `src/main/resources/assets/smokemod/sounds.json` | Объявление звуковых событий. |
| `src/main/resources/assets/smokemod/sounds/*.ogg` | Аудио (затяжка, выдох и т.д.). |
| `src/main/resources/assets/smokemod/models/item/*.json` | Модели предметов (в т.ч. overrides для `lit`). |
| `src/main/resources/assets/smokemod/textures/item/*` | Текстуры предметов. |
| `src/main/resources/smokemod.mixins.json` | Список миксинов сервера/общего кода. |
| `src/client/resources/modid.client.mixins.json` | Клиентские миксины. |

## Сборка

| Файл | Назначение |
|------|------------|
| `build.gradle` | Loom, split sources, зависимости Fabric. |
| `gradle.properties` | Версии Minecraft, Yarn, Loader, Fabric API. |
