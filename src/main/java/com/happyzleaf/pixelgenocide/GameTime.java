package com.happyzleaf.pixelgenocide;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.concurrent.TimeUnit;

public class GameTime {
	public static GameTime ONETIME = new GameTime(0, null);
	
	public final long value;
	public final TimeUnit unit;
	
	public GameTime(long value, TimeUnit unit) {
		this.value = value;
		this.unit = unit;
	}
	
	public String getReadableTime() {
		return value + " " + unit.name().charAt(0) + unit.name().substring(1, unit.name().length()).toLowerCase();
	}
	
	public static class Serializer implements TypeSerializer<GameTime> {
		@Override
		public GameTime deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
			String gameTime = value.getString();
			if (gameTime.equals("ONETIME")) {
				return ONETIME;
			} else {
				String[] serialized = gameTime.split("=");
				return new GameTime(Long.parseLong(serialized[0]), TimeUnit.valueOf(serialized[1]));
			}
		}
		
		@Override
		public void serialize(TypeToken<?> type, GameTime obj, ConfigurationNode value) throws ObjectMappingException {
			if (obj == ONETIME) {
				value.setValue("ONETIME");
			} else {
				value.setValue(obj.value + "=" + obj.unit.name());
			}
		}
	}
}
