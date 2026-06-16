package shop.pomelo.mod.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 处理玩家事件，确保玩家钱币数据在死亡重生后保留
 */
public class PlayerEventHandler {
    
    /**
     * 当玩家死亡重生时，复制持久化数据（包括钱币）到新玩家实体
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            // 如果不是死亡（比如从末地传送），persistentData 会自动保留
            return;
        }
        
        // 死亡重生时，手动复制钱币数据
        var original = event.getOriginal();
        var newPlayer = event.getEntity();
        
        // 复制钱币数据
        int money = original.getPersistentData().getInt("pomeloshopmod:money");
        newPlayer.getPersistentData().putInt("pomeloshopmod:money", money);
        
        // 如果是服务器玩家，同步钱币到客户端
        if (newPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                serverPlayer,
                new shop.pomelo.mod.network.SyncMoneyPacket(money)
            );
        }
    }
}