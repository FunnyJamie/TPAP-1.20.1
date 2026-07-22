package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TPAP.MODID);

    //Add Items below
    public static final RegistryObject<Item> SMALL_MOLD = ITEMS.register("small_mold", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_MOLD = ITEMS.register("medium_mold", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_MOLD = ITEMS.register("large_mold", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HUGE_MOLD = ITEMS.register("huge_mold", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PLANE_MOLD = ITEMS.register("plane_mold", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
