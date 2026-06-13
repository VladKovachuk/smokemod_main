package com.example;

import com.example.effects.EffectManager;
import com.example.nicotine.NicotineManager;
import com.example.particle.CigaretteCloudParticle;
import com.example.shader.ShaderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class ExampleModClient implements ClientModInitializer {

	/** Уровень никотина на клиенте (0–100), синхронизируется с сервером */
	public static int clientNicotineLevel = 100;
	private static final int LUNGS_ICON_SIZE = 24;
	private static final Identifier LUNGS_EMPTY_TEXTURE = Identifier.of(ExampleMod.MOD_ID,
			"textures/gui/lungs_empty.png");
	private static final Identifier LUNGS_FULL_TEXTURE = Identifier.of(ExampleMod.MOD_ID,
			"textures/gui/lungs_full.png");

	/** Менеджер шейдеров для пост-эффектов */
	private static ShaderManager shaderManager;

	/** Доступ к менеджеру шейдеров из миксина (рендер на финальном кадре) */
	public static ShaderManager getShaderManager() {
		return shaderManager;
	}

	@Override
	public void onInitializeClient() {
		// Инициализируем менеджер шейдеров
		shaderManager = new ShaderManager();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(shaderManager);

		// Привязываем кастомную частицу к нашей фабрике
		ParticleFactoryRegistry.getInstance().register(ExampleMod.CIGARETTE_CLOUD, CigaretteCloudParticle.Factory::new);

		// Регистрируем предикат модели для сигареты (зажжена/не зажжена — меняет
		// текстуру)
		ModelPredicateProviderRegistry.register(
				ExampleMod.CIGARETTE,
				Identifier.of(ExampleMod.MOD_ID, "lit"),
				(stack, world, entity, seed) -> {
					if (stack.getNbt() != null && stack.getNbt().getBoolean("lit")) {
						return 1.0f;
					}
					return 0.0f;
				});

		// Предикат модели для джоинта (зажжён/не зажжён — меняет текстуру)
		ModelPredicateProviderRegistry.register(
				ExampleMod.JOINT,
				Identifier.of(ExampleMod.MOD_ID, "lit"),
				(stack, world, entity, seed) -> {
					if (stack.getNbt() != null && stack.getNbt().getBoolean("lit")) {
						return 1.0f;
					}
					return 0.0f;
				});

		// Приём пакета с сервера: обновляем clientNicotineLevel
		ClientPlayNetworking.registerGlobalReceiver(NicotineManager.NICOTINE_SYNC_ID,
				(client, handler, buf, responseSender) -> {
					int level = buf.readInt();
					client.execute(() -> clientNicotineLevel = level);
				});

		// Приём пакета с сервера: обновляем уровни эффектов с параметрами
		ClientPlayNetworking.registerGlobalReceiver(EffectManager.EFFECT_SYNC_ID,
				(client, handler, buf, responseSender) -> {
					int effectCount = buf.readInt();
					// Используем структуру для хранения всех параметров эффекта
					class EffectData {
						float value, fadeIn, fadeOut;
						EffectData(float v, float fi, float fo) { value = v; fadeIn = fi; fadeOut = fo; }
					}
					java.util.Map<String, EffectData> receivedEffects = new java.util.HashMap<>();
					for (int i = 0; i < effectCount; i++) {
						receivedEffects.put(buf.readString(), new EffectData(buf.readFloat(), buf.readFloat(), buf.readFloat()));
					}

					client.execute(() -> {
						for (java.util.Map.Entry<String, EffectData> entry : receivedEffects.entrySet()) {
							String effectTypeName = entry.getKey();
							EffectData data = entry.getValue();
							try {
								EffectManager.EffectType type = EffectManager.EffectType.valueOf(effectTypeName);
								switch (type) {
									case DESATURATION:
										shaderManager.setDesaturationParams(data.value, data.fadeIn, data.fadeOut);
										break;
									case BLUR:
										shaderManager.setBlur(data.value);
										break;
									case DISTORTION:
										shaderManager.setDistortion(data.value);
										break;
								}
							} catch (IllegalArgumentException e) {
								ExampleMod.LOGGER.warn("Unknown effect type: {}", effectTypeName);
							}
						}
					});
				});

		// HUD: индикатор лёгких, ниже прицела
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.options.debugEnabled)
				return;
			if (mc.player == null)
				return;

			int width = mc.getWindow().getScaledWidth();
			int height = mc.getWindow().getScaledHeight();
			int x = (width / 2) - (LUNGS_ICON_SIZE / 2);
			int y = (height / 2) + 70;

			// 1) Рисуем пустой контур целиком
			context.drawTexture(
					LUNGS_EMPTY_TEXTURE,
					x, y,
					0, 0,
					LUNGS_ICON_SIZE, LUNGS_ICON_SIZE,
					LUNGS_ICON_SIZE, LUNGS_ICON_SIZE);

			// 2) Рисуем заполнение (снизу вверх) по уровню никотина
			int nicotine = Math.max(0, Math.min(100, clientNicotineLevel));
			int lungTop = 7; // пиксель на текстуре, где начинаются лёгкие (верх)
			int lungBottom = 20; // пиксель на текстуре, где заканчиваются лёгкие (низ)
			int lungRange = lungBottom - lungTop;
			int filledPixels = Math.round((nicotine / 100.0f) * lungRange);
			if (filledPixels > 0) {
				int uvY = lungBottom - filledPixels;
				int drawY = y + uvY;

				context.drawTexture(
						LUNGS_FULL_TEXTURE,
						x, drawY,
						0, uvY,
						LUNGS_ICON_SIZE, filledPixels,
						LUNGS_ICON_SIZE, LUNGS_ICON_SIZE);
			}
			// Временный текстовый индикатор процентов никотина

			String percentText = clientNicotineLevel + "%";
			int textX = x + (LUNGS_ICON_SIZE / 2) -
					(mc.textRenderer.getWidth(percentText) / 2);
			int textY = y + LUNGS_ICON_SIZE - 20;
			context.drawText(mc.textRenderer, percentText, textX, textY, 0xFFFFFF, true);

		});

		// Приём пакета мгновенного сброса (смерть игрока и т.п.) — без плавного затухания
		ClientPlayNetworking.registerGlobalReceiver(EffectManager.EFFECT_CLEAR_ID,
				(client, handler, buf, responseSender) -> {
					client.execute(() -> {
						if (shaderManager != null) {
							shaderManager.setInstantReset(true);
						}
					});
				});

		// ВАЖНО: рендеринг пост-эффекта и обновление uniform перенесено в GameRendererMixin (TAIL метода render).
		// Раньше здесь был END_CLIENT_TICK, но это вызывало слишком быстрое обновление при высоком FPS.
	}
}
