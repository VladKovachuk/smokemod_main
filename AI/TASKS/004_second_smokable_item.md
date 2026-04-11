# 004 — Второй курительный предмет с другим никотином

## Goal

Добавить новый предмет (например, «сигара») с **другим** `getNicotineAmount()`, зарегистрировать его и добавить в креатив.

## Requirements

- Наследование от `CigaretteItem` или общая база — по текущей архитектуре достаточно переопределить `getNicotineAmount()`.
- Регистрация в `ExampleMod` + модель/текстура/lang по аналогии с `cigarette`.
- Использовать `NicotineManager.addPuff(player, amount)` или уже существующий поток через `getNicotineAmount()`.
- партикл дыма другой, а именно campfire_cosy_smoke

## Files involved

- `src/main/java/com/example/item/` — новый класс предмета.
- `ExampleMod.java` — регистрация `Item`.
- `assets/smokemod/models/item/`, `lang/`, возможно `textures/item/`.

## Definition of Done

- В игре двапредмета; у второго заметно другое пополнение «лёгких» при затяжке (проверка по HUD).
- Нет дублирования больших кусков кода из `CigaretteItem` без необходимости.
