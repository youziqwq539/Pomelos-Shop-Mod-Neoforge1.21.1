package shop.pomelo.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncMoneyPacket(int money) implements CustomPacketPayload {
    public static final Type<SyncMoneyPacket> TYPE = new Type<>(ResourceLocation.parse("pomeloshopmod:sync_money"));

    public static final StreamCodec<ByteBuf, SyncMoneyPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        SyncMoneyPacket::money,
        SyncMoneyPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
