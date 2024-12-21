package io.tebex.plugin.gui;


import net.minecraft.world.item.ItemStack;

public class TebexGuiItem {

    private final TebexGuiAction<TebexBuyScreenHandler> action;
    private final ItemStack stack;

    public TebexGuiItem(final ItemStack stack, final TebexGuiAction<TebexBuyScreenHandler> action) {
        this.action = action;
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public String toString() {
        return this.stack.toString();
    }

    public TebexGuiAction<TebexBuyScreenHandler> getAction() {
        return this.action;
    }
}
