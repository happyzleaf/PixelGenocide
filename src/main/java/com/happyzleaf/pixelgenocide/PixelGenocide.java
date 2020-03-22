package com.happyzleaf.pixelgenocide;

import com.google.inject.Inject;
import com.happyzleaf.pixelgenocide.util.TimedTask;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
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
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Plugin(id = PixelGenocide.PLUGIN_ID, name = PixelGenocide.PLUGIN_NAME, version = PixelGenocide.VERSION, authors = {"happyzleaf"},
		description = "PixelGenocide cleans all the non-special pixelmon in the server to reduce lag.",
		url = "http://happyzleaf.com/", dependencies = @Dependency(id = "pixelmon", version = "7.2.2"))
public class PixelGenocide {
	public static final String PLUGIN_ID = "pixelgenocide";
	public static final String PLUGIN_NAME = "PixelGenocide";
	public static final String VERSION = "1.0.6";

	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);

	private static TimedTask task = new TimedTask(PixelGenocide::cleanPixelmon, s -> MessageChannel.TO_ALL.send(PGConfig.getMessageTimer(s)));

	private static URL getWebsite() {
		try {
			return new URL("https://happyzleaf.com/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	@DefaultConfig(sharedRoot = true)
	ConfigurationLoader<CommentedConfigurationNode> configLoader;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private File configFile;

	@Listener
	public void init(GameInitializationEvent event) {
		PGConfig.init(configLoader, configFile);

		CommandSpec clean = CommandSpec.builder()
				.arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
				.executor((src, args) -> {
					Optional<WorldInfo> optWorldInfo = args.getOne("world");
					if (optWorldInfo.isPresent()) {
						WorldInfo worldInfo = optWorldInfo.get();
						Optional<org.spongepowered.api.world.World> optWorld = Sponge.getServer().getWorld(worldInfo.getWorldName());
						if (optWorld.isPresent()) {
							org.spongepowered.api.world.World world = optWorld.get();
							if (src.hasPermission(PLUGIN_ID + ".command.cleanpixelmon." + world.getName())) {
								int quantity = cleanPixelmon((World) world);
								if (quantity > 0) {
									MessageChannel.TO_ALL.send(PGConfig.getMessageCleaned(quantity));
								}
								src.sendMessage(Text.of(TextColors.GREEN, "Successfully cleaned all the non-special pixelmon in " + world.getName()));
								return CommandResult.success();
							} else {
								src.sendMessage(Text.of(TextColors.RED, "You don't have the permission to clean that world!"));
							}
						} else {
							src.sendMessage(Text.of(TextColors.RED, "There's a problem with the world " + worldInfo.getWorldName() + "."));
						}
					} else {
						if (src.hasPermission(PLUGIN_ID + ".command.cleanpixelmon")) {
							cleanPixelmon();
							src.sendMessage(Text.of(TextColors.GREEN, "Successfully cleaned all the non-special pixelmon in all the worlds!"));
							return CommandResult.success();
						} else {
							src.sendMessage(Text.of(TextColors.RED, "You don't have the permission to clean all the worlds!"));
						}
					}
					return CommandResult.empty();
				})
				.build();
		CommandSpec main = CommandSpec.builder()
				.child(clean, "clean")
				.executor((src, args) -> {
					src.sendMessage(Text.of(TextColors.GREEN, PLUGIN_NAME, TextColors.DARK_GREEN, " v" + VERSION + " made by ").concat(Text.builder("happyzleaf").style(TextStyles.UNDERLINE).color(TextColors.GREEN).onHover(TextActions.showText(Text.of(TextColors.GREEN, "Click to go to my *wonderful* website"))).onClick(TextActions.openUrl(getWebsite())).build()).concat(Text.of(TextColors.GREEN, ".")));
					return CommandResult.success();
				})
				.build();
		Sponge.getCommandManager().register(this, main, PLUGIN_ID);

		LOGGER.info(PLUGIN_NAME + " by happyzleaf loaded! (https://happyzleaf.com/)");
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		task.setDuration(PGConfig.timer, PGConfig.timerRate);
		task.start(this);
	}

	public static void cleanPixelmon() {
		int quantity = 0;
		for (org.spongepowered.api.world.World world : Sponge.getServer().getWorlds()) {
			quantity += cleanPixelmon((World) world);
		}
		if (quantity > 0) {
			MessageChannel.TO_ALL.send(PGConfig.getMessageCleaned(quantity));
		}
	}

	public static int cleanPixelmon(World world) {
		int quantity = 0;
		for (Entity entity : world.loadedEntityList) {
			if (entity instanceof EntityPixelmon) {
				EntityPixelmon pixelmon = (EntityPixelmon) entity;
				if (!(!pixelmon.canDespawn || pixelmon.hasOwner() || pixelmon.battleController != null || pixelmon.getPokemonData().isInRanch() || PGConfig.shouldKeepPokemon(pixelmon))) {
					pixelmon.unloadEntity();
					quantity++;
				}
			}
		}
		return quantity;
	}

	@Listener
	public void onReload(GameReloadEvent event) {
		task.cancel();
		PGConfig.loadConfig();
		task.setDuration(PGConfig.timer, PGConfig.timerRate);
		task.start(this);

		MessageReceiver receiver = Sponge.getServer().getConsole();
		if (event.getSource() instanceof MessageReceiver) {
			receiver = (MessageReceiver) event.getSource();
		}
		receiver.sendMessage(Text.of(TextColors.GREEN, "[PixelGenocide] Reloaded! The timer has been restarted."));
	}
}
