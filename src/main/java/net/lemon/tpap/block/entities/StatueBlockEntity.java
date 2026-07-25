package net.lemon.tpap.block.entities;

import net.lemon.tpap.block.StatueBlock;
import net.lemon.tpap.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Map;

public class StatueBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final Map<Integer, RawAnimation> POSE_ANIMATIONS = new HashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int pose = 0;
    private boolean propped = false;

    public StatueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STATUE_BE.get(), pos, state);
    }

    public int getPose() {
        return this.pose;
    }

    public int getPoseCount() {
        return this.getBlockState().getBlock() instanceof StatueBlock statue ? statue.getPoseCount() : 1;
    }

    public void cyclePose() {
        this.pose = (this.pose + 1) % this.getPoseCount();
        this.markUpdated();
    }

    public boolean isProppable() {
        return this.getBlockState().getBlock() instanceof StatueBlock statue && statue.isProppable();
    }

    public boolean hasProps() {
        return this.propped;
    }

    public void setPropped(boolean propped) {
        if (propped && !this.isProppable()) {
            return;
        }
        this.propped = propped;
        this.markUpdated();
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static RawAnimation getPoseAnimation(int pose) {
        return POSE_ANIMATIONS.computeIfAbsent(pose, p -> RawAnimation.begin().thenPlayAndHold("pose" + p));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "pose_controller", 5, state ->
                state.setAndContinue(getPoseAnimation(this.pose))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Pose", this.pose);
        tag.putBoolean("Propped", this.propped);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.pose = Math.floorMod(tag.getInt("Pose"), this.getPoseCount());
        this.propped = tag.getBoolean("Propped") && this.isProppable();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        // Generous box so tall/wide models (e.g. a giraffe) aren't culled
        // when the base block leaves the camera frustum.
        return new AABB(this.worldPosition).inflate(2.0, 0.0, 2.0).expandTowards(0.0, 5.0, 0.0);
    }
}