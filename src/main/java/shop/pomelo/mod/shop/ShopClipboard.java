package shop.pomelo.mod.shop;

import net.minecraft.world.item.ItemStack;

/**
 * 商店剪贴板，用于存储复制的商品信息
 */
public class ShopClipboard {
    private static ShopClipboard instance;
    
    private ItemStack copiedItemStack;
    private int copiedBuyPrice;
    private int copiedSellPrice;
    private int copiedAmount;
    private int copiedStock;
    
    private ShopClipboard() {
    }
    
    public static ShopClipboard getInstance() {
        if (instance == null) {
            instance = new ShopClipboard();
        }
        return instance;
    }
    
    /**
     * 复制商品信息到剪贴板
     */
    public void copy(ShopItem item) {
        this.copiedItemStack = item.getItemStack().copy();
        this.copiedBuyPrice = item.getBuyPrice();
        this.copiedSellPrice = item.getSellPrice();
        this.copiedAmount = item.getAmount();
        this.copiedStock = item.getStock();
    }
    
    /**
     * 检查剪贴板是否有内容
     */
    public boolean hasContent() {
        return copiedItemStack != null && !copiedItemStack.isEmpty();
    }
    
    /**
     * 获取复制的物品堆
     */
    public ItemStack getCopiedItemStack() {
        return copiedItemStack != null ? copiedItemStack.copy() : ItemStack.EMPTY;
    }
    
    /**
     * 获取复制的购买价格
     */
    public int getCopiedBuyPrice() {
        return copiedBuyPrice;
    }
    
    /**
     * 获取复制的出售价格
     */
    public int getCopiedSellPrice() {
        return copiedSellPrice;
    }
    
    /**
     * 获取复制的数量
     */
    public int getCopiedAmount() {
        return copiedAmount;
    }
    
    /**
     * 获取复制的库存
     */
    public int getCopiedStock() {
        return copiedStock;
    }
    
    /**
     * 清空剪贴板
     */
    public void clear() {
        copiedItemStack = null;
        copiedBuyPrice = 0;
        copiedSellPrice = 0;
        copiedAmount = 0;
        copiedStock = 0;
    }
}