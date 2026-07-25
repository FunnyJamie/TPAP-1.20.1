package net.lemon.tpap.block;

import net.lemon.tpap.block.entities.StatueBlockEntity;
import net.lemon.tpap.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StatueBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final String statueId;
    private final int poseCount;
    private final boolean proppable;

    public StatueBlock(String statueId, int poseCount, boolean proppable, Properties properties) {
        super(properties);
        this.statueId = statueId;
        this.poseCount = poseCount;
        this.proppable = proppable;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ROTATION, 0)
                .setValue(WATERLOGGED, false));
    }

    public String getStatueId() {
        return this.statueId;
    }

    public int getPoseCount() {
        return this.poseCount;
    }

    public boolean isProppable() {
        return this.proppable;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F))
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModItems.POLE.get())) {
            if (!this.proppable) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(pos) instanceof StatueBlockEntity statue) {
                if (statue.hasProps()) {
                    statue.setPropped(false);
                    if (!player.getAbilities().instabuild) {
                        ItemStack refund = new ItemStack(ModItems.POLE.get());
                        if (!player.getInventory().add(refund)) {
                            player.drop(refund, false);
                        }
                    }
                } else {
                    statue.setPropped(true);
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
            }
            return InteractionResult.CONSUME;
        }
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof StatueBlockEntity statue) {
                statue.cyclePose();
            }
        } else {
            level.setBlock(pos, state.setValue(ROTATION, (state.getValue(ROTATION) + 1) % 16), Block.UPDATE_ALL);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof StatueBlockEntity statue) {
            statue.resetToPlacementDefaults();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Refund the inserted pole when a propped statue is broken.
            // The statue block itself drops via its loot table.
            if (level.getBlockEntity(pos) instanceof StatueBlockEntity statue && statue.hasProps()) {
                popResource(level, pos, new ItemStack(ModItems.POLE.get()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StatueBlockEntity(pos, state);
    }
}