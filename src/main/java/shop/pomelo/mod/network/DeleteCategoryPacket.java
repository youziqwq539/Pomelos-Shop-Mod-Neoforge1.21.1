package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.PomelosShopMod;

public record DeleteCategoryPacket(
    String categoryId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeleteCategoryPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "delete_category"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteCategoryPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        DeleteCategoryPacket::categoryId,
        DeleteCategoryPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
