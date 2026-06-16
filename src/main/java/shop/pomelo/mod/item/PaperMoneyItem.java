package shop.pomelo.mod.item;

import net.minecraft.world.item.Item;

/**
 * 十纸币物品 - 每个十纸币算作商店钱币系统的10单位
 * 在背包中（包括钱包、潜影盒、收纳袋等容器）的十纸币会自动同步到商店余额
 */
public class PaperMoneyItem extends Item {
    public PaperMoneyItem(Properties properties) {
        super(properties.stacksTo(99)); // 最大堆叠99个
    }
    
    /**
     * 获取此物品的价值（每个十纸币 = 10个方块币）
     */
    public int getValue() {
        return 10;
    }
}