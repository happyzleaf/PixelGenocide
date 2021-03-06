package com.happyzleaf.pixelgenocide;

import com.google.common.reflect.TypeToken;
import com.happyzleaf.pixelgenocide.condition.*;
import com.happyzleaf.pixelgenocide.placeholder.PlaceholderBridge;
import com.happyzleaf.pixelgenocide.util.GameTime;
import com.happyzleaf.pixelgenocide.util.Helper;
import com.happyzleaf.pixelgenocide.util.TimedTask;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Config {
	private static Object plugin;
	private static ConfigurationLoader<CommentedConfigurationNode> loader;
	private static CommentedConfigurationNode node;
	private static Path path;

	private static GameTime timerDuration = new GameTime(10, TimeUnit.MINUTES);
	private static TimedTask.Info timerRate = new TimedTask.Info("s <= 5 ? 1 : s <= 15 ? 5 : s <= 60 ? 30 : s <= 600 ? 300 : s <= 1800 ? 600 : 1800");

	private static String messageTimer = "&4Useless pok\u00E9mon will be wiped away in &c%timer_human%&4.";
	private static String messageCleaned = "&a%wiped% pok\u00E9mon have been wiped away.";

	private static Conditions conditions = new Conditions();

	private static TimedTask task = new TimedTask(() -> {
		Task.builder().async().execute(() -> {
			Set<EntityPixelmon> toWipe = Wipe.all();
			Task.builder().execute(() -> {
				int wiped = (int) toWipe.stream().peek(EntityPixelmon::unloadEntity).count();
				if (wiped > 0) {
					broadcastMessageWiped(MessageChannel.TO_ALL, wiped);
				}
			}).submit(plugin);
		}).submit(plugin);
	}, s -> broadcastMessageTimer(MessageChannel.TO_ALL, s));

	public static void init(Object plugin) {
		Config.plugin = plugin;

		ConfigRoot root = Sponge.getConfigManager().getSharedConfig(plugin);
		loader = root.getConfig();
		path = root.getConfigPath();

		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(GameTime.class), new GameTime.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TimedTask.Info.class), new TimedTask.InfoSerializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(IsValidCondition.class), new IsValidCondition.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(SpecCondition.class), new SpecCondition.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(SpeciesCondition.class), new SpeciesCondition.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(UltraBeastCondition.class), new UltraBeastCondition.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(BossCondition.class), new BossCondition.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(WithPokerusCondition.class), new WithPokerusCondition.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(WithParticlesCondition.class), new WithParticlesCondition.Serializer());

		loadConfig();
	}

	public static void loadConfig() {
		try {
			task.cancel();

			if (Files.notExists(path)) {
				node = loader.createEmptyNode();

				saveConfig();
			}

			node = loader.load();

			timerDuration = node.getNode("timer", "duration").getValue(TypeToken.of(GameTime.class));

			try {
				timerRate = node.getNode("timer", "rate").getValue(TypeToken.of(TimedTask.Info.class));
			} catch (ObjectMappingException e) {
				timerRate = new TimedTask.Info("0");

				PixelGenocide.LOGGER.error("Couldn't deserialize the timer rate. Nothing will be broadcasted.", e);
			}

			messageTimer = node.getNode("broadcast", "timer").getString();
			messageCleaned = node.getNode("broadcast", "cleaned").getString();

			conditions.load(node.getNode("conditions"));

			startTask();

			// Forces a save to update old versions
			saveConfig();
		} catch (Exception e) {
			PixelGenocide.LOGGER.error("There was a problem while loading the configuration.", e);
		}
	}

	public static void saveConfig() {
		try {
			node.getNode("timer").setComment("You can find more info at https://github.com/happyzleaf/PixelGenocide/wiki/Configuration#timer");
			node.getNode("timer", "duration").setValue(TypeToken.of(GameTime.class), timerDuration);
			node.getNode("timer", "rate").setComment("How often the remaining time till cleaning should be displayed.").setValue(TypeToken.of(TimedTask.Info.class), timerRate);

			node.getNode("broadcast").setComment("You can find more info at https://github.com/happyzleaf/PixelGenocide/wiki/Configuration#broadcast");
			node.getNode("broadcast", "timer").setComment("Placeholders: %timer_seconds% %timer_human%. Leave empty to disable.").setValue(messageTimer);
			node.getNode("broadcast", "cleaned").setComment("Placeholders: %wiped%. Leave empty to disable.").setValue(messageCleaned);

			node.getNode("conditions").setComment("You can find more info at https://github.com/happyzleaf/PixelGenocide/wiki/Configuration#conditions");
			conditions.save(node.getNode("conditions"));

			loader.save(node);
		} catch (Exception e) {
			PixelGenocide.LOGGER.error("There was a problem while saving the configuration.", e);
		}
	}

	public static void broadcastMessageTimer(MessageChannel channel, long remainingSeconds) {
		if (!messageTimer.isEmpty()) {
			channel.send(PlaceholderBridge.parseText(
					messageTimer.replace("%timer_seconds%", String.valueOf(remainingSeconds))
							.replace("%timer_human%", Helper.toHuman(remainingSeconds)),
					null,
					null
			));
		}
	}

	public static void broadcastMessageWiped(MessageChannel channel, int wiped) {
		if (!Config.messageCleaned.isEmpty()) {
			channel.send(PlaceholderBridge.parseText(
					Config.messageCleaned.replace("%wiped%", String.valueOf(wiped)),
					null,
					null
			));
		}
	}

	public static Conditions getConditions() {
		return conditions;
	}

	public static TimedTask getTask() {
		return task;
	}

	public static void startTask() {
		task.setDuration(timerDuration, timerRate);
		task.start(plugin);
	}
}
