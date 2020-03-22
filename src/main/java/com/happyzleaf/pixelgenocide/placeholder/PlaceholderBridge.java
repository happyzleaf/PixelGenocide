package com.happyzleaf.pixelgenocide.placeholder;

import com.happyzleaf.pixelgenocide.PixelGenocide;
import com.happyzleaf.pixelgenocide.Wipe;
import com.happyzleaf.pixelgenocide.util.Helper;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Token;
import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaceholderBridge {
	public static void init(Object plugin) {
		Sponge.getServiceManager().provideUnchecked(PlaceholderService.class).loadAll(new PlaceholderBridge(), plugin).forEach(o -> {
			try {
				o.author("happyzleaf").tokens("timer_seconds", "timer_human", "wiped").buildAndRegister();
			} catch (Exception e) {
				PixelGenocide.LOGGER.error("There was a problem while trying to register the placeholders.", e);
			}
		});
	}

	@Placeholder(id = "timer")
	public Object timer(@Token String token) throws NoValueException {
		switch (token.toLowerCase()) {
			case "seconds":
				return PixelGenocide.task.getRemainingSeconds();
			case "human":
				return Helper.toHuman(PixelGenocide.task.getRemainingSeconds());
		}

		throw new NoValueException("Missing token.", new ArrayList<>(Arrays.asList("seconds", "human")));
	}

	@Placeholder(id = "wiped")
	public Object wiped() {
		return Wipe.lastWipeTotal();
	}
}
