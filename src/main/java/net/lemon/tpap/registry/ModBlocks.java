package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.block.SculptorsStationBlock;
import net.lemon.tpap.block.StatueBlock;
import net.lemon.tpap.item.MoldSize;
import net.lemon.tpap.item.SculptorsStationItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TPAP.MODID);

    /** All registered statues. Drives the shared BE type's valid blocks and the creative tab. */
    public static final List<RegistryObject<StatueBlock>> STATUES = new ArrayList<>();
    public static final List<RegistryObject<Item>> STATUE_ITEMS = new ArrayList<>();
    public static final Map<String, List<StatueEntry>> GENUS_MAP = new LinkedHashMap<>();

    public record StatueEntry(String statueId, String genus, MoldSize moldSize,
                              RegistryObject<StatueBlock> block, RegistryObject<Item> item) {
    }

    //Add Statues below: registerStatue(statueId, genus, poseCount, proppable, moldSize)
    public static final RegistryObject<StatueBlock> TRICERATOPS_HORRIDUS = registerStatue("triceratops_horridus", "triceratops", 5, false, MoldSize.LARGE);
    public static final RegistryObject<StatueBlock> TARBOSAURUS_BATAAR = registerStatue("tarbosaurus_bataar", "tarbosaurus", 5, true, MoldSize.HUGE);





    public static final RegistryObject<SculptorsStationBlock> SCULPTORS_STATION = BLOCKS.register("sculptors_station",
            () -> new SculptorsStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.5F).noOcclusion()));

    private static RegistryObject<StatueBlock> registerStatue(String statueId, String genus, int poseCount,
                                                              boolean proppable, MoldSize moldSize) {
        RegistryObject<StatueBlock> block = BLOCKS.register(statueId,
                () -> new StatueBlock(statueId, genus, poseCount, proppable, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .sound(SoundType.STONE)
                        .strength(1.5F, 6.0F)
                        .noOcclusion()));
        RegistryObject<Item> item = ModItems.ITEMS.register(statueId, () -> new BlockItem(block.get(), new Item.Properties()));
        STATUES.add(block);
        STATUE_ITEMS.add(item);
        GENUS_MAP.computeIfAbsent(genus, g -> new ArrayList<>())
                .add(new StatueEntry(statueId, genus, moldSize, block, item));
        return block;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}