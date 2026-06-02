package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.function.Consumer;

public class SelectInventoryItemScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/inventory_select_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private final Consumer<ItemStack> onSelect;
    private int hoveredSlot = -1;
    
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int SLOTS_PER_ROW = 9;
    private static final int INVENTORY_ROWS = 4;

    public SelectInventoryItemScreen(Screen parentScreen, Consumer<ItemStack> onSelect) {
        super(Component.translatable("shop.pomeloshopmod.select_inventory_item"));
        this.parentScreen = parentScreen;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND_TEXTURE, centerX - 105, centerY - 48, 0, 0, 210, 96, 210, 96);

        guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 58, Color.WHITE.getRGB());

        Inventory inventory = Minecraft.getInstance().player.getInventory();
        
        int startX = centerX - 91;
        int startY = centerY - 38;
        
        hoveredSlot = -1;
        
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotIndex = row * SLOTS_PER_ROW + col;
                int x = startX + col * (SLOT_SIZE + SLOT_GAP);
                int y = startY + row * (SLOT_SIZE + SLOT_GAP);
                
                boolean isHovered = mouseX >= x && mouseX <= x + SLOT_SIZE && 
                                   mouseY >= y && mouseY <= y + SLOT_SIZE;
                
                if (isHovered) {
                    hoveredSlot = slotIndex;
                    guiGraphics.blit(BUTTONS_TEXTURE, x, y, 32, 75, SLOT_SIZE, SLOT_SIZE);
                }
                
                ItemStack itemStack = inventory.items.get(slotIndex);
                if (!itemStack.isEmpty()) {
                    guiGraphics.renderItem(itemStack, x + 1, y + 1);
                    guiGraphics.renderItemDecorations(this.font, itemStack, x + 1, y + 1);
                }
            }
        }

        if (hoveredSlot >= 0) {
            ItemStack hoveredStack = inventory.items.get(hoveredSlot);
            if (!hoveredStack.isEmpty()) {
                guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
            }
        }

        drawCancelButton(guiGraphics, centerX, centerY, mouseX, mouseY);
    }

    private void drawCancelButton(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int btnX = centerX - 30;
        int btnY = centerY + 49;
        int btnW = 60;
        int btnH = 16;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && 
                         mouseY >= btnY && mouseY <= btnY + btnH;
        
        guiGraphics.blit(BUTTONS_TEXTURE, btnX, btnY, hovered ? 196 : 134, 150, btnW, btnH);
        
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.cancel"), btnX + btnW / 2, btnY + 3, 
            hovered ? Color.WHITE.getRGB() : Color.LIGHT_GRAY.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            if (hoveredSlot >= 0) {
                Inventory inventory = Minecraft.getInstance().player.getInventory();
                ItemStack selectedStack = inventory.items.get(hoveredSlot);
                if (!selectedStack.isEmpty()) {
                    onSelect.accept(selectedStack.copy());
                    this.onClose();
                    return true;
                }
            }

            int btnX = centerX - 30;
            int btnY = centerY + 49;
            int btnW = 60;
            int btnH = 16;

            if (mouseX >= btnX && mouseX <= btnX + btnW && 
                mouseY >= btnY && mouseY <= btnY + btnH) {
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
