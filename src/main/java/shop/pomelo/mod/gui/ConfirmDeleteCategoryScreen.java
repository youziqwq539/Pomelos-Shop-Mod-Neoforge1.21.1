package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.network.DeleteCategoryPacket;
import shop.pomelo.mod.shop.ShopCategory;
import shop.pomelo.mod.sound.ModSounds;

import java.awt.*;

public class ConfirmDeleteCategoryScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/create_category_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private final ShopCategory categoryToDelete;

    public ConfirmDeleteCategoryScreen(Screen parentScreen, ShopCategory categoryToDelete) {
        super(Component.translatable("shop.pomeloshopmod.confirm_delete_category_title"));
        this.parentScreen = parentScreen;
        this.categoryToDelete = categoryToDelete;
    }
    
    /**
     * 播放按钮点击音效
     */
    private void playButtonClickSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(ModSounds.SHOP_CLICK.get(), 0.3F, 1.0F);
        }
    }

    @Override
    protected void init() {
        super.init();
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND_TEXTURE, centerX - 124 - 132, centerY - 65 - 187, 0, 0, 512, 512, 512, 512);

        // 标题
        drawCenteredStringNoShadow(guiGraphics, this.title, centerX, centerY - 55, Color.BLACK.getRGB());

        // 提示信息
        Component confirmText = Component.translatable("shop.pomeloshopmod.confirm_delete_category_message", categoryToDelete.getDisplayName());
        drawCenteredStringNoShadow(guiGraphics, confirmText, centerX, centerY - 20, Color.BLACK.getRGB());

        // 绘制确认和取消按钮
        drawButtons(guiGraphics, centerX, centerY, mouseX, mouseY);
    }

    private void drawButtons(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int confirmBtnX = centerX - 65;
        int confirmBtnY = centerY + 20;
        int cancelBtnX = centerX + 5;
        int cancelBtnY = centerY + 20;
        int btnW = 60;
        int btnH = 16;

        boolean confirmHovered = mouseX >= confirmBtnX && mouseX <= confirmBtnX + btnW &&
                                 mouseY >= confirmBtnY && mouseY <= confirmBtnY + btnH;
        boolean cancelHovered = mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW &&
                                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH;

        guiGraphics.blit(BUTTONS_TEXTURE, confirmBtnX, confirmBtnY, confirmHovered ? 196 : 134, 150, btnW, btnH);
        guiGraphics.blit(BUTTONS_TEXTURE, cancelBtnX, cancelBtnY, cancelHovered ? 196 : 134, 150, btnW, btnH);

        guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.confirm"),
            confirmBtnX + btnW / 2, confirmBtnY + 3, Color.WHITE.getRGB());
        guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.cancel"),
            cancelBtnX + btnW / 2, cancelBtnY + 3, Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            int confirmBtnX = centerX - 65;
            int confirmBtnY = centerY + 20;
            int cancelBtnX = centerX + 5;
            int cancelBtnY = centerY + 20;
            int btnW = 60;
            int btnH = 16;

            // 点击确认按钮
            if (mouseX >= confirmBtnX && mouseX <= confirmBtnX + btnW && 
                mouseY >= confirmBtnY && mouseY <= confirmBtnY + btnH) {
                playButtonClickSound();
                deleteCategory();
                return true;
            }

            // 点击取消按钮
            if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH) {
                playButtonClickSound();
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void deleteCategory() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new DeleteCategoryPacket(categoryToDelete.getId())
        );
        this.onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}