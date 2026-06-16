package shop.pomelo.mod.item;

import net.minecraft.world.item.Item;

/**
 * 方块币物品 - 每个方块币算作商店钱币系统的1单位
 * 在背包中（包括钱包、潜影盒、收纳袋等容器）的方块币会自动同步到商店余额
 */
public class CoinItem extends Item {
    public CoinItem(Properties properties) {
        super(properties.stacksTo(100)); // 最大堆叠100个
    }
}