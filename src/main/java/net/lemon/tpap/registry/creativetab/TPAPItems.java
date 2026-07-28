package net.lemon.tpap.registry.creativetab;

import net.lemon.tpap.registry.ModBlocks;
import net.lemon.tpap.registry.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class TPAPItems {
    public static final List<RegistryObject<? extends Item>> ITEMS = List.of(
            //Spawn Eggs
            ModItems.HUGE_MOLD,
            ModItems.LARGE_MOLD,
            ModItems.MEDIUM_MOLD,
            ModItems.SMALL_MOLD,
            ModItems.PLANE_MOLD,
            ModItems.POLE,
            ModItems.SCULPTORS_STATION_ITEM


    );

    public static void displayItems(CreativeModeTab.Output output) {
        ITEMS.forEach(item -> output.accept(item.get()));
    }
}
