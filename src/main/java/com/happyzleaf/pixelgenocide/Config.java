package com.happyzleaf.pixelgenocide;

import com.google.common.reflect.TypeToken;
import com.happyzleaf.pixelgenocide.placeholder.PlaceholderBridge;
import com.happyzleaf.pixelgenocide.util.GameTime;
import com.happyzleaf.pixelgenocide.util.Helper;
import com.happyzleaf.pixelgenocide.util.TimedTask;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Config {
	private static ConfigurationLoader<CommentedConfigurationNode> loader;
	private static CommentedConfigurationNode node;
	private static File file;
	
	public static GameTime timer = new GameTime(10, TimeUnit.MINUTES);
	private static String messageTimer = "&4Useless pok\u00E9mon will be wiped away in &c%timer_human%&4.";
	public static TimedTask.Info timerRate = new TimedTask.Info("s <= 5 ? 1 : s <= 15 ? 5 : s <= 60 ? 30 : s <= 600 ? 300 : s <= 1800 ? 600 : 1800");

	private static String messageCleaned = "&7%wiped% pok\u00E9mon have been wiped away.";
	private static int maxSpecialPlayerBlocks = 100;
	
	private static boolean keepLegendaries = true;
	private static boolean keepUltraBeasts = true;
	private static boolean keepBosses = true;
	private static boolean keepShinies = true;
	private static boolean keepWithPokerus = true;
	private static boolean keepWithParticles = false;
	private static boolean keepWithinSpecialPlayer = false;
	private static List<String> whitelist = new ArrayList<>();
	private static List<String> blacklist = new ArrayList<>();
	
	static {
		whitelist.add(EnumSpecies.Pikachu.name);
		whitelist.add(EnumSpecies.Eevee.name);
		whitelist.add(EnumSpecies.Ditto.name);
		
		blacklist.add(EnumSpecies.Zubat.name);
		blacklist.add(EnumSpecies.Geodude.name);
		blacklist.add(EnumSpecies.Caterpie.name);
	}
	
	public static void init(ConfigurationLoader<CommentedConfigurationNode> loader, File file) {
		Config.loader = loader;
		Config.file = file;
		
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(GameTime.class), new GameTime.Serializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TimedTask.Info.class), new TimedTask.InfoSerializer());
		
		loadConfig();
	}
	
	public static void loadConfig() {
		if (!file.exists()) {
			saveConfig();
		}
		
		load();
		
		ConfigurationNode miscellaneous = node.getNode("miscellaneous");
		try {
			timer = miscellaneous.getNode("timer").getValue(TypeToken.of(GameTime.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}

		try {
			timerRate = miscellaneous.getNode("timerRate").getValue(TypeToken.of(TimedTask.Info.class));
		} catch (ObjectMappingException e) {
			timerRate = new TimedTask.Info("0");

			PixelGenocide.LOGGER.error("Couldn't deserialize the timerRate. Nothing will be broadcasted.", e);
		}

		maxSpecialPlayerBlocks = miscellaneous.getNode("maxSpecialPlayerBlocks").getInt();
		
		ConfigurationNode message = miscellaneous.getNode("message");
		messageTimer = message.getNode("timer").getString();
		messageCleaned = message.getNode("cleaned").getString();
		
		CommentedConfigurationNode keep = node.getNode("keep");
		keepLegendaries = keep.getNode("legendaries").getBoolean();
		keepUltraBeasts = keep.getNode("ultraBeasts").getBoolean();
		keepBosses = keep.getNode("bosses").getBoolean();
		keepShinies = keep.getNode("shinies").getBoolean();
		keepWithPokerus = keep.getNode("withPokerus").getBoolean();
		keepWithParticles = keep.getNode("withParticles").getBoolean();
		if (keepWithParticles) {
			PluginContainer ep = Sponge.getPluginManager().getPlugin("entity-particles").orElse(null);
			if (ep == null) {
				PixelGenocide.LOGGER.info("entity-particles was not found, the support (most likely) won't work.");
			} else {
				if (ep.getVersion().orElse("").equals("2.1")) {
					PixelGenocide.LOGGER.info("entity-particles found, the support has been enabled.");
					keepWithParticles = false;
				} else {
					PixelGenocide.LOGGER.info("entity-particles found, but it's an untested version, please set \"keep.withParticles\" to \"false\" if you encounter any problem.");
				}
			}
		}
		keepWithinSpecialPlayer = keep.getNode("withinSpecialPlayer").getBoolean();
		try {
			whitelist = keep.getNode("whitelist").getList(TypeToken.of(String.class));
			blacklist = keep.getNode("blacklist").getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveConfig() {
		load();
		
		CommentedConfigurationNode miscellaneous = node.getNode("miscellaneous");
		try {
			miscellaneous.getNode("timer").setValue(new TypeToken<GameTime>() {}, timer);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		try {
			miscellaneous.getNode("timerRate").setComment("How often the remaining time till cleaning should be displayed.").setValue(TypeToken.of(TimedTask.Info.class), timerRate);
		} catch (ObjectMappingException e) {
			PixelGenocide.LOGGER.error("Couldn't serialize the timerRate. Problems will be encountered.", e);
		}

		miscellaneous.getNode("maxSpecialPlayerBlocks").setComment("How many blocks the pixelmon will not be removed within a special player. See keep.withinSpecialPlayer for more details.").setValue(maxSpecialPlayerBlocks);
		
		CommentedConfigurationNode message = miscellaneous.getNode("message");
		message.getNode("timer").setComment("Placeholders: %timer_seconds% %timer_human%.").setValue(messageTimer);
		message.getNode("cleaned").setComment("Placeholders: %wiped%.").setValue(messageCleaned);
		
		CommentedConfigurationNode keep = node.getNode("keep").setComment("Whether the pixelmon should be kept.");
		keep.getNode("legendaries").setValue(keepLegendaries);
		keep.getNode("ultraBeasts").setValue(keepUltraBeasts);
		keep.getNode("bosses").setValue(keepBosses);
		keep.getNode("shinies").setValue(keepShinies);
		keep.getNode("withPokerus").setValue(keepWithPokerus);
		keep.getNode("withParticles").setComment("You will need entity-particles for this to work.").setValue(keepWithParticles);
		keep.getNode("withinSpecialPlayer").setComment("The pixelmon will not be cleared if they're near a player with the permission '" + PixelGenocide.PLUGIN_ID + ".specialplayer'. WARNING: Could cost performance.").setValue(keepWithinSpecialPlayer);
		keep.getNode("whitelist").setComment("Keep these pixelmon regardless their specs.").setValue(whitelist);
		keep.getNode("blacklist").setComment("Remove these pixelmon regardless their specs.").setValue(blacklist);
		
		save();
	}
	
	private static void load() {
		try {
			node = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void save() {
		try {
			loader.save(node);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Text getMessageTimer(long remainingSeconds) {
		return PlaceholderBridge.parseText(
				messageTimer.replace("%timer_seconds%", String.valueOf(remainingSeconds))
						.replace("%timer_human%", Helper.toHuman(remainingSeconds)),
				null,
				null
		);
	}

	public static Text getMessageWiped(int wiped) {
		return PlaceholderBridge.parseText(
				Config.messageCleaned.replace("%wiped%", String.valueOf(wiped))
						.replace("HAVEHAS", wiped == 1 ? "have" : "has"),
				null,
				null
		);
	}

	public static boolean shouldKeepPokemon(EntityPixelmon pixelmon) {
		String name = pixelmon.getPokemonName();
		return whitelist.contains(name)
				|| !blacklist.contains(name)
				&& (keepLegendaries && EnumSpecies.legendaries.contains(name)
				|| keepUltraBeasts && EnumSpecies.ultrabeasts.contains(name)
				|| keepBosses && pixelmon.isBossPokemon()
				|| keepShinies && pixelmon.getPokemonData().isShiny()
				|| keepWithPokerus && pixelmon.getPokerus().isPresent()
				|| keepWithParticles && hasParticles((Entity) pixelmon)
				|| keepWithinSpecialPlayer && isWithinSpecialPlayer((Entity) pixelmon));
	}
	
	private static boolean hasParticles(Entity entity) {
//		Key<Value<String>> idKey = (Key<Value<String>>) entity.getKeys().stream().filter(key -> key.getId().equals("entity-particles:id")).findFirst().orElse(null);
//		if (idKey != null) {
//			String key = entity.get(idKey).orElse(null);
//			if (key != null) {
//				//the entity has an aura which id is "key"
//			}
//		}
		return entity.getKeys().stream().anyMatch(key -> key.getId().equals("entity-particles:id")); // Will provide support for "active" value later
	}
	
	private static boolean isWithinSpecialPlayer(Entity entity) {
		return Sponge.getServer().getOnlinePlayers().stream()
				.filter(player -> player.hasPermission(PixelGenocide.PLUGIN_ID + ".specialplayer"))
				.anyMatch(player -> player.getPosition().distance(entity.getLocation().getPosition()) <= maxSpecialPlayerBlocks);
	}
}
