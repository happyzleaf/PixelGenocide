package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UltraBeastCondition implements Condition {
	private final boolean enabled;

	public UltraBeastCondition(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		return pokemon.getSpecies().isUltraBeast();
	}

	// I know this isn't much elegant but I prefer to have this serializer than accepting booleans inside the Conditions class
	public static class Serializer implements TypeSerializer<UltraBeastCondition> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable UltraBeastCondition obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The object is null.");
			}

			value.setValue(obj.enabled);
		}

		@Nullable
		@Override
		public UltraBeastCondition deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			return new UltraBeastCondition(value.getBoolean(false));
		}
	}
}
