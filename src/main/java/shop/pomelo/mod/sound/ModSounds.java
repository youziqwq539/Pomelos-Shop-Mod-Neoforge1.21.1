package shop.pomelo.mod.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import shop.pomelo.mod.PomelosShopMod;

/**
 * 模组音效注册类
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = 
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PomelosShopMod.MODID);
    
    // 商店按钮点击音效
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOP_CLICK = 
        SOUND_EVENTS.register("shop_click", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(PomelosShopMod.MODID, "shop_click")));
    
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}