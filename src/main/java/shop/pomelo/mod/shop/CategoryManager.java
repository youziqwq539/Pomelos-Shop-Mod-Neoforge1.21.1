package shop.pomelo.mod.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import shop.pomelo.mod.PomelosShopMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CategoryManager {
    private static CategoryManager instance;
    private final Map<String, ShopCategory> categories = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Path dataPath;
    private volatile List<ShopCategory> cachedList = List.of();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private volatile boolean dirty = false;
    private ScheduledExecutorService autoSaveExecutor;

    private CategoryManager() {
        this.dataPath = null;
    }

    private CategoryManager(Path dataPath) {
        this.dataPath = dataPath;
        loadFromFile();
    }

    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager();
        }
        return instance;
    }

    public static void initialize(MinecraftServer server) {
        Path path = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data").resolve("pomeloshopmod").resolve("categories.json");
        instance = new CategoryManager(path);
    }

    public void startAutoSave() {
        if (autoSaveExecutor != null) {
            autoSaveExecutor.shutdown();
        }
        autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
        autoSaveExecutor.scheduleWithFixedDelay(() -> {
            if (dirty && dataPath != null) {
                flushSave();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stopAutoSave() {
        if (autoSaveExecutor != null) {
            autoSaveExecutor.shutdown();
            try {
                if (!autoSaveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    autoSaveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                autoSaveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addCategory(String id, String displayName, UUID creatorUUID, String creatorName) {
        if (categories.containsKey(id)) {
            return;
        }

        ShopCategory category = new ShopCategory(id, displayName, creatorUUID, creatorName);
        categories.put(id, category);
        invalidateCache();
        markDirty();
    }

    public boolean removeCategory(String id) {
        ShopCategory category = categories.get(id);
        if (category == null) {
            return false;
        }

        categories.remove(id);
        invalidateCache();
        markDirty();
        return true;
    }

    public void removeCategory(String id, UUID playerUUID) {
        ShopCategory category = categories.get(id);
        if (category == null) {
            return;
        }

        if (!category.getCreatorUUID().equals(playerUUID)) {
            return;
        }

        categories.remove(id);
        invalidateCache();
        markDirty();
    }

    public void invalidateCache() {
        cachedList = List.of();
    }

    public List<ShopCategory> getCategories() {
        List<ShopCategory> cache = cachedList;
        if (!cache.isEmpty()) {
            return cache;
        }
        synchronized (this) {
            if (cachedList.isEmpty()) {
                cachedList = List.copyOf(categories.values());
            }
            return cachedList;
        }
    }

    public ShopCategory getCategory(String id) {
        return categories.get(id);
    }

    public void clearCategories() {
        categories.clear();
        invalidateCache();
    }

    public void addCategoryWithoutSave(ShopCategory category) {
        categories.put(category.getId(), category);
    }

    private void markDirty() {
        dirty = true;
    }

    public void flushSave() {
        if (!dirty || dataPath == null) {
            return;
        }
        saveToFile();
        dirty = false;
    }

    private void saveToFile() {
        if (dataPath == null) {
            return;
        }

        try {
            Files.createDirectories(dataPath.getParent());

            JsonObject root = new JsonObject();
            JsonArray categoriesArray = new JsonArray();

            for (ShopCategory category : categories.values()) {
                JsonObject catObj = new JsonObject();
                catObj.addProperty("id", category.getId());
                catObj.addProperty("displayName", category.getDisplayName());
                catObj.addProperty("creatorUUID", category.getCreatorUUID().toString());
                catObj.addProperty("creatorName", category.getCreatorName());
                categoriesArray.add(catObj);
            }

            root.add("categories", categoriesArray);

            Files.writeString(dataPath, GSON.toJson(root));
            PomelosShopMod.LOGGER.info("Categories saved to {}", dataPath);
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to save categories", e);
        }
    }

    private void loadFromFile() {
        categories.clear();
        invalidateCache();
        dirty = false;

        if (dataPath == null || !Files.exists(dataPath)) {
            PomelosShopMod.LOGGER.info("Categories file not found, starting with empty categories");
            return;
        }

        try {
            String content = Files.readString(dataPath);
            JsonObject root = GSON.fromJson(content, JsonObject.class);

            if (root.has("categories")) {
                JsonArray categoriesArray = root.getAsJsonArray("categories");
                for (int i = 0; i < categoriesArray.size(); i++) {
                    JsonObject catObj = categoriesArray.get(i).getAsJsonObject();
                    String id = catObj.get("id").getAsString();
                    String displayName = catObj.get("displayName").getAsString();
                    UUID creatorUUID = UUID.fromString(catObj.get("creatorUUID").getAsString());
                    String creatorName = catObj.get("creatorName").getAsString();

                    categories.put(id, new ShopCategory(id, displayName, creatorUUID, creatorName));
                }
            }

            invalidateCache();
            PomelosShopMod.LOGGER.info("Loaded {} categories", categories.size());
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to load categories", e);
        }
    }
}
