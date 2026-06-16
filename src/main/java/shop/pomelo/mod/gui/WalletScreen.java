package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {
    // 独立的贴图引用 - 你可以在这里更换你自己的贴图
    private static final ResourceLocation WALLET_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
        "pomeloshopmod", "textures/gui/wallet_background.png");

    // 槽位悬停/高亮独立贴图引用
    private static final ResourceLocation WALLET_SLOT_HIGHLIGHT = ResourceLocation.fromNamespaceAndPath(
        "pomeloshopmod", "textures/gui/wallet_slot_highlight.png");

    private static final int COLOR_WHITE = 0xFFFFFF;

    public WalletScreen(WalletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        // 背景尺寸：296x166
        this.imageWidth = 296;
        this.imageHeight = 166;
        // 标题位置：左上角
        this.titleLabelX = 12;
        this.titleLabelY = 12;
        // 物品栏标签：在右侧物品栏上方
        this.inventoryLabelX = 116;
        this.inventoryLabelY = 47;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 绘制背景贴图
        guiGraphics.blit(WALLET_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 绘制标题 - 左上角
        String title = this.title.getString();
        guiGraphics.drawString(this.font, title, this.titleLabelX, this.titleLabelY, COLOR_WHITE, true);

        // 绘制"物品栏"标签
        String inventoryLabel = this.playerInventoryTitle.getString();
        guiGraphics.drawString(this.font, inventoryLabel, this.inventoryLabelX, this.inventoryLabelY, COLOR_WHITE, true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // 使用独立贴图绘制悬停高亮
        this.renderCustomSlotHighlight(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 使用独立的悬停高亮贴图绘制槽位高亮效果
     */
    private void renderCustomSlotHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.isActive()) {
            RenderSystem.enableBlend();
            guiGraphics.blit(WALLET_SLOT_HIGHLIGHT,
                this.leftPos + this.hoveredSlot.x - 1,
                this.topPos + this.hoveredSlot.y - 1,
                0, 0, 18, 18, 18, 18);
        }
    }
}