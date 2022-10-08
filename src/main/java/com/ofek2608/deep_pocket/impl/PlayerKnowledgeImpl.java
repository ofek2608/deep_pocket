package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.PlayerKnowledge;
import com.ofek2608.deep_pocket.integration.DeepPocketFTBTeams;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

class PlayerKnowledgeImpl implements PlayerKnowledge {
	private final Set<ItemType> set = new HashSet<>();
	private final @Nullable DeepPocketServerApiImpl api;
	final @Nullable UUID player;

	PlayerKnowledgeImpl() {
		this.api = null;
		this.player = null;
	}

	PlayerKnowledgeImpl(DeepPocketServerApiImpl api, UUID player) {
		this.api = api;
		this.player = player;
	}

	PlayerKnowledgeImpl(DeepPocketServerApiImpl api, CompoundTag saved) {
		this.api = api;
		this.player = saved.getUUID("player");
		for (Tag item : saved.getList("items", 10))
			set.add(ItemType.load((CompoundTag)item));
	}

	CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("player", player == null ? net.minecraft.Util.NIL_UUID : player);
		ListTag items = new ListTag();
		for (ItemType item : this.set)
			items.add(item.save());
		saved.put("items", items);
		return saved;
	}


	@Override
	public boolean contains(ItemType type) {
		return set.contains(type);
	}

	void execute(Predicate<Set<ItemType>> action, Consumer<PacketDistributor.PacketTarget> send) {
		if (!action.test(set))
			return;
		if (api == null || player == null)
			return;
		send.accept(DeepPocketFTBTeams.teamPacketTarget(player));
		for (UUID teamMember : DeepPocketFTBTeams.getTeamMembers(false, player)) {
			if (teamMember.equals(player))
				continue;
			action.test(api.getKnowledge(teamMember).set);
		}
	}

	@Override
	public void add(ItemType... types) {
		List<ItemType> typesAsList = Arrays.asList(types);
		execute(set->set.addAll(typesAsList), target-> DeepPocketPacketHandler.cbAddKnowledge(target, types));
	}

	@Override
	public void remove(ItemType... types) {
		List<ItemType> typesAsList = Arrays.asList(types);
		typesAsList.forEach(set::remove);
		execute(set->{
			boolean changed = false;
			for (ItemType type : types)
				changed = set.remove(type) || changed;
			return changed;
		}, target-> DeepPocketPacketHandler.cbRemoveKnowledge(target, types));
	}

	@Override
	public @UnmodifiableView Set<ItemType> asSet() {
		return Collections.unmodifiableSet(set);
	}

	@Override
	public void clear() {
		execute(set->{
			if (set.isEmpty())
				return false;
			set.clear();
			return true;
		}, DeepPocketPacketHandler::cbClearKnowledge);
	}
}
