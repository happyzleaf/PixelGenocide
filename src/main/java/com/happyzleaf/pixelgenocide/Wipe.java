package com.happyzleaf.pixelgenocide;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

import java.util.stream.Stream;

public class Wipe {
	private static long lastWipeTotal = 0;
	public static long all() {
		return lastWipeTotal = Sponge.getServer().getWorlds().stream().mapToInt(world -> world((World) world)).sum();
	}

	public static long lastWipeTotal() {
		return lastWipeTotal;
	}

	public static int world(World world) {
		Stream<EntityPixelmon> entities = world.loadedEntityList.stream()
				.filter(entity -> entity instanceof EntityPixelmon)
				.map(entity -> (EntityPixelmon) entity)
				.filter(pokemon -> !(!pokemon.canDespawn // If the pokémon can despawn
						|| pokemon.hasOwner() // doesn't have any owner
						|| pokemon.battleController != null // isn't in a battle
						|| pokemon.getPokemonData().isInRanch() // isn't in a ranch
						|| Config.shouldKeepPokemon(pokemon))); // and isn't excluded by the config

		entities.forEach(EntityPixelmon::unloadEntity); // terminate its existence :)

		return (int) entities.count();
	}
}
