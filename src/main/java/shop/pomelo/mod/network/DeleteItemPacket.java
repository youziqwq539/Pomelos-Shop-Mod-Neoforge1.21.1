package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.PomelosShopMod;

public record DeleteItemPacket(
    String itemId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeleteItemPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "delete_item"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteItemPacket> STREAM_CODEC = StreamCodec.of(
        DeleteItemPacket::encode,
        DeleteItemPacket::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buffer, DeleteItemPacket packet) {
        buffer.writeUtf(packet.itemId);
    }
    
    private static DeleteItemPacket decode(RegistryFriendlyByteBuf buffer) {
        String itemId = buffer.readUtf();
        return new DeleteItemPacket(itemId);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
