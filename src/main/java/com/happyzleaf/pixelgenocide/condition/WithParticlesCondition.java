package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.happyzleaf.pixelgenocide.PixelGenocide;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.plugin.PluginContainer;

public class WithParticlesCondition implements Condition {
	private final boolean enabled;

	public WithParticlesCondition(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		if (enabled) {
			PluginContainer ep = Sponge.getPluginManager().getPlugin("entity-particles").orElse(null);
			if (ep == null) {
				PixelGenocide.LOGGER.info("entity-particles was not found, the support (most likely) won't work.");
			} else {
				if (ep.getVersion().orElse("").equals("2.1")) {
					PixelGenocide.LOGGER.info("entity-particles found, the support has been enabled.");
				} else {
					PixelGenocide.LOGGER.info("Please disable the condition 'withParticles' if you encounter any problems.");
				}
			}
		}

		return enabled;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		return ((Entity) pokemon).getKeys().stream().anyMatch(key -> key.getId().equals("entity-particles:id"));
	}

	// I know this isn't much elegant but I prefer to have this serializer than accepting booleans inside the Conditions class
	public static class Serializer implements TypeSerializer<WithParticlesCondition> {
		@Override
		public void serialize(@NonNull TypeToken<?> type, @Nullable WithParticlesCondition obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
			if (obj == null) {
				throw new ObjectMappingException("The object is null.");
			}

			value.setValue(obj.enabled);
		}

		@Nullable
		@Override
		public WithParticlesCondition deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
			return new WithParticlesCondition(value.getBoolean(false));
		}
	}
}
