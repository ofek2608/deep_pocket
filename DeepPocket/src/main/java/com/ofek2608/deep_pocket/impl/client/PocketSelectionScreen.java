package com.ofek2608.deep_pocket.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.impl.ClientAPIImpl;
import com.ofek2608.deep_pocket.impl.PacketHandler;
import com.ofek2608.deep_pocket.impl.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.IntConsumer;

import static com.ofek2608.deep_pocket.impl.client.Sprite.rect;

public final class PocketSelectionScreen extends Screen {
	private final ClientAPIImpl api;
	
	private int offX, minY, maxY;
	private final List<PocketProperties> pockets = new ArrayList<>();
	private final ScrollComponent scroll = new ScrollComponent();
	private final SimpleEditBox searchText;
	private boolean hoveringScroll;
	private boolean hoveringSearch;
	private boolean hoveringCreate;
	private boolean draggingScroll;
	private IntConsumer scrollFunction = null;
	
	public PocketSelectionScreen(ClientAPIImpl api) {
		super(Component.empty());
		this.api = api;
		this.searchText = new SimpleEditBox(
				Minecraft.getInstance().font,
				0, 0, 88, 10,
				Component.translatable("gui.search_box")
		);
		scroll.elementHeight = 16;
		scroll.rowElementCount = 1;
	}
	
	@Override
	protected void init() {
		addRenderableWidget(this.searchText);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	private void updateWindowSize() {
		Window window = Minecraft.getInstance().getWindow();
		int width = window.getGuiScaledWidth();
		int height = window.getGuiScaledHeight();
		offX = width / 2;
		minY = height / 8;
		maxY = height * 7 / 8;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		updateWindowSize();
		renderBackground(poseStack);
		updatePocketList();
		renderPocketList(mx, my);
		
		if (scrollFunction != null) {
			renderScroll(mx, my, partialTick, scrollFunction);
		}
		
		RenderSystem.applyModelViewMatrix();
		super.render(poseStack, mx, my, partialTick);
	}
	
	private void updatePocketList() {
		pockets.clear();
		api.getVisiblePockets()
				.map(api::getProperties)
				.filter(this::matchesSearch)
				.forEach(pockets::add);
	}
	
	private boolean matchesSearch(PocketProperties properties) {
		return properties.getName().toLowerCase(Locale.ROOT).contains(searchText.getValue().toLowerCase(Locale.ROOT));
	}
	
	private void renderPocketList(int mx, int my) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Sprites.TEXTURE);
		
		int width = Sprites.WIDTH;
		int x = offX - width / 2;
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		scroll.setRect(x + 5, x + 149, minY + Sprites.FRAME_TOP.h, maxY - Sprites.FRAME_BOT.h);
		scroll.elementCount = pockets.size();
		
		int scrollHeight = scroll.getH();
		
		int y = minY;
		y = Sprites.FRAME_TOP.blit(x, y);
		y = Sprites.FRAME_ROW.blit(x, y, width, scrollHeight);
		y = Sprites.FRAME_BOT.blit(x, y);
		
		hoveringScroll = isHover(mx, my, x + 153, x + 161, minY + 19, minY + 19 + scrollHeight);
		hoveringSearch = isHover(mx, my, x + 61, x + 149, minY + 5, minY + 17);
		hoveringCreate = isHover(mx, my, x + 133, maxY - 21);
		
		searchText.x = x + 61;
		searchText.y = minY + 5;
		searchText.setWidth(88);
		searchText.setHeight(12);
		searchText.setBordered(true);
		
		(hoveringScroll || draggingScroll ? Sprites.SCROLL_H : Sprites.SCROLL_N).blit(x, scroll.getScrollbarY());
		(hoveringSearch ? Sprites.CONTENT_SEARCH_H : Sprites.CONTENT_SEARCH_N).blit(x, minY + 5);
		(canCreatePocket() ? hoveringCreate ? Sprites.BTN_ADD_H : Sprites.BTN_ADD_N : Sprites.BTN_ADD_D).blit(x + 133, maxY - 21);
		
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		
		
		
		scrollFunction = i -> {
			PocketProperties pocket = pockets.get(i);
			boolean hovering = i == scroll.hoveredIndex;
			int displayY = i * 16;
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, Sprites.TEXTURE);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			(hovering ? Sprites.CONTENT_POCKET_H : Sprites.CONTENT_POCKET_N).blit(x, displayY);
			GuiUtils.setShaderColor(pocket.getColor());
			Sprites.CONTENT_POCKET_COLOR.blit(x, displayY);
			
