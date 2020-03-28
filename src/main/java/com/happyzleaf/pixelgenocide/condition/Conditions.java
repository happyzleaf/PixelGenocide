package com.happyzleaf.pixelgenocide.condition;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.*;

public class Conditions {
	private static final IsValidCondition IS_VALID = new IsValidCondition();

	private SpeciesCondition whitelist = new SpeciesCondition(true, EnumSpecies.Pikachu, EnumSpecies.Eevee, EnumSpecies.Ditto).setWhitelist();
	private SpeciesCondition blacklist = new SpeciesCondition(false, EnumSpecies.Magikarp, EnumSpecies.Zubat, EnumSpecies.Geodude).setBlacklist();

	private Set<SpecCondition> specs = new HashSet<>(Arrays.asList(
			new SpecCondition("shiny", true, false),
			new SpecCondition("boss:4", true, false),
			new SpecCondition("pokerus:a", true, false),
			new SpecCondition("pokerus:b", true, false),
			new SpecCondition("pokerus:c", true, false),
			new SpecCondition("pokerus:d", true, false),
			new SpecCondition("legendary:true", true, false)
	));

	private UltraBeastCondition ultraBeasts = new UltraBeastCondition(true);
	private WithParticlesCondition withParticles = new WithParticlesCondition(false);
	private WithinSpecialPlayerCondition withinSpecialPlayer = new WithinSpecialPlayerCondition(false, 100);

	private List<Condition> conditions = new ArrayList<>();

	public void load(CommentedConfigurationNode node) throws ObjectMappingException {
		conditions.clear();

		conditions.add(IS_VALID);
		conditions.add(whitelist = node.getNode("whitelist").getValue(TypeToken.of(SpeciesCondition.class)).setWhitelist());
		conditions.add(blacklist = node.getNode("blacklist").getValue(TypeToken.of(SpeciesCondition.class)).setBlacklist());
		conditions.addAll(specs = new HashSet<>(node.getNode("specs").getList(TypeToken.of(SpecCondition.class))));
		conditions.add(ultraBeasts = node.getNode("ultraBeasts").getValue(TypeToken.of(UltraBeastCondition.class)));
		conditions.add(withParticles = node.getNode("withParticles").getValue(TypeToken.of(WithParticlesCondition.class)));
		conditions.add(withinSpecialPlayer = node.getNode("withinSpecialPlayer").getValue(TypeToken.of(WithinSpecialPlayerCondition.class)));

		conditions.removeIf(c -> !c.isEnabled());
	}

	public void save(CommentedConfigurationNode node) throws ObjectMappingException {
		node.getNode("whitelist").setValue(TypeToken.of(SpeciesCondition.class), whitelist);
		node.getNode("blacklist").setValue(TypeToken.of(SpeciesCondition.class), blacklist);
		node.getNode("specs").setValue(new TypeToken<List<SpecCondition>>() {}, new ArrayList<>(specs));
		node.getNode("ultraBeasts").setValue(TypeToken.of(UltraBeastCondition.class), ultraBeasts);
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
