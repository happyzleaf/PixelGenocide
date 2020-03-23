package com.happyzleaf.pixelgenocide.placeholder;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class PlaceholderBridge {
	private static PlaceholderAPI service = null;

	public static void init(Object plugin) {
		if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
			service = new PlaceholderAPI(plugin);
		}
	}

	public static Text parseText(String message, Object source, Object observer) {
		Text text = TextSerializers.FORMATTING_CODE.deserialize(message);
		if (service != null) {
			return service.parseText(text, source, observer);
		}

		return text;
	}
}
