package net.lemon.tpap.item;

import net.lemon.tpap.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintItem extends Item {
    public static final String GENUS_TAG = "Genus";

    public BlueprintItem(Properties properties) {
        super(properties);
    }

    public static ItemStack forGenus(String genus) {
        ItemStack stack = new ItemStack(net.lemon.tpap.registry.ModItems.BLUEPRINT.get());
        stack.getOrCreateTag().putString(GENUS_TAG, genus);
        return stack;
    }

    @Nullable
    public static String getGenus(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(GENUS_TAG)) {
            return stack.getTag().getString(GENUS_TAG);
        }
        return null;
    }

    public static boolean isValid(ItemStack stack) {
        String genus = getGenus(stack);
        return genus != null && ModBlocks.GENUS_MAP.containsKey(genus);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String genus = getGenus(stack);
        if (genus == null || !ModBlocks.GENUS_MAP.containsKey(genus)) {
            tooltip.add(Component.translatable("tooltip.tpap.blueprint.unknown").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("genus.tpap." + genus).withStyle(ChatFormatting.AQUA));
        ModBlocks.largestMoldFor(genus).ifPresent(size ->
                tooltip.add(Component.translatable("tooltip.tpap.blueprint.mold", size.getDisplayName())
                        .withStyle(ChatFormatting.GRAY)));
    }
}