package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

/**
 * 可拖拽的钱包显示组件，在物品栏界面显示玩家余额
 */
public class DraggableWalletWidget {
    private static final ResourceLocation BALANCE_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/balance.png");
    
    // 钱包尺寸
    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;
    
    // 相对位置（百分比，0-1）
    private static float relativeX = -1;
    private static float relativeY = -1;
    
    // 是否已初始化
    private static boolean initialized = false;
    
    // 拖拽状态
    private static boolean isDragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;
    
    // 玩家余额
    private static int playerMoney = 0;
    
    /**
     * 更新玩家余额（从网络包接收）
     */
    public static void setPlayerMoney(int money) {
        playerMoney = money;
    }
    
    /**
     * 获取玩家余额
     */
    public static int getPlayerMoney() {
        return playerMoney;
    }
    
    /**
     * 根据相对位置计算绝对位置
     */
    private static int calculateAbsoluteX() {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        return Math.max(0, Math.min(screenWidth - WIDTH, (int)(relativeX * screenWidth)));
    }
    
    private static int calculateAbsoluteY() {
        Minecraft minecraft = Minecraft.getInstance();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        return Math.max(0, Math.min(screenHeight - HEIGHT, (int)(relativeY * screenHeight)));
    }
    
    /**
     * 在物品栏界面渲染钱包
     */
    public static void render(InventoryScreen screen, GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 初始化默认位置（右下角）
        if (!initialized) {
            relativeX = (screenWidth - WIDTH - 10) / (float) screenWidth;
            relativeY = (screenHeight - HEIGHT - 10) / (float) screenHeight;
            initialized = true;
        }
        
        // 计算当前绝对位置
        int walletX = calculateAbsoluteX();
        int walletY = calculateAbsoluteY();
        
        // 渲染钱包背景
        RenderSystem.enableBlend();
        guiGraphics.blit(BALANCE_TEXTURE, walletX, walletY, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        RenderSystem.disableBlend();
        
        // 绘制余额文字（白色字体）
        String balanceText = "$" + playerMoney;
        int textX = walletX + WIDTH / 2;
        int textY = walletY + HEIGHT / 2 - 4; // 居中偏上一点
        
        // 白色字体
        guiGraphics.drawString(minecraft.font, balanceText, textX, textY, Color.WHITE.getRGB(), false);
    }
    
    /**
     * 处理鼠标按下事件（开始拖拽）
     */
    public static boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (button == 0) { // 左键
            int walletX = calculateAbsoluteX();
            int walletY = calculateAbsoluteY();
            
            if (mouseX >= walletX && mouseX <= walletX + WIDTH &&
                mouseY >= walletY && mouseY <= walletY + HEIGHT) {
                isDragging = true;
                dragOffsetX = (int) mouseX - walletX;
                dragOffsetY = (int) mouseY - walletY;
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理鼠标释放事件（结束拖拽）
     */
    public static boolean onMouseUp(double mouseX, double mouseY, int button) {
        if (button == 0 && isDragging) {
            isDragging = false;
            return true;
        }
        return false;
    }
    
    /**
     * 处理鼠标拖拽事件
     */
    public static boolean onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            Minecraft minecraft = Minecraft.getInstance();
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            
            // 计算新的绝对位置
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;
            
            // 限制在屏幕范围内
            newX = Math.max(0, Math.min(screenWidth - WIDTH, newX));
            newY = Math.max(0, Math.min(screenHeight - HEIGHT, newY));
            
            // 更新相对位置
            relativeX = newX / (float) screenWidth;
            relativeY = newY / (float) screenHeight;
            
            return true;
        }
        return false;
    }
    
    /**
     * 获取钱包X位置
     */
    public static int getWalletX() {
        return calculateAbsoluteX();
    }
    
    /**
     * 获取钱包Y位置
     */
    public static int getWalletY() {
        return calculateAbsoluteY();
    }
    
    /**
     * 设置钱包位置（绝对坐标）
     */
    public static void setWalletPosition(int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        relativeX = x / (float) screenWidth;
        relativeY = y / (float) screenHeight;
        initialized = true;
    }
}