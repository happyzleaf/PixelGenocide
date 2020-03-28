package com.happyzleaf.pixelgenocide;

import com.pixelmonmod.pixelmon.api.pokemon.ISpecType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.SpecValue;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NicknameSpec extends SpecValue<String> implements ISpecType {
	public NicknameSpec(String value) {
		super("nickname", value);
	}

	public List<String> getKeys() {
		return Arrays.asList("nickname", "nick");
	}

	public SpecValue<?> parse(@Nullable String arg) {
		return new NicknameSpec(arg);
	}

	public SpecValue<?> readFromNBT(NBTTagCompound nbt) {
		return this.parse(nbt.getString(NbtKeys.NICKNAME));
	}

	public void writeToNBT(NBTTagCompound nbt, SpecValue<?> value) {
		nbt.setString(NbtKeys.NICKNAME, value.value.toString());
	}

	public Class<? extends SpecValue<String>> getSpecClass() {
		return NicknameSpec.class;
	}

	public String toParameterForm(SpecValue<?> value) {
		return value.key + ":" + value.value.toString();
	}

	public Class<String> getValueClass() {
		return String.class;
	}

	public void apply(EntityPixelmon pixelmon) {
		apply(pixelmon.getPokemonData());
	}

	public void apply(Pokemon pokemon) {
		pokemon.setNickname(value);
	}

	public boolean matches(EntityPixelmon pixelmon) {
		return matches(pixelmon.getPokemonData());
	}

	public boolean matches(Pokemon pokemon) {
		return ObjectUtils.compare(value, pokemon.getNickname()) == 0;
	}

	public SpecValue<String> clone() {
		return new NicknameSpec(value);
	}
}
