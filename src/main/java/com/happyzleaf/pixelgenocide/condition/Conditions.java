package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.*;

public class Conditions {
	private static IsValidCondition isValid = new IsValidCondition(false);

	private SpeciesCondition whitelist = new SpeciesCondition(true, EnumSpecies.Pikachu, EnumSpecies.Eevee, EnumSpecies.Ditto).setWhitelist();
	private SpeciesCondition blacklist = new SpeciesCondition(false, EnumSpecies.Magikarp, EnumSpecies.Zubat, EnumSpecies.Geodude).setBlacklist();

	private Set<SpecCondition> specs = new HashSet<>(Arrays.asList(
			new SpecCondition("shiny", true, false),
			new SpecCondition("legendary:true", true, false)
	));

	private UltraBeastCondition ultraBeasts = new UltraBeastCondition(true);
	private BossCondition bosses = new BossCondition(true);
	private WithPokerusCondition withPokerus = new WithPokerusCondition(true);
	private WithParticlesCondition withParticles = new WithParticlesCondition(false);
	private WithinSpecialPlayerCondition withinSpecialPlayer = new WithinSpecialPlayerCondition(false, 100);

	private List<Condition> conditions = new ArrayList<>();

	public void load(CommentedConfigurationNode node) throws ObjectMappingException {
		conditions.clear();

		conditions.add(isValid = node.getNode("debug").getValue(TypeToken.of(IsValidCondition.class), isValid));
		conditions.add(whitelist = node.getNode("whitelist").getValue(TypeToken.of(SpeciesCondition.class), whitelist).setWhitelist());
		conditions.add(blacklist = node.getNode("blacklist").getValue(TypeToken.of(SpeciesCondition.class), blacklist).setBlacklist());
		conditions.addAll(specs = Optional.ofNullable(node.getNode("specs").getList(TypeToken.of(SpecCondition.class), (List<SpecCondition>) null))
				.map(s -> ((Set<SpecCondition>) new HashSet<>(s)))
				.orElse(specs));
		conditions.add(ultraBeasts = node.getNode("ultraBeasts").getValue(TypeToken.of(UltraBeastCondition.class), ultraBeasts));
		conditions.add(bosses = node.getNode("bosses").getValue(TypeToken.of(BossCondition.class), bosses));
		conditions.add(withPokerus = node.getNode("withPokerus").getValue(TypeToken.of(WithPokerusCondition.class), withPokerus));
		conditions.add(withParticles = node.getNode("withParticles").getValue(TypeToken.of(WithParticlesCondition.class), withParticles));
		conditions.add(withinSpecialPlayer = node.getNode("withinSpecialPlayer").getValue(TypeToken.of(WithinSpecialPlayerCondition.class), withinSpecialPlayer));

		conditions.removeIf(c -> !c.isEnabled());
	}

	public void save(CommentedConfigurationNode node) throws ObjectMappingException {
		node.getNode("debug").setComment("This is an experimental feature and should remain 'false'. If set to 'true', this lets you skip the check for pokémon that are not supposed so despawn. AKA lets you wipe pokémon from /pokespawn. This is intended for debugging purposes and could potentially break other plugins behavior.")
				.setValue(TypeToken.of(IsValidCondition.class), isValid);
		node.getNode("whitelist").setValue(TypeToken.of(SpeciesCondition.class), whitelist);
		node.getNode("blacklist").setValue(TypeToken.of(SpeciesCondition.class), blacklist);
		node.getNode("specs").setValue(new TypeToken<List<SpecCondition>>() {}, new ArrayList<>(specs));
		node.getNode("ultraBeasts").setValue(TypeToken.of(UltraBeastCondition.class), ultraBeasts);
		node.getNode("bosses").setValue(TypeToken.of(BossCondition.class), bosses);
		node.getNode("withPokerus").setValue(TypeToken.of(WithPokerusCondition.class), withPokerus);
		node.getNode("withParticles").setValue(TypeToken.of(WithParticlesCondition.class), withParticles);
		node.getNode("withinSpecialPlayer").setValue(TypeToken.of(WithinSpecialPlayerCondition.class), withinSpecialPlayer);
	}

	public boolean test(EntityPixelmon pokemon) {
		for (Condition c : conditions) {
			if (c.test(pokemon)) {
				return !c.isAnti();
			}
		}

		return false;
	}
}
