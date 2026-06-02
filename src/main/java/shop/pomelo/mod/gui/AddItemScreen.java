package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.network.ListItemPacket;
import shop.pomelo.mod.shop.CategoryManager;
import shop.pomelo.mod.shop.ShopCategory;

import java.awt.*;
import java.util.List;

public class AddItemScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/add_item_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private ItemStack itemStack;
    private TexturedEditBox amountBox;
    private TexturedEditBox priceBox;
    private TexturedEditBox sellPriceBox;
    private String selectedCategory = "";
    private int categoryScrollOffset = 0;
    private static final int CATEGORIES_PER_PAGE = 3;

    public AddItemScreen(Screen parentScreen, ItemStack itemStack) {
        super(Component.translatable("shop.pomeloshopmod.add_item_title"));
        this.parentScreen = parentScreen;
        this.itemStack = itemStack.copy();
    }

    public void updateItemStack(ItemStack newStack) {
        this.itemStack = newStack.copy();
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.amountBox = new TexturedEditBox(this.font, centerX - 40, centerY - 50, 80, 18, 
            Component.translatable("shop.pomeloshopmod.amount"));
        this.amountBox.setValue("1");
        this.amountBox.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.amountBox);

        this.priceBox = new TexturedEditBox(this.font, centerX - 40, centerY - 25, 80, 18, 
            Component.translatable("shop.pomeloshopmod.price"));
        this.priceBox.setValue("10");
        this.priceBox.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.priceBox);

        this.sellPriceBox = new TexturedEditBox(this.font, centerX - 40, centerY, 80, 18, 
            Component.translatable("shop.pomeloshopmod.sell_price_input"));
        this.sellPriceBox.setValue("");
        this.sellPriceBox.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.sellPriceBox);
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND_TEXTURE, centerX - 120 - 136, centerY - 100 - 156, 0, 0, 512, 512, 512, 512);

        drawCenteredStringNoShadow(guiGraphics, this.title, centerX, centerY - 90, Color.BLACK.getRGB());

        guiGraphics.renderItem(itemStack, centerX - 35, centerY - 70);
        drawCenteredStringNoShadow(guiGraphics, itemStack.getHoverName(), centerX + 10, centerY - 65, Color.BLACK.getRGB());

        drawSelectInventoryButton(guiGraphics, centerX, centerY, mouseX, mouseY);

        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.amount_label"), 
            centerX - 80, centerY - 55, Color.BLACK.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.price_label"), 
            centerX - 80, centerY - 30, Color.BLACK.getRGB(), false);
        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.sell_price_label"), 
            centerX - 80, centerY - 5, Color.BLACK.getRGB(), false);

        drawCategorySelection(guiGraphics, centerX, centerY, mouseX, mouseY);

        this.amountBox.render(guiGraphics, mouseX, mouseY, partialTick);
        this.priceBox.render(guiGraphics, mouseX, mouseY, partialTick);
        this.sellPriceBox.render(guiGraphics, mouseX, mouseY, partialTick);
        
        drawButtons(guiGraphics, centerX, centerY, mouseX, mouseY);
    }

    private void drawSelectInventoryButton(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int btnX = centerX + 50;
        int btnY = centerY - 67;
        int btnW = 60;
        int btnH = 14;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && 
                         mouseY >= btnY && mouseY <= btnY + btnH;
        
        guiGraphics.blit(BUTTONS_TEXTURE, btnX, btnY, hovered ? 186 : 124, 196, btnW, btnH);
        
        drawCenteredStringNoShadow(guiGraphics, Component.translatable("shop.pomeloshopmod.select_inventory"), 
            btnX + btnW / 2, btnY + 2, Color.BLACK.getRGB());
    }

    private void drawButtons(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int confirmBtnX = centerX - 65;
        int confirmBtnY = centerY + 80;
        int cancelBtnX = centerX + 5;
        int cancelBtnY = centerY + 80;
        int btnW = 60;
        int btnH = 16;

        boolean confirmHovered = mouseX >= confirmBtnX && mouseX <= confirmBtnX + btnW && 
                                 mouseY >= confirmBtnY && mouseY <= confirmBtnY + btnH;
        boolean cancelHovered = mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH;

        guiGraphics.blit(BUTTONS_TEXTURE, confirmBtnX, confirmBtnY, confirmHovered ? 196 : 134, 150, btnW, btnH);
        guiGraphics.blit(BUTTONS_TEXTURE, cancelBtnX, cancelBtnY, cancelHovered ? 196 : 134, 150, btnW, btnH);

        drawCenteredStringNoShadow(guiGraphics, Component.translatable("shop.pomeloshopmod.confirm"), 
            confirmBtnX + btnW / 2, confirmBtnY + 3, Color.BLACK.getRGB());
        drawCenteredStringNoShadow(guiGraphics, Component.translatable("shop.pomeloshopmod.cancel"), 
            cancelBtnX + btnW / 2, cancelBtnY + 3, Color.BLACK.getRGB());
    }

    private void drawCategorySelection(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        List<ShopCategory> categories = CategoryManager.getInstance().getCategories();
        
        if (categories.isEmpty()) {
            drawCenteredStringNoShadow(guiGraphics, 
                Component.translatable("shop.pomeloshopmod.no_categories"), 
                centerX, centerY + 20, Color.BLACK.getRGB());
            return;
        }

        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.select_category"), 
            centerX - 80, centerY + 18, Color.BLACK.getRGB(), false);

        int y = centerY + 32;
        for (int i = categoryScrollOffset; i < Math.min(categories.size(), categoryScrollOffset + CATEGORIES_PER_PAGE); i++) {
            ShopCategory category = categories.get(i);
            boolean isSelected = category.getId().equals(selectedCategory);
            boolean isHovered = mouseX >= centerX - 60 && mouseX <= centerX + 60 && 
                               mouseY >= y && mouseY <= y + 14;

            if (isSelected || isHovered) {
                guiGraphics.blit(BUTTONS_TEXTURE, centerX - 60, y, 0, 196, 120, 14);
            }
            
            drawCenteredStringNoShadow(guiGraphics, category.getDisplayName(), centerX, y + 3, Color.BLACK.getRGB());
            
            y += 16;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            int selectBtnX = centerX + 50;
            int selectBtnY = centerY - 72;
            int selectBtnW = 60;
            int selectBtnH = 14;

            if (mouseX >= selectBtnX && mouseX <= selectBtnX + selectBtnW && 
                mouseY >= selectBtnY && mouseY <= selectBtnY + selectBtnH) {
                this.openInventorySelect();
                return true;
            }

            int confirmBtnX = centerX - 65;
            int confirmBtnY = centerY + 80;
            int cancelBtnX = centerX + 5;
            int cancelBtnY = centerY + 80;
            int btnW = 60;
            int btnH = 16;

            if (mouseX >= confirmBtnX && mouseX <= confirmBtnX + btnW && 
                mouseY >= confirmBtnY && mouseY <= confirmBtnY + btnH) {
                this.confirmListing();
                return true;
            }

            if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH) {
                this.onClose();
                return true;
            }

            List<ShopCategory> categories = CategoryManager.getInstance().getCategories();
            int y = centerY + 32;
            
            for (int i = categoryScrollOffset; i < Math.min(categories.size(), categoryScrollOffset + CATEGORIES_PER_PAGE); i++) {
                if (mouseX >= centerX - 60 && mouseX <= centerX + 60 && 
                    mouseY >= y && mouseY <= y + 14) {
                    selectedCategory = categories.get(i).getId();
                    return true;
                }
                y += 16;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openInventorySelect() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new SelectInventoryItemScreen(this, stack -> {
                this.itemStack = stack.copy();
            }));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<ShopCategory> categories = CategoryManager.getInstance().getCategories();
        int maxScroll = Math.max(0, categories.size() - CATEGORIES_PER_PAGE);
        int scrollAmount = (int) Math.signum(scrollY);
        categoryScrollOffset = Math.max(0, Math.min(maxScroll, categoryScrollOffset - scrollAmount));
        return true;
    }

    private void confirmListing() {
        try {
            int amount = Integer.parseInt(amountBox.getValue());
            int price = Integer.parseInt(priceBox.getValue());
            
            if (amount <= 0 || price <= 0) {
                return;
            }

            if (selectedCategory.isEmpty()) {
                return;
            }

            int sellPrice = 0;
            String sellPriceText = sellPriceBox.getValue().trim();
            if (!sellPriceText.isEmpty()) {
                sellPrice = Integer.parseInt(sellPriceText);
            }

            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new ListItemPacket(itemStack, price, sellPrice, amount, selectedCategory)
            );

            this.onClose();
        } catch (NumberFormatException e) {
        }
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