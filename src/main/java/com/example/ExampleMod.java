package com.example;

import com.example.item.CigaretteItem;
import com.example.nicotine.NicotineManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "smokemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Звук затяжки сигареты
	public static final Identifier SOUND_CIGARETTE_ID = Identifier.of(MOD_ID, "sound_cigarette");
	public static final SoundEvent SOUND_CIGARETTE = SoundEvent.of(SOUND_CIGARETTE_ID);

	// Звук выдоха после использования сигареты
	public static final Identifier SOUND_EXHALATION_ID = Identifier.of(MOD_ID, "exhalation50");
	public static final SoundEvent SOUND_EXHALATION = SoundEvent.of(SOUND_EXHALATION_ID);

	// Предмет сигареты
	// maxDamage(100) = максимальная прочность 100 (5 использований по 20% каждое)
	public static final Item CIGARETTE = new CigaretteItem(new Item.Settings().maxDamage(100));

	@Override
	public void onInitialize() {
		// Регистрация звуков в реестре
		Registry.register(Registries.SOUND_EVENT, SOUND_CIGARETTE_ID, SOUND_CIGARETTE);
		Registry.register(Registries.SOUND_EVENT, SOUND_EXHALATION_ID, SOUND_EXHALATION);

		// Регистрация предмета в реестре игры (Identifier = "имя_мода:имя_предмета")
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cigarette"), CIGARETTE);

		// Добавляем сигарету во вкладку "Материалы" (Ingredients) в креативе
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(CIGARETTE);
		});

		// Система никотина (лёгких)
		NicotineManager.register();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			NicotineManager.onPlayerJoin(handler.player);
		});

		LOGGER.info("Smoke Mod initialized");
	}
}