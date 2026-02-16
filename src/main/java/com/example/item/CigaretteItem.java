package com.example.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Предмет сигареты для Smoke Mod.
 * Можно надеть в слот шлема (головы).
 * Имеет механику "натягивания" (затяжки) как у лука.
 */
public class CigaretteItem extends Item implements Equipment {

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
		return 200; // 10 секунд (можно изменить под свои нужды)
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
			// Здесь в будущем можно добавить эффекты сигареты
			// Например: эффекты, частицы дыма, звуки и т.д.
			
			if (!player.getAbilities().creativeMode) {
				// В будущем можно добавить уменьшение прочности или расход
				// stack.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
			}
		}
		
		return stack;
	}

	/**
	 * Вызывается, когда игрок прерывает использование (например, переключил предмет).
	 */
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		// Здесь можно добавить логику при прерывании затяжки
		// Например, частичный эффект или звук
	}
}
