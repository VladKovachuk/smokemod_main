package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Регистрируем предикат модели для сигареты (зажжена/не зажжена)
		ModelPredicateProviderRegistry.register(
			ExampleMod.CIGARETTE,
			Identifier.of(ExampleMod.MOD_ID, "lit"),
			(stack, world, entity, seed) -> {
				// Проверяем NBT флаг "lit"
				if (stack.getNbt() != null && stack.getNbt().getBoolean("lit")) {
					return 1.0f; // Зажжена - используем модель lit_cigarette
				}
				return 0.0f; // Не зажжена - используем обычную модель
			}
		);
	}
}