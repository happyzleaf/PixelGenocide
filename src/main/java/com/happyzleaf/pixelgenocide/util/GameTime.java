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
	public long value; // TODO make private
	public TimeUnit unit; // TODO make private
	
	public GameTime(long value, TimeUnit unit) {
		checkArgument(value >= 0, "The value must be 0 or above.");
		checkArgument(checkNotNull(unit, "unit").ordinal() >= 3, "You cannot use anything below seconds.");
		this.value = value;
		this.unit = unit;
	}
	
	public long getValue() {
		return value;
	}
	
	public TimeUnit getTimeUnit() {
		return unit;
	}
	
	public long toSeconds() {
		return unit.toSeconds(value);
	}
	
	public String getReadableTime() { // TODO remove
		return value + " " + unit.name().charAt(0) + unit.name().substring(1).toLowerCase();
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
