package com.example;

import com.example.item.CigaretteItem;
import com.example.item.JointItem;
import com.example.nicotine.NicotineManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
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

	// Предмет джоинта (аналог сигареты, своя модель и текстура)
	public static final Item JOINT = new JointItem(new Item.Settings().maxDamage(100));

	// Кастомная частица дыма для сигареты
	public static final DefaultParticleType CIGARETTE_CLOUD = FabricParticleTypes.simple();

	// Кастомная вкладка в креативе
	public static final ItemGroup SMOKEMOD_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(CIGARETTE))
			.displayName(Text.translatable("itemGroup.smokemod.main"))
			.entries((context, entries) -> {
				entries.add(CIGARETTE);
				
				ItemStack litCigarette = new ItemStack(CIGARETTE);
				litCigarette.getOrCreateNbt().putBoolean("lit", true);
				entries.add(litCigarette);

				entries.add(JOINT);

				ItemStack litJoint = new ItemStack(JOINT);
				litJoint.getOrCreateNbt().putBoolean("lit", true);
				entries.add(litJoint);
			})
			.build();

	@Override
	public void onInitialize() {
		// Регистрация звуков в реестре
		Registry.register(Registries.SOUND_EVENT, SOUND_CIGARETTE_ID, SOUND_CIGARETTE);
		Registry.register(Registries.SOUND_EVENT, SOUND_EXHALATION_ID, SOUND_EXHALATION);

		// Регистрация кастомной частицы
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "cigarette_cloud"), CIGARETTE_CLOUD);

		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cigarette"), CIGARETTE);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "joint"), JOINT);

		// Регистрируем вкладку в креативе
		Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "main"), SMOKEMOD_GROUP);

		// Система никотина (лёгких)
		NicotineManager.register();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			NicotineManager.onPlayerJoin(handler.player);
		});

		// Регистрация системы продолжительного выдоха
		com.example.item.ExhalationManager.register();

		LOGGER.info("Smoke Mod initialized");
	}
}