package com.ofek2608.deep_pocket.def.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.api.types.EntryStack;
import com.ofek2608.deep_pocket.api.utils.GuiUtils;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.def.client.ScrollComponent;
import com.ofek2608.deep_pocket.def.client.Sprite;
import com.ofek2608.deep_pocket.def.client.widget.CompactEditBox;
import com.ofek2608.deep_pocket.def.client.widget.SimpleEditBox;
import com.ofek2608.deep_pocket.def.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static com.ofek2608.deep_pocket.def.client.Sprite.rect;

public final class PocketSelectionScreen extends Screen {
	private final DPClientAPI api;
	
	private int offX, minY, maxY;
	private final List<PocketProperties> pockets = new ArrayList<>();
	private final ScrollComponent scroll = new ScrollComponent();
	private final SimpleEditBox searchText;
	private boolean hoveringCreate;
	
	public PocketSelectionScreen(DPClientAPI api) {
		super(Component.empty());
		this.api = api;
		this.searchText = new CompactEditBox(
				Minecraft.getInstance().font,
				0, 0, 88, 12,
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
		int windowWidth = window.getGuiScaledWidth();
		int windowHeight = window.getGuiScaledHeight();
		int height = Math.min(windowHeight * 3 / 4, 360);
		offX = windowWidth / 2;
		minY = (windowHeight - height) / 2;
		maxY = minY + height;
	}
	
	@Override
	public void render(GuiGraphics graphics, int mx, int my, float partialTick) {
		updateWindowSize();
		renderBackground(graphics);
		updatePocketList();
		renderPocketList(graphics, mx, my, partialTick);
		
		RenderSystem.applyModelViewMatrix();
		super.render(graphics, mx, my, partialTick);
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
	
	private void renderPocketList(GuiGraphics graphics, int mx, int my, float partialTick) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Sprites.TEXTURE);
		
		int width = Sprites.WIDTH;
		int x = offX - width / 2;
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		scroll.setRect(x + 5, x + 149, minY + Sprites.FRAME_TOP.h, maxY - Sprites.FRAME_BOT.h);
		scroll.elementCount = pockets.size();
		scroll.scrollbarX = x + 153;
		
		int scrollHeight = scroll.getH();
		
		int y = minY;
		y = Sprites.FRAME_TOP.blit(x, y);
		y = Sprites.FRAME_ROW.blit(x, y, width, scrollHeight);
		Sprites.FRAME_BOT.blit(x, y);
		
		boolean hoveringSearch = isHover(mx, my, x + 61, x + 149, minY + 5, minY + 17);
		hoveringCreate = isHover(mx, my, x + 133, maxY - 21);
		
		searchText.setX(x + 61);
		searchText.setY(minY + 5);
		searchText.setWidth(88);
		searchText.setHeight(12);
		searchText.setBordered(true);
		
		(hoveringSearch ? Sprites.CONTENT_SEARCH_H : Sprites.CONTENT_SEARCH_N).blit(x, minY + 5);
		PocketWidgetsRenderer.renderButtonPlus(x + 133, maxY - 21, canCreatePocket() ? hoveringCreate ? 2 : 1 : 0);
		
		scroll.render(mx, my, partialTick, i -> {
			PocketProperties pocket = pockets.get(i);
			boolean hovering = i == scroll.hoveredIndex;
			int displayY = i * 16;
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, Sprites.TEXTURE);
			
			RenderSystem.setShaderColor(1, 1, 1, 1);
			(hovering ? Sprites.CONTENT_POCKET_H : Sprites.CONTENT_POCKET_N).blit(x, displayY);
			
			GuiUtils.setShaderColor(pocket.getColor());
			Sprites.CONTENT_POCKET_COLOR.blit(x, displayY);
			
			RenderSystem.setShaderColor(1, 1, 1, 1);
			api.getEntryCategory(pocket.getIcon().category()).render(
					graphics,
					new EntryStack(pocket.getIcon()),
					x + 5, displayY
			);
			graphics.drawString(font, pocket.getName(), x + 26, displayY + 4, 0xFFFFFF);
		});
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
		scroll.mouseMoved(mx, my);
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		if (scroll.mouseClicked(mx, my, btn)) {
			return true;
		}
		if (btn == 0) {
			if (hoveringCreate) {
				createPocket();
				return true;
			}
			if (scroll.hoveredIndex >= 0) {
				openPocket(pockets.get(scroll.hoveredIndex).getPocketId());
				return true;
			}
		}
		return super.mouseClicked(mx, my, btn);
	}
	
	private void createPocket() {
		if (!canCreatePocket()) {
			return;
		}
		GuiUtils.playClickSound();
		api.requestCreatePocket();
	}
	
	private void openPocket(UUID pocketId) {
		GuiUtils.playClickSound();
		
		LocalPlayer player = Minecraft.getInstance().player;
		Optional<Pocket> pocket = api.getPocket(pocketId);
		
		if (player == null || pocket.isEmpty()) {
			return;
		}
		
		PocketScreen.open(api, player, pocket.get());
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
		scroll.mouseReleased(mx, my, btn);
		return super.mouseReleased(mx, my, btn);
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		return scroll.mouseScrolled(mx, my, delta);
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
	}
}
