package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IsValidCondition implements Condition {
	private final boolean debug;

	public IsValidCondition(boolean debug) {
		this.debug = debug;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		return !(pokemon.canDespawn || debug) || pokemon.hasOwner() || pokemon.battleController != null || pokemon.getPokemonData().isInRanch();
	}

	public static class Serializer implements TypeSerializer<IsValidCondition> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable IsValidCondition obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The object is null.");
			}

			value.setValue(obj.debug);
		}

		@Nullable
		@Override
		public IsValidCondition deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			return new IsValidCondition(value.getBoolean(false));
		}
	}
}
