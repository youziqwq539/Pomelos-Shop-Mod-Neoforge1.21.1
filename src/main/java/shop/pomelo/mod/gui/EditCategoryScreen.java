package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.network.UpdateCategoryPacket;
import shop.pomelo.mod.shop.ShopCategory;
import shop.pomelo.mod.sound.ModSounds;

import java.awt.*;

public class EditCategoryScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/create_category_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private final ShopCategory category;
    private TexturedEditBox nameBox;

    public EditCategoryScreen(Screen parentScreen, ShopCategory category) {
        super(Component.translatable("shop.pomeloshopmod.edit_category_title"));
        this.parentScreen = parentScreen;
        this.category = category;
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
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.nameBox = new TexturedEditBox(this.font, centerX - 100, centerY - 30, 200, 20, 
            Component.translatable("shop.pomeloshopmod.category_name"));
        this.nameBox.setMaxLength(50);
        this.nameBox.setValue(category.getDisplayName());
        this.addRenderableWidget(this.nameBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND_TEXTURE, centerX - 124 - 132, centerY - 65 - 187, 0, 0, 512, 512, 512, 512);

        guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 55, Color.WHITE.getRGB());

        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.category_name_label"), 
            centerX - 100, centerY - 45, Color.LIGHT_GRAY.getRGB());

        this.nameBox.render(guiGraphics, mouseX, mouseY, partialTick);
        
        drawButtons(guiGraphics, centerX, centerY, mouseX, mouseY);
    }

    private void drawButtons(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int confirmBtnX = centerX - 65;
        int confirmBtnY = centerY + 30;
        int cancelBtnX = centerX + 5;
        int cancelBtnY = centerY + 30;
        int btnW = 60;
        int btnH = 16;

        boolean confirmHovered = mouseX >= confirmBtnX && mouseX <= confirmBtnX + btnW && 
                                 mouseY >= confirmBtnY && mouseY <= confirmBtnY + btnH;
        boolean cancelHovered = mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH;

        guiGraphics.blit(BUTTONS_TEXTURE, confirmBtnX, confirmBtnY, confirmHovered ? 196 : 134, 150, btnW, btnH);
        guiGraphics.blit(BUTTONS_TEXTURE, cancelBtnX, cancelBtnY, cancelHovered ? 196 : 134, 150, btnW, btnH);

        guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.confirm"), 
            confirmBtnX + btnW / 2, confirmBtnY + 3, Color.BLACK.getRGB());
        guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.cancel"), 
            cancelBtnX + btnW / 2, cancelBtnY + 3, Color.BLACK.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            int confirmBtnX = centerX - 65;
            int confirmBtnY = centerY + 30;
            int cancelBtnX = centerX + 5;
            int cancelBtnY = centerY + 30;
            int btnW = 60;
            int btnH = 16;

            if (mouseX >= confirmBtnX && mouseX <= confirmBtnX + btnW && 
                mouseY >= confirmBtnY && mouseY <= confirmBtnY + btnH) {
                playButtonClickSound();
                this.confirmUpdate();
                return true;
            }

            if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH) {
                playButtonClickSound();
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void confirmUpdate() {
        String newName = nameBox.getValue().trim();
        if (newName.isEmpty()) {
            return;
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new UpdateCategoryPacket(category.getId(), newName)
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
