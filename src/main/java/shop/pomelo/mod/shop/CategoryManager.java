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
        addCategory(id, displayName, creatorUUID, creatorName, null);
    }

    public void addCategory(String id, String displayName, UUID creatorUUID, String creatorName, String parentId) {
        if (categories.containsKey(id)) {
            return;
        }

        // 如果指定了父分类ID，验证父分类存在且是一级分类
        if (parentId != null && !parentId.isEmpty()) {
            ShopCategory parent = categories.get(parentId);
            if (parent == null || parent.isSubCategory()) {
                return; // 父分类不存在或父分类本身是二级分类
            }
        }

        int orderIndex = categories.size();
        ShopCategory category = new ShopCategory(id, displayName, creatorUUID, creatorName, orderIndex, parentId);
        categories.put(id, category);
        invalidateCache();
        markDirty();
    }

    public boolean removeCategory(String id) {
        ShopCategory category = categories.get(id);
        if (category == null) {
            return false;
        }

        // 如果删除的是二级分类，先删除其下的一级分类
        if (!category.isSubCategory()) {
            // 找到所有以此分类为父分类的一级分类并删除
            List<String> subCategoryIds = new ArrayList<>();
            for (ShopCategory cat : categories.values()) {
                if (id.equals(cat.getParentId())) {
                    subCategoryIds.add(cat.getId());
                }
            }
            // 删除所有子分类及其商品
            for (String subId : subCategoryIds) {
                categories.remove(subId);
                ShopManager.getInstance().removeItemsByCategory(subId);
            }
        }

        categories.remove(id);
        invalidateCache();
        markDirty();
        
        // Remove all items in this category
        ShopManager.getInstance().removeItemsByCategory(id);
        
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
        
        // Remove all items in this category
        ShopManager.getInstance().removeItemsByCategory(id);
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
                cachedList = List.copyOf(categories.values().stream()
                    .sorted(Comparator.comparingInt(ShopCategory::getOrderIndex))
                    .toList());
            }
            return cachedList;
        }
    }

    /**
     * 获取所有一级分类（没有父分类的分类）
     */
    public List<ShopCategory> getMainCategories() {
        return categories.values().stream()
            .filter(cat -> !cat.isSubCategory())
            .sorted(Comparator.comparingInt(ShopCategory::getOrderIndex))
            .toList();
    }

    /**
     * 获取指定父分类下的所有一级分类
     */
    public List<ShopCategory> getSubCategories(String parentId) {
        return categories.values().stream()
            .filter(cat -> parentId.equals(cat.getParentId()))
            .sorted(Comparator.comparingInt(ShopCategory::getOrderIndex))
            .toList();
    }

    public ShopCategory getCategory(String id) {
        return categories.get(id);
    }

    public ShopCategory getCategoryByName(String name) {
        for (ShopCategory category : categories.values()) {
            if (category.getDisplayName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }

    public void clearCategories() {
        categories.clear();
        invalidateCache();
    }

    public void addCategoryWithoutSave(ShopCategory category) {
        categories.put(category.getId(), category);
    }

    public boolean updateCategoryName(String categoryId, String newName) {
        ShopCategory category = categories.get(categoryId);
        if (category == null) {
            return false;
        }
        
        category.setDisplayName(newName);
        invalidateCache();
        markDirty();
        return true;
    }

    public boolean moveCategoryUp(String categoryId) {
        if (!categories.containsKey(categoryId)) {
            return false;
        }
        
        // Get all categories sorted by orderIndex
        List<ShopCategory> sorted = new ArrayList<>(categories.values().stream()
            .sorted(Comparator.comparingInt(ShopCategory::getOrderIndex))
            .toList());
        
        int index = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getId().equals(categoryId)) {
                index = i;
                break;
            }
        }
        
        if (index <= 0) {
            return false;
        }
        
        // Swap positions in the list
        ShopCategory temp = sorted.get(index);
        sorted.set(index, sorted.get(index - 1));
        sorted.set(index - 1, temp);
        
        // Reassign order indices to maintain order
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setOrderIndex(i);
        }
        
        invalidateCache();
        markDirty();
        return true;
    }

    public boolean moveCategoryDown(String categoryId) {
        if (!categories.containsKey(categoryId)) {
            return false;
        }
        
        // Get all categories sorted by orderIndex
        List<ShopCategory> sorted = new ArrayList<>(categories.values().stream()
            .sorted(Comparator.comparingInt(ShopCategory::getOrderIndex))
            .toList());
        
        int index = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getId().equals(categoryId)) {
                index = i;
                break;
            }
        }
        
        if (index < 0 || index >= sorted.size() - 1) {
            return false;
        }
        
        // Swap positions in the list
        ShopCategory temp = sorted.get(index);
        sorted.set(index, sorted.get(index + 1));
        sorted.set(index + 1, temp);
        
        // Reassign order indices to maintain order
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setOrderIndex(i);
        }
        
        invalidateCache();
        markDirty();
        return true;
    }

    /**
     * 设置分类的父分类
     * @param childCategoryId 子分类ID或名称
     * @param parentCategoryId 父分类ID或名称（空字符串或"none"表示移除父分类）
     * @return 是否成功设置
     */
    public boolean setCategoryParent(String childCategoryId, String parentCategoryId) {
        // 通过ID或名称查找子分类
        ShopCategory childCategory = categories.get(childCategoryId);
        if (childCategory == null) {
            childCategory = getCategoryByName(childCategoryId);
        }
        
        if (childCategory == null) {
            return false;
        }
        
        // 检查父分类
        String newParentId = null;
        if (parentCategoryId != null && !parentCategoryId.isEmpty() && 
            !parentCategoryId.equalsIgnoreCase("none") && !parentCategoryId.equalsIgnoreCase("null")) {
            // 通过ID或名称查找父分类
            ShopCategory parentCategory = categories.get(parentCategoryId);
            if (parentCategory == null) {
                parentCategory = getCategoryByName(parentCategoryId);
            }
            
            if (parentCategory == null) {
                return false;
            }
            
            // 不能把自己设为自己的父分类
            if (parentCategory.getId().equals(childCategory.getId())) {
                return false;
            }
            
            // 不能形成循环：检查父分类的祖先链
            String checkId = parentCategory.getId();
            while (checkId != null && !checkId.isEmpty()) {
                if (checkId.equals(childCategory.getId())) {
                    return false; // 形成循环
                }
                ShopCategory checkCat = categories.get(checkId);
                if (checkCat == null) break;
                checkId = checkCat.getParentId();
            }
            
            newParentId = parentCategory.getId();
        }
        
        // 设置新的父分类
        childCategory.setParentId(newParentId);
        invalidateCache();
        markDirty();
        return true;
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
                catObj.addProperty("orderIndex", category.getOrderIndex());
                if (category.getParentId() != null && !category.getParentId().isEmpty()) {
                    catObj.addProperty("parentId", category.getParentId());
                }
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
                    int orderIndex = catObj.has("orderIndex") ? catObj.get("orderIndex").getAsInt() : i;
                    String parentId = catObj.has("parentId") ? catObj.get("parentId").getAsString() : null;

                    categories.put(id, new ShopCategory(id, displayName, creatorUUID, creatorName, orderIndex, parentId));
                }
            }

            invalidateCache();
            PomelosShopMod.LOGGER.info("Loaded {} categories", categories.size());
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to load categories", e);
        }
    }

    public JsonObject exportToJson() {
        JsonObject root = new JsonObject();
        JsonArray categoriesArray = new JsonArray();

        for (ShopCategory category : categories.values()) {
            JsonObject catObj = new JsonObject();
            catObj.addProperty("id", category.getId());
            catObj.addProperty("displayName", category.getDisplayName());
            catObj.addProperty("creatorUUID", category.getCreatorUUID().toString());
            catObj.addProperty("creatorName", category.getCreatorName());
            catObj.addProperty("orderIndex", category.getOrderIndex());
            if (category.getParentId() != null && !category.getParentId().isEmpty()) {
                catObj.addProperty("parentId", category.getParentId());
            }
            categoriesArray.add(catObj);
        }

        root.add("categories", categoriesArray);
        return root;
    }

    public int importFromJson(JsonObject root, boolean clearExisting) {
        if (clearExisting) {
            categories.clear();
            invalidateCache();
        }

        int importedCount = 0;
        if (root.has("categories")) {
            JsonArray categoriesArray = root.getAsJsonArray("categories");
            for (int i = 0; i < categoriesArray.size(); i++) {
                JsonObject catObj = categoriesArray.get(i).getAsJsonObject();
                String id = catObj.get("id").getAsString();
                String displayName = catObj.get("displayName").getAsString();
                UUID creatorUUID = UUID.fromString(catObj.get("creatorUUID").getAsString());
                String creatorName = catObj.get("creatorName").getAsString();
                int orderIndex = catObj.has("orderIndex") ? catObj.get("orderIndex").getAsInt() : i;
                String parentId = catObj.has("parentId") ? catObj.get("parentId").getAsString() : null;

                categories.put(id, new ShopCategory(id, displayName, creatorUUID, creatorName, orderIndex, parentId));
                importedCount++;
            }
        }

        if (importedCount > 0) {
            invalidateCache();
            markDirty();
        }

        return importedCount;
    }
}
