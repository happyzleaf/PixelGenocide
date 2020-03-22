package com.happyzleaf.pixelgenocide.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author happyzleaf
 * @since 06/01/2019
 */
public class GameTime {
	public final long value;
	public final TimeUnit unit;
	
	public GameTime(long value, TimeUnit unit) {
		checkArgument(value >= 0, "The value must be 0 or above.");
		checkArgument(checkNotNull(unit, "unit").ordinal() >= 3, "You cannot use anything below seconds.");
		this.value = value;
		this.unit = unit;
	}

	private long seconds = -1;
	public long toSeconds() {
		if (seconds == -1) {
			seconds = unit.toSeconds(value);
		}

		return seconds;
	}

	@Override
	public String toString() {
		return "GameTime{" + unit.name() + ":" + value + "}";
	}

	public static class Serializer implements TypeSerializer<GameTime> {
		@Override
		public GameTime deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
			String gameTime = value.getString();
			String[] serialized = gameTime.split("=");
			return new GameTime(Long.parseLong(serialized[0]), TimeUnit.valueOf(serialized[1]));
		}
		
		@Override
		public void serialize(TypeToken<?> type, GameTime obj, ConfigurationNode value) throws ObjectMappingException {
			value.setValue(obj.value + "=" + obj.unit.name());
		}
	}
}
