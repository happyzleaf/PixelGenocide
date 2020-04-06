package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class WithPokerusCondition implements Condition {
	private final boolean enabled;

	public WithPokerusCondition(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		return pokemon.getPokerus().map(pokerus -> pokerus.type.duration >= 0).orElse(false);
	}

	// I know this isn't much elegant but I prefer to have this serializer than accepting booleans inside the Conditions class
	public static class Serializer implements TypeSerializer<WithPokerusCondition> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable WithPokerusCondition obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The object is null.");
			}

			value.setValue(obj.enabled);
		}

		@Nullable
		@Override
		public WithPokerusCondition deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			return new WithPokerusCondition(value.getBoolean(false));
		}
	}
}
