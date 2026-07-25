package net.lemon.tpap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lemon.tpap.block.StatueBlock;
import net.lemon.tpap.block.entities.StatueBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class StatueBlockRenderer extends GeoBlockRenderer<StatueBlockEntity> {
    public StatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new StatueModel());
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        int rotation = this.animatable.getBlockState().getValue(StatueBlock.ROTATION);
        poseStack.mulPose(Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(rotation)));
    }
}