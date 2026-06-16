package shop.pomelo.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import shop.pomelo.mod.network.CreateCategoryPacket;
import shop.pomelo.mod.shop.CategoryManager;
import shop.pomelo.mod.shop.ShopCategory;
import shop.pomelo.mod.sound.ModSounds;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class CreateCategoryScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/create_category_background.png");
    private static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/buttons.png");
    private static final ResourceLocation DROPDOWN_TEXTURE = ResourceLocation.fromNamespaceAndPath("pomeloshopmod", "textures/gui/dropdown_buttons.png");
    
    private final Screen parentScreen;
    private TexturedEditBox nameBox;
    private int selectedParentIndex = 0; // 0 表示无父分类（一级分类）
    private boolean parentDropdownOpen = false;
    private int parentDropdownScroll = 0;

    public CreateCategoryScreen(Screen parentScreen) {
        super(Component.translatable("shop.pomeloshopmod.create_category_title"));
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
        
        // 绘制父分类选择
        drawParentSelector(guiGraphics, centerX, centerY, mouseX, mouseY);
        
        drawButtons(guiGraphics, centerX, centerY, mouseX, mouseY);
    }

    private void drawParentSelector(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        List<ShopCategory> mainCategories = CategoryManager.getInstance().getMainCategories();
        
        // 父分类标签
        guiGraphics.drawString(this.font, Component.translatable("shop.pomeloshopmod.parent_category_label"), 
            centerX - 100, centerY - 5, Color.LIGHT_GRAY.getRGB());
        
        // 下拉框位置
        int dropdownX = centerX - 100;
        int dropdownY = centerY + 5;
        int dropdownW = 200;
        int dropdownH = 20;
        
        // 当前选中的父分类名称
        String selectedText;
        if (selectedParentIndex == 0) {
            selectedText = Component.translatable("shop.pomeloshopmod.no_parent").getString();
        } else {
            int idx = selectedParentIndex - 1;
            if (idx >= 0 && idx < mainCategories.size()) {
                selectedText = mainCategories.get(idx).getDisplayName();
            } else {
                selectedText = Component.translatable("shop.pomeloshopmod.no_parent").getString();
                selectedParentIndex = 0;
            }
        }
        
        // 绘制下拉框背景
        boolean hovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && 
                         mouseY >= dropdownY && mouseY <= dropdownY + dropdownH;
        guiGraphics.blit(DROPDOWN_TEXTURE, dropdownX, dropdownY, hovered ? 200 : 0, 0, dropdownW, dropdownH, 400, 38);
        
        // 绘制选中文本
        int textWidth = this.font.width(selectedText);
        int maxTextWidth = dropdownW - 20;
        if (textWidth > maxTextWidth) {
            selectedText = selectedText.substring(0, this.font.width(selectedText) > maxTextWidth ? 
                (int)(selectedText.length() * maxTextWidth / textWidth) : selectedText.length()) + "...";
        }
        guiGraphics.drawString(this.font, selectedText, dropdownX + 5, dropdownY + 5, Color.WHITE.getRGB(), false);
        
        // 绘制下拉箭头
        guiGraphics.drawCenteredString(this.font, parentDropdownOpen ? "▲" : "▼", 
            dropdownX + dropdownW - 10, dropdownY + 5, Color.WHITE.getRGB());
        
        // 如果下拉框打开，绘制选项列表
        if (parentDropdownOpen) {
            int optionY = dropdownY + dropdownH + 2;
            int optionH = 18;
            int maxVisible = 5;
            
            int totalOptions = mainCategories.size() + 1;
            
            // 绘制"无父分类"选项行
            boolean noParentHovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && 
                                      mouseY >= optionY && mouseY <= optionY + optionH;
            guiGraphics.blit(DROPDOWN_TEXTURE, dropdownX, optionY, noParentHovered ? 200 : 0, 20, dropdownW, optionH, 400, 38);
            String noParentText = Component.translatable("shop.pomeloshopmod.no_parent").getString();
            guiGraphics.drawCenteredString(this.font, noParentText, centerX, optionY + 4, 
                selectedParentIndex == 0 ? Color.GREEN.getRGB() : Color.WHITE.getRGB());
            
            // 绘制父分类选项行
            optionY += optionH;
            int startIndex = parentDropdownScroll;
            int endIndex = Math.min(startIndex + maxVisible - 1, mainCategories.size());
            for (int i = startIndex; i < endIndex; i++) {
                ShopCategory cat = mainCategories.get(i);
                boolean optionHovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && 
                                        mouseY >= optionY && mouseY <= optionY + optionH;
                guiGraphics.blit(DROPDOWN_TEXTURE, dropdownX, optionY, optionHovered ? 200 : 0, 20, dropdownW, optionH, 400, 38);
                guiGraphics.drawCenteredString(this.font, cat.getDisplayName(), centerX, optionY + 4, 
                    selectedParentIndex == i + 1 ? Color.GREEN.getRGB() : Color.WHITE.getRGB());
                optionY += optionH;
            }
            
            // 如果选项超过最大可见数，绘制滚动提示
            if (totalOptions > maxVisible) {
                String scrollHint = (parentDropdownScroll + 1) + "-" + 
                    Math.min(parentDropdownScroll + maxVisible - 1, mainCategories.size()) + 
                    " / " + mainCategories.size();
                guiGraphics.drawCenteredString(this.font, scrollHint, centerX, optionY + 2, Color.LIGHT_GRAY.getRGB());
            }
        }
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
        
        // 获取父分类ID
        String parentId = null;
        if (selectedParentIndex > 0) {
            List<ShopCategory> mainCategories = CategoryManager.getInstance().getMainCategories();
            int idx = selectedParentIndex - 1;
            if (idx >= 0 && idx < mainCategories.size()) {
                parentId = mainCategories.get(idx).getId();
            }
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            new CreateCategoryPacket(id, name, parentId)
        );

        this.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // 处理下拉框点击
            int dropdownX = centerX - 100;
            int dropdownY = centerY + 5;
            int dropdownW = 200;
            int dropdownH = 20;
            
            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && 
                mouseY >= dropdownY && mouseY <= dropdownY + dropdownH) {
                parentDropdownOpen = !parentDropdownOpen;
                return true;
            }
            
            // 处理下拉选项点击
            if (parentDropdownOpen) {
                List<ShopCategory> mainCategories = CategoryManager.getInstance().getMainCategories();
                int optionY = dropdownY + dropdownH + 2;
                int optionH = 18;
                int maxVisible = 5;
                
                // 点击"无父分类"选项
                if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && 
                    mouseY >= optionY && mouseY <= optionY + optionH) {
                    selectedParentIndex = 0;
                    parentDropdownOpen = false;
                    return true;
                }
                
                // 点击父分类选项
                optionY += optionH;
                int startIndex = parentDropdownScroll;
                int endIndex = Math.min(startIndex + maxVisible - 1, mainCategories.size());
                for (int i = startIndex; i < endIndex; i++) {
                    if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownW && 
                        mouseY >= optionY && mouseY <= optionY + optionH) {
                        selectedParentIndex = i + 1;
                        parentDropdownOpen = false;
                        return true;
                    }
                    optionY += optionH;
                }
                
                // 点击其他区域关闭下拉框
                parentDropdownOpen = false;
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
                this.createCategory();
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (parentDropdownOpen) {
            List<ShopCategory> mainCategories = CategoryManager.getInstance().getMainCategories();
            int maxVisible = 5;
            int totalOptions = mainCategories.size();
            
            if (totalOptions > maxVisible - 1) {
                if (deltaY > 0) {
                    if (parentDropdownScroll > 0) {
                        parentDropdownScroll--;
                    }
                } else if (deltaY < 0) {
                    if (parentDropdownScroll < totalOptions - maxVisible + 1) {
                        parentDropdownScroll++;
                    }
                }
                return true;
            }
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
}