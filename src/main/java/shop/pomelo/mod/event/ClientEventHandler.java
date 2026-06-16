package shop.pomelo.mod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import shop.pomelo.mod.PomelosShopMod;
import shop.pomelo.mod.gui.DraggableWalletWidget;

/**
 * 客户端事件处理器，处理物品栏界面相关的钱包显示
 */
@EventBusSubscriber(modid = PomelosShopMod.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    
    private static boolean wasInventoryOpen = false;
    
    /**
     * 每帧检查物品栏是否打开，打开时请求同步余额
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        boolean isInventoryOpen = mc.screen instanceof InventoryScreen;
        
        // 当物品栏刚打开时，请求服务器同步余额
        if (isInventoryOpen && !wasInventoryOpen) {
            requestMoneySync();
        }
        
        wasInventoryOpen = isInventoryOpen;
    }
    
    /**
     * 在物品栏界面渲染后绘制钱包
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            DraggableWalletWidget.render(inventoryScreen, event.getGuiGraphics());
        }
    }
    
    /**
     * 处理物品栏界面的鼠标按下事件
     */
    @SubscribeEvent
    public static void onMouseDown(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof InventoryScreen) {
            if (DraggableWalletWidget.onMouseDown(event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * 处理物品栏界面的鼠标释放事件
     */
    @SubscribeEvent
    public static void onMouseUp(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() instanceof InventoryScreen) {
            if (DraggableWalletWidget.onMouseUp(event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * 处理物品栏界面的鼠标拖拽事件
     */
    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() instanceof InventoryScreen) {
            if (DraggableWalletWidget.onMouseDrag(event.getMouseX(), event.getMouseY(), 
                event.getMouseButton(), event.getDragX(), event.getDragY())) {
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * 请求服务器同步余额到客户端
     */
    private static void requestMoneySync() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // 发送请求余额同步的网络包
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new shop.pomelo.mod.network.RequestMoneyPacket()
            );
        }
    }
}