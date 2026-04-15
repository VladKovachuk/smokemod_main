package com.example.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class CigaretteCloudParticle extends SpriteBillboardParticle {

	private final SpriteProvider spriteProvider;

	protected CigaretteCloudParticle(ClientWorld world, double x, double y, double z, double velocityX,
			double velocityY, double velocityZ, SpriteProvider spriteProvider) {
		super(world, x, y, z, velocityX, velocityY, velocityZ);
		this.spriteProvider = spriteProvider;
		this.velocityMultiplier = 0.96F;

		// БАЗОВЫЙ класс Particle в майнкрафте по умолчанию добавляет +0.1 к Y (подлет
		// вверх)
		// и пересчитывает вектор. Нам нужно отменить этот бред и поставить точную
		// скорость:
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;

		// Отключаем гравитацию на всякий случай
		this.gravityStrength = 0.0F;

		// Наш фиксированный большой размер
		this.scale = 0.20F;

		// Легкая вариация цвета (от темно-серого до белого), как у облака
		float color = (this.random.nextFloat() * 0.3F) + 0.7F;
		this.red = color;
		this.green = color;
		this.blue = color;

		// Время жизни
		this.maxAge = (int) (8.0D / (this.random.nextDouble() * 0.8D + 0.2D)) + 4;
		this.setSpriteForAge(spriteProvider);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.dead) {
			this.setSpriteForAge(this.spriteProvider);
		}
	}

	public static class Factory implements ParticleFactory<DefaultParticleType> {
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Override
		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x,
				double y, double z, double velocityX, double velocityY, double velocityZ) {
			return new CigaretteCloudParticle(clientWorld, x, y, z, velocityX, velocityY, velocityZ,
					this.spriteProvider);
		}
	}
}
