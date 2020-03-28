package com.happyzleaf.pixelgenocide;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Wipe {
	private static Map<String, Integer> lastWipe = new HashMap<>();

	public static Set<EntityPixelmon> all() {
		lastWipe.clear();

		return Sponge.getServer().getWorlds().stream().flatMap(world -> world(world).stream()).collect(Collectors.toSet());
	}

	public static Set<EntityPixelmon> world(World world) {
		Set<EntityPixelmon> wiped = ((net.minecraft.world.World) world).loadedEntityList.stream()
				.filter(entity -> entity instanceof EntityPixelmon)
				.map(entity -> (EntityPixelmon) entity)
				.filter(pokemon -> !Config.getConditions().test(pokemon))
				.collect(Collectors.toSet());

		lastWipe.put(world.getProperties().getWorldName(), wiped.size());

		return wiped;
	}

	public static int getTotalWiped() {
		return lastWipe.values().stream().mapToInt(Integer::intValue).sum();
	}

	public static int getWiped(WorldProperties world) {
		return lastWipe.get(world.getWorldName());
	}
}
