package net.lemon.tpap.item;

import net.lemon.tpap.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public enum MoldSize {
    SMALL(ModItems.SMALL_MOLD, "small"),
    MEDIUM(ModItems.MEDIUM_MOLD, "medium"),
    LARGE(ModItems.LARGE_MOLD, "large"),
    HUGE(ModItems.HUGE_MOLD, "huge");

    private final Supplier<Item> moldItem;
    private final String name;

    MoldSize(Supplier<Item> moldItem, String name) {
        this.moldItem = moldItem;
        this.name = name;
    }

    public Item getMoldItem() {
        return this.moldItem.get();
    }

    public boolean matches(ItemStack stack) {
        return stack.is(this.getMoldItem());
    }

    public Component getDisplayName() {
        return Component.translatable("moldsize.tpap." + this.name);
    }
}