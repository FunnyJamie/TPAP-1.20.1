package net.lemon.tpap.menu;

import net.lemon.tpap.block.entities.SculptorsStationBlockEntity;
import net.lemon.tpap.item.BlueprintItem;
import net.lemon.tpap.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class SculptorsStationDrawerMenu extends AbstractContainerMenu {
    public static final int COLUMNS = 9;
    public static final int VISIBLE_ROWS = 6;
    public static final int VISIBLE_SLOTS = COLUMNS * VISIBLE_ROWS;
    public static final int TOTAL_ROWS = (SculptorsStationBlockEntity.DRAWER_SLOTS + COLUMNS - 1) / COLUMNS;
    public static final int MAX_SCROLL_ROW = TOTAL_ROWS - VISIBLE_ROWS;

    private static final Container EMPTY_CONTAINER = new SimpleContainer(0);
    private static final int INV_START = VISIBLE_SLOTS;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    @Nullable
    private final SculptorsStationBlockEntity station;
    private final ItemStackHandler drawerInventory;
    private final DataSlot scrollRow = DataSlot.standalone();

    public SculptorsStationDrawerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, resolveStation(playerInventory, buf));
    }

    public SculptorsStationDrawerMenu(int containerId, Inventory playerInventory, @Nullable SculptorsStationBlockEntity station) {
        super(ModMenuTypes.SCULPTORS_STATION_DRAWER_MENU.get(), containerId);
        this.station = station;
        this.drawerInventory = station != null ? station.getDrawerInventory() : new ItemStackHandler(0);
        this.scrollRow.set(0);
        this.addDataSlot(this.scrollRow);

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                this.addSlot(new DrawerSlot(col + row * COLUMNS, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 139 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 197));
        }

        if (station != null && !playerInventory.player.level().isClientSide) {
            station.startOpen(playerInventory.player);
        }
    }

    @Nullable
    private static SculptorsStationBlockEntity resolveStation(Inventory playerInventory, FriendlyByteBuf buf) {
        return playerInventory.player.level().getBlockEntity(buf.readBlockPos())
                instanceof SculptorsStationBlockEntity station ? station : null;
    }

    @Nullable
    public SculptorsStationBlockEntity getStation() {
        return this.station;
    }

    public int getScrollRow() {
        return this.scrollRow.get();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id <= MAX_SCROLL_ROW) {
            this.scrollRow.set(id);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index < VISIBLE_SLOTS) {
                if (!this.moveItemStackTo(slotStack, INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof BlueprintItem) {
                // Insert across the whole drawer, not just the visible
                // window. Server-only: the client's view of hidden slots
                // is unreliable, so let the sync system apply the result.
                if (player.level().isClientSide) {
                    return ItemStack.EMPTY;
                }
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(this.drawerInventory, slotStack.copy(), false);
                if (remainder.getCount() == slotStack.getCount()) {
                    return ItemStack.EMPTY;
                }
                slotStack.setCount(remainder.getCount());
                if (slotStack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                return ItemStack.EMPTY;
            } else if (index < INV_END) {
                if (!this.moveItemStackTo(slotStack, INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, INV_START, INV_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.station != null && !player.level().isClientSide) {
            this.station.stopOpen(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.station != null && !this.station.isRemoved()
                && player.distanceToSqr(Vec3.atCenterOf(this.station.getBlockPos())) <= 64.0;
    }

    /**
     * A fixed on-screen slot that maps to a scrolling window over the
     * drawer handler: handler index = scrollRow * COLUMNS + visibleIndex.
     * Indices past the handler's end (partial last row) are inert.
     */
    private class DrawerSlot extends Slot {
        private final int visibleIndex;

        DrawerSlot(int visibleIndex, int x, int y) {
            super(EMPTY_CONTAINER, visibleIndex, x, y);
            this.visibleIndex = visibleIndex;
        }

        private int handlerIndex() {
            return SculptorsStationDrawerMenu.this.scrollRow.get() * COLUMNS + this.visibleIndex;
        }

        private boolean isValidIndex() {
            return this.handlerIndex() < SculptorsStationDrawerMenu.this.drawerInventory.getSlots();
        }

        @Override
        public ItemStack getItem() {
            return this.isValidIndex()
                    ? SculptorsStationDrawerMenu.this.drawerInventory.getStackInSlot(this.handlerIndex())
                    : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            if (this.isValidIndex()) {
                SculptorsStationDrawerMenu.this.drawerInventory.setStackInSlot(this.handlerIndex(), stack);
            }
        }

        @Override
        public void setChanged() {
            if (this.isValidIndex()) {
                SculptorsStationDrawerMenu.this.drawerInventory.setStackInSlot(this.handlerIndex(), this.getItem());
            }
        }

        @Override
        public ItemStack remove(int amount) {
            return this.isValidIndex()
                    ? SculptorsStationDrawerMenu.this.drawerInventory.extractItem(this.handlerIndex(), amount, false)
                    : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.isValidIndex()
                    && SculptorsStationDrawerMenu.this.drawerInventory.isItemValid(this.handlerIndex(), stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !this.getItem().isEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public boolean isActive() {
            return this.isValidIndex();
        }
    }
}