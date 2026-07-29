package net.lemon.tpap;

import com.mojang.logging.LogUtils;
import net.lemon.tpap.client.sculptors.SculptorsStationBlockRenderer;
import net.lemon.tpap.client.sculptors.SculptorsStationDrawerScreen;
import net.lemon.tpap.client.sculptors.SculptorsStationScreen;
import net.lemon.tpap.client.statue.StatueBlockRenderer;
import net.lemon.tpap.registry.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TPAP.MODID)
public class TPAP
{
    public static final String MODID = "tpap";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TPAP(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.SCULPTORS_STATION_MENU.get(), SculptorsStationScreen::new));
            event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.SCULPTORS_STATION_DRAWER_MENU.get(), SculptorsStationDrawerScreen::new));
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerBlockEntityRenderer(ModBlockEntities.STATUE_BE.get(), StatueBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.SCULPTORS_STATION_BE.get(), SculptorsStationBlockRenderer::new);
        }
    }
}
