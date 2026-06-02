package shop.pomelo.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.PomelosShopMod;

public record CreateCategoryPacket(
    String categoryId,
    String displayName
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CreateCategoryPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "create_category"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateCategoryPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        CreateCategoryPacket::categoryId,
        ByteBufCodecs.STRING_UTF8,
        CreateCategoryPacket::displayName,
        CreateCategoryPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
