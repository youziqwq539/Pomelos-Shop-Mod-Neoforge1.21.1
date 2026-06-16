package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.PomelosShopMod;

public record UpdateItemPacket(
    String itemId,
    int buyPrice,
    int sellPrice,
    int amount,
    String category,
    int stock,
    ItemStack newItemStack
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateItemPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "update_item"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateItemPacket> STREAM_CODEC = StreamCodec.of(
        UpdateItemPacket::encode,
        UpdateItemPacket::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buffer, UpdateItemPacket packet) {
        buffer.writeUtf(packet.itemId);
        buffer.writeVarInt(packet.buyPrice);
        buffer.writeVarInt(packet.sellPrice);
        buffer.writeVarInt(packet.amount);
        buffer.writeUtf(packet.category);
        buffer.writeVarInt(packet.stock);
        ItemStack.STREAM_CODEC.encode(buffer, packet.newItemStack);
    }
    
    private static UpdateItemPacket decode(RegistryFriendlyByteBuf buffer) {
        String itemId = buffer.readUtf();
        int buyPrice = buffer.readVarInt();
        int sellPrice = buffer.readVarInt();
        int amount = buffer.readVarInt();
        String category = buffer.readUtf();
        int stock = buffer.readVarInt();
        ItemStack newItemStack = ItemStack.STREAM_CODEC.decode(buffer);
        
        return new UpdateItemPacket(itemId, buyPrice, sellPrice, amount, category, stock, newItemStack);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
