package com.happyzleaf.pixelgenocide.condition;

import com.flowpowered.math.vector.Vector3d;
import com.happyzleaf.pixelgenocide.PixelGenocide;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;

@ConfigSerializable
public class WithinSpecialPlayerCondition implements Condition {
	@Setting
	public boolean enabled;

	@Setting
	public int maxBlocksDistance;

	public WithinSpecialPlayerCondition(boolean enabled, int maxBlocksDistance) {
		this.enabled = enabled;
		this.maxBlocksDistance = maxBlocksDistance;
	}

	public WithinSpecialPlayerCondition() {}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean test(EntityPixelmon pokemon) {
		Vector3d position = ((Entity) pokemon).getLocation().getPosition();
		return Sponge.getServer().getOnlinePlayers().stream()
				.filter(player -> player.hasPermission(PixelGenocide.PLUGIN_ID + ".specialplayer"))
				.anyMatch(player -> player.getPosition().distance(position) <= maxBlocksDistance);
	}
}
