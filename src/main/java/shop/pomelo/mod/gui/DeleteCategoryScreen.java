package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.shop.CategoryManager;
import shop.pomelo.mod.shop.ShopCategory;
import shop.pomelo.mod.sound.ModSounds;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DeleteCategoryScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/delete_category_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    
    private final Screen parentScreen;
    private int scrollOffset = 0;
    private static final int CATEGORIES_PER_PAGE = 6;

    // 退出按钮位置和大小
    private static final int EXIT_BTN_WIDTH = 20;
    private static final int EXIT_BTN_HEIGHT = 20;

    public DeleteCategoryScreen(Screen parentScreen) {
        super(Component.translatable("shop.pomeloshopmod.delete_category_title"));
        this.parentScreen = parentScreen;
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

    private boolean canScrollUp() {
        return scrollOffset > 0;
    }

    private boolean canScrollDown() {
        List<CategoryDisplayItem> displayItems = buildCategoryDisplayList();
        return scrollOffset + CATEGORIES_PER_PAGE < displayItems.size();
    }

    private void scrollUp() {
        if (canScrollUp()) {
            scrollOffset--;
        }
    }

    private void scrollDown() {
        if (canScrollDown()) {
            scrollOffset++;
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

    private List<CategoryDisplayItem> buildCategoryDisplayList() {
        List<CategoryDisplayItem> items = new ArrayList<>();
        List<ShopCategory> mainCategories = CategoryManager.getInstance().getMainCategories();
        
        for (ShopCategory mainCat : mainCategories) {
            items.add(new CategoryDisplayItem(mainCat, false));
            // 添加该一级分类下的二级分类
            List<ShopCategory> subCategories = CategoryManager.getInstance().getSubCategories(mainCat.getId());
            for (ShopCategory subCat : subCategories) {
                items.add(new CategoryDisplayItem(subCat, true));
            }
        }
        return items;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND_TEXTURE, centerX - 120 - 136, centerY - 90 - 166, 0, 0, 512, 512, 512, 512);

        drawCenteredStringNoShadow(guiGraphics, this.title, centerX, centerY - 80, Color.BLACK.getRGB());

        List<CategoryDisplayItem> displayItems = buildCategoryDisplayList();
        
        if (displayItems.isEmpty()) {
            drawCenteredStringNoShadow(guiGraphics, 
                Component.translatable("shop.pomeloshopmod.no_categories_to_delete"), 
                centerX, centerY, Color.BLACK.getRGB());
        } else {
            int y = centerY - 55;
            int endIndex = Math.min(scrollOffset + CATEGORIES_PER_PAGE, displayItems.size());
            
            for (int i = scrollOffset; i < endIndex; i++) {
                CategoryDisplayItem item = displayItems.get(i);
                ShopCategory category = item.category;
                
                int catWidth = 200;
                
                boolean isHovered = mouseX >= centerX - 100 && mouseX <= centerX - 100 + catWidth && 
                                   mouseY >= y && mouseY <= y + 18;
                
                if (isHovered) {
                    guiGraphics.blit(BUTTONS_TEXTURE, centerX - 100, y, 0, 214, catWidth, 18);
                }
                
                // 二级分类显示前缀
                String displayText = item.isSubCategory ? "  └ " + category.getDisplayName() : category.getDisplayName();
                int textColor = item.isSubCategory ? new Color(80, 80, 80).getRGB() : Color.BLACK.getRGB();
                drawCenteredStringNoShadow(guiGraphics, displayText, centerX, y + 4, textColor);
                
                y += 22;
            }
            
            String pageText = (scrollOffset + 1) + "-" + Math.min(scrollOffset + CATEGORIES_PER_PAGE, displayItems.size()) + 
                             " / " + displayItems.size();
            int totalHeight = CATEGORIES_PER_PAGE * 22;
            drawCenteredStringNoShadow(guiGraphics, pageText, centerX, centerY - 55 + totalHeight, Color.BLACK.getRGB());

            drawScrollButtons(guiGraphics, centerX, centerY, mouseX, mouseY, displayItems.size());
        }

        drawBackButton(guiGraphics, centerX, centerY, mouseX, mouseY);
        drawExitButton(guiGraphics, centerX, centerY, mouseX, mouseY);
    }

    private void drawExitButton(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int btnX = centerX + 105;
        int btnY = centerY - 82;
        
        boolean hovered = mouseX >= btnX && mouseX <= btnX + EXIT_BTN_WIDTH && 
                         mouseY >= btnY && mouseY <= btnY + EXIT_BTN_HEIGHT;
        
        if (hovered) {
            guiGraphics.blit(BUTTONS_TEXTURE, btnX, btnY, 0, 234, EXIT_BTN_WIDTH, EXIT_BTN_HEIGHT);
        }
        
        drawCenteredStringNoShadow(guiGraphics, "✕", btnX + EXIT_BTN_WIDTH / 2, btnY + 4, Color.BLACK.getRGB());
    }

    private void drawScrollButtons(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY, int totalItems) {
        int btnX = centerX + 110;
        int upBtnY = centerY - 55;
        int downBtnY = centerY + 42;
        int btnW = 20;
        int btnH = 20;

        boolean upHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= upBtnY && mouseY <= upBtnY + btnH;
        boolean downHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= downBtnY && mouseY <= downBtnY + btnH;

        if (canScrollUp() && upHovered) {
            guiGraphics.blit(BUTTONS_TEXTURE, btnX, upBtnY, 0, 234, btnW, btnH);
        }
        if (canScrollDown() && downHovered) {
            guiGraphics.blit(BUTTONS_TEXTURE, btnX, downBtnY, 0, 234, btnW, btnH);
        }

        Color upArrowColor = canScrollUp() ? Color.BLACK : new Color(100, 100, 110, 200);
        Color downArrowColor = canScrollDown() ? Color.BLACK : new Color(100, 100, 110, 200);

        drawCenteredStringNoShadow(guiGraphics, "▲", btnX + btnW / 2, upBtnY + 5, upArrowColor.getRGB());
        drawCenteredStringNoShadow(guiGraphics, "▼", btnX + btnW / 2, downBtnY + 5, downArrowColor.getRGB());
    }

    private void drawBackButton(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int btnX = centerX - 50;
        int btnY = centerY + 234;
        int btnW = 100;
        int btnH = 20;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        if (hovered) {
            guiGraphics.blit(BUTTONS_TEXTURE, btnX, btnY, 22, 234, btnW, btnH);
        }
        drawCenteredStringNoShadow(guiGraphics, Component.translatable("shop.pomeloshopmod.back"), 
            btnX + btnW / 2, btnY + 5, Color.BLACK.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (button == 0) {
            int btnX = centerX + 110;
            if (mouseX >= btnX && mouseX <= btnX + 20) {
                if (mouseY >= centerY - 55 && mouseY <= centerY - 35 && canScrollUp()) {
                    scrollUp();
                    return true;
                }
                if (mouseY >= centerY + 42 && mouseY <= centerY + 62 && canScrollDown()) {
                    scrollDown();
                    return true;
                }
            }

            if (mouseX >= centerX - 50 && mouseX <= centerX + 50 && 
                mouseY >= centerY + 234 && mouseY <= centerY + 254) {
                playButtonClickSound();
                this.onClose();
                return true;
            }

            // 退出按钮
            int exitBtnX = centerX + 105;
            int exitBtnY = centerY - 82;
            if (mouseX >= exitBtnX && mouseX <= exitBtnX + EXIT_BTN_WIDTH &&
                mouseY >= exitBtnY && mouseY <= exitBtnY + EXIT_BTN_HEIGHT) {
                playButtonClickSound();
                this.onClose();
                return true;
            }
        }

        List<CategoryDisplayItem> displayItems = buildCategoryDisplayList();
        
        if (!displayItems.isEmpty()) {
            int y = centerY - 55;
            int endIndex = Math.min(scrollOffset + CATEGORIES_PER_PAGE, displayItems.size());
            int catWidth = 200;
            
            for (int i = scrollOffset; i < endIndex; i++) {
                CategoryDisplayItem item = displayItems.get(i);
                ShopCategory category = item.category;
                
                if (mouseX >= centerX - 100 && mouseX <= centerX - 100 + catWidth && 
                    mouseY >= y && mouseY <= y + 18) {
                    playButtonClickSound();
                    // 打开确认删除屏幕
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ConfirmDeleteCategoryScreen(this, category));
                    }
                    return true;
                }
                y += 22;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        List<CategoryDisplayItem> displayItems = buildCategoryDisplayList();
        
        if (displayItems.size() > CATEGORIES_PER_PAGE) {
            if (deltaY > 0) {
                scrollUp();
            } else if (deltaY < 0) {
                scrollDown();
            }
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
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
    
    private static class CategoryDisplayItem {
        final ShopCategory category;
        final boolean isSubCategory;
        
        CategoryDisplayItem(ShopCategory category, boolean isSubCategory) {
            this.category = category;
            this.isSubCategory = isSubCategory;
        }
    }
}