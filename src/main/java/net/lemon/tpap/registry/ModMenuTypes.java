package net.lemon.tpap.registry;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.menu.SculptorsStationDrawerMenu;
import net.lemon.tpap.menu.SculptorsStationMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TPAP.MODID);

    public static final RegistryObject<MenuType<SculptorsStationMenu>> SCULPTORS_STATION_MENU =
            MENUS.register("sculptors_station", () -> IForgeMenuType.create(SculptorsStationMenu::new));

    public static final RegistryObject<MenuType<SculptorsStationDrawerMenu>> SCULPTORS_STATION_DRAWER_MENU =
            MENUS.register("sculptors_station_drawer", () -> IForgeMenuType.create(SculptorsStationDrawerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}