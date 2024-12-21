package io.tebex.plugin.gui;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class TebexItemBuilder {

    private final Item material;
    private final TebexGuiAction<TebexBuyScreenHandler> action;
    private TebexGuiItem guiItem;
    private String displayName;
    private List<String> lore;
    private DataComponentType<?>[] hideFlags;
    private boolean isEnchanted;

    private TebexItemBuilder(final Item material,
            final TebexGuiAction<TebexBuyScreenHandler> action) {
        this.material = material;
        this.action = action;
    }

    public static TebexItemBuilder from(final Item material) {
        return new TebexItemBuilder(material, null);
    }

    public TebexGuiItem asGuiItem(final TebexGuiAction<TebexBuyScreenHandler> clickAction) {
        return new TebexGuiItem(this.buildItemStack(), clickAction);
    }

    public ItemStack buildItemStack() {
        final ItemStack stack = new ItemStack(this.material);

        this.lore.forEach(loreEntry -> {
            stack.set(DataComponents.LORE,
                    ItemLore.EMPTY.withLineAdded(Component.literal(loreEntry)));
        });

        stack.set(DataComponents.CUSTOM_NAME, Component.literal(this.displayName));
        //FIXME
        //        for (DataComponentTypes tooltipSection : hideFlags) {
        //            stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, List.of
        //            (tooltipSection));
        //        }

        if (this.isEnchanted) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }

        return stack;
    }

    public void enchant() {
        this.isEnchanted = true;
    }

    public TebexItemBuilder hideFlags(final DataComponentType<?>... itemFlags) {
        this.hideFlags = itemFlags;
        return this;
    }

    public TebexItemBuilder name(final String name) {
        this.displayName = name;
        return this;
    }

    public TebexItemBuilder lore(final List<String> lore) {
        this.lore = lore;
        return this;
    }

    @Override
    public String toString() {
        return "TebexItemBuilder{" + "displayName=" + this.displayName + ", material="
                + this.material + ", lore='" + this.lore.toString() + ", hideFlags="
                + Arrays.toString(this.hideFlags) + ", isEnchanted=" + this.isEnchanted
                + ", action=" + this.action + '}';
    }
}
