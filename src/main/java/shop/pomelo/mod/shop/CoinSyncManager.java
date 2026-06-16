package shop.pomelo.mod.shop;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import shop.pomelo.mod.item.ModItems;

/**
 * 方块币同步管理器 - 计算玩家背包中所有方块币数量并同步到商店钱币系统
 * 包括：主背包、快捷栏、钱包、潜影盒、收纳袋等容器
 * 方块币：每个算1单位
 * 十纸币：每个算10单位
 * 50纸币：每个算50单位
 * 100纸币：每个算100单位
 */
public class CoinSyncManager {
    
    /**
     * 计算玩家背包中所有方块币的总数量
     * 包括主背包、快捷栏、钱包槽位、以及背包中容器内的方块币
     */
    public static int countTotalCoins(ServerPlayer player) {
        int total = 0;
        
        // 1. 主背包（27格）+ 快捷栏（9格）
        total += countCoinsInContainer(player.getInventory());
        
        // 2. 钱包槽位（如果玩家持有钱包物品）
        total += countCoinsInWallet(player);
        
        // 3. 背包中的容器物品（潜影盒、收纳袋等）
        total += countCoinsInContainersInInventory(player.getInventory());
        
        return total;
    }
    
    /**
     * 计算容器中的方块币数量（包括方块币、十纸币、50纸币和100纸币）
     */
    private static int countCoinsInContainer(Container container) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                // 方块币：每个算1单位
                if (stack.getItem() == ModItems.COIN.get()) {
                    count += stack.getCount();
                }
                // 十纸币：每个算10单位
                else if (stack.getItem() == ModItems.PAPER_MONEY.get()) {
                    count += stack.getCount() * 10;
                }
                // 50纸币：每个算50单位
                else if (stack.getItem() == ModItems.FIFTY_PAPER_MONEY.get()) {
                    count += stack.getCount() * 50;
                }
                // 100纸币：每个算100单位
                else if (stack.getItem() == ModItems.HUNDRED_PAPER_MONEY.get()) {
                    count += stack.getCount() * 100;
                }
            }
        }
        return count;
    }
    
    /**
     * 计算钱包中的方块币数量（包括方块币和十纸币）
     */
    private static int countCoinsInWallet(ServerPlayer player) {
        int count = 0;
        // 检查玩家背包中的钱包物品
        Container inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.WALLET.get()) {
                // 钱包使用 DataComponents.CONTAINER 存储内容
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                if (contents != null) {
                    count += countCoinsInItemContainerContents(contents);
                }
            }
        }
        return count;
    }
    
    /**
     * 计算背包中容器物品（潜影盒、收纳袋等）内的方块币数量
     * 注意：排除钱包物品，因为钱包在 countCoinsInWallet 中单独统计
     */
    private static int countCoinsInContainersInInventory(Container inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                // 排除钱包物品（钱包在 countCoinsInWallet 中单独统计）
                if (stack.getItem() == ModItems.WALLET.get()) {
                    continue;
                }
                
                // 检查是否是潜影盒（使用 DataComponents.CONTAINER）
                ItemContainerContents containerContents = stack.get(DataComponents.CONTAINER);
                if (containerContents != null) {
                    count += countCoinsInItemContainerContents(containerContents);
                }
                
                // 检查是否是收纳袋（使用 DataComponents.BUNDLE_CONTENTS）
                BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
                if (bundleContents != null) {
                    count += countCoinsInBundleContents(bundleContents);
                }
            }
        }
        return count;
    }
    
    /**
     * 计算 ItemContainerContents（潜影盒内容）中的方块币数量（包括方块币、十纸币、50纸币和100纸币）
     */
    private static int countCoinsInItemContainerContents(ItemContainerContents contents) {
        return contents.stream()
            .filter(stack -> !stack.isEmpty())
            .mapToInt(stack -> {
                // 方块币：每个算1单位
                if (stack.getItem() == ModItems.COIN.get()) {
                    return stack.getCount();
                }
                // 十纸币：每个算10单位
                else if (stack.getItem() == ModItems.PAPER_MONEY.get()) {
                    return stack.getCount() * 10;
                }
                // 50纸币：每个算50单位
                else if (stack.getItem() == ModItems.FIFTY_PAPER_MONEY.get()) {
                    return stack.getCount() * 50;
                }
                // 100纸币：每个算100单位
                else if (stack.getItem() == ModItems.HUNDRED_PAPER_MONEY.get()) {
                    return stack.getCount() * 100;
                }
                return 0;
            })
            .sum();
    }
    
    /**
     * 计算 BundleContents（收纳袋内容）中的方块币数量（包括方块币、十纸币、50纸币和100纸币）
     */
    private static int countCoinsInBundleContents(BundleContents contents) {
        int count = 0;
        int size = contents.size();
        for (int i = 0; i < size; i++) {
            ItemStack item = contents.getItemUnsafe(i);
            if (!item.isEmpty()) {
                // 方块币：每个算1单位
                if (item.getItem() == ModItems.COIN.get()) {
                    count += item.getCount();
                }
                // 十纸币：每个算10单位
                else if (item.getItem() == ModItems.PAPER_MONEY.get()) {
                    count += item.getCount() * 10;
                }
                // 50纸币：每个算50单位
                else if (item.getItem() == ModItems.FIFTY_PAPER_MONEY.get()) {
                    count += item.getCount() * 50;
                }
                // 100纸币：每个算100单位
                else if (item.getItem() == ModItems.HUNDRED_PAPER_MONEY.get()) {
                    count += item.getCount() * 100;
                }
            }
        }
        return count;
    }
    
    /**
     * 同步方块币数量到商店钱币系统
     * 将玩家背包中的方块币数量设置为商店余额
     */
    public static void syncCoinsToMoney(ServerPlayer player) {
        int totalCoins = countTotalCoins(player);
        ShopManager.getInstance().setPlayerMoney(player, totalCoins);
    }
    
    /**
     * 获取玩家总余额（商店余额 + 背包中的方块币）
     * 注意：如果已同步，商店余额应该等于背包方块币数量
     */
    public static int getTotalBalance(ServerPlayer player) {
        int shopMoney = ShopManager.getInstance().getPlayerMoney(player);
        int coinsInInventory = countTotalCoins(player);
        // 如果已同步，两者应该相等；这里返回较大的值以确保准确性
        return Math.max(shopMoney, coinsInInventory);
    }
}