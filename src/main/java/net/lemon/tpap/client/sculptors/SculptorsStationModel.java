package net.lemon.tpap.client.sculptors;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.block.entities.SculptorsStationBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SculptorsStationModel extends GeoModel<SculptorsStationBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(TPAP.MODID, "geo/sculptors_station.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(TPAP.MODID, "textures/block/sculptors_station.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(TPAP.MODID, "animations/sculptors_station.json");

    @Override
    public ResourceLocation getModelResource(SculptorsStationBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculptorsStationBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculptorsStationBlockEntity animatable) {
        return ANIMATIONS;
    }
}