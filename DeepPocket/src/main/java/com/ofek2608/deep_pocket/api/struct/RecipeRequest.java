package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class RecipeRequest {
	private final ElementType result;
	private final long amount;
	private final UUID pattern;

	public RecipeRequest(ElementType result, long amount, UUID pattern) {
		this.result = result;
		this.amount = amount;
		this.pattern = pattern;
	}

	public ElementType getResult() {
		return result;
	}

	public long getAmount() {
		return amount;
	}

	public UUID getPattern() {
		return pattern;
	}



	public static void encode(FriendlyByteBuf buf, RecipeRequest recipeRequest) {
		ElementType.encode(buf, recipeRequest.result);
		buf.writeLong(recipeRequest.amount);
		buf.writeUUID(recipeRequest.pattern);
	}

	public static RecipeRequest decode(FriendlyByteBuf buf) {
		return new RecipeRequest(
						ElementType.decode(buf),
						buf.readLong(),
						buf.readUUID()
		);
	}
}
