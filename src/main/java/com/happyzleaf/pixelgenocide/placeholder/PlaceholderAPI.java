package com.happyzleaf.pixelgenocide.placeholder;

import com.happyzleaf.pixelgenocide.PixelGenocide;
import com.happyzleaf.pixelgenocide.Wipe;
import com.happyzleaf.pixelgenocide.util.Helper;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Token;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaceholderAPI {
	private PlaceholderService service;

	public PlaceholderAPI(Object plugin) {
		service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);

		service.loadAll(this, plugin).forEach(o -> {
			try {
				o.author("happyzleaf")
						.tokens("pixelgenocide_timer_seconds",
								"pixelgenocide_timer_human",
								"pixelgenocide_wiped",
								"pixelgenocide_wiped_<worldName>")
						.buildAndRegister();
			} catch (Exception e) {
				PixelGenocide.LOGGER.error("There was a problem while trying to register the placeholders.", e);
			}
		});
	}

	@Placeholder(id = "pixelgenocide")
	public Object pixelgenocide(@Token String token) throws NoValueException {
		String[] args = token.toLowerCase().split("_");
		if (args.length == 0) {
			throw new NoValueException("Missing token(s).", new ArrayList<>(Arrays.asList("timer_seconds", "timer_human", "wiped", "wiped_<worldName>")));
		}

		switch (args[0]) {
			case "timer":
				if (args.length != 2) {
					throw new NoValueException("Missing token(s) or too many arguments.", new ArrayList<>(Arrays.asList("seconds", "human")));
				}

				switch (args[1]) {
					case "seconds":
						return PixelGenocide.task.getRemainingSeconds();
					case "human":
						return Helper.toHuman(PixelGenocide.task.getRemainingSeconds());
					default:
						throw new NoValueException("Wrong input.", new ArrayList<>(Arrays.asList("seconds", "human")));
				}
			case "wiped":
				if (args.length == 1) {
					return Wipe.getTotalWiped();
				} else if (args.length == 2) {
					WorldProperties world = Sponge.getServer().getAllWorldProperties().stream()
							.filter(worldProperties -> worldProperties.getWorldName().toLowerCase().equals(args[1]))
							.findAny()
							.orElse(null);
					if (world == null) {
						throw new NoValueException(String.format("Wrong input. Cannot find world '%s'.", args[1]));
					}

					return Wipe.getWiped(world);
				} else {
					throw new NoValueException("Too many arguments.");
				}
			default:
				throw new NoValueException("Wrong input.", new ArrayList<>(Arrays.asList("timer_seconds", "timer_human", "wiped", "wiped_<worldName>")));
		}
	}

	public Text parseText(Text message, Object source, Object observer) {
		return service.replacePlaceholders(message, source, observer);
	}
}
