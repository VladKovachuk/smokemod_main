package com.example.shader;

import com.example.ExampleMod;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import com.example.mixin.client.PostEffectProcessorAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

import java.io.IOException;

/**
 * Обобщенный менеджер шейдеров для управления пост-эффектами.
 * Поддерживает загрузку шейдеров из ресурсов и управление uniform переменными.
 * Адаптирован для Minecraft 1.20.1.
 *
 * --- НАСТРОЙКА СКОРОСТЕЙ ---
 * FADE_IN_SPEED  — скорость появления серости (0.0 → цель). В единицах за кадр.
 *                  Например, 0.02 = за ~50 кадров (≈2.5 сек) достигнет значения 1.0.
 *
 * FADE_OUT_SPEED — скорость исчезновения серости (цель → 0.0). В единицах за кадр.
 *                  Например, 0.008 = за ~125 кадров (≈6 сек) уйдёт от 1.0 до 0.0.
 *
 * При смерти игрока серость сбрасывается мгновенно (setInstantReset(true)).
 * ---------------------------
 */
public class ShaderManager implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Identifier.of(ExampleMod.MOD_ID, "shader_manager");

    // =========================================================
    //  НАСТРОЙКА СКОРОСТЕЙ — меняй только эти константы
    // =========================================================

    /**
     * Скорость ПОЯВЛЕНИЯ серости (единиц в секунду).
     * 0.5f  → за 2 секунды дойдет от 0 до 1.0
     * 1.0f  → за 1 секунду
     */
    private static final float FADE_IN_SPEED = 0.2f;

    /**
     * Скорость ИСЧЕЗНОВЕНИЯ серости (единиц в секунду).
     * 0.1f  → за 10 секунд от 1.0 до 0
     */
    private static final float FADE_OUT_SPEED = 0.020f;

    // =========================================================

    private PostEffectProcessor shaderEffect;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private long lastUpdateMillis = 0;

    /** Текущее визуальное (интерполированное) значение серости — то, что реально рендерится */
    private float currentDesaturation = 0.0f;

    /** Целевое значение серости — выставляется сервером через пакет */
    private float targetDesaturation = 0.0f;

    // Динамические скорости для десатурации (приходят с сервера)
    private float desatFadeInSpeed = 0.4f;
    private float desatFadeOutSpeed = 0.05f;

    /** Blur и Distortion — заготовки под будущие эффекты */
    private float blur = 0.0f;
    private float targetBlur = 0.0f;
    private float blurFadeIn = 0.5f;
    private float blurFadeOut = 0.1f;
    private float distortion = 0.0f;

    /**
     * Если true — currentDesaturation немедленно сбрасывается в 0.
     * Выставляется при смерти игрока.
     */
    private boolean instantReset = false;

    public ShaderManager() {
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        if (shaderEffect != null) {
            shaderEffect.close();
            shaderEffect = null;
        }
        try {
            shaderEffect = new PostEffectProcessor(
                    client.getTextureManager(),
                    manager,
                    client.getFramebuffer(),
                    Identifier.of(ExampleMod.MOD_ID, "shaders/post/desaturation.json")
            );
            shaderEffect.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
        } catch (IOException e) {
            ExampleMod.LOGGER.error("Failed to load shader effect", e);
            shaderEffect = null;
        } catch (Exception e) {
            ExampleMod.LOGGER.error("Unknown error loading shader effect", e);
            shaderEffect = null;
        }
    }

    /**
     * Обновляет интерполяцию и uniform переменные шейдера.
     * Вызывается каждый кадр из GameRendererMixin.
     *
     * @param tickDelta Время между тиками
     */
    public void updateUniforms(float tickDelta) {
        if (shaderEffect == null) return;

        // --- Расчет времени дельты ---
        long now = System.currentTimeMillis();
        if (lastUpdateMillis == 0) lastUpdateMillis = now;
        float deltaTimeSeconds = (now - lastUpdateMillis) / 1000.0f;
        lastUpdateMillis = now;

        // Ограничиваем дельту, чтобы избежать скачков при лагах/паузе
        if (deltaTimeSeconds > 0.1f) deltaTimeSeconds = 0.05f;

        // --- Мгновенный сброс (например при смерти) ---
        if (instantReset) {
            currentDesaturation = 0.0f;
            targetDesaturation = 0.0f;
            instantReset = false;
        }

        // --- Плавная интерполяция к целевому значению на основе времени ---
        if (currentDesaturation < targetDesaturation) {
            // Появление серости (используем динамическую скорость)
            currentDesaturation = Math.min(targetDesaturation, currentDesaturation + (desatFadeInSpeed * deltaTimeSeconds));
        } else if (currentDesaturation > targetDesaturation) {
            // Исчезновение серости (используем динамическую скорость)
            currentDesaturation = Math.max(targetDesaturation, currentDesaturation - (desatFadeOutSpeed * deltaTimeSeconds));
        }

        // --- Передаём интерполированное значение в GLSL ---
        for (PostEffectPass pass : ((PostEffectProcessorAccessor) shaderEffect).getPasses()) {
            net.minecraft.client.gl.GlUniform desatUniform = pass.getProgram().getUniformByName("desaturation");
            if (desatUniform != null) {
                desatUniform.set(currentDesaturation);
            }

            // Также обновляем пиксели и проекцию, чтобы избежать "зума"
            net.minecraft.client.gl.GlUniform pixelSizeUniform = pass.getProgram().getUniformByName("pixelSize");
            if (pixelSizeUniform != null) {
                pixelSizeUniform.set(1.0f / client.getWindow().getFramebufferWidth(), 1.0f / client.getWindow().getFramebufferHeight());
            }

            net.minecraft.client.gl.GlUniform projMatUniform = pass.getProgram().getUniformByName("ProjMat");
            if (projMatUniform != null) {
                // Создаем матрицу, которая просто растягивает шейдер на весь экран без зума
                Matrix4f projMat = new Matrix4f().setOrtho(0.0f, 1.0f, 1.0f, 0.0f, 0.1f, 1000.0f);
                projMatUniform.set(projMat);
            }

            net.minecraft.client.gl.GlUniform outSizeUniform = pass.getProgram().getUniformByName("OutSize");
            if (outSizeUniform != null) {
                outSizeUniform.set((float)client.getWindow().getFramebufferWidth(), (float)client.getWindow().getFramebufferHeight());
            }

            net.minecraft.client.gl.GlUniform inSizeUniform = pass.getProgram().getUniformByName("InSize");
            if (inSizeUniform != null) {
                inSizeUniform.set((float)client.getWindow().getFramebufferWidth(), (float)client.getWindow().getFramebufferHeight());
            }
        }
    }

    /**
     * Рендерит пост-эффект.
     * Вызывается каждый кадр из GameRendererMixin, только если эффект активен.
     *
     * @param tickDelta Время между тиками
     */
    public void render(float tickDelta) {
        if (shaderEffect != null && currentDesaturation > 0.001f) {
            // Принудительно обновляем размеры перед рендером
            shaderEffect.setupDimensions(
                    client.getWindow().getFramebufferWidth(),
                    client.getWindow().getFramebufferHeight()
            );

            // Сохраняем стейт и рисуем
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            shaderEffect.render(tickDelta);

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);

            // Возвращаем привязку к главному фреймбуферу
            client.getFramebuffer().beginWrite(false);
        }
    }

    // =========================================================
    //  Методы управления — вызываются из ExampleModClient
    // =========================================================

    /**
     * Выставляет параметры серости, полученные с сервера.
     */
    public void setDesaturationParams(float target, float fadeIn, float fadeOut) {
        this.targetDesaturation = Math.max(0.0f, Math.min(1.0f, target));
        this.desatFadeInSpeed = fadeIn;
        this.desatFadeOutSpeed = fadeOut;
    }

    /**
     * Старый метод для совместимости (если нужен)
     */
    public void setDesaturation(float value) {
        this.targetDesaturation = Math.max(0.0f, Math.min(1.0f, value));
    }

    /**
     * Мгновенно сбрасывает серость в 0 в следующем кадре.
     * Используется при смерти игрока, телепортации и т.п.
     */
    public void setInstantReset(boolean reset) {
        this.instantReset = reset;
    }

    // Заготовки под будущие эффекты
    public void setBlur(float value) { this.blur = Math.max(0.0f, Math.min(1.0f, value)); }
    public void setDistortion(float value) { this.distortion = Math.max(0.0f, Math.min(1.0f, value)); }

    public float getDesaturation() { return currentDesaturation; }
    public float getTargetDesaturation() { return targetDesaturation; }
    public float getBlur() { return blur; }
    public float getDistortion() { return distortion; }

    /**
     * Закрывает шейдерный эффект.
     */
    public void close() {
        if (shaderEffect != null) {
            shaderEffect.close();
            shaderEffect = null;
        }
    }
}
