package net.lemon.tpap.block.entities;

import net.lemon.tpap.item.BlueprintItem;
import net.lemon.tpap.menu.SculptorsStationDrawerMenu;
import net.lemon.tpap.menu.SculptorsStationMenu;
import net.lemon.tpap.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculptorsStationBlockEntity extends BlockEntity implements GeoBlockEntity {
    public static final int DRAWER_SLOTS = 261;
    private static final int EVENT_OPEN_COUNT = 1;

    private static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlayAndHold("close");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ItemStackHandler drawerInventory = new ItemStackHandler(DRAWER_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof BlueprintItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            SculptorsStationBlockEntity.this.setChanged();
        }
    };

    /** Client-side open target, synced via block events. Starts closed. */
    private boolean open = false;
    private boolean openedOnce = false;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            level.blockEvent(pos, state.getBlock(), EVENT_OPEN_COUNT, current);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof SculptorsStationMenu menu) {
                return menu.getStation() == SculptorsStationBlockEntity.this;
            }
            if (player.containerMenu instanceof SculptorsStationDrawerMenu menu) {
                return menu.getStation() == SculptorsStationBlockEntity.this;
            }
            return false;
        }
    };

    public SculptorsStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULPTORS_STATION_BE.get(), pos, state);
    }

    public ItemStackHandler getDrawerInventory() {
        return this.drawerInventory;
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.level, this.worldPosition, this.getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.level, this.worldPosition, this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.level, this.worldPosition, this.getBlockState());
        }
    }

    public void dropContents() {
        if (this.level != null) {
            for (int slot = 0; slot < this.drawerInventory.getSlots(); slot++) {
                Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(),
                        this.worldPosition.getZ(), this.drawerInventory.getStackInSlot(slot));
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_OPEN_COUNT) {
            boolean nowOpen = param > 0;
            if (nowOpen != this.open) {
                this.open = nowOpen;
                this.openedOnce |= nowOpen;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "drawer_controller", 10, state -> {
            if (this.open) {
                return state.setAndContinue(OPEN);
            }
            // Freshly loaded stations start closed without playing the
            // close animation -- only close after an actual open.
            return this.openedOnce ? state.setAndContinue(CLOSE) : PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("DrawerInventory", this.drawerInventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("DrawerInventory")) {
            this.drawerInventory.deserializeNBT(tag.getCompound("DrawerInventory"));
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        // The combined 16x32 model spans this block plus the workbench
        // half beside it.
        return new AABB(this.worldPosition).inflate(1.0, 0.0, 1.0);
    }
}