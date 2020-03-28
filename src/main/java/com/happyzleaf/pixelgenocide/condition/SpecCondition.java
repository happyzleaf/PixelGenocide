package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpecCondition implements Condition {
	private final String specString;

	private final PokemonSpec spec;
	private final boolean enabled;
	private final boolean anti;

	public SpecCondition(String spec, boolean enabled, boolean anti) {
		this.specString = checkNotNull(spec, "spec");

		this.spec = new PokemonSpec(spec);
		this.enabled = enabled;
		this.anti = anti;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isAnti() {
		return anti;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		return spec.matches(pokemon);
	}

	@Override
	public String toString() {
		return "SpecCondition{spec='" + specString + "', anti=" + anti + "}";
	}

	public static class Serializer implements TypeSerializer<SpecCondition> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable SpecCondition obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The spec condition is null.");
			}

			value.getNode("spec").setValue(obj.specString);

			if (!obj.enabled || !value.getNode("enabled").isVirtual()) {
				value.getNode("enabled").setValue(obj.enabled);
			}

			if (obj.anti || !value.getNode("anti").isVirtual()) {
				value.getNode("anti").setValue(obj.anti);
			}
		}

		@Nullable
		@Override
		public SpecCondition deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			String spec = value.getNode("spec").getString();
			if (spec == null) {
				throw new ObjectMappingException("The spec is missing.");
			}

			boolean enabled = value.getNode("enabled").getBoolean(true);

			boolean anti = value.getNode("anti").getBoolean(false);

			return new SpecCondition(spec, enabled, anti);
		}
	}
}
