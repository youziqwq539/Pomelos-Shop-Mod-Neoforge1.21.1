package shop.pomelo.mod.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import shop.pomelo.mod.PomelosShopMod;
import shop.pomelo.mod.shop.CoinSyncManager;
import shop.pomelo.mod.shop.ShopManager;

/**
 * 方块币同步事件处理器 - 监听玩家背包变化并同步方块币到商店钱币系统
 */
@EventBusSubscriber(modid = PomelosShopMod.MODID)
public class CoinSyncEventHandler {
    
    // 同步间隔（每5秒同步一次，避免频繁同步）
    private static final int SYNC_INTERVAL_TICKS = 100; // 100 ticks = 5秒
    private static int tickCounter = 0;
    
    /**
     * 玩家Tick事件 - 定期同步方块币数量
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // 只在服务端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        ServerPlayer player = (ServerPlayer) event.getEntity();
        
        // 每5秒同步一次
        tickCounter++;
        if (tickCounter >= SYNC_INTERVAL_TICKS) {
            tickCounter = 0;
            CoinSyncManager.syncCoinsToMoney(player);
            
            // 同步余额到客户端
            ShopManager.getInstance().syncMoneyToClient(player);
        }
    }
    
    /**
     * 容器关闭事件 - 玩家关闭容器界面时同步方块币
     * 包括关闭钱包、潜影盒、收纳袋等
     */
    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        ServerPlayer player = (ServerPlayer) event.getEntity();
        // 容器关闭后立即同步方块币
        CoinSyncManager.syncCoinsToMoney(player);
        ShopManager.getInstance().syncMoneyToClient(player);
    }
}