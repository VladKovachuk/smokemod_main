package com.example.mixin.client;

import com.example.ExampleModClient;
import com.example.shader.ShaderManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Применяет пост-эффект (десатурация и др.) к финальному кадру.
 *
 * Инжектится в конец GameRenderer.render(...), когда мир, рука и GUI уже
 * отрисованы в главный фреймбуфер (minecraft:main). Это повторяет поведение
 * Psychedelicraft, где эффект накладывался на весь готовый кадр.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render(FJZ)V", at = @At("TAIL"))
    private void smokemod$renderPostEffect(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        ShaderManager shaderManager = ExampleModClient.getShaderManager();
        if (shaderManager != null) {
            // Обновляем uniform-переменные актуальным tickDelta и рендерим эффект
            shaderManager.updateUniforms(tickDelta);
            shaderManager.render(tickDelta);
        }
    }
}