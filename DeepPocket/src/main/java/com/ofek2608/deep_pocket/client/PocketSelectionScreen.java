package com.ofek2608.deep_pocket.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.impl.ClientAPIImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntConsumer;

public final class PocketSelectionScreen extends Screen {
	private final ClientAPIImpl api;
	
	private int offX, minY, maxY;
	private List<PocketProperties> pockets = new ArrayList<>();
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
		return properties.getName().contains(searchText.getValue());
	}
	
	private void renderPocketList(int mx, int my) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Sprite.Pockets.TEXTURE);
		
		int width = Sprite.Pockets.WIDTH;
		int x = offX - width / 2;
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		scroll.setRect(x + 5, x + 149, minY + Sprite.Pockets.FRAME_TOP.h, maxY - Sprite.Pockets.FRAME_BOT.h);
		scroll.elementHeight = 16;
		scroll.elementCount = 14;
		
		int scrollHeight = scroll.getH();
		
		int y = minY;
		y = Sprite.Pockets.FRAME_TOP.blit(x, y);
		y = Sprite.Pockets.FRAME_ROW.blit(x, y, width, scrollHeight);
		y = Sprite.Pockets.FRAME_BOT.blit(x, y);
		
		hoveringScroll = isHover(mx, my, x + 153, x + 161, minY + 19, minY + 19 + scrollHeight);
		hoveringSearch = isHover(mx, my, x + 61, x + 149, minY + 5, minY + 17);
		hoveringCreate = isHover(mx, my, x + 133, maxY - 21);
		
		searchText.x = x + 61;
		searchText.y = minY + 5;
		searchText.setWidth(88);
		searchText.setHeight(12);
		searchText.setBordered(true);
		
		(hoveringScroll || draggingScroll ? Sprite.Pockets.SCROLL_H : Sprite.Pockets.SCROLL_N).blit(x, scroll.getScrollbarY());
		(hoveringSearch ? Sprite.Pockets.CONTENT_SEARCH_H : Sprite.Pockets.CONTENT_SEARCH_N).blit(x, minY + 5);
		(canCreatePocket() ? hoveringCreate ? Sprite.Pockets.BTN_ADD_H : Sprite.Pockets.BTN_ADD_N : Sprite.Pockets.BTN_ADD_D).blit(x + 133, maxY - 21);
		
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		
		
		
		scrollFunction = i -> {
			Random random = new Random(0x102030 + i);
			boolean hovering = i == scroll.hoveredIndex;
			int displayY = i * 16;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, Sprite.Pockets.TEXTURE);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			(hovering ? Sprite.Pockets.CONTENT_POCKET_H : Sprite.Pockets.CONTENT_POCKET_N).blit(x, displayY);
			RenderSystem.setShaderColor(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1);
			Sprite.Pockets.CONTENT_POCKET_COLOR.blit(x, displayY);
			
			itemRenderer.renderGuiItem(new ItemStack(hovering ? Items.FURNACE : Items.CRAFTING_TABLE), x + 5, displayY);
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
			if (stack.is(Items.STONE)) {//TODO change to pocket factory item
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
				if (!canCreatePocket()) {
					return true;
				}
				createPocket();
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
	
	private record Sprite(int x, int y, int w, int h, int u0, int v0, int u1, int v1) {
		private static Sprite of(int x, int y, int w, int h) {
			return new Sprite(
					x, y, w, h,
					x, y, x + w, y + h
			);
		}
		
		private static ResourceLocation texture(String name) {
			return DeepPocketMod.loc("textures/gui/pocket/" + name + ".png");
		}
		
		public int blit(int x, int y) {
			return blit(x, y, w, h);
		}
		
		public int blit(int x0, int y0, int w, int h) {
			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			int x1 = x0 + w;
			int y1 = y0 + h;
			
			bufferbuilder.vertex(x0, y1, 0).uv(u0 / 256f, v1 / 256f).endVertex();
			bufferbuilder.vertex(x1, y1, 0).uv(u1 / 256f, v1 / 256f).endVertex();
			bufferbuilder.vertex(x1, y0, 0).uv(u1 / 256f, v0 / 256f).endVertex();
			bufferbuilder.vertex(x0, y0, 0).uv(u0 / 256f, v0 / 256f).endVertex();
			BufferUploader.drawWithShader(bufferbuilder.end());
			
			return y1;
		}
		
		public float blit(float x, float y) {
			return blit(x, y, u1 - u0, v1 - v0);
		}
		
		public float blit(float x0, float y0, float w, float h) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			float x1 = x0 + w;
			float y1 = y0 + h;
			
			bufferbuilder.vertex(x0, y1, 0).uv(u0 / 256f, v1 / 256f).endVertex();
			bufferbuilder.vertex(x1, y1, 0).uv(u1 / 256f, v1 / 256f).endVertex();
			bufferbuilder.vertex(x1, y0, 0).uv(u1 / 256f, v0 / 256f).endVertex();
			bufferbuilder.vertex(x0, y0, 0).uv(u0 / 256f, v0 / 256f).endVertex();
			BufferUploader.drawWithShader(bufferbuilder.end());
			
			return y1;
		}
		
		interface Pockets {
			ResourceLocation TEXTURE = texture("pockets");
			int WIDTH = 166;
			Sprite FRAME_TOP = of(0, 0, WIDTH, 21);
			Sprite FRAME_ROW = of(0, 21, WIDTH, 16);
			Sprite FRAME_BOT = of(0, 37, WIDTH, 25);
			Sprite CONTENT_SEARCH_N = of(0, 62, WIDTH, 12);
			Sprite CONTENT_SEARCH_H = of(0, 74, WIDTH, 12);
			Sprite CONTENT_POCKET_N = of(0, 86, WIDTH, 16);
			Sprite CONTENT_POCKET_H = of(0, 102, WIDTH, 16);
			Sprite CONTENT_POCKET_COLOR = of(0, 118, WIDTH, 16);
			Sprite SCROLL_N = of(0, 134, WIDTH, 2);
			Sprite SCROLL_H = of(0, 136, WIDTH, 2);
			Sprite BTN_ADD_D = of(208, 0, 16, 16);
			Sprite BTN_ADD_N = of(224, 0, 16, 16);
			Sprite BTN_ADD_H = of(240, 0, 16, 16);
		}
	}
}
