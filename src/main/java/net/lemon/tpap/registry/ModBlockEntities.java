package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.block.entities.SculptorsStationBlockEntity;
import net.lemon.tpap.block.entities.StatueBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TPAP.MODID);

    public static final RegistryObject<BlockEntityType<StatueBlockEntity>> STATUE_BE =
            BLOCK_ENTITIES.register("statue", () -> BlockEntityType.Builder.of(StatueBlockEntity::new,
                    ModBlocks.STATUES.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static final RegistryObject<BlockEntityType<SculptorsStationBlockEntity>> SCULPTORS_STATION_BE =
            BLOCK_ENTITIES.register("sculptors_station.json", () -> BlockEntityType.Builder.of(SculptorsStationBlockEntity::new,
                    ModBlocks.SCULPTORS_STATION.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}