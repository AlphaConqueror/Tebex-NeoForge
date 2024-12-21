package io.tebex.plugin.gui;

import java.util.HashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TebexBuyScreenHandler extends AbstractContainerMenu {

    private final HashMap<Integer, TebexGuiItem> guiItems;
    private boolean cancelled = false;

    public TebexBuyScreenHandler(final MenuType<?> type, final int containerId,
            final HashMap<Integer, TebexGuiItem> guiItems) {
        super(type, containerId);
        this.guiItems = guiItems;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(final @NotNull Player pPlayer, final int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(final int slotId, final int button, final @NotNull ClickType clickType,
            final @NotNull Player player) {
        if (this.cancelled) {
            return;
        }

        if (slotId >= 0 && slotId < this.slots.size()) {
            final TebexGuiItem item = this.guiItems.get(slotId);
            if (item != null && item.getAction() != null) {
                item.getAction().execute(this);
            }
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(final @NotNull Player player) {
        return true;
    }

    public void setCancelled(final boolean value) {
        this.cancelled = value;
    }
}
