package com.happyzleaf.pixelgenocide;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;

public class Wipe {
	private static Map<String, Integer> lastWipe = new HashMap<>();

	public static int all() {
		lastWipe.clear();

		return Sponge.getServer().getWorlds().stream().mapToInt(Wipe::world).sum();
	}

	public static int world(World world) {
		int wiped = (int) ((net.minecraft.world.World) world).loadedEntityList.stream()
				.filter(entity -> entity instanceof EntityPixelmon)
				.map(entity -> (EntityPixelmon) entity)
				.filter(pokemon -> !Config.getConditions().test(pokemon)) // If the pokémon doesn't meet our criteria
				.peek(EntityPixelmon::unloadEntity) // terminate its existence :)
				.count();

		lastWipe.put(world.getProperties().getWorldName(), wiped);

		return wiped;
	}

	public static int getTotalWiped() {
		return lastWipe.values().stream().mapToInt(Integer::intValue).sum();
	}

	public static int getWiped(WorldProperties world) {
		return lastWipe.get(world.getWorldName());
	}
}
