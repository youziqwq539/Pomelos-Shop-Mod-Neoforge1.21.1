package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.PomelosShopMod;
import shop.pomelo.mod.shop.ShopCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncCategoriesPacket(
    List<ShopCategory> categories
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCategoriesPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "sync_categories"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCategoriesPacket> STREAM_CODEC = StreamCodec.of(
        SyncCategoriesPacket::encode,
        SyncCategoriesPacket::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buffer, SyncCategoriesPacket packet) {
        buffer.writeVarInt(packet.categories.size());
        for (ShopCategory category : packet.categories) {
            buffer.writeUtf(category.getId());
            buffer.writeUtf(category.getDisplayName());
            buffer.writeUtf(category.getCreatorUUID().toString());
            buffer.writeUtf(category.getCreatorName());
            buffer.writeVarInt(category.getOrderIndex());
            String parentId = category.getParentId();
            buffer.writeBoolean(parentId != null && !parentId.isEmpty());
            if (parentId != null && !parentId.isEmpty()) {
                buffer.writeUtf(parentId);
            }
        }
    }
    
    private static SyncCategoriesPacket decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ShopCategory> categories = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            String id = buffer.readUtf();
            String displayName = buffer.readUtf();
            UUID creatorUUID = UUID.fromString(buffer.readUtf());
            String creatorName = buffer.readUtf();
            int orderIndex = buffer.readVarInt();
            String parentId = null;
            if (buffer.readBoolean()) {
                parentId = buffer.readUtf();
            }
            
            ShopCategory category = new ShopCategory(id, displayName, creatorUUID, creatorName, orderIndex, parentId);
            categories.add(category);
        }
        
        return new SyncCategoriesPacket(categories);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
