package net.lemon.tpap.menu;

import net.lemon.tpap.block.entities.SculptorsStationBlockEntity;
import net.lemon.tpap.item.BlueprintItem;
import net.lemon.tpap.item.MoldSize;
import net.lemon.tpap.registry.ModBlocks;
import net.lemon.tpap.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SculptorsStationMenu extends AbstractContainerMenu {
    public static final int MOLD_DAMAGE_PER_CRAFT = 20;

    private static final int BLUEPRINT_SLOT = 0;
    private static final int MOLD_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    private static final int INV_START = 3;
    private static final int INV_END = 30;
    private static final int HOTBAR_END = 39;

    @Nullable
    private final SculptorsStationBlockEntity station;
    private final DataSlot selectedIndex = DataSlot.standalone();

    private final SimpleContainer inputContainer = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            SculptorsStationMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer resultContainer = new ResultContainer();

    private List<ModBlocks.StatueEntry> visibleEntries = List.of();
    @Nullable
    private String genus = null;

    public SculptorsStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, resolveStation(playerInventory, buf));
    }

    public SculptorsStationMenu(int containerId, Inventory playerInventory, @Nullable SculptorsStationBlockEntity station) {
        super(ModMenuTypes.SCULPTORS_STATION_MENU.get(), containerId);
        this.station = station;
        this.selectedIndex.set(-1);
        this.addDataSlot(this.selectedIndex);

        this.addSlot(new Slot(this.inputContainer, BLUEPRINT_SLOT, 20, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BlueprintItem;
            }
        });
        this.addSlot(new Slot(this.inputContainer, MOLD_SLOT, 20, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isMold(stack);
            }
        });
        this.addSlot(new Slot(this.resultContainer, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                ItemStack mold = SculptorsStationMenu.this.inputContainer.getItem(MOLD_SLOT);
                mold.hurtAndBreak(MOLD_DAMAGE_PER_CRAFT, player, p -> {});
                SculptorsStationMenu.this.inputContainer.setChanged();
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
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

    private static boolean isMold(ItemStack stack) {
        for (MoldSize size : MoldSize.values()) {
            if (size.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public SculptorsStationBlockEntity getStation() {
        return this.station;
    }

    public List<ModBlocks.StatueEntry> getVisibleEntries() {
        return this.visibleEntries;
    }

    public int getSelectedIndex() {
        return this.selectedIndex.get();
    }

    public boolean hasBlueprint() {
        return !this.inputContainer.getItem(BLUEPRINT_SLOT).isEmpty();
    }

    public boolean isCraftable(ModBlocks.StatueEntry entry) {
        ItemStack mold = this.inputContainer.getItem(MOLD_SLOT);
        return entry.moldSize().matches(mold)
                && mold.getMaxDamage() - mold.getDamageValue() >= MOLD_DAMAGE_PER_CRAFT;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < this.visibleEntries.size() && this.isCraftable(this.visibleEntries.get(id))) {
            this.selectedIndex.set(id);
            this.setupResultSlot();
        }
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.inputContainer) {
            String newGenus = BlueprintItem.getGenus(this.inputContainer.getItem(BLUEPRINT_SLOT));
            if (!Objects.equals(newGenus, this.genus)) {
                this.genus = newGenus;
                this.visibleEntries = newGenus == null
                        ? List.of()
                        : ModBlocks.GENUS_MAP.getOrDefault(newGenus, List.of());
                this.selectedIndex.set(-1);
            }
            this.setupResultSlot();
        }
        super.slotsChanged(container);
    }

    private void setupResultSlot() {
        int index = this.selectedIndex.get();
        if (index >= 0 && index < this.visibleEntries.size() && this.isCraftable(this.visibleEntries.get(index))) {
            this.resultContainer.setItem(0, new ItemStack(this.visibleEntries.get(index).item().get()));
        } else {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
        }
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index == RESULT_SLOT) {
                slotStack.getItem().onCraftedBy(slotStack, player.level(), player);
                if (!this.moveItemStackTo(slotStack, INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (index == BLUEPRINT_SLOT || index == MOLD_SLOT) {
                if (!this.moveItemStackTo(slotStack, INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof BlueprintItem) {
                if (!this.moveItemStackTo(slotStack, BLUEPRINT_SLOT, BLUEPRINT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isMold(slotStack)) {
                if (!this.moveItemStackTo(slotStack, MOLD_SLOT, MOLD_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < INV_END) {
                if (!this.moveItemStackTo(slotStack, INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, INV_START, INV_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            slot.setChanged();
            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
            this.broadcastChanges();
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.removeItemNoUpdate(0);
        if (!player.level().isClientSide) {
            this.clearContainer(player, this.inputContainer);
            if (this.station != null) {
                this.station.stopOpen(player);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.station != null && !this.station.isRemoved()
                && player.distanceToSqr(Vec3.atCenterOf(this.station.getBlockPos())) <= 64.0;
    }
}