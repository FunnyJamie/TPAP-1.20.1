package net.lemon.tpap.client;

import net.lemon.tpap.block.entities.SculptorsStationBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SculptorsStationBlockRenderer extends GeoBlockRenderer<SculptorsStationBlockEntity> {
    public SculptorsStationBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new SculptorsStationModel());
    }
}