package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import shop.pomelo.mod.shop.CategoryManager;
import shop.pomelo.mod.shop.ShopCategory;
import shop.pomelo.mod.shop.ShopItem;
import shop.pomelo.mod.shop.ShopManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends AbstractContainerScreen<ShopMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/shop_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    private static final ResourceLocation SCROLL_UP_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/scroll_up.png");
    private static final ResourceLocation SCROLL_DOWN_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/scroll_down.png");
    private static final ResourceLocation SCROLL_UP_HOVER_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/scroll_up_hover.png");
    private static final ResourceLocation SCROLL_DOWN_HOVER_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/scroll_down_hover.png");
    private static final ResourceLocation[] ROW_HOVER_TEXTURES = {
        ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/row1_hover.png"),
        ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/row2_hover.png"),
        ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/row3_hover.png"),
        ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/row4_hover.png")
    };
    
    private static final int ITEMS_PER_PAGE = 28;
    private static final int CATEGORIES_PER_PAGE = 4;
    
    private static final int COLOR_WHITE = Color.WHITE.getRGB();
    private static final int COLOR_BLACK = Color.BLACK.getRGB();
    private static final int COLOR_GRAY = new Color(180, 180, 180).getRGB();
    private static final int COLOR_DISABLED = new Color(100, 100, 110, 50).getRGB();
    private static final int COLOR_GREEN = new Color(100, 200, 100).getRGB();

    private int currentPage = 0;
    private String currentCategory = "";
    private List<ShopItem> displayedItems = new ArrayList<>();
    private int playerMoney = 0;
    private ShopItem hoveredItem = null;
    private int selectedAmount = 1;
    private boolean showAmountSelector = false;
    private ShopItem selectedItem = null;

    private int categoryScrollOffset = 0;

    public ShopScreen(ShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        updateDisplayedItems();
    }

    private void updateDisplayedItems() {
        displayedItems = ShopManager.getInstance().getItemsByCategory(currentCategory);
        currentPage = 0;
    }
    
    public void refreshItems() {
        updateDisplayedItems();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        RenderSystem.enableBlend();
        
        guiGraphics.blit(BACKGROUND_TEXTURE, startX - 96, startY - 146, 0, 0, 512, 512, 512, 512);

        drawCategories(guiGraphics, startX, startY, mouseX, mouseY);
        drawAddItemButton(guiGraphics, startX, startY, mouseX, mouseY);
        drawItems(guiGraphics, startX, startY, mouseX, mouseY);
        drawMoneyDisplay(guiGraphics, startX, startY);
        drawNavigationButtons(guiGraphics, startX, startY, mouseX, mouseY);

        if (showAmountSelector && selectedItem != null) {
            drawAmountSelector(guiGraphics, startX, startY, mouseX, mouseY);
        }
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    private void drawCategories(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        List<ShopCategory> categories = CategoryManager.getInstance().getCategories();

        boolean isAddButtonHovered = mouseX >= startX + 8 && mouseX <= startX + 28 && 
                                     mouseY >= startY + 8 && mouseY <= startY + 28;
        
        guiGraphics.blit(BUTTONS_TEXTURE, startX + 7, startY + 7, isAddButtonHovered ? 25 : 0, 0, 22, 22);
        guiGraphics.drawCenteredString(this.font, "+", startX + 18, startY + 12, 
            isAddButtonHovered ? COLOR_WHITE : COLOR_GRAY);
        
        boolean isDeleteButtonHovered = mouseX >= startX + 42 && mouseX <= startX + 62 && 
                                        mouseY >= startY + 8 && mouseY <= startY + 28;
        
        guiGraphics.blit(BUTTONS_TEXTURE, startX + 41, startY + 7, isDeleteButtonHovered ? 25 : 0, 0, 22, 22);
        guiGraphics.drawCenteredString(this.font, "-", startX + 52, startY + 12,
            isDeleteButtonHovered ? COLOR_WHITE : COLOR_GRAY);
        
        int y = startY + 35;
        for (int i = categoryScrollOffset; i < Math.min(categories.size(), categoryScrollOffset + CATEGORIES_PER_PAGE); i++) {
            ShopCategory category = categories.get(i);

            boolean isHovered = mouseX >= startX + 8 && mouseX <= startX + 68 && 
                               mouseY >= y && mouseY <= y + 25;
            boolean isSelected = category.getId().equals(currentCategory);

            if (isSelected || isHovered) {
                guiGraphics.blit(BUTTONS_TEXTURE, startX + 7, y - 1, 0, 24, 62, 27);
            }

            String text = category.getDisplayName();
            int textWidth = this.font.width(text);
            int textX = startX + 40 - textWidth / 2;
            int textColor = (isSelected || isHovered) ? COLOR_WHITE : COLOR_BLACK;
            guiGraphics.drawString(this.font, text, textX, y + 8, textColor, false);

            y += 30;
        }

        if (categories.size() > CATEGORIES_PER_PAGE) {
            drawCategoryScrollButtons(guiGraphics, startX, startY, mouseX, mouseY, categories);
        }
    }

    private void drawCategoryScrollButtons(GuiGraphics guiGraphics, int startX, int startY, 
                                           int mouseX, int mouseY, List<ShopCategory> categories) {
        boolean canScrollUp = categoryScrollOffset > 0;
        boolean canScrollDown = categoryScrollOffset < categories.size() - CATEGORIES_PER_PAGE;

        int btnX = startX + 8;
        int upBtnY = startY + 35 + CATEGORIES_PER_PAGE * 30 + 2;
        int downBtnY = upBtnY + 22;
        int btnW = 56;
        int btnH = 20;

        boolean upHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= upBtnY && mouseY <= upBtnY + btnH;
        boolean downHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= downBtnY && mouseY <= downBtnY + btnH;

        if (canScrollUp) {
            guiGraphics.blit(upHovered ? SCROLL_UP_HOVER_TEXTURE : SCROLL_UP_TEXTURE, btnX, upBtnY, 0, 0, btnW, btnH, btnW, btnH);
        }
        if (canScrollDown) {
            guiGraphics.blit(downHovered ? SCROLL_DOWN_HOVER_TEXTURE : SCROLL_DOWN_TEXTURE, btnX, downBtnY, 0, 0, btnW, btnH, btnW, btnH);
        }

        int upArrowColor = canScrollUp ? COLOR_WHITE : COLOR_DISABLED;
        int downArrowColor = canScrollDown ? COLOR_WHITE : COLOR_DISABLED;

        guiGraphics.drawCenteredString(this.font, "▲", btnX + btnW / 2, upBtnY + 5, upArrowColor);
        guiGraphics.drawCenteredString(this.font, "▼", btnX + btnW / 2, downBtnY + 5, downArrowColor);
    }

    private void drawAddItemButton(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        int btnX = startX + 323;
        int btnY = startY + 100;
        int btnW = 40;
        int btnH = 40;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        if (hovered) {
            guiGraphics.blit(BUTTONS_TEXTURE, btnX, btnY, 192, 0, btnW, btnH);
        } else {
            guiGraphics.blit(BUTTONS_TEXTURE, btnX, btnY, 150, 0, btnW, btnH);
        }
        guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.add_item"), btnX + btnW / 2, btnY + 12, COLOR_WHITE);
    }

    private void drawMoneyDisplay(GuiGraphics guiGraphics, int startX, int startY) {
        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.balance_label", playerMoney), startX + 10, startY + 202,
            COLOR_BLACK, false);
    }

    private void drawItems(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        int itemsX = startX + 90;
        int itemsY = startY + 10;
        int itemIndex = 0;

        hoveredItem = null;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 7; col++) {
                int idx = currentPage * ITEMS_PER_PAGE + itemIndex;
                if (idx >= displayedItems.size()) break;

                ShopItem item = displayedItems.get(idx);
                int x = itemsX + col * 32;
                int y = itemsY + row * 40;

                boolean isHovered = mouseX >= x && mouseX <= x + 28 && mouseY >= y && mouseY <= y + 28;

                if (isHovered) {
                    if (row == 2) {
                        guiGraphics.blit(ROW_HOVER_TEXTURES[row], x - 5, y - 5, 0, 0, 38, 38, 38, 38);
                    } else {
                        guiGraphics.blit(ROW_HOVER_TEXTURES[row], x - 1, y - 1, 0, 0, 30, 30, 30, 30);
                    }
                }

                guiGraphics.renderItem(item.getDisplayStack(), x + 6, y + 6);

                if (isHovered) {
                    hoveredItem = item;
                }

                itemIndex++;
            }
        }
    }

    private void drawNavigationButtons(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        int totalPages = (displayedItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        
        if (totalPages > 1) {
            boolean canPrev = currentPage > 0;
            boolean canNext = currentPage < totalPages - 1;

            if (canPrev) {
                boolean prevHovered = mouseX >= startX + 90 && mouseX <= startX + 140 &&
                                     mouseY >= startY + 200 && mouseY <= startY + 215;
                if (prevHovered) {
                    guiGraphics.blit(BUTTONS_TEXTURE, startX + 89, startY + 199, 0, 107, 52, 17);
                }
            }
            if (canNext) {
                boolean nextHovered = mouseX >= startX + 260 && mouseX <= startX + 310 &&
                                     mouseY >= startY + 200 && mouseY <= startY + 215;
                if (nextHovered) {
                    guiGraphics.blit(BUTTONS_TEXTURE, startX + 259, startY + 199, 0, 107, 52, 17);
                }
            }

            guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.prev_page"), startX + 115, startY + 202, COLOR_WHITE);
            guiGraphics.drawCenteredString(this.font, Component.translatable("shop.pomeloshopmod.next_page"), startX + 285, startY + 202, COLOR_WHITE);

            String pageText = String.format("%d / %d", currentPage + 1, totalPages);
            guiGraphics.drawCenteredString(this.font, pageText, startX + 195, startY + 202, COLOR_WHITE);
        }
    }

    private void drawAmountSelector(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        int sx = startX + 82;
        int sy = startY + 168;

        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.amount_label"), sx + 4, sy + 7, COLOR_BLACK, false);

        boolean hoverMinus10 = mouseX >= sx + 36 && mouseX <= sx + 58 && mouseY >= sy + 4 && mouseY <= sy + 18;
        boolean hoverMinus1 = mouseX >= sx + 62 && mouseX <= sx + 80 && mouseY >= sy + 4 && mouseY <= sy + 18;
        boolean hoverPlus1 = mouseX >= sx + 108 && mouseX <= sx + 126 && mouseY >= sy + 4 && mouseY <= sy + 18;
        boolean hoverPlus10 = mouseX >= sx + 130 && mouseX <= sx + 152 && mouseY >= sy + 4 && mouseY <= sy + 18;

        guiGraphics.blit(BUTTONS_TEXTURE, sx + 36, sy + 4, hoverMinus10 ? 24 : 0, 126, 22, 14);
        drawCenteredStringNoShadow(guiGraphics, "-10", sx + 47, sy + 6, COLOR_BLACK);

        guiGraphics.blit(BUTTONS_TEXTURE, sx + 62, sy + 4, hoverMinus1 ? 68 : 48, 126, 18, 14);
        drawCenteredStringNoShadow(guiGraphics, "-1", sx + 71, sy + 6, COLOR_BLACK);

        drawCenteredStringNoShadow(guiGraphics, String.valueOf(selectedAmount), sx + 95, sy + 7, COLOR_BLACK);

        guiGraphics.blit(BUTTONS_TEXTURE, sx + 108, sy + 4, hoverPlus1 ? 108 : 88, 126, 18, 14);
        drawCenteredStringNoShadow(guiGraphics, "+1", sx + 117, sy + 6, COLOR_BLACK);

        guiGraphics.blit(BUTTONS_TEXTURE, sx + 130, sy + 4, hoverPlus10 ? 152 : 128, 126, 22, 14);
        drawCenteredStringNoShadow(guiGraphics, "+10", sx + 141, sy + 6, COLOR_BLACK);

        if (selectedItem != null) {
            int total = selectedItem.getBuyPrice() * selectedAmount;
            guiGraphics.drawString(this.font, "= $" + total, sx + 158, sy + 7,
                COLOR_GREEN, false);
        }

        boolean hoverCancel = mouseX >= sx + 200 && mouseX <= sx + 232 && mouseY >= sy + 2 && mouseY <= sy + 16;
        boolean hoverConfirm = mouseX >= sx + 200 && mouseX <= sx + 232 && mouseY >= sy + 17 && mouseY <= sy + 31;

        guiGraphics.blit(BUTTONS_TEXTURE, sx + 200, sy + 2, hoverCancel ? 102 : 68, 150, 32, 14);
        drawCenteredStringNoShadow(guiGraphics, Component.translatable("shop.pomeloshopmod.cancel"), sx + 216, sy + 4, COLOR_BLACK);

        guiGraphics.blit(BUTTONS_TEXTURE, sx + 200, sy + 17, hoverConfirm ? 34 : 0, 150, 32, 14);
        drawCenteredStringNoShadow(guiGraphics, Component.translatable("shop.pomeloshopmod.confirm"), sx + 216, sy + 19, COLOR_BLACK);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        if (hoveredItem != null && !showAmountSelector) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(hoveredItem.getHoverName());
            
            if (hoveredItem.canBuy()) {
                tooltip.add(Component.translatable("shop.pomeloshopmod.buy_price", hoveredItem.getBuyPrice()));
            }
            if (hoveredItem.canSell()) {
                tooltip.add(Component.translatable("shop.pomeloshopmod.sell_price", hoveredItem.getSellPrice()));
            }
            
            tooltip.add(Component.translatable("shop.pomeloshopmod.left_click_buy"));
            tooltip.add(Component.translatable("shop.pomeloshopmod.right_click_sell"));
            tooltip.add(Component.translatable("shop.pomeloshopmod.shift_right_click_batch_sell"));
            
            if (hoveredItem.isPlayerListing()) {
                tooltip.add(Component.translatable("shop.pomeloshopmod.ctrl_right_click_delete"));
            }

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if (showAmountSelector) {
            return handleAmountSelectorClick((int) mouseX, (int) mouseY, startX, startY);
        }

        if (button == 0 && mouseX >= startX + 10 && mouseX <= startX + 30 && 
            mouseY >= startY + 10 && mouseY <= startY + 30) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new CreateCategoryScreen(this));
            }
            return true;
        }

        if (button == 0 && mouseX >= startX + 50 && mouseX <= startX + 70 && 
            mouseY >= startY + 10 && mouseY <= startY + 30) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new DeleteCategoryScreen(this));
            }
            return true;
        }

        if (button == 0 && mouseX >= startX + 323 && mouseX <= startX + 363 && 
            mouseY >= startY + 100 && mouseY <= startY + 140) {
            if (this.minecraft != null) {
                this.openAddItemScreen();
            }
            return true;
        }

        if (button == 0 && handleCategoryScrollClick((int) mouseX, (int) mouseY, startX, startY)) {
            return true;
        }

        if (handleCategoryClick((int) mouseX, (int) mouseY, startX, startY)) {
            return true;
        }

        if (handleItemClick((int) mouseX, (int) mouseY, button, startX, startY)) {
            return true;
        }

        if (handleNavigationClick((int) mouseX, (int) mouseY, startX, startY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleCategoryScrollClick(int mouseX, int mouseY, int startX, int startY) {
        List<ShopCategory> categories = CategoryManager.getInstance().getCategories();
        if (categories.size() <= CATEGORIES_PER_PAGE) return false;

        int btnX = startX + 10;
        int upBtnY = startY + 35 + CATEGORIES_PER_PAGE * 30 + 2;
        int downBtnY = upBtnY + 22;
        int btnW = 60;
        int btnH = 20;

        if (mouseX >= btnX && mouseX <= btnX + btnW) {
            if (mouseY >= upBtnY && mouseY <= upBtnY + btnH && categoryScrollOffset > 0) {
                categoryScrollOffset--;
                updateDisplayedItems();
                return true;
            }
            if (mouseY >= downBtnY && mouseY <= downBtnY + btnH && 
                categoryScrollOffset < categories.size() - CATEGORIES_PER_PAGE) {
                categoryScrollOffset++;
                updateDisplayedItems();
                return true;
            }
        }
        return false;
    }

    private boolean handleCategoryClick(int mouseX, int mouseY, int startX, int startY) {
        List<ShopCategory> categories = CategoryManager.getInstance().getCategories();
        
        int y = startY + 35;
        for (int i = categoryScrollOffset; i < Math.min(categories.size(), categoryScrollOffset + CATEGORIES_PER_PAGE); i++) {
            if (mouseX >= startX + 10 && mouseX <= startX + 70 && mouseY >= y && mouseY <= y + 25) {
                currentCategory = categories.get(i).getId();
                updateDisplayedItems();
                return true;
            }
            y += 30;
        }
        return false;
    }

    private boolean handleItemClick(int mouseX, int mouseY, int button, int startX, int startY) {
        if (hoveredItem == null) return false;

        if (button == 1 && net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            if (hoveredItem.isPlayerListing()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new shop.pomelo.mod.network.DeleteItemPacket(hoveredItem.getId())
                );
            }
            return true;
        }

        if (button == 1 && net.minecraft.client.gui.screens.Screen.hasShiftDown() && hoveredItem.canSell()) {
            int sellAmount = 1;
            if (this.minecraft != null && this.minecraft.player != null) {
                net.minecraft.world.item.ItemStack shopItem = hoveredItem.getDisplayStack();
                int totalInInventory = 0;
                for (net.minecraft.world.item.ItemStack invItem : this.minecraft.player.getInventory().items) {
                    if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(invItem, shopItem)) {
                        totalInInventory += invItem.getCount();
                    }
                }
                sellAmount = totalInInventory;
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new shop.pomelo.mod.network.ShopActionPacket(
                    shop.pomelo.mod.network.ShopActionPacket.Action.SELL, 
                    hoveredItem.getId(), 
                    sellAmount
                )
            );
            return true;
        }

        if (button == 0 && hoveredItem.canBuy()) {
            selectedItem = hoveredItem;
            selectedAmount = 1;
            showAmountSelector = true;
            return true;
        } else if (button == 1 && hoveredItem.canSell()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new shop.pomelo.mod.network.ShopActionPacket(
                    shop.pomelo.mod.network.ShopActionPacket.Action.SELL, 
                    hoveredItem.getId(), 
                    1
                )
            );
            return true;
        }

        return false;
    }

    private boolean handleAmountSelectorClick(int mouseX, int mouseY, int startX, int startY) {
        int sx = startX + 82;
        int sy = startY + 168;
        int w = 236, h = 32;

        if (mouseX < sx || mouseX > sx + w || mouseY < sy || mouseY > sy + h) {
            showAmountSelector = false;
            selectedItem = null;
            return true;
        }

        if (mouseY >= sy + 4 && mouseY <= sy + 18) {
            if (mouseX >= sx + 36 && mouseX <= sx + 58) {
                selectedAmount = Math.max(1, selectedAmount - 10);
                return true;
            }
            if (mouseX >= sx + 62 && mouseX <= sx + 80) {
                selectedAmount = Math.max(1, selectedAmount - 1);
                return true;
            }
            if (mouseX >= sx + 108 && mouseX <= sx + 126) {
                selectedAmount = Math.min(64, selectedAmount + 1);
                return true;
            }
            if (mouseX >= sx + 130 && mouseX <= sx + 152) {
                selectedAmount = Math.min(64, selectedAmount + 10);
                return true;
            }
            if (mouseX >= sx + 200 && mouseX <= sx + 232) {
                showAmountSelector = false;
                selectedItem = null;
                return true;
            }
        }

        if (mouseY >= sy + 17 && mouseY <= sy + 31) {
            if (mouseX >= sx + 200 && mouseX <= sx + 232) {
                if (selectedItem != null) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new shop.pomelo.mod.network.ShopActionPacket(
                            shop.pomelo.mod.network.ShopActionPacket.Action.BUY,
                            selectedItem.getId(),
                            selectedAmount
                        )
                    );
                }
                showAmountSelector = false;
                selectedItem = null;
                return true;
            }
        }

        return true;
    }

    private boolean handleNavigationClick(int mouseX, int mouseY, int startX, int startY) {
        int totalPages = (displayedItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        
        if (totalPages <= 1) return false;

        if (mouseY >= startY + 200 && mouseY <= startY + 215) {
            if (mouseX >= startX + 90 && mouseX <= startX + 140 && currentPage > 0) {
                currentPage--;
                return true;
            }
            if (mouseX >= startX + 260 && mouseX <= startX + 310 && currentPage < totalPages - 1) {
                currentPage++;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if (mouseX >= startX + 5 && mouseX <= startX + 75 && mouseY >= startY + 35 && mouseY <= startY + 215) {
            List<ShopCategory> categories = CategoryManager.getInstance().getCategories();
            int maxScroll = Math.max(0, categories.size() - CATEGORIES_PER_PAGE);
            int scrollAmount = (int) Math.signum(scrollY);
            categoryScrollOffset = Math.max(0, Math.min(maxScroll, categoryScrollOffset - scrollAmount));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showAmountSelector) {
            if (keyCode == 256) {
                showAmountSelector = false;
                selectedItem = null;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void setPlayerMoney(int money) {
        this.playerMoney = money;
    }
    
    private void openAddItemScreen() {
        if (this.minecraft != null && this.minecraft.player != null) {
            net.minecraft.world.item.ItemStack heldItem = this.minecraft.player.getMainHandItem();
            this.minecraft.setScreen(new AddItemScreen(this, heldItem));
        }
    }
}
