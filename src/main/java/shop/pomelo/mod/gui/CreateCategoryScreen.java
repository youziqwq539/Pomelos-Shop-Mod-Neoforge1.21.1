package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.network.CreateCategoryPacket;

import java.awt.*;
import java.util.UUID;

public class CreateCategoryScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/create_category_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private TexturedEditBox nameBox;

    public CreateCategoryScreen(Screen parentScreen) {
        super(Component.translatable("shop.pomeloshopmod.create_category_title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameBox = new TexturedEditBox(this.font, centerX - 100, centerY - 30, 200, 20, 
            Component.translatable("shop.pomeloshopmod.category_name"));
        this.nameBox.setMaxLength(50);
        this.addRenderableWidget(this.nameBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, centerX - 124 - 132, centerY - 65 - 187, 0, 0, 512, 512, 512, 512);

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
            confirmBtnX + btnW / 2, confirmBtnY + 3, Color.WHITE.getRGB());
        guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.cancel"),
            cancelBtnX + btnW / 2, cancelBtnY + 3, Color.WHITE.getRGB());
    }

    private void createCategory() {
        String name = nameBox.getValue().trim();

        if (name.isEmpty()) {
            return;
        }

        String id = "cat_" + UUID.randomUUID().toString().substring(0, 8);

        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new CreateCategoryPacket(id, name)
        );

        this.onClose();
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
                this.createCategory();
                return true;
            }

            if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH) {
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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