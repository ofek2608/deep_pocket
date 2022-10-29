package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public final class RecipeRequest {
	private final ItemType result;
	private final long amount;
	private final UUID[] patterns;

	public RecipeRequest(ItemType result, long amount, UUID[] patterns) {
		this.result = result;
		this.amount = amount;
		this.patterns = patterns.clone();
	}

	private RecipeRequest(ItemType result, long amount, UUID[] patterns, int a) {
		this.result = result;
		this.amount = amount;
		this.patterns = patterns;
	}

	public ItemType getResult() {
		return result;
	}

	public long getAmount() {
		return amount;
	}

	public UUID[] getPatterns() {
		return patterns.clone();
	}

	public int getPatternsCount() {
		return patterns.length;
	}

	public UUID getPattern(int index) {
		return patterns[index];
	}



	public static void encode(FriendlyByteBuf buf, RecipeRequest recipeRequest) {
		ItemType.encode(buf, recipeRequest.result);
		buf.writeLong(recipeRequest.amount);
		DeepPocketUtils.encodeArray(buf, recipeRequest.patterns, FriendlyByteBuf::writeUUID);
	}

	public static RecipeRequest decode(FriendlyByteBuf buf) {
		return new RecipeRequest(
						ItemType.decode(buf),
						buf.readLong(),
						DeepPocketUtils.decodeArray(buf, UUID[]::new, FriendlyByteBuf::readUUID),
						0
		);
	}
}