			Item item = ForgeRegistries.ITEMS.getValue(pocket.getIcon().id());
			
			itemRenderer.renderGuiItem(new ItemStack(item), x + 5, displayY);
			font.draw(new PoseStack(), pocket.getName(), x + 26, displayY + 4, 0xFFFFFF);
		};
	}
	
	private boolean canCreatePocket() {
		if (!api.getServerConfig().requirePocketFactory()) {
			return true;
		}
		LocalPlayer localPlayer = Minecraft.getInstance().player;
		if (localPlayer == null) {
			return false;
		}
		for (ItemStack stack : localPlayer.getInventory().items) {
			if (stack.is(ModItems.POCKET_FACTORY.get())) {
				return true;
			}
		}
		return false;
	}
	
	public void renderScroll(double mx, double my, float partialTick, IntConsumer renderIndex) {
		scroll.render(mx, my, partialTick, renderIndex);
	}
	
	public static boolean isHover(double mx, double my, float x0, float x1, float y0, float y1) {
		return x0 <= mx && mx < x1 && y0 <= my && my < y1;
	}
	
	public static boolean isHover(double mx, double my, float x0, float y0, float size) {
		return isHover(mx, my, x0, x0 + size, y0, y0 + size);
	}
	
	public static boolean isHover(double mx, double my, float x0, float y0) {
		return isHover(mx, my, x0, y0, 16);
	}
	
	@Override
	public void mouseMoved(double mx, double my) {
		if (draggingScroll) {
			scroll.updateScroll(my);
		}
		
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		Minecraft minecraft = Minecraft.getInstance();
		if (btn == 0) {
			minecraft.keyboardHandler.setSendRepeatsToGui(false);
			if (hoveringScroll) {
				draggingScroll = true;
				scroll.updateScroll(my);
				return true;
			}
			if (hoveringCreate) {
				createPocket();
				return true;
			}
			if (scroll.hoveredIndex >= 0) {
				openPocket(pockets.get(scroll.hoveredIndex).getPocketId());
				return true;
			}
		}
		boolean result = super.mouseClicked(mx, my, btn);;
		if (searchText.isFocused()) {
			minecraft.keyboardHandler.setSendRepeatsToGui(true);
		}
		return result;
	}
	
	private void createPocket() {
		if (!canCreatePocket()) {
			return;
		}
		GuiUtils.playClickSound();
		PacketHandler.sbCreatePocket();
	}
	
	private void openPocket(UUID pocketId) {
		GuiUtils.playClickSound();
		//TODO
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		Minecraft minecraft = Minecraft.getInstance();
		if (keyCode == InputConstants.KEY_ESCAPE) {
			minecraft.setScreen(null);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean mouseReleased(double mx, double my, int btn) {
		boolean result = super.mouseReleased(mx, my, btn);
		if (btn == 0) {
			if (draggingScroll) {
				draggingScroll = false;
				result = true;
			}
		}
		return result;
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		super.mouseScrolled(mx, my, delta);
		scroll.onScroll(delta);
		return true;
	}
	
	private interface Sprites {
		ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/pocket_list.png");
		int WIDTH = 166;
		Sprite FRAME_TOP = rect(0, 0, WIDTH, 21);
		Sprite FRAME_ROW = rect(0, 21, WIDTH, 16);
		Sprite FRAME_BOT = rect(0, 37, WIDTH, 25);
		Sprite CONTENT_SEARCH_N = rect(0, 62, WIDTH, 12);
		Sprite CONTENT_SEARCH_H = rect(0, 74, WIDTH, 12);
		Sprite CONTENT_POCKET_N = rect(0, 86, WIDTH, 16);
		Sprite CONTENT_POCKET_H = rect(0, 102, WIDTH, 16);
		Sprite CONTENT_POCKET_COLOR = rect(0, 118, WIDTH, 16);
		Sprite SCROLL_N = rect(0, 134, WIDTH, 2);
		Sprite SCROLL_H = rect(0, 136, WIDTH, 2);
		Sprite BTN_ADD_D = rect(208, 0, 16, 16);
		Sprite BTN_ADD_N = rect(224, 0, 16, 16);
		Sprite BTN_ADD_H = rect(240, 0, 16, 16);
	}
}
