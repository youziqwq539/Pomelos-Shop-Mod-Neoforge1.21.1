package shop.pomelo.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import shop.pomelo.mod.shop.ShopManager;

/**
 * 客户端请求服务器同步余额的网络包
 */
public record RequestMoneyPacket() implements CustomPacketPayload {
    public static final Type<RequestMoneyPacket> TYPE = new Type<>(ResourceLocation.parse("pomeloshopmod:request_money"));
    
    public static final StreamCodec<FriendlyByteBuf, RequestMoneyPacket> STREAM_CODEC = StreamCodec.of(
        RequestMoneyPacket::encode,
        RequestMoneyPacket::decode
    );
    
    private static void encode(FriendlyByteBuf buffer, RequestMoneyPacket packet) {
        // 无数据需要编码
    }
    
    private static RequestMoneyPacket decode(FriendlyByteBuf buffer) {
        return new RequestMoneyPacket();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 服务器处理请求余额同步
     */
    public static void handle(RequestMoneyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                int money = ShopManager.getInstance().getPlayerMoney(serverPlayer);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer, 
                    new SyncMoneyPacket(money)
                );
            }
        });
    }
}