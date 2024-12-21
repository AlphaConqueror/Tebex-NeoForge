package io.tebex.plugin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ItemUtil {

    private static final Map<String, Item> ITEM_CACHE = new HashMap<>();

    public static Optional<Item> fromString(final String material) {
        if (ITEM_CACHE.containsKey(material)) {
            return Optional.of(ITEM_CACHE.get(material));
        }

        if (material.contains(":")) {
            final String namespace = material.split(":")[0];
            final String itemName = material.split(":")[1];
            final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, itemName);

            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
            item.ifPresent(value -> ITEM_CACHE.put(material, value));

            if (item.isEmpty()) { // attempt block item lookup
                final Optional<Block> blockItem = BuiltInRegistries.BLOCK.getOptional(id);
                if (blockItem.isPresent()) {
                    item = Optional.of(blockItem.get().asItem());
                    ITEM_CACHE.put(material, item.get());
                }
            }
            return item;
        }

        // no namespace in material identifier
        return Optional.empty();
    }
}
