package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpeciesCondition implements Condition {
	private final Set<EnumSpecies> species = new HashSet<>();
	private final boolean enabled;
	private boolean anti = false;

	public SpeciesCondition(boolean enabled, Collection<EnumSpecies> species) {
		this.enabled = enabled;
		this.species.addAll(checkNotNull(species, "species"));
	}

	public SpeciesCondition(boolean enabled, EnumSpecies... species) {
		this(enabled, Arrays.asList(species));
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public SpeciesCondition setWhitelist() {
		anti = false;

		return this;
	}

	public SpeciesCondition setBlacklist() {
		anti = true;

		return this;
	}

	@Override
	public boolean isAnti() {
		return anti;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		return species.contains(pokemon.getSpecies());
	}

	@Override
	public String toString() {
		return "SpeciesCondition{species=" + species + ", anti=" + anti + "}";
	}

	public static class Serializer implements TypeSerializer<SpeciesCondition> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable SpeciesCondition obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The species condition is null.");
			}

			value.getNode("enabled").setValue(obj.enabled);

			value.getNode("species").setValue(new TypeToken<List<String>>() {}, obj.species.stream().map(EnumSpecies::getPokemonName).collect(Collectors.toList()));
		}

		@Nullable
		@Override
		public SpeciesCondition deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			boolean enabled = value.getNode("enabled").getBoolean(true);

			Set<EnumSpecies> species = new HashSet<>();
			for (String name : value.getNode("species").getList(TypeToken.of(String.class), ArrayList::new)) {
				EnumSpecies s = EnumSpecies.getFromNameAnyCaseNoTranslate(name);
				if (s == null) {
					throw new ObjectMappingException(String.format("The species '%s' is nowhere to be found.", name));
				}

				species.add(s);
			}

			return new SpeciesCondition(enabled, species);
		}
	}
}
