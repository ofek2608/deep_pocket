package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.enums.CrafterStatus;
import com.ofek2608.deep_pocket.api.pocket_process.CrafterContext;
import com.ofek2608.deep_pocket.api.pocket_process.ProcessRecipe;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.api.struct.LevelBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.*;

final class PocketProcess {
	private PocketProcess() {}
	
	
	private final class ProcessRecipeImpl implements ProcessRecipe {
		private final UUID patternId;
		private final CraftingPattern pattern;
		private final ElementTypeStack[] patternInput;
		private final long[] elementCount;
		private long countNeeded, countCrafted, countCrafting;
		private final List<CrafterContext> contexts = new ArrayList<>();
		
		private ProcessRecipeImpl(UUID patternId, CraftingPattern pattern) {
			this.patternId = patternId;
			this.pattern = pattern;
			this.patternInput = pattern.getInputCountMap();
			this.elementCount = new long[patternInput.length];
		}
		
		@Override public UUID getPatternId() { return patternId; }
		@Override public CraftingPattern getPattern() { return pattern; }
		
		@Override public long getCountNeeded() { return countNeeded; }
		@Override public long getCountCrafted() { return countCrafted; }
		@Override public long getCountCrafting() { return countCrafting; }
		@Override public void setCountNeeded(long count) { this.countNeeded = count; }
		@Override public void setCountCrafted(long count) { this.countCrafted = count; }
		@Override public void setCountCrafting(long count) { this.countCrafting = count; }
		
		@Override
		public void dumpInventory(List<ElementTypeStack> toClear) {
			var iterator = toClear.iterator();
			while (iterator.hasNext()) {
				ElementTypeStack stack = iterator.next();
				for (int i = 0; i < patternInput.length; i++) {
					if (patternInput[i].getType().equals(stack.getType())) {
						elementCount[i] = advancedSum(elementCount[i], stack.getCount());
						iterator.remove();
						break;
					}
				}
			}
		}
		
		@Override
		public void requestInventory(List<ElementTypeStack> toFill) {
			//Validation
			for (int i = 0; i < patternInput.length; i++)
				if (advancedMin(patternInput[i].getCount(), elementCount[i]) != elementCount[i])
					return;
			//Subtraction
			for (int i = 0; i < patternInput.length; i++)
				elementCount[i] = advancedSub(elementCount[i], patternInput[i].getCount());
			//Addition
			toFill.addAll(Arrays.asList(patternInput));
		}
		
		@Override
		public void executeTick() {
		
		}
		
		@Override
		public boolean isFinished() {
			return advancedMin(countCrafted, countNeeded) == countNeeded;
		}
		
		@Override
		public CrafterStatus getStatus() {
			if (isFinished())
				return CrafterStatus.FINISHED;
			return contexts.stream().map(CrafterContext::getStatus).reduce(CrafterStatus.MISSING_CRAFTER, CrafterStatus::join);
		}
		
		@Override
		public CompoundTag saveData() {
			CompoundTag saved = new CompoundTag();
			//TODO save elementCount
			saved.putLong("countNeeded", countNeeded);
			saved.putLong("countCrafted", countCrafted);
			saved.putLong("countCrafting", countCrafting);
			//TODO save contexts
			return saved;
		}
		
		@Override
		public void loadData(CompoundTag saved) {
			//TODO load elementCount
			countNeeded = saved.getLong("countNeeded");
			countCrafted = saved.getLong("countCrafted");
			countCrafting = saved.getLong("countCrafting");
			//TODO load contexts
		}
	}
	
	private final class CrafterContextImpl implements CrafterContext {
		private final ProcessRecipe parent;
		private final LevelBlockPos position;
		private CrafterStatus status = CrafterStatus.WORKING;
		private final List<ElementTypeStack> inventory = new ArrayList<>();
		private boolean isCrafting;
		
		private CrafterContextImpl(ProcessRecipe parent, LevelBlockPos position) {
			this.parent = parent;
			this.position = position;
		}
		
		@Override
		public ProcessRecipe getParent() {
			return parent;
		}
		
		@Override
		public UUID getPatternId() {
			return parent.getPatternId();
		}
		
		@Override
		public CraftingPattern getPattern() {
			return parent.getPattern();
		}
		
		@Override
		public LevelBlockPos getPosition() {
			return position;
		}
		
		@Override
		public CrafterStatus getStatus() {
			return status;
		}
		
		@Override
		public void setStatus(CrafterStatus status) {
			this.status = status;
		}
		
		@Override
		public void startCrafting() {
			if (isCrafting)
				throw new IllegalStateException();
			parent.requestInventory(inventory);
			inventory.clear();
			isCrafting = true;
		}
		
		@Override
		public void cancelCrafting() {
			if (!isCrafting)
				return;
			parent.dumpInventory(inventory);
			inventory.clear();
			isCrafting = false;
		}
		
		@Override
		public void notifyFinishedCraft() {
			if (!isCrafting)
				return;
			parent.setCountCrafting(parent.getCountCrafting() + 1);
			inventory.clear();
			isCrafting = false;
		}
		
		@Override
		public boolean isCrafting() {
			return isCrafting;
		}
		
		@Override
		public List<ElementTypeStack> getInventory() {
			if (!isCrafting || inventory.isEmpty())
				parent.requestInventory(inventory);
			return inventory;
		}
		
		@Override
		public void instantCraftOne() {
			//TODO
		}
		
		@Override
		public void instantCraftAll() {
			//TODO
		}
	}
}
