package net.lemon.tpap.client.sculptors;

import net.lemon.tpap.menu.SculptorsStationDrawerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class SculptorsStationDrawerScreen extends AbstractContainerScreen<SculptorsStationDrawerMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final ResourceLocation SCROLLER_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
    private static final int GRID_TOP_HEIGHT = SculptorsStationDrawerMenu.VISIBLE_ROWS * 18 + 17;
    private static final int SCROLLBAR_GAP = 2;
    private static final int SCROLLBAR_WIDTH = 14;
    private static final int TRACK_Y = 18;
    private static final int TRACK_HEIGHT = SculptorsStationDrawerMenu.VISIBLE_ROWS * 18;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int SCROLLER_TRAVEL = TRACK_HEIGHT - SCROLLER_HEIGHT;

    private float scrollOffs;
    private boolean scrolling;

    public SculptorsStationDrawerScreen(SculptorsStationDrawerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 114 + SculptorsStationDrawerMenu.VISIBLE_ROWS * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, GRID_TOP_HEIGHT);
        guiGraphics.blit(BG_LOCATION, this.leftPos, this.topPos + GRID_TOP_HEIGHT, 0, 126, this.imageWidth, 96);

        int trackX = this.leftPos + this.imageWidth + SCROLLBAR_GAP;
        int trackY = this.topPos + TRACK_Y;
        guiGraphics.fill(trackX, trackY - 1, trackX + SCROLLBAR_WIDTH, trackY + TRACK_HEIGHT + 1, 0xFF373737);
        guiGraphics.fill(trackX + 1, trackY, trackX + SCROLLBAR_WIDTH - 1, trackY + TRACK_HEIGHT, 0xFF8B8B8B);
        guiGraphics.blit(SCROLLER_LOCATION, trackX + 1, trackY + (int) (SCROLLER_TRAVEL * this.scrollOffs),
                176, 0, 12, 15);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int trackX = this.leftPos + this.imageWidth + SCROLLBAR_GAP;
        int trackY = this.topPos + TRACK_Y;
        if (mouseX >= trackX && mouseX < trackX + SCROLLBAR_WIDTH
                && mouseY >= trackY && mouseY < trackY + TRACK_HEIGHT) {
            this.scrolling = true;
            this.setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            this.setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.setScroll(this.scrollOffs - (float) delta / SculptorsStationDrawerMenu.MAX_SCROLL_ROW);
        return true;
    }

    private void setScrollFromMouse(double mouseY) {
        this.setScroll(((float) mouseY - (this.topPos + TRACK_Y) - SCROLLER_HEIGHT / 2.0F) / SCROLLER_TRAVEL);
    }

    private void setScroll(float offs) {
        this.scrollOffs = Mth.clamp(offs, 0.0F, 1.0F);
        int row = (int) (this.scrollOffs * SculptorsStationDrawerMenu.MAX_SCROLL_ROW + 0.5F);
        if (row != this.menu.getScrollRow()) {
            this.menu.clickMenuButton(this.minecraft.player, row);
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, row);
        }
    }
}