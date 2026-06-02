package shop.pomelo.mod.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ShopItem {
    private final String id;
    private final ItemStack itemStack;
    private final int buyPrice;
    private final int sellPrice;
    private final String category;
    private final boolean canBuy;
    private final boolean canSell;
    private final UUID sellerUUID;
    private final String sellerName;
    private final int amount;
    private final long orderIndex;
    // Cache the display name to avoid repeated ItemStack copy + Component resolution
    private final Component cachedHoverName;

    public ShopItem(String id, ItemStack itemStack, int buyPrice, int sellPrice, String category, boolean canBuy, boolean canSell) {
        this(id, itemStack, buyPrice, sellPrice, category, canBuy, canSell, null, null, itemStack.getCount(), 0);
    }

    public ShopItem(String id, ItemStack itemStack, int buyPrice, int sellPrice, String category,
                    boolean canBuy, boolean canSell, UUID sellerUUID, String sellerName, int amount) {
        this(id, itemStack, buyPrice, sellPrice, category, canBuy, canSell, sellerUUID, sellerName, amount, 0);
    }

    public ShopItem(String id, ItemStack itemStack, int buyPrice, int sellPrice, String category,
                    boolean canBuy, boolean canSell, UUID sellerUUID, String sellerName, int amount, long orderIndex) {
        this.id = id;
        this.itemStack = itemStack.copy(); // Store one copy at construction time
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.category = category;
        this.canBuy = canBuy;
        this.canSell = canSell;
        this.sellerUUID = sellerUUID;
        this.sellerName = sellerName;
        this.amount = amount;
        this.orderIndex = orderIndex;
        // Pre-compute the hover name once — ItemStack.getHoverName() creates a new Component each call
        this.cachedHoverName = this.itemStack.getHoverName();
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the stored ItemStack directly WITHOUT copying.
     * Callers MUST copy if they intend to mutate the returned stack.
     * This avoids expensive NBT deep copies on every getter call.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public String getCategory() {
        return category;
    }

    public boolean canBuy() {
        return canBuy && buyPrice > 0;
    }

    public boolean canSell() {
        return canSell && sellPrice > 0;
    }

    /**
     * Returns the stored ItemStack directly WITHOUT copying.
     * Callers MUST copy if they intend to mutate the returned stack.
     * This avoids expensive NBT deep copies on every getter call.
     */
    public ItemStack getDisplayStack() {
        return itemStack;
    }

    /**
     * Returns the pre-computed hover name. This avoids creating a new Component
     * on every call to ItemStack.getHoverName().
     */
    public Component getHoverName() {
        return cachedHoverName;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public String getSellerName() {
        return sellerName;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isPlayerListing() {
        return sellerUUID != null;
    }

    public long getOrderIndex() {
        return orderIndex;
    }
}
