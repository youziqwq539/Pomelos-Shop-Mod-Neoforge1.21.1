package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.PomelosShopMod;

public record ListItemPacket(
    ItemStack itemStack,
    int price,
    int sellPrice,
    int amount,
    String categoryId,
    int stock
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ListItemPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "list_item"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ListItemPacket> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC,
        ListItemPacket::itemStack,
        ByteBufCodecs.VAR_INT,
        ListItemPacket::price,
        ByteBufCodecs.VAR_INT,
        ListItemPacket::sellPrice,
        ByteBufCodecs.VAR_INT,
        ListItemPacket::amount,
        ByteBufCodecs.STRING_UTF8,
        ListItemPacket::categoryId,
        ByteBufCodecs.VAR_INT,
        ListItemPacket::stock,
        ListItemPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
