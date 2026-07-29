package net.lemon.tpap.block;

import net.lemon.tpap.block.entities.SculptorsStationBlockEntity;
import net.lemon.tpap.menu.SculptorsStationDrawerMenu;
import net.lemon.tpap.menu.SculptorsStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class SculptorsStationBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StationPart> PART = EnumProperty.create("part", StationPart.class);

    public SculptorsStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, StationPart.WORKBENCH));
    }

    public static Direction getPartnerDirection(BlockState state) {
        Direction side = state.getValue(FACING).getClockWise();
        return state.getValue(PART) == StationPart.WORKBENCH ? side : side.getOpposite();
    }

    @Nullable
    public static SculptorsStationBlockEntity getStationBlockEntity(Level level, BlockPos pos, BlockState state) {
        BlockPos drawerPos = state.getValue(PART) == StationPart.DRAWER
                ? pos
                : pos.relative(getPartnerDirection(state));
        return level.getBlockEntity(drawerPos) instanceof SculptorsStationBlockEntity station ? station : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos drawerPos = context.getClickedPos().relative(facing.getClockWise());
        if (context.getLevel().getBlockState(drawerPos).canBeReplaced(context)
                && context.getLevel().getWorldBorder().isWithinBounds(drawerPos)) {
            return this.defaultBlockState().setValue(FACING, facing);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos drawerPos = pos.relative(getPartnerDirection(state));
            level.setBlock(drawerPos, state.setValue(PART, StationPart.DRAWER), Block.UPDATE_ALL);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == getPartnerDirection(state)) {
            boolean validPartner = neighborState.is(this)
                    && neighborState.getValue(PART) != state.getValue(PART)
                    && neighborState.getValue(FACING) == state.getValue(FACING);
            if (!validPartner) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(PART) == StationPart.WORKBENCH) {
            BlockPos drawerPos = pos.relative(getPartnerDirection(state));
            BlockState drawerState = level.getBlockState(drawerPos);
            if (drawerState.is(this) && drawerState.getValue(PART) == StationPart.DRAWER) {
                level.setBlock(drawerPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, drawerPos, Block.getId(drawerState));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof SculptorsStationBlockEntity station) {
            station.dropContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        SculptorsStationBlockEntity station = getStationBlockEntity(level, pos, state);
        if (station == null || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        MenuProvider provider = state.getValue(PART) == StationPart.WORKBENCH
                ? new SimpleMenuProvider((id, inventory, p) -> new SculptorsStationMenu(id, inventory, station),
                Component.translatable("container.tpap.sculptors_station"))
                : new SimpleMenuProvider((id, inventory, p) -> new SculptorsStationDrawerMenu(id, inventory, station),
                Component.translatable("container.tpap.sculptors_station_drawer"));
        NetworkHooks.openScreen(serverPlayer, provider, station.getBlockPos());
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof SculptorsStationBlockEntity station) {
            station.recheckOpen();
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, param);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == StationPart.DRAWER
                ? RenderShape.ENTITYBLOCK_ANIMATED
                : RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == StationPart.DRAWER ? new SculptorsStationBlockEntity(pos, state) : null;
    }

    public enum StationPart implements StringRepresentable {
        WORKBENCH("workbench"),
        DRAWER("drawer");

        private final String name;

        StationPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}