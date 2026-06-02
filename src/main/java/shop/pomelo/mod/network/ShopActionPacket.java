package shop.pomelo.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShopActionPacket(Action action, String itemId, int amount) implements CustomPacketPayload {
    public static final Type<ShopActionPacket> TYPE = new Type<>(ResourceLocation.parse("pomeloshopmod:shop_action"));

    public static final StreamCodec<ByteBuf, ShopActionPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT.map(Action::fromId, Action::getId),
        ShopActionPacket::action,
        ByteBufCodecs.STRING_UTF8,
        ShopActionPacket::itemId,
        ByteBufCodecs.VAR_INT,
        ShopActionPacket::amount,
        ShopActionPacket::new
    );

    public enum Action {
        BUY(0),
        SELL(1);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Action fromId(int id) {
            for (Action action : values()) {
                if (action.id == id) {
                    return action;
                }
            }
            return BUY;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
