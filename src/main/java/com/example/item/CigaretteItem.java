package com.example.item;

import com.example.ExampleMod;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Предмет сигареты для Smoke Mod.
 * Можно надеть в слот шлема (головы).
 * Имеет механику "натягивания" (затяжки) как у лука.
 */
public class CigaretteItem extends Item implements Equipment {

	/** Кулдаун после использования: 4 секунды = 80 тиков */
	private static final int COOLDOWN_TICKS = 80;

	public CigaretteItem(Settings settings) {
		super(settings);
	}

	/**
	 * Сообщаем игре, что этот предмет относится к слоту головы.
	 */
	@Override
	public EquipmentSlot getSlotType() {
		return EquipmentSlot.HEAD;
	}

	/**
	 * Вызывается при ПКМ с сигаретой в руке.
	 * Начинает анимацию "затяжки" (аналог натягивания лука).
	 */
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		// Не начинаем использование, если предмет на кулдауне
		if (user.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.pass(itemStack);
		}
		
		// Проигрываем звук затяжки при начале использования (на сервере для синхронизации)
		if (!world.isClient) {
			world.playSound(null, user.getBlockPos(), 
				ExampleMod.SOUND_CIGARETTE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		}
		
		// Начинаем использование предмета (анимация натягивания)
		user.setCurrentHand(hand);
		return TypedActionResult.consume(itemStack);
	}

	/**
	 * Определяет длительность использования (в тиках).
	 * 20 тиков = 1 секунда.
	 * Здесь можно настроить, как долго нужно "затягиваться".
	 */
	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 50; // 2.5 секунды 
	}

	/**
	 * Определяет, как предмет используется (как еда/напиток - можно есть/пить).
	 * Используем UseAction.BOW для анимации как у лука.
	 */
	@Override
	public net.minecraft.util.UseAction getUseAction(ItemStack stack) {
		return net.minecraft.util.UseAction.BOW; // Анимация как у лука
	}

	/**
	 * Вызывается, когда игрок отпускает ПКМ после использования.
	 * Здесь будет логика "выдоха" или эффектов сигареты.
	 */
	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (user instanceof PlayerEntity player) {
			// При первом использовании помечаем сигарету как зажженную
			if (!world.isClient) {
				var nbt = stack.getOrCreateNbt();
				if (!nbt.getBoolean("lit")) {
					nbt.putBoolean("lit", true);
				}
			}

			// Звук выдоха после использования сигареты
			if (!world.isClient) {
				world.playSound(null, user.getBlockPos(),
					ExampleMod.SOUND_EXHALATION, SoundCategory.PLAYERS, 1.0f, 1.0f);
			}

			// Частицы дыма "изо рта" — только на сервере, видны всем игрокам
			if (!world.isClient && world instanceof ServerWorld serverWorld) {
				spawnExhalationSmoke(serverWorld, player);
			}

			// Ставим кулдаун 4 секунды после использования
			player.getItemCooldownManager().set(this, COOLDOWN_TICKS);

			if (!player.getAbilities().creativeMode) {
				stack.damage(20, user, (entity) -> {
					if (entity instanceof PlayerEntity) {
						((PlayerEntity) entity).sendToolBreakStatus(user.getActiveHand());
					}
				});
			}
		}
		
		return stack;
	}

	/**
	 * Вызывается, когда игрок прерывает использование (отпустил ПКМ раньше).
	 * Кулдаун не ставим. Останавливаем звук затяжки у этого игрока.
	 */
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (world.isClient) return;
		if (remainingUseTicks >= getMaxUseTime(stack)) return; // Полное использование — не сюда
		// Останавливаем звук затяжки у игрока
		if (user instanceof ServerPlayerEntity serverPlayer) {
			serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(
				ExampleMod.SOUND_CIGARETTE_ID, SoundCategory.PLAYERS));
		}
	}

	/**
	 * Спавнит струю дыма "изо рта" игрока.
	 * Частицы летят вперёд по направлению взгляда с небольшим подъёмом.
	 * Вызывается только на сервере — дым видят все игроки.
	 */
	private void spawnExhalationSmoke(ServerWorld world, PlayerEntity user) {
		Vec3d look = user.getRotationVec(1.0F);
	
		// Считаем только направление по горизонтали
		double horizontalMag = Math.sqrt(look.x * look.x + look.z * look.z);
		// Если игрок смотрит в пол, берем минимальное значение, чтобы не было ошибки
		double dirX = horizontalMag < 0.1 ? 0 : look.x / horizontalMag;
		double dirZ = horizontalMag < 0.1 ? 0 : look.z / horizontalMag;
	
		// УВЕЛИЧИВАЕМ СКОРОСТЬ
		double speedForward = 0.25; // Сильнее дуем вперед 0.25
		double speedUp = 0.15;      // Сильнее толкаем вверх, чтобы не падало вниз 0.15
	
		// ПОЗИЦИЯ: Поднимаем точку спавна выше (на уровень носа/глаз)
		// Чтобы даже если частица чуть упадет, она не оказалась в груди
		double x = user.getX() + dirX * 0.4;
		double y = user.getEyeY() - 0.05; // Почти на уровне глаз
		double z = user.getZ() + dirZ * 0.4;
	
		for (int i = 0; i < 15; i++) {  // 15 -> 30 количество
			double vX = dirX * speedForward + (world.random.nextDouble() - 0.5) * 0.1;
			double vZ = dirZ * speedForward + (world.random.nextDouble() - 0.5) * 0.1;
			
			// vY теперь всегда ощутимо положительный
			double vY = speedUp + world.random.nextDouble() * 0.15;
	
			world.spawnParticles(ParticleTypes.CLOUD, 
				x, y, z, 
				0,          
				vX, vY, vZ, 
				1.0         // Увеличили общий множитель силы
			);
		}
	}
}
