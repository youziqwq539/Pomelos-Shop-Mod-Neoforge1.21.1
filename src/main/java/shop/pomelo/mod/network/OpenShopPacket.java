package shop.pomelo.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenShopPacket() implements CustomPacketPayload {
    public static final Type<OpenShopPacket> TYPE = new Type<>(ResourceLocation.parse("pomeloshopmod:open_shop"));
    public static final OpenShopPacket INSTANCE = new OpenShopPacket();

    public static final StreamCodec<ByteBuf, OpenShopPacket> STREAM_CODEC = StreamCodec.of(
        (buffer, packet) -> {},
        buffer -> INSTANCE
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
