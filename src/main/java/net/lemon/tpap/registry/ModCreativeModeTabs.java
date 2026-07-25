package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.item.BlueprintItem;
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

    public static final RegistryObject<CreativeModeTab> STATUES_TAB = TABS.register("statues_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.TRICERATOPS_HORRIDUS.get()))
                    .title(Component.translatable("creativetab.statues_tab"))
                    .displayItems((pParameters, pOutput) ->
                            ModBlocks.STATUE_ITEMS.forEach(item -> pOutput.accept(item.get()))
                    ).build());

    public static final RegistryObject<CreativeModeTab> BLUEPRINTS_TAB = TABS.register("blueprints_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BLUEPRINT.get()))
                    .title(Component.translatable("creativetab.blueprints_tab"))
                    .displayItems((pParameters, pOutput) ->
                            ModBlocks.GENUS_MAP.keySet().forEach(g -> pOutput.accept(BlueprintItem.forGenus(g)))
                    ).build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
