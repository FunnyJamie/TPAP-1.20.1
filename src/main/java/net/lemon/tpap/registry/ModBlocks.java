package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.block.StatueBlock;
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
import java.util.List;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TPAP.MODID);

    public static final List<RegistryObject<StatueBlock>> STATUES = new ArrayList<>();
    public static final List<RegistryObject<Item>> STATUE_ITEMS = new ArrayList<>();

    //Add Statues below: registerStatue(statueId, poseCount, proppable)
    public static final RegistryObject<StatueBlock> TRICERATOPS_HORRIDUS = registerStatue("triceratops_horridus", 5, false);






    private static RegistryObject<StatueBlock> registerStatue(String statueId, int poseCount, boolean proppable) {
        RegistryObject<StatueBlock> block = BLOCKS.register(statueId,
                () -> new StatueBlock(statueId, poseCount, proppable, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .sound(SoundType.STONE)
                        .strength(1.5F, 6.0F)
                        .noOcclusion()));
        RegistryObject<Item> item = ModItems.ITEMS.register(statueId, () -> new BlockItem(block.get(), new Item.Properties()));
        STATUES.add(block);
        STATUE_ITEMS.add(item);
        return block;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}