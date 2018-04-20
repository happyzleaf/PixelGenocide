package com.happyzleaf.pixelgenocide;

import de.randombyte.entityparticles.data.EntityParticlesKeys;
import org.spongepowered.api.entity.Entity;

public class EPBridge {
	public static boolean hasParticles(Entity entity) {
		return entity.get(EntityParticlesKeys.PARTICLE_ID).isPresent();
	}
}
