package com.happyzleaf.pixelgenocide.condition;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;

public interface Condition {
	/**
	 * @return	<code>true</code> If the condition is enabled and the results should be applied.
	 * 			<code>false</code> Otherwise.
	 */
	default boolean isEnabled() {
		return true;
	}

	/**
	 * @return	<code>true</code> If the pokémon should pass the test to be removed.
	 * 			<code>false</code> If the pokémon should fail the test to be kept.
	 */
	default boolean isAnti() {
		return false;
	}

	/**
	 * @param pokemon The pokémon to be tested.
	 * @return	<code>true</code> If the pokémon passed the test.
	 * 			<code>false</code> Otherwise.
	 */
	boolean test(EntityPixelmon pokemon);
}
