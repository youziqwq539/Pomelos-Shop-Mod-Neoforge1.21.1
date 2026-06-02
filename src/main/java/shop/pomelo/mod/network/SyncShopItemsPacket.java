package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.PomelosShopMod;
import shop.pomelo.mod.shop.ShopItem;

import java.util.ArrayList;
import java.util.List;

public record SyncShopItemsPacket(
    List<ShopItem> items
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncShopItemsPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "sync_shop_items"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncShopItemsPacket> STREAM_CODEC = StreamCodec.of(
        SyncShopItemsPacket::encode,
        SyncShopItemsPacket::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buffer, SyncShopItemsPacket packet) {
        buffer.writeVarInt(packet.items.size());
        for (ShopItem item : packet.items) {
            buffer.writeUtf(item.getId());
            buffer.writeUtf(item.getCategory());
            buffer.writeVarInt(item.getBuyPrice());
            buffer.writeVarInt(item.getSellPrice());
            buffer.writeBoolean(item.canBuy());
            buffer.writeBoolean(item.canSell());
            buffer.writeVarInt(item.getAmount());
            buffer.writeUtf(item.getSellerName() != null ? item.getSellerName() : "");
            buffer.writeBoolean(item.getSellerUUID() != null);
            if (item.getSellerUUID() != null) {
                buffer.writeUtf(item.getSellerUUID().toString());
            }
            buffer.writeVarLong(item.getOrderIndex());
            
            ItemStack.STREAM_CODEC.encode(buffer, item.getDisplayStack());
        }
    }
    
    private static SyncShopItemsPacket decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ShopItem> items = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            String id = buffer.readUtf();
            String category = buffer.readUtf();
            int buyPrice = buffer.readVarInt();
            int sellPrice = buffer.readVarInt();
            boolean canBuy = buffer.readBoolean();
            boolean canSell = buffer.readBoolean();
            int amount = buffer.readVarInt();
            String sellerName = buffer.readUtf();
            if (sellerName.isEmpty()) {
                sellerName = null;
            }
            
            boolean hasSellerUUID = buffer.readBoolean();
            java.util.UUID sellerUUID = null;
            if (hasSellerUUID) {
                sellerUUID = java.util.UUID.fromString(buffer.readUtf());
            }
            long orderIndex = buffer.readVarLong();
            
            ItemStack displayStack = ItemStack.STREAM_CODEC.decode(buffer);
            
            ShopItem item = new ShopItem(
                id,
                displayStack,
                buyPrice,
                sellPrice,
                category,
                canBuy,
                canSell,
                sellerUUID,
                sellerName,
                amount,
                orderIndex
            );
            items.add(item);
        }
        
        return new SyncShopItemsPacket(items);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
