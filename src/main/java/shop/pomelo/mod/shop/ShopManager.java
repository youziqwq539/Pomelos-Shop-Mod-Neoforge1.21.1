package shop.pomelo.mod.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.PomelosShopMod;
import shop.pomelo.mod.network.SyncMoneyPacket;
import shop.pomelo.mod.network.SyncShopItemsPacket;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ShopManager {
    private static final ShopManager INSTANCE = new ShopManager();
    private final Map<String, ShopItem> items = new ConcurrentHashMap<>();
    private final Map<String, List<ShopItem>> categoryIndex = new ConcurrentHashMap<>();
    private long nextOrderIndex = 0;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Use null as sentinel for "cache invalidated" — fixes infinite rebuild loop
    // when items map is empty (List.of().isEmpty() == true, causing repeated rebuilds)
    private volatile List<ShopItem> sortedCache = null;
    private volatile boolean dirty = false;
    private ScheduledExecutorService saveExecutor;
    private Path dataPath;

    private ShopManager() {
    }

    public static ShopManager getInstance() {
        return INSTANCE;
    }

    public void startAutoSave() {
        if (saveExecutor != null) {
            saveExecutor.shutdown();
        }
        saveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PomeloShop-AutoSave");
            t.setDaemon(true);
            return t;
        });
        saveExecutor.scheduleWithFixedDelay(() -> {
            if (dirty && dataPath != null) {
                saveToFile(dataPath);
                dirty = false;
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stopAutoSave() {
        if (saveExecutor != null) {
            saveExecutor.shutdown();
            try {
                if (!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    saveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                saveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setDataPath(Path path) {
        this.dataPath = path;
    }

    public void invalidateCache() {
        sortedCache = null;
        categoryIndex.clear();
    }

    private void rebuildCategoryIndex() {
        categoryIndex.clear();
        items.values().stream()
            .collect(Collectors.groupingBy(ShopItem::getCategory))
            .forEach((category, itemList) ->
                categoryIndex.put(category, List.copyOf(itemList.stream()
                    .sorted(Comparator.comparingLong(ShopItem::getOrderIndex))
                    .toList()))
            );
    }

    private List<ShopItem> getSortedCache() {
        List<ShopItem> cache = sortedCache;
        if (cache != null) {
            return cache;
        }
        synchronized (this) {
            if (sortedCache == null) {
                sortedCache = List.copyOf(
                    items.values().stream()
                        .sorted(Comparator.comparingLong(ShopItem::getOrderIndex))
                        .toList()
                );
            }
            return sortedCache;
        }
    }

    public void addItem(ShopItem item) {
        items.put(item.getId(), item);
        if (item.getOrderIndex() >= nextOrderIndex) {
            nextOrderIndex = item.getOrderIndex() + 1;
        }
        invalidateCache();
    }

    public void clearItems() {
        items.clear();
        invalidateCache();
    }

    public ShopItem getItem(String id) {
        return items.get(id);
    }

    public void updateItem(String id, int buyPrice, int sellPrice, int amount, String category) {
        updateItem(id, buyPrice, sellPrice, amount, category, 0);
    }

    public void updateItem(String id, int buyPrice, int sellPrice, int amount, String category, int stock) {
        ShopItem existing = items.get(id);
        if (existing == null) {
            return;
        }

        updateItem(id, buyPrice, sellPrice, amount, category, stock, existing.getItemStack().copy());
    }

    public void updateItem(String id, int buyPrice, int sellPrice, int amount, String category, int stock, net.minecraft.world.item.ItemStack newItemStack) {
        ShopItem existing = items.get(id);
        if (existing == null) {
            return;
        }

        ShopItem updated = new ShopItem(
            id,
            newItemStack.copy(),
            buyPrice,
            sellPrice,
            category,
            buyPrice > 0,
            sellPrice > 0,
            existing.getSellerUUID(),
            existing.getSellerName(),
            amount,
            existing.getOrderIndex(),
            stock
        );

        items.put(id, updated);
        invalidateCache();
        markDirty();
    }

    /**
     * 更新商品库存
     */
    public void updateItemStock(String id, int newStock) {
        ShopItem existing = items.get(id);
        if (existing == null) {
            return;
        }

        ShopItem updated = new ShopItem(
            id,
            existing.getItemStack().copy(),
            existing.getBuyPrice(),
            existing.getSellPrice(),
            existing.getCategory(),
            existing.canBuy(),
            existing.canSell(),
            existing.getSellerUUID(),
            existing.getSellerName(),
            existing.getAmount(),
            existing.getOrderIndex(),
            newStock
        );

        items.put(id, updated);
        invalidateCache();
        markDirty();
    }

    /**
     * 同步商品数据给所有在线玩家
     */
    public void syncItemsToAllPlayers() {
        java.util.List<ShopItem> itemsList = new java.util.ArrayList<>(getAllItems());
        net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
            new SyncShopItemsPacket(itemsList)
        );
    }

    public int getItemCount() {
        return items.size();
    }

    public Collection<ShopItem> getAllItems() {
        return getSortedCache();
    }

    public List<ShopItem> getItemsByCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return getSortedCache();
        }

        List<ShopItem> cached = categoryIndex.get(categoryId);
        if (cached != null) {
            return cached;
        }

        // Lazy rebuild: only rebuild the index if it's empty AND we have items
        if (categoryIndex.isEmpty() && !items.isEmpty()) {
            synchronized (this) {
                if (categoryIndex.isEmpty()) {
                    rebuildCategoryIndex();
                }
            }
            cached = categoryIndex.get(categoryId);
            if (cached != null) {
                return cached;
            }
        }

        return List.of();
    }

    public List<ShopItem> getItemsPage(int page, int itemsPerPage) {
        List<ShopItem> all = getSortedCache();
        int start = page * itemsPerPage;
        if (start >= all.size()) {
            return List.of();
        }
        int end = Math.min(start + itemsPerPage, all.size());
        return all.subList(start, end);
    }

    public List<ShopItem> getItemsPageByCategory(String categoryId, int page, int itemsPerPage) {
        List<ShopItem> categoryItems = getItemsByCategory(categoryId);
        int start = page * itemsPerPage;
        if (start >= categoryItems.size()) {
            return List.of();
        }
        int end = Math.min(start + itemsPerPage, categoryItems.size());
        return categoryItems.subList(start, end);
    }

    public int getTotalPages(int itemsPerPage) {
        if (itemsPerPage <= 0) return 0;
        return (items.size() + itemsPerPage - 1) / itemsPerPage;
    }

    public int getTotalPagesByCategory(String categoryId, int itemsPerPage) {
        if (itemsPerPage <= 0) return 0;
        List<ShopItem> categoryItems = getItemsByCategory(categoryId);
        return (categoryItems.size() + itemsPerPage - 1) / itemsPerPage;
    }

    public List<ShopItem> searchItems(String query) {
        String lowerQuery = query.toLowerCase();
        return getSortedCache().stream()
            .filter(item -> {
                String itemName = item.getHoverName().getString().toLowerCase();
                String itemId = item.getId().toLowerCase();
                return itemName.contains(lowerQuery) || itemId.contains(lowerQuery);
            })
            .toList();
    }

    public boolean buyItem(ServerPlayer player, String itemId, int amount) {
        ShopItem item = items.get(itemId);
        if (item == null || !item.canBuy()) {
            return false;
        }

        // 检查库存：如果商品有有限库存且已售空，则无法购买
        if (item.isSoldOut()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.item_sold_out")
            );
            return false;
        }

        // 检查购买数量是否超过库存
        if (item.hasLimitedStock() && amount > item.getStock()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.insufficient_stock", item.getStock())
            );
            return false;
        }

        int totalCost = item.getBuyPrice() * amount;
        if (!hasEnoughMoney(player, totalCost)) {
            return false;
        }

        if (!removeMoney(player, totalCost)) {
            return false;
        }

        // Single copy — getItemStack() no longer copies internally
        ItemStack purchasedItem = item.getItemStack().copy();
        purchasedItem.setCount(amount);

        if (!player.getInventory().add(purchasedItem)) {
            player.drop(purchasedItem, false);
        }

        // 减少库存（如果有有限库存）
        if (item.hasLimitedStock()) {
            int newStock = item.getStock() - amount;
            // 如果库存减到0或以下，设为-1表示已售空
            if (newStock <= 0) {
                newStock = -1;
            }
            updateItemStock(itemId, newStock);
            
            // 同步更新给所有在线玩家
            syncItemsToAllPlayers();
        }

        return true;
    }

    public boolean sellItem(ServerPlayer player, String itemId, int amount) {
        ShopItem item = items.get(itemId);
        if (item == null || !item.canSell()) {
            return false;
        }

        // Single copy — getItemStack() no longer copies internally
        ItemStack itemToSell = item.getItemStack().copy();
        itemToSell.setCount(amount);

        // Quick check: does player have enough of this item?
        if (!player.getInventory().contains(itemToSell)) {
            return false;
        }

        // Remove items from inventory — uses the original (non-copy) stack for matching
        player.getInventory().clearOrCountMatchingItems(
            stack -> ItemStack.isSameItemSameComponents(stack, item.getItemStack()),
            amount,
            player.getInventory()
        );

        int totalValue = item.getSellPrice() * amount;
        addMoney(player, totalValue);

        // 出售后增加库存
        int currentStock = item.getStock();
        if (currentStock == -1) {
            // 已售空状态，出售后恢复库存
            item.setStock(amount);
        } else if (currentStock > 0) {
            // 有限库存，增加库存
            item.setStock(currentStock + amount);
        }
        // 无限库存（stock == 0）不需要修改

        // 同步库存更新给所有玩家
        syncItemsToAllPlayers();

        return true;
    }

    public int getPlayerMoney(ServerPlayer player) {
        return player.getPersistentData().getInt("pomeloshopmod:money");
    }

    public boolean hasEnoughMoney(ServerPlayer player, int amount) {
        return getPlayerMoney(player) >= amount;
    }

    public boolean removeMoney(ServerPlayer player, int amount) {
        int currentMoney = getPlayerMoney(player);
        if (currentMoney < amount) {
            return false;
        }
        player.getPersistentData().putInt("pomeloshopmod:money", currentMoney - amount);
        return true;
    }

    public void addMoney(ServerPlayer player, int amount) {
        int currentMoney = getPlayerMoney(player);
        player.getPersistentData().putInt("pomeloshopmod:money", currentMoney + amount);
    }

    public void setPlayerMoney(ServerPlayer player, int amount) {
        player.getPersistentData().putInt("pomeloshopmod:money", amount);
    }

    public void markDirty() {
        dirty = true;
    }

    public void saveToFile(Path path) {
        try {
            JsonObject root = new JsonObject();

            JsonArray itemsArray = new JsonArray();
            for (ShopItem item : items.values()) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("id", item.getId());
                itemObj.addProperty("item", BuiltInRegistries.ITEM.getKey(item.getItemStack().getItem()).toString());
                itemObj.addProperty("count", item.getItemStack().getCount());
                itemObj.addProperty("buyPrice", item.getBuyPrice());
                itemObj.addProperty("sellPrice", item.getSellPrice());
                itemObj.addProperty("category", item.getCategory());
                itemObj.addProperty("canBuy", item.canBuy());
                itemObj.addProperty("canSell", item.canSell());
                if (item.getSellerUUID() != null) {
                    itemObj.addProperty("sellerUUID", item.getSellerUUID().toString());
                }
                if (item.getSellerName() != null) {
                    itemObj.addProperty("sellerName", item.getSellerName());
                }
                itemObj.addProperty("orderIndex", item.getOrderIndex());
                itemObj.addProperty("stock", item.getStock());
                itemsArray.add(itemObj);
            }
            root.add("items", itemsArray);

            Files.createDirectories(path.getParent());
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to save shop data", e);
        }
    }

    public void loadFromFile(Path path) {
        items.clear();
        categoryIndex.clear();
        sortedCache = null;
        nextOrderIndex = 0;
        dirty = false;

        if (!Files.exists(path)) {
            PomelosShopMod.LOGGER.info("Shop data file not found, using defaults");
            loadDefaultItems();
            return;
        }

        try (Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root.has("items")) {
                JsonArray itemsArray = root.getAsJsonArray("items");
                for (int i = 0; i < itemsArray.size(); i++) {
                    JsonObject itemObj = itemsArray.get(i).getAsJsonObject();
                    String id = itemObj.get("id").getAsString();
                    String itemStr = itemObj.get("item").getAsString();
                    int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;
                    int buyPrice = itemObj.get("buyPrice").getAsInt();
                    int sellPrice = itemObj.get("sellPrice").getAsInt();
                    String category = itemObj.get("category").getAsString();
                    boolean canBuy = itemObj.has("canBuy") ? itemObj.get("canBuy").getAsBoolean() : true;
                    boolean canSell = itemObj.has("canSell") ? itemObj.get("canSell").getAsBoolean() : true;
                    UUID sellerUUID = null;
                    String sellerName = null;
                    if (itemObj.has("sellerUUID")) {
                        sellerUUID = UUID.fromString(itemObj.get("sellerUUID").getAsString());
                    }
                    if (itemObj.has("sellerName")) {
                        sellerName = itemObj.get("sellerName").getAsString();
                    }
                    long orderIndex = itemObj.has("orderIndex") ? itemObj.get("orderIndex").getAsLong() : i;
                    int stock = itemObj.has("stock") ? itemObj.get("stock").getAsInt() : 0;

                    net.minecraft.resources.ResourceLocation itemLoc = net.minecraft.resources.ResourceLocation.parse(itemStr);
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemLoc);
                    if (item != net.minecraft.world.item.Items.AIR) {
                        ItemStack stack = new ItemStack(item, count);
                        addItem(new ShopItem(id, stack, buyPrice, sellPrice, category, canBuy, canSell, sellerUUID, sellerName, count, orderIndex, stock));
                    }
                }
            }
        } catch (IOException e) {
            PomelosShopMod.LOGGER.error("Failed to load shop data", e);
            loadDefaultItems();
        }
    }

    private void loadDefaultItems() {
        items.clear();
        categoryIndex.clear();
        sortedCache = null;
        nextOrderIndex = 0;
        dirty = false;
        PomelosShopMod.LOGGER.info("No default items loaded. Players can list their own items.");
    }

    public String listItem(ServerPlayer seller, ItemStack itemStack, int price, int sellPrice, int amount, String categoryId) {
        return listItem(seller, itemStack, price, sellPrice, amount, categoryId, 0);
    }

    public String listItem(ServerPlayer seller, ItemStack itemStack, int price, int sellPrice, int amount, String categoryId, int stock) {
        if (itemStack.isEmpty() || price <= 0 || amount <= 0) {
            return null;
        }

        if (BannedItemsManager.getInstance().isBanned(itemStack)) {
            PomelosShopMod.LOGGER.warn("Player {} attempted to list banned item: {}",
                seller.getName().getString(),
                BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
            return null;
        }

        ItemStack itemToSell = itemStack.copy();
        itemToSell.setCount(amount);

        String itemId = "player_" + java.util.UUID.randomUUID().toString().substring(0, 8);

        boolean canSell = sellPrice > 0;

        synchronized (this) {
            long orderIdx = nextOrderIndex++;
            ShopItem shopItem = new ShopItem(
                itemId,
                itemToSell,
                price,
                sellPrice,
                categoryId,
                true,
                canSell,
                seller.getUUID(),
                seller.getName().getString(),
                amount,
                orderIdx,
                stock
            );
            addItem(shopItem);
        }

        markDirty();

        return itemId;
    }

    public boolean removeListing(String itemId, ServerPlayer player) {
        ShopItem item = items.get(itemId);

        PomelosShopMod.LOGGER.debug("Attempting to remove item: {}", itemId);

        if (item == null) {
            PomelosShopMod.LOGGER.debug("Item not found: {}", itemId);
            return false;
        }

        if (!item.isPlayerListing()) {
            PomelosShopMod.LOGGER.debug("Item is not a player listing: {}", itemId);
            return false;
        }

        if (item.getSellerUUID() == null) {
            PomelosShopMod.LOGGER.debug("Item has no seller UUID: {}", itemId);
            return false;
        }

        PomelosShopMod.LOGGER.debug("Item seller UUID: {}, Player UUID: {}",
            item.getSellerUUID(), player.getUUID());

        if (!item.getSellerUUID().equals(player.getUUID())) {
            PomelosShopMod.LOGGER.debug("Player UUID does not match seller UUID");
            return false;
        }

        items.remove(itemId);
        invalidateCache();
        markDirty();
        PomelosShopMod.LOGGER.debug("Item removed successfully: {}", itemId);
        return true;
    }

    public void removeItemsByCategory(String categoryId) {
        java.util.List<String> toRemove = new java.util.ArrayList<>();
        for (ShopItem item : items.values()) {
            if (categoryId.equals(item.getCategory())) {
                toRemove.add(item.getId());
            }
        }
        for (String id : toRemove) {
            items.remove(id);
        }
        if (!toRemove.isEmpty()) {
            invalidateCache();
            markDirty();
            PomelosShopMod.LOGGER.debug("Removed {} items from category: {}", toRemove.size(), categoryId);
        }
    }

    public void clearAllItems(net.minecraft.server.MinecraftServer server) {
        items.clear();
        invalidateCache();
        markDirty();
        PomelosShopMod.LOGGER.info("All shop items cleared");
    }

    public void removeBannedItems(net.minecraft.server.MinecraftServer server, BannedItemsManager bannedManager) {
        boolean removed = false;
        java.util.List<String> toRemove = new java.util.ArrayList<>();
        for (ShopItem shopItem : items.values()) {
            if (bannedManager.isBanned(shopItem.getItemStack())) {
                toRemove.add(shopItem.getId());
            }
        }
        for (String id : toRemove) {
            items.remove(id);
            removed = true;
            PomelosShopMod.LOGGER.debug("Removed banned item listing: {}", id);
        }
        if (removed) {
            invalidateCache();
            markDirty();
        }
    }

    public JsonObject exportToJson() {
        JsonObject root = new JsonObject();

        JsonArray itemsArray = new JsonArray();
        for (ShopItem item : items.values()) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("id", item.getId());
            itemObj.addProperty("item", BuiltInRegistries.ITEM.getKey(item.getItemStack().getItem()).toString());
            itemObj.addProperty("count", item.getItemStack().getCount());
            itemObj.addProperty("buyPrice", item.getBuyPrice());
            itemObj.addProperty("sellPrice", item.getSellPrice());
            itemObj.addProperty("category", item.getCategory());
            itemObj.addProperty("canBuy", item.canBuy());
            itemObj.addProperty("canSell", item.canSell());
            if (item.getSellerUUID() != null) {
                itemObj.addProperty("sellerUUID", item.getSellerUUID().toString());
            }
            if (item.getSellerName() != null) {
                itemObj.addProperty("sellerName", item.getSellerName());
            }
            itemObj.addProperty("orderIndex", item.getOrderIndex());
            itemsArray.add(itemObj);
        }
        root.add("items", itemsArray);

        return root;
    }

    public int importFromJson(JsonObject root, boolean clearExisting) {
        if (clearExisting) {
            items.clear();
            categoryIndex.clear();
            sortedCache = null;
            nextOrderIndex = 0;
        }

        int importedCount = 0;
        if (root.has("items")) {
            JsonArray itemsArray = root.getAsJsonArray("items");
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject itemObj = itemsArray.get(i).getAsJsonObject();
                String id = itemObj.get("id").getAsString();
                String itemStr = itemObj.get("item").getAsString();
                int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;
                int buyPrice = itemObj.get("buyPrice").getAsInt();
                int sellPrice = itemObj.get("sellPrice").getAsInt();
                String category = itemObj.get("category").getAsString();
                boolean canBuy = itemObj.has("canBuy") ? itemObj.get("canBuy").getAsBoolean() : true;
                boolean canSell = itemObj.has("canSell") ? itemObj.get("canSell").getAsBoolean() : true;
                UUID sellerUUID = null;
                String sellerName = null;
                if (itemObj.has("sellerUUID")) {
                    sellerUUID = UUID.fromString(itemObj.get("sellerUUID").getAsString());
                }
                if (itemObj.has("sellerName")) {
                    sellerName = itemObj.get("sellerName").getAsString();
                }
                long orderIndex = itemObj.has("orderIndex") ? itemObj.get("orderIndex").getAsLong() : nextOrderIndex;

                net.minecraft.resources.ResourceLocation itemLoc = net.minecraft.resources.ResourceLocation.parse(itemStr);
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemLoc);
                if (item != net.minecraft.world.item.Items.AIR) {
                    ItemStack stack = new ItemStack(item, count);
                    addItem(new ShopItem(id, stack, buyPrice, sellPrice, category, canBuy, canSell, sellerUUID, sellerName, count, orderIndex));
                    importedCount++;
                }
            }
        }

        if (importedCount > 0) {
            invalidateCache();
            markDirty();
        }

        return importedCount;
    }

    /**
     * 同步玩家余额到客户端
     */
    public void syncMoneyToClient(ServerPlayer player) {
        int money = getPlayerMoney(player);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new SyncMoneyPacket(money));
    }
}
