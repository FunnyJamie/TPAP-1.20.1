package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.registry.creativetab.TPAPItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TPAP.MODID);

    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = TABS.register("items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.HUGE_MOLD.get()))
            .title(Component.translatable("creativetab.items_tab"))
            .displayItems((pParameters, pOutput) -> {
                TPAPItems.displayItems(pOutput);
            }).build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
