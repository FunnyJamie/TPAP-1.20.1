package net.lemon.tpap.client.statue;

import net.lemon.tpap.TPAP;
import net.lemon.tpap.block.entities.StatueBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/**
 * IMPORTANT NAMING CONVENTIONS:
 * - geo/<statueId>.geo.json, textures/block/<statueId>.png, animations/<statueId>.json
 * - Pose animations named "pose0".."poseN-1"
 * - Optional "poles" group bone (sub-bones pole1, pole2, ... reposition per pose).
 */
public class StatueModel extends GeoModel<StatueBlockEntity> {
    private static final String POLES_BONE = "poles";

    @Override
    public ResourceLocation getModelResource(StatueBlockEntity animatable) {
        return new ResourceLocation(TPAP.MODID, "geo/" + animatable.getStatueId() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StatueBlockEntity animatable) {
        return new ResourceLocation(TPAP.MODID, "textures/block/" + animatable.getStatueId() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(StatueBlockEntity animatable) {
        return new ResourceLocation(TPAP.MODID, "animations/" + animatable.getStatueId() + ".json");
    }

    @Override
    public void setCustomAnimations(StatueBlockEntity animatable, long instanceId, AnimationState<StatueBlockEntity> animationState) {
        this.getBone(POLES_BONE).ifPresent(poles -> poles.setHidden(!animatable.hasProps()));
    }
}