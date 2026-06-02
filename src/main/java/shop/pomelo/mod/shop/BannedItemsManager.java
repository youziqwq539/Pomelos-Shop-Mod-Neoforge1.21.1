package shop.pomelo.mod.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.PomelosShopMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BannedItemsManager {
    private static final BannedItemsManager INSTANCE = new BannedItemsManager();
    private final Set<String> bannedItemIds = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private BannedItemsManager() {}

    public static BannedItemsManager getInstance() {
        return INSTANCE;
    }

    public boolean isBanned(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return bannedItemIds.contains(id.toString());
    }

    public boolean isBanned(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return bannedItemIds.contains(id.toString());
    }

    public boolean isBanned(String itemId) {
        return bannedItemIds.contains(itemId);
    }

    public boolean banItem(String itemId) {
        if (bannedItemIds.add(itemId)) {
            PomelosShopMod.LOGGER.info("Banned item: {}", itemId);
            return true;
        }
        return false;
    }

    public boolean unbanItem(String itemId) {
        if (bannedItemIds.remove(itemId)) {
            PomelosShopMod.LOGGER.info("Unbanned item: {}", itemId);
            return true;
        }
        return false;
    }

    public Set<String> getBannedItemIds() {
        return new HashSet<>(bannedItemIds);
    }

    public Set<String> getBannedItemNames() {
        return bannedItemIds.stream()
            .map(id -> {
                ResourceLocation loc = ResourceLocation.parse(id);
                Item item = BuiltInRegistries.ITEM.get(loc);
                // Avoid creating a new ItemStack just to get the display name —
                // Item.getDescription() or getName(ItemStack) needs a stack, but
                // Item.getDescriptionId() + translation is overkill. Use a single
                // ItemStack.EMPTY-like approach: just use the default instance.
                return item != net.minecraft.world.item.Items.AIR
                    ? item.getDescription().getString()
                    : id;
            })
            .collect(Collectors.toSet());
    }

    public void loadFromFile(Path path) {
        bannedItemIds.clear();
        try {
            if (!Files.exists(path)) {
                PomelosShopMod.LOGGER.info("No banned items file found at {}", path);
                return;
            }
            String content = Files.readString(path);
            JsonObject root = GSON.fromJson(content, JsonObject.class);
            if (root != null && root.has("bannedItems")) {
                JsonArray array = root.getAsJsonArray("bannedItems");
                for (JsonElement element : array) {
                    bannedItemIds.add(element.getAsString());
                }
            }
            PomelosShopMod.LOGGER.info("Loaded {} banned items", bannedItemIds.size());
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to load banned items", e);
        }
    }

    public void saveToFile(Path path) {
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            JsonArray array = new JsonArray();
            for (String id : bannedItemIds) {
                array.add(id);
            }
            root.add("bannedItems", array);
            Files.writeString(path, GSON.toJson(root));
            PomelosShopMod.LOGGER.info("Saved {} banned items", bannedItemIds.size());
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to save banned items", e);
        }
    }
}
