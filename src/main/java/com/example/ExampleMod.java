package com.example;

import com.example.item.CigaretteItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "smokemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Предмет сигареты
	public static final Item CIGARETTE = new CigaretteItem(new Item.Settings());

	@Override
	public void onInitialize() {
		// Регистрация предмета в реестре игры (Identifier = "имя_мода:имя_предмета")
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cigarette"), CIGARETTE);

		// Добавляем сигарету во вкладку "Материалы" (Ingredients) в креативе
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(CIGARETTE);
		});

		LOGGER.info("Smoke Mod initialized");
	}
}