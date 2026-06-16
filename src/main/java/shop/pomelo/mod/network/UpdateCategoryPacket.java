package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.PomelosShopMod;

public record UpdateCategoryPacket(
    String categoryId,
    String newName
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateCategoryPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "update_category"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCategoryPacket> STREAM_CODEC = StreamCodec.of(
        UpdateCategoryPacket::encode,
        UpdateCategoryPacket::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buffer, UpdateCategoryPacket packet) {
        buffer.writeUtf(packet.categoryId);
        buffer.writeUtf(packet.newName);
    }
    
    private static UpdateCategoryPacket decode(RegistryFriendlyByteBuf buffer) {
        String categoryId = buffer.readUtf();
        String newName = buffer.readUtf();
        
        return new UpdateCategoryPacket(categoryId, newName);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
