package net.lemon.tpap.client.sculptors;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.menu.SculptorsStationMenu;
import net.lemon.tpap.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SculptorsStationScreen extends AbstractContainerScreen<SculptorsStationMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation(TPAP.MODID, "textures/gui/sculptors_station_screen.png");
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 3;
    private static final int GRID_SIZE = GRID_COLUMNS * GRID_ROWS;
    private static final int GRID_X = 52;
    private static final int GRID_Y = 14;
    private static final int SCROLLBAR_X = 119;
    private static final int SCROLLBAR_Y = 15;
    private static final int SCROLLER_TRAVEL = 41;

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private List<ModBlocks.StatueEntry> lastEntries = List.of();

    public SculptorsStationScreen(SculptorsStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int scrollerY = this.topPos + SCROLLBAR_Y + (int) (SCROLLER_TRAVEL * this.scrollOffs);
        guiGraphics.blit(BG_LOCATION, this.leftPos + SCROLLBAR_X, scrollerY,
                176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
        this.renderButtons(guiGraphics, mouseX, mouseY);
        this.renderEntries(guiGraphics);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<ModBlocks.StatueEntry> entries = this.menu.getVisibleEntries();
        for (int index = this.startIndex; index < this.startIndex + GRID_SIZE && index < entries.size(); index++) {
            int gridIndex = index - this.startIndex;
            int x = this.leftPos + GRID_X + gridIndex % GRID_COLUMNS * 16;
            int y = this.topPos + GRID_Y + gridIndex / GRID_COLUMNS * 18 + 2;
            int vOffset = this.imageHeight;
            if (index == this.menu.getSelectedIndex()) {
                vOffset += 18;
            } else if (this.menu.isCraftable(entries.get(index))
                    && mouseX >= x && mouseY >= y - 1 && mouseX < x + 16 && mouseY < y + 17) {
                vOffset += 36;
            }
            guiGraphics.blit(BG_LOCATION, x, y - 1, 0, vOffset, 16, 18);
        }
    }

    private void renderEntries(GuiGraphics guiGraphics) {
        List<ModBlocks.StatueEntry> entries = this.menu.getVisibleEntries();
        for (int index = this.startIndex; index < this.startIndex + GRID_SIZE && index < entries.size(); index++) {
            int gridIndex = index - this.startIndex;
            int x = this.leftPos + GRID_X + gridIndex % GRID_COLUMNS * 16;
            int y = this.topPos + GRID_Y + gridIndex / GRID_COLUMNS * 18 + 2;
            guiGraphics.renderItem(new ItemStack(entries.get(index).item().get()), x, y);
            if (!this.menu.isCraftable(entries.get(index))) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.fill(x, y - 1, x + 16, y + 17, 0xB0373737);
                guiGraphics.pose().popPose();
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        List<ModBlocks.StatueEntry> entries = this.menu.getVisibleEntries();
        for (int index = this.startIndex; index < this.startIndex + GRID_SIZE && index < entries.size(); index++) {
            int gridIndex = index - this.startIndex;
            int x = this.leftPos + GRID_X + gridIndex % GRID_COLUMNS * 16;
            int y = this.topPos + GRID_Y + gridIndex / GRID_COLUMNS * 18 + 2;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y - 1 && mouseY < y + 17) {
                ModBlocks.StatueEntry entry = entries.get(index);
                List<Component> lines = new ArrayList<>();
                lines.add(new ItemStack(entry.item().get()).getHoverName());
                if (!this.menu.isCraftable(entry)) {
                    lines.add(Component.translatable("tooltip.tpap.requires_mold",
                            entry.moldSize().getDisplayName()));
                }
                guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        List<ModBlocks.StatueEntry> entries = this.menu.getVisibleEntries();
        for (int index = this.startIndex; index < this.startIndex + GRID_SIZE && index < entries.size(); index++) {
            int gridIndex = index - this.startIndex;
            double x = mouseX - (this.leftPos + GRID_X + gridIndex % GRID_COLUMNS * 16);
            double y = mouseY - (this.topPos + GRID_Y + gridIndex / GRID_COLUMNS * 18 + 1);
            if (x >= 0 && x < 16 && y >= 0 && y < 18) {
                if (this.menu.isCraftable(entries.get(index))
                        && this.menu.clickMenuButton(this.minecraft.player, index)) {
                    this.minecraft.getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);
                }
                return true;
            }
        }
        double scrollX = mouseX - (this.leftPos + SCROLLBAR_X);
        double scrollY = mouseY - (this.topPos + SCROLLBAR_Y);
        if (scrollX >= 0 && scrollX < 12 && scrollY >= 0 && scrollY < SCROLLER_TRAVEL + 15) {
            this.scrolling = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            this.scrollOffs = Mth.clamp(
                    ((float) mouseY - (this.topPos + SCROLLBAR_Y) - 7.5F) / SCROLLER_TRAVEL, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5F) * GRID_COLUMNS;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isScrollBarActive()) {
            this.scrollOffs = Mth.clamp(this.scrollOffs - (float) delta / this.getOffscreenRows(), 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5F) * GRID_COLUMNS;
        }
        return true;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.menu.getVisibleEntries() != this.lastEntries) {
            this.lastEntries = this.menu.getVisibleEntries();
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
    }

    private boolean isScrollBarActive() {
        return this.menu.getVisibleEntries().size() > GRID_SIZE;
    }

    private int getOffscreenRows() {
        return (this.menu.getVisibleEntries().size() + GRID_COLUMNS - 1) / GRID_COLUMNS - GRID_ROWS;
    }
}