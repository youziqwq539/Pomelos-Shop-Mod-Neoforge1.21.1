package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedEditBox extends EditBox {
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/input_box.png");

    private ResourceLocation texture;
    private int textYOffset = 0;

    public TexturedEditBox(Font font, int x, int y, int width, int height, Component message) {
        this(font, x, y, width, height, message, DEFAULT_TEXTURE);
    }

    public TexturedEditBox(Font font, int x, int y, int width, int height, Component message, ResourceLocation texture) {
        super(font, x, y, width, height, message);
        this.texture = texture;
        this.textYOffset = (height - 8) / 2;
        setBordered(false);
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public void setTextYOffset(int offset) {
        this.textYOffset = offset;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        Font font = Minecraft.getInstance().font;
        String text = this.getValue();
        int textX = this.getX() + 4;
        int textY = this.getY() + this.textYOffset;

        String displayText = text;
        int maxTextWidth = this.width - 8;
        if (font.width(text) > maxTextWidth) {
            displayText = font.plainSubstrByWidth(text, maxTextWidth);
        }

        guiGraphics.drawString(font, displayText, textX, textY, 0x000000, false);

        if (this.isFocused() && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = textX + font.width(displayText.substring(0, Math.min(this.getCursorPosition(), displayText.length())));
            guiGraphics.fill(cursorX, textY, cursorX + 1, textY + 8, 0xFFE0E0E0);
        }
    }
}