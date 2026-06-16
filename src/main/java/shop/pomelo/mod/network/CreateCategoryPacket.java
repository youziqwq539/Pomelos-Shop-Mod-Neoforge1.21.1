package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.PomelosShopMod;

public record CreateCategoryPacket(
    String categoryId,
    String displayName,
    String parentId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CreateCategoryPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "create_category"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateCategoryPacket> STREAM_CODEC = StreamCodec.of(
        CreateCategoryPacket::encode,
        CreateCategoryPacket::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buffer, CreateCategoryPacket packet) {
        buffer.writeUtf(packet.categoryId);
        buffer.writeUtf(packet.displayName);
        buffer.writeBoolean(packet.parentId != null && !packet.parentId.isEmpty());
        if (packet.parentId != null && !packet.parentId.isEmpty()) {
            buffer.writeUtf(packet.parentId);
        }
    }
    
    private static CreateCategoryPacket decode(RegistryFriendlyByteBuf buffer) {
        String categoryId = buffer.readUtf();
        String displayName = buffer.readUtf();
        boolean hasParent = buffer.readBoolean();
        String parentId = null;
        if (hasParent) {
            parentId = buffer.readUtf();
        }
        
        return new CreateCategoryPacket(categoryId, displayName, parentId);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
