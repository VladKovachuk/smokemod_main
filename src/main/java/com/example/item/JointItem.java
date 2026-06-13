package com.example.item;

/**
 * Предмет «джоинт» для Smoke Mod.
 * затяжка, выдох, никотин, прочность, надевание на голову.
 *
 * Если в будущем понадобится изменить количество никотина,
 * длительность затяжки или другие параметры —
 * достаточно переопределить соответствующие методы здесь.
 */
public class JointItem extends CigaretteItem {

	public JointItem(Settings settings) {
		super(settings);
	}
}
