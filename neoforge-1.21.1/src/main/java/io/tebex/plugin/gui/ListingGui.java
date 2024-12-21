package io.tebex.plugin.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ListingGui {

    private final int rows;
    private final HashMap<Integer, TebexGuiItem> guiItems;
    private final MenuType<ChestMenu> containerScreenHandler;
    private final ServerPlayer player;
    private Container container;
    private String title;
    private ArrayList<String> lore;

    public ListingGui(final int rows, final MenuType<ChestMenu> screenHandlerType,
            final ServerPlayer player) {
        this.rows = rows;
        this.container = new SimpleContainer(rows * 9);
        this.lore = new ArrayList<>();
        this.guiItems = new HashMap<>();
        this.containerScreenHandler = screenHandlerType;
        this.player = player;
    }

    private static MenuType<?> getMenuTypeByRows(final int rows) {
        if (rows <= 1) {
            return MenuType.GENERIC_9x1;
        }

        if (rows >= 6) {
            return MenuType.GENERIC_9x6;
        }

        return switch (rows) {
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> throw new IllegalStateException("Unexpected value: " + rows);
        };
    }

    public ListingGui setTitle(final String title) {
        this.title = title;
        return this;
    }

    public ListingGui lore(final ArrayList<String> lore) {
        this.lore = lore;
        return this;
    }

    public ListingGui create() {
        this.container = new SimpleContainer(this.rows * 9);
        return this;
    }

    public Container getContainer() {
        return this.container;
    }

    public int getRows() {
        return this.rows;
    }

    public void addItem(final TebexGuiItem guiItem) {
        int nextSlot = 0;
        while (this.guiItems.containsKey(nextSlot) && nextSlot < this.rows * 9) {
            nextSlot++;
        }
        this.guiItems.put(nextSlot, guiItem);
    }

    public void addItem(final int index, final TebexGuiItem guiItem) {
        this.guiItems.put(index, guiItem);
    }

    public void open() {
        this.container.clearContent();

        for (final Map.Entry<Integer, TebexGuiItem> guiItems : this.guiItems.entrySet()) {
            final TebexGuiItem guiItem = guiItems.getValue();
            final ItemStack stack = guiItem.getStack();
            this.container.setItem(guiItems.getKey(), stack);
        }

        final MenuProvider menuProvider = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(final int containerId,
                    @NotNull final Inventory playerInventory, @NotNull final Player player) {
                return new TebexBuyScreenHandler(getMenuTypeByRows(ListingGui.this.rows),
                        containerId, ListingGui.this.guiItems);
            }

            @Override
            public @NotNull net.minecraft.network.chat.Component getDisplayName() {
                return Component.literal(ListingGui.this.title);
            }
        };

        this.player.openMenu(menuProvider);
    }

    public void setItem(final int slot, final TebexGuiItem guiItem) {
        this.container.setItem(slot, guiItem.getStack());
    }

    public void updateTitle(final String replace) {
        this.title = replace;
    }

    public TebexGuiItem getItemInSlot(final int slot) {
        return this.guiItems.get(slot);
    }

    public void close() {}
}
