package io.tebex.plugin.gui;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.util.ItemUtil;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.CategoryPackage;
import io.tebex.sdk.obj.ICategory;
import io.tebex.sdk.obj.SubCategory;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class BuyGUI {

    private final TebexPlugin platform;
    private final YamlDocument config;

    public BuyGUI(final TebexPlugin platform) {
        this.platform = platform;
        this.config = platform.getPlatformConfig().getYamlDocument();
    }

    public void open(final ServerPlayer player) {
        final List<Category> categories = this.platform.getStoreCategories();
        if (categories == null) {
            player.sendSystemMessage(
                    Component.literal("Failed to get listing. Please contact an administrator."),
                    false);
            this.platform.warning("Player " + player.getName()
                            + " used buy command, but no listings are active in your store.",
                    "Ensure your store is set up and has at least one active listing. Use /tebex "
                            + "reload to load new listings.");
            return;
        }

        final int rows = this.config.getInt("gui.menu.home.rows") < 1 ? categories.size() / 9 + 1
                : this.config.getInt("gui.menu.home.rows");
        final ListingGui listingGui = new ListingGui(rows, this.getScreenHandlerType(rows), player);
        listingGui.setTitle(Component.literal(this.convertToLegacyString(
                this.config.getString("gui.menu.home.title", "Server Shop"))).getString());

        categories.sort(Comparator.comparingInt(Category::getOrder));

        categories.forEach(category -> listingGui.addItem(
                this.getCategoryItemBuilder(category).asGuiItem(action -> {
                    listingGui.close();
                    this.openCategoryMenu(player, category);
                })));

        this.platform.executeBlocking(listingGui::open);
    }

    private MenuType<ChestMenu> getScreenHandlerType(final int rows) {
        final MenuType<ChestMenu> type;
        switch (rows) {
            case 1 -> type = MenuType.GENERIC_9x1;
            case 2 -> type = MenuType.GENERIC_9x2;
            case 3 -> type = MenuType.GENERIC_9x3;
            case 4 -> type = MenuType.GENERIC_9x4;
            case 5 -> type = MenuType.GENERIC_9x5;
            default -> type = MenuType.GENERIC_9x6;
        }
        return type;
    }

    private String convertToLegacyString(final String str) {
        return str.replace("&", "§");
    }

    private void openCategoryMenu(final ServerPlayer player, final ICategory category) {
        final int rows =
                this.config.getInt("gui.menu.category.rows") < 1 ? category.getPackages().size() / 9
                        + 1 : this.config.getInt("gui.menu.category.rows");

        final ListingGui subListingGui =
                new ListingGui(rows, this.getScreenHandlerType(rows), player);
        subListingGui.setTitle(Component.literal(this.convertToLegacyString(
                this.config.getString("gui.menu.category.title")
                        .replace("%category%", category.getName()))).getString());

        category.getPackages().sort(Comparator.comparingInt(CategoryPackage::getOrder));

        if (category instanceof final Category cat) {
            if (cat.getSubCategories() != null) {
                cat.getSubCategories().forEach(subCategory -> subListingGui.addItem(
                        this.getCategoryItemBuilder(subCategory).asGuiItem(action -> {
                            this.openCategoryMenu(player, subCategory);
                        })));

                final TebexGuiItem backItem = this.getBackItemBuilder().asGuiItem(action -> {
                    action.setCancelled(true);
                    this.open(player);
                });
                final int backItemSlot = subListingGui.getRows() * 9 - 5;
                subListingGui.addItem(backItemSlot, backItem);
                //subListingGui.setItem(backItemSlot, backItem);
            }
        } else if (category instanceof SubCategory) {
            final SubCategory subCategory = (SubCategory) category;

            subListingGui.setTitle(Component.literal(
                    this.convertToLegacyString(this.config.getString("gui.menu.sub-category.title"))
                            .replace("%category%", subCategory.getParent().getName())
                            .replace("%sub_category%", category.getName())).getString());

            final TebexGuiItem backItem = this.getBackItemBuilder().asGuiItem(action -> {
                action.setCancelled(true);
                this.openCategoryMenu(player, subCategory.getParent());
            });
            final int backItemSlot = subListingGui.getRows() * 9 - 5;

            subListingGui.addItem(backItemSlot, backItem);
            //subListingGui.setItem(subListingGui.getRows() * 9 - 5,backItem);
        }

        category.getPackages().forEach(categoryPackage -> subListingGui.addItem(
                this.getPackageItemBuilder(categoryPackage).asGuiItem(action -> {
                    player.closeContainer();

                    // Create Checkout Url
                    this.platform.getSDK().createCheckoutUrl(categoryPackage.getId(),
                            player.getName().getString()).thenAccept(checkout -> {
                        player.sendSystemMessage(Component.literal("§aYou can checkout here: "),
                                false);
                        player.sendSystemMessage(Component.literal("§a" + checkout.getUrl())
                                .setStyle(Style.EMPTY.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.OPEN_URL,
                                                checkout.getUrl()))), false);
                    }).exceptionally(ex -> {
                        player.sendSystemMessage(Component.literal(
                                "§cFailed to create checkout URL. Please contact an "
                                        + "administrator."), false);
                        this.platform.error("Failed to create checkout URL for a user.", ex);
                        return null;
                    });
                })));

        subListingGui.open();
    }

    private TebexItemBuilder getCategoryItemBuilder(final ICategory category) {
        final Section section = this.config.getSection("gui.item.category");

        final String itemType = section.getString("material");

        final Item defaultItem =
                ItemUtil.fromString(itemType).isPresent() ? ItemUtil.fromString(itemType).get()
                        : null;
        final Item item =
                ItemUtil.fromString(category.getGuiItem()).isPresent() ? ItemUtil.fromString(
                        category.getGuiItem()).get() : defaultItem;

        final String name = section.getString("name");
        final List<String> lore = section.getStringList("lore");

        return TebexItemBuilder.from(item != null ? item : Items.BOOK)
                .hideFlags(DataComponents.ENCHANTMENTS, DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE)
                .name(name != null ? this.remapLegacyFormatSeparator(
                        this.italicize(this.handlePlaceholders(category, name)))
                        : this.remapLegacyFormatSeparator(category.getName())).lore(lore.stream()
                        .map(line -> this.remapLegacyFormatSeparator(
                                this.italicize(this.handlePlaceholders(category, line))))
                        .collect(Collectors.toList()));
    }

    private TebexItemBuilder getPackageItemBuilder(final CategoryPackage categoryPackage) {
        final Section section = this.config.getSection(
                "gui.item." + (categoryPackage.hasSale() ? "package-sale" : "package"));

        if (section == null) {
            this.platform.warning("Invalid configuration section for " + (categoryPackage.hasSale()
                            ? "package-sale" : "package"),
                    "Check that your package definition for `" + categoryPackage.getName()
                            + "` in config.yml is valid.");
            return null;
        }

        final String itemType = section.getString("material");
        final Item material =
                BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemType.toLowerCase()));

        final String name = section.getString("name");
        final List<String> lore = section.getStringList("lore");


        final Component guiName = Component.literal(this.convertToLegacyString(
                name != null ? this.handlePlaceholders(categoryPackage, name)
                        : categoryPackage.getName())).setStyle(Style.EMPTY.withItalic(true));
        final List<String> guiLore = lore.stream().map(line -> Component.literal(
                        this.convertToLegacyString(this.handlePlaceholders(categoryPackage, line)))
                .setStyle(Style.EMPTY.withItalic(true)).getString()).collect(Collectors.toList());

        material.asItem();
        final TebexItemBuilder guiElementBuilder = TebexItemBuilder.from(material)
                .hideFlags(DataComponents.ENCHANTMENTS, DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE).name(guiName.getString()).lore(guiLore);

        if (categoryPackage.hasSale()) {
            guiElementBuilder.enchant();
        }

        return guiElementBuilder;
    }

    private TebexItemBuilder getBackItemBuilder() {
        final Section section = this.config.getSection("gui.item.back");

        final String itemType = section.getString("material");
        final Item material =
                BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemType.toLowerCase()));

        final String name = section.getString("name");
        final List<String> lore = section.getStringList("lore");

        material.asItem();
        return TebexItemBuilder.from(material)
                .hideFlags(DataComponents.ENCHANTMENTS, DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE)
                .name(Component.literal(this.convertToLegacyString(name != null ? name : "§fBack"))
                        .getString()).lore(lore.stream()
                        .map(line -> Component.literal(this.convertToLegacyString(line))
                                .setStyle(Style.EMPTY.withItalic(true)).getString())
                        .collect(Collectors.toList()));
    }

    private String handlePlaceholders(final Object obj, String str) {
        if (obj instanceof final ICategory category) {
            str = str.replace("%category%", category.getName());
        } else if (obj instanceof final CategoryPackage categoryPackage) {
            final DecimalFormat decimalFormat = new DecimalFormat("#.##");

            str = str.replace("%package_name%", categoryPackage.getName())
                    .replace("%package_price%", decimalFormat.format(categoryPackage.getPrice()))
                    .replace("%package_currency_name%",
                            this.platform.getStoreInformation().getStore().getCurrency()
                                    .getIso4217()).replace("%package_currency%",
                            this.platform.getStoreInformation().getStore().getCurrency()
                                    .getSymbol());

            if (categoryPackage.hasSale()) {
                str = str.replace("%package_discount%",
                                decimalFormat.format(categoryPackage.getSale().getDiscount()))
                        .replace("%package_sale_price%", decimalFormat.format(
                                categoryPackage.getPrice() - categoryPackage.getSale()
                                        .getDiscount()));
            }
        }

        return str;
    }

    private String italicize(final String input) {
        return "§o" + input + "§r";
    }

    private String remapLegacyFormatSeparator(final String input) {
        return input.replaceAll("&", "§");
    }
}
