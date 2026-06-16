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
import shop.pomelo.mod.sound.ModSounds;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddItemScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/add_item_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private ItemStack itemStack;
    private TexturedEditBox amountBox;
    private TexturedEditBox priceBox;
    private TexturedEditBox sellPriceBox;
    private TexturedEditBox stockBox;
    private String selectedCategory = "";
    private int categoryScrollOffset = 0;
    private static final int CATEGORIES_PER_PAGE = 3;
    
    private static final int COLOR_DISABLED = new Color(150, 150, 150).getRGB();
    private static final int COLOR_SUB_CATEGORY = new Color(80, 80, 80).getRGB();
    
    private Set<String> expandedCategories = new HashSet<>(); // 存储展开的一级分类ID

    public AddItemScreen(Screen parentScreen, ItemStack itemStack) {
        super(Component.translatable("shop.pomeloshopmod.add_item_title"));
        this.parentScreen = parentScreen;
        this.itemStack = itemStack.copy();
    }
    
    /**
     * 播放按钮点击音效
     */
    private void playButtonClickSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(ModSounds.SHOP_CLICK.get(), 0.3F, 1.0F);
        }
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

        // 库存输入框 - 在出售价格右侧
        this.stockBox = new TexturedEditBox(this.font, centerX + 55, centerY, 50, 18, 
            Component.translatable("shop.pomeloshopmod.stock_label"));
        this.stockBox.setValue("");
        this.stockBox.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.stockBox);
        
        // 默认选择第一个可选择的分类
        List<CategorySelectItem> categories = getSelectableCategories();
        for (CategorySelectItem item : categories) {
            if (!item.disabled) {
                selectedCategory = item.category.getId();
                break;
            }
        }
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }
    
    /**
     * 获取可选择的分类列表（二级分类 + 没有二级分类的一级分类）
     * 支持展开/折叠显示
     */
    private List<CategorySelectItem> getSelectableCategories() {
        List<CategorySelectItem> items = new ArrayList<>();
        List<ShopCategory> mainCategories = CategoryManager.getInstance().getMainCategories();
        
        for (ShopCategory mainCat : mainCategories) {
            List<ShopCategory> subCategories = CategoryManager.getInstance().getSubCategories(mainCat.getId());
            boolean hasSubCategories = !subCategories.isEmpty();
            
            if (hasSubCategories) {
                // 有二级分类的一级分类不可选择，显示展开/折叠箭头
                boolean isExpanded = expandedCategories.contains(mainCat.getId());
                items.add(new CategorySelectItem(mainCat, false, true, isExpanded));
                // 只有展开时才显示二级分类
                if (isExpanded) {
                    for (ShopCategory subCat : subCategories) {
                        items.add(new CategorySelectItem(subCat, true, false, false));
                    }
                }
            } else {
                // 没有二级分类的一级分类可选择
                items.add(new CategorySelectItem(mainCat, false, false, false));
            }
        }
        return items;
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
        // 设置库存标签
        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.stock_label"), 
            centerX + 55, centerY - 20, Color.BLACK.getRGB(), false);

        drawCategorySelection(guiGraphics, centerX, centerY, mouseX, mouseY);

        this.amountBox.render(guiGraphics, mouseX, mouseY, partialTick);
        this.priceBox.render(guiGraphics, mouseX, mouseY, partialTick);
        this.sellPriceBox.render(guiGraphics, mouseX, mouseY, partialTick);
        this.stockBox.render(guiGraphics, mouseX, mouseY, partialTick);
        
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
        List<CategorySelectItem> categories = getSelectableCategories();
        
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
            CategorySelectItem item = categories.get(i);
            ShopCategory category = item.category;
            
            boolean isSelected = category.getId().equals(selectedCategory);
            boolean isHovered = mouseX >= centerX - 60 && mouseX <= centerX + 60 && 
                               mouseY >= y && mouseY <= y + 14;

            if (item.disabled) {
                // 不可选择的一级分类（有二级分类）显示展开/折叠箭头
                String arrow = item.isExpanded ? "▼ " : "▶ ";
                String text = arrow + category.getDisplayName();
                int textColor = isHovered ? Color.BLACK.getRGB() : COLOR_DISABLED;
                drawCenteredStringNoShadow(guiGraphics, text, centerX, y + 3, textColor);
            } else {
                if (isSelected || isHovered) {
                    guiGraphics.blit(BUTTONS_TEXTURE, centerX - 60, y, 0, 196, 120, 14);
                }
                
                // 二级分类居中显示
                String text = category.getDisplayName();
                int textColor = item.isSubCategory ? COLOR_SUB_CATEGORY : Color.BLACK.getRGB();
                drawCenteredStringNoShadow(guiGraphics, text, centerX, y + 3, textColor);
            }
            
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
                playButtonClickSound();
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
                playButtonClickSound();
                this.confirmListing();
                return true;
            }

            if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW && 
                mouseY >= cancelBtnY && mouseY <= cancelBtnY + btnH) {
                playButtonClickSound();
                this.onClose();
                return true;
            }

            List<CategorySelectItem> categories = getSelectableCategories();
            int y = centerY + 32;
            
            for (int i = categoryScrollOffset; i < Math.min(categories.size(), categoryScrollOffset + CATEGORIES_PER_PAGE); i++) {
                CategorySelectItem item = categories.get(i);
                
                if (mouseX >= centerX - 60 && mouseX <= centerX + 60 && 
                    mouseY >= y && mouseY <= y + 14) {
                    
                    if (item.disabled) {
                        // 点击有二级分类的一级分类：展开/折叠
                        if (expandedCategories.contains(item.category.getId())) {
                            expandedCategories.remove(item.category.getId());
                        } else {
                            expandedCategories.add(item.category.getId());
                        }
                    } else {
                        // 点击可选择的分类：选中
                        selectedCategory = item.category.getId();
                    }
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
        List<CategorySelectItem> categories = getSelectableCategories();
        int maxScroll = Math.max(0, categories.size() - CATEGORIES_PER_PAGE);
        int scrollAmount = (int) Math.signum(scrollY);
        categoryScrollOffset = Math.max(0, Math.min(maxScroll, categoryScrollOffset - scrollAmount));
        return true;
    }

    private void confirmListing() {
        try {
            // Check if item stack is empty (air)
            if (itemStack.isEmpty()) {
                this.onClose();
                return;
            }

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

            // 获取库存值，0表示无限库存
            int stock = 0;
            String stockText = stockBox.getValue().trim();
            if (!stockText.isEmpty()) {
                stock = Integer.parseInt(stockText);
                if (stock < 0) stock = 0;
            }

            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new ListItemPacket(itemStack, price, sellPrice, amount, selectedCategory, stock)
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
    
    private static class CategorySelectItem {
        final ShopCategory category;
        final boolean isSubCategory;
        final boolean disabled; // 不可选择（有二级分类的一级分类）
        final boolean isExpanded; // 是否展开
        
        CategorySelectItem(ShopCategory category, boolean isSubCategory, boolean disabled, boolean isExpanded) {
            this.category = category;
            this.isSubCategory = isSubCategory;
            this.disabled = disabled;
            this.isExpanded = isExpanded;
        }
    }
}