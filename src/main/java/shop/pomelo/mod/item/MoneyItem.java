package shop.pomelo.mod.item;

import net.minecraft.world.item.Item;

/**
 * 纸币物品基类 - 可以设置不同的价值
 * 在背包中（包括钱包、潜影盒、收纳袋等容器）的纸币会自动同步到商店余额
 */
public class MoneyItem extends Item {
    private final int value;
    
    public MoneyItem(Properties properties, int value) {
        super(properties.stacksTo(99)); // 最大堆叠99个
        this.value = value;
    }
    
    /**
     * 获取此物品的价值
     */
    public int getValue() {
        return value;
    }
}