package com.happyzleaf.pixelgenocide;

import com.happyzleaf.pixelgenocide.placeholder.PlaceholderBridge;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.net.MalformedURLException;
import java.net.URL;

@Plugin(id = PixelGenocide.PLUGIN_ID, name = PixelGenocide.PLUGIN_NAME, version = PixelGenocide.VERSION,
		description = "PixelGenocide wipes away useless pok\u00E9mon to prevent lag.",
		authors = {"happyzleaf"}, url = "http://happyzleaf.com/",
		dependencies = {@Dependency(id = "pixelmon", version = "7.2.2"), @Dependency(id = "placeholderapi", optional = true)})
public class PixelGenocide {
	public static final String PLUGIN_ID = "pixelgenocide";
	public static final String PLUGIN_NAME = "PixelGenocide";
	public static final String VERSION = "1.1.0-hotfix";

	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);

	private static URL getWebsite() {
		try {
			return new URL("https://happyzleaf.com/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Listener
	public void init(GameInitializationEvent event) {
		Config.init(this);

		Sponge.getCommandManager().register(this, CommandSpec.builder()
				.child(CommandSpec.builder()
						.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.world(Text.of("world")))))
						.executor((src, args) -> {
							WorldProperties worldInfo = args.<WorldProperties>getOne("world").orElse(null);
							if (worldInfo == null) {
								if (!src.hasPermission(PLUGIN_ID + ".command.wipepokemon")) {
									throw new CommandException(Text.of(TextColors.RED, "You don't have the permission to wipe all the worlds!"));
								}

								int wiped = (int) Wipe.all().stream().peek(EntityPixelmon::unloadEntity).count();
								if (wiped > 0) {
									MessageChannel.TO_ALL.send(Config.getMessageWiped(wiped));
								}

								src.sendMessage(Text.of(TextColors.GREEN, "Successfully wiped away useless pok\u00E9mon in every world."));
								return CommandResult.success();
							}

							World world = Sponge.getServer().getWorld(worldInfo.getUniqueId()).orElse(null);
							if (world == null) {
								throw new CommandException(Text.of(TextColors.RED, String.format("There's a problem with the world %s.", worldInfo.getWorldName())));
							}

							if (!src.hasPermission(PLUGIN_ID + ".command.wipepokemon." + world.getName())) {
								throw new CommandException(Text.of(TextColors.RED, "You don't have the permission to wipe that world!"));
							}

							int wiped = (int) Wipe.world(world).stream().peek(EntityPixelmon::unloadEntity).count();
							if (wiped > 0) {
								MessageChannel.combined(MessageChannel.world(world), MessageChannel.fixed(src)).send(Config.getMessageWiped(wiped));
							}

							src.sendMessage(Text.of(TextColors.GREEN, String.format("Successfully wiped away useless pok\u00E9mon in %s.", world.getName())));
							return CommandResult.success();
						})
						.build(), "wipe")
				.executor((src, args) -> {
					src.sendMessage(Text.of(TextColors.GREEN, PLUGIN_NAME, TextColors.DARK_GREEN, " v" + VERSION + " made by ").concat(Text.builder("happyzleaf").style(TextStyles.UNDERLINE).color(TextColors.GREEN).onHover(TextActions.showText(Text.of(TextColors.GREEN, "Click to go to my *wonderful* website"))).onClick(TextActions.openUrl(getWebsite())).build()).concat(Text.of(TextColors.GREEN, ".")));
					return CommandResult.success();
				})
				.build(), PLUGIN_ID);

		LOGGER.info(PLUGIN_NAME + " by happyzleaf loaded! (https://happyzleaf.com/)");
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		PlaceholderBridge.init(this);
	}

	@Listener
	public void onReload(GameReloadEvent event) {
		Config.loadConfig();
		((MessageReceiver) event.getSource()).sendMessage(Text.of(TextColors.GREEN, "[PixelGenocide] Reloaded! The timer has been restarted."));
	}
}
