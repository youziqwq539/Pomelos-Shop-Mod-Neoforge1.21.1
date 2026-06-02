package shop.pomelo.mod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;
import shop.pomelo.mod.network.OpenShopPacket;

@EventBusSubscriber(modid = PomelosShopMod.MODID, value = Dist.CLIENT)
public class KeyBindings {
    public static final String CATEGORY = "key.categories.pomeloshopmod";
    public static final KeyMapping OPEN_SHOP = new KeyMapping(
        "key.pomeloshopmod.open_shop",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        CATEGORY
    );

    private KeyBindings() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_SHOP.consumeClick()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(OpenShopPacket.INSTANCE);
        }
    }
}
