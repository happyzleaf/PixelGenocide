package com.happyzleaf.pixelgenocide.condition;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;

public class IsValidCondition implements Condition {
	@Override
	public boolean test(EntityPixelmon pokemon) {
		return !pokemon.canDespawn || pokemon.hasOwner() || pokemon.battleController != null || pokemon.getPokemonData().isInRanch();
	}
}
