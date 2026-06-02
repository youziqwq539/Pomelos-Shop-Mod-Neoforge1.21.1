import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FinalTexture {
    
    public static void main(String[] args) {
        String outputDir = "src/main/resources/assets/pomeloshopmod/textures/gui";
        
        try {
            createShopBackgroundTexture(outputDir);
            createShopPanelTexture(outputDir);
            createFinalButtonsTexture(outputDir);
            createCreateCategoryTexture(outputDir);
            createDeleteCategoryTexture(outputDir);
            createAddItemTexture(outputDir);
            createInventorySelectTexture(outputDir);
            createRowHoverTextures(outputDir);
            System.out.println("Done! All textures generated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    
    private static void saveImage(BufferedImage img, String path) throws IOException {
        ImageIO.write(img, "png", new File(path));
    }

    private static void createShopBackgroundTexture(String outputDir) throws IOException {
        int tw = 512;
        int th = 512;

        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        int bgW = 320, bgH = 220;
        int bgX = (tw - bgW) / 2;
        int bgY = (th - bgH) / 2;

        int leftW = 76;
        int dividerX = bgX + leftW;
        int rightW = bgW - leftW;
        int bottomH = 26;
        int topH = bgH - bottomH;

        Color outerBorder = new Color(50, 60, 90, 100);
        Color mainBg = new Color(18, 18, 26, 230);

        g.setColor(outerBorder);
        g.fillRoundRect(bgX - 1, bgY - 1, bgW + 2, bgH + 2, 16, 16);

        g.setColor(mainBg);
        g.fillRoundRect(bgX, bgY, bgW, bgH, 15, 15);

        g.setColor(new Color(14, 14, 22, 60));
        g.fillRect(bgX + 4, bgY + 4, leftW - 8, topH - 8);

        g.setColor(new Color(20, 20, 30, 45));
        g.fillRect(dividerX + 4, bgY + 4, rightW - 8, topH - 8);

        g.setColor(new Color(24, 24, 34, 55));
        g.fillRect(bgX + 4, bgY + topH + 4, bgW - 8, bottomH - 8);

        g.setColor(new Color(50, 55, 80, 70));
        g.fillRect(dividerX, bgY + 6, 1, topH - 12);

        g.dispose();
        saveImage(img, outputDir + "/shop_background.png");

        System.out.println("shop_background.png " + tw + "x" + th);
        System.out.println("  Full bg:      (" + bgX + "," + bgY + ") " + bgW + "x" + bgH);
        System.out.println("  Left panel:   (" + bgX + "," + bgY + ") " + leftW + "x" + topH);
        System.out.println("  Right panel:  (" + dividerX + "," + bgY + ") " + rightW + "x" + topH);
        System.out.println("  Bottom bar:   (" + bgX + "," + (bgY + topH) + ") " + bgW + "x" + bottomH);
    }

    private static void createShopPanelTexture(String outputDir) throws IOException {
        int tw = 256;
        int th = 256;

        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        Color panelColor = new Color(15, 15, 22, 240);
        Color panelBorder = new Color(40, 50, 70, 180);

        int border = 10;
        int innerX = border;
        int innerY = border;
        int innerW = 240;
        int innerH = 220;

        g.setColor(panelBorder);
        g.fillRoundRect(innerX - 1, innerY - 1, innerW + 2, innerH + 2, 14, 14);

        g.setColor(panelColor);
        g.fillRoundRect(innerX, innerY, innerW, innerH, 14, 14);

        g.dispose();
        saveImage(img, outputDir + "/shop_panel.png");

        System.out.println("shop_panel.png " + tw + "x" + th);
        System.out.println("  Interior: (" + innerX + "," + innerY + ") " + innerW + "x" + innerH);
    }

    private static void createCreateCategoryTexture(String outputDir) throws IOException {
        int tw = 512;
        int th = 512;

        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        Color bgDark = new Color(20, 20, 28, 180);
        Color btnNormal = new Color(35, 35, 45, 200);
        Color btnHover = new Color(60, 90, 160, 160);

        int bgW = 248, bgH = 116;
        int bgX = (tw - bgW) / 2;
        int bgY = (th - bgH - 22) / 2;

        g.setColor(bgDark);
        g.fillRoundRect(bgX, bgY, bgW, bgH, 16, 16);

        int btnW = 82, btnH = 20;
        int confirmU = bgX, confirmV = bgY + bgH + 2;
        int cancelU = confirmU + btnW + 2, cancelV = confirmV;

        g.setColor(btnNormal);
        g.fillRoundRect(confirmU, confirmV, btnW, btnH, 8, 8);
        g.setColor(btnHover);
        g.fillRoundRect(confirmU, confirmV, btnW, btnH, 8, 8);

        g.setColor(btnNormal);
        g.fillRoundRect(cancelU, cancelV, btnW, btnH, 8, 8);
        g.setColor(btnHover);
        g.fillRoundRect(cancelU, cancelV, btnW, btnH, 8, 8);

        g.dispose();
        saveImage(img, outputDir + "/create_category_background.png");

        System.out.println("create_category_background.png " + tw + "x" + th + " (power of 2)");
        System.out.println("  Background:   (" + bgX + "," + bgY + ") " + bgW + "x" + bgH);
        System.out.println("  Confirm btn:  (" + confirmU + "," + confirmV + ") " + btnW + "x" + btnH);
        System.out.println("  Cancel btn:   (" + cancelU + "," + cancelV + ") " + btnW + "x" + btnH);
    }

    private static void createDeleteCategoryTexture(String outputDir) throws IOException {
        int tw = 512;
        int th = 512;

        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        Color bgDark = new Color(20, 20, 28, 180);

        int bgW = 240, bgH = 180;
        int bgX = (tw - bgW) / 2;
        int bgY = (th - bgH) / 2;

        g.setColor(bgDark);
        g.fillRoundRect(bgX, bgY, bgW, bgH, 16, 16);

        g.dispose();
        saveImage(img, outputDir + "/delete_category_background.png");

        System.out.println("delete_category_background.png " + tw + "x" + th + " (power of 2)");
        System.out.println("  Background:   (" + bgX + "," + bgY + ") " + bgW + "x" + bgH);
    }

    private static void createAddItemTexture(String outputDir) throws IOException {
        int tw = 512;
        int th = 512;

        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        Color bgDark = new Color(18, 18, 26, 220);
        Color borderDark = new Color(40, 40, 55, 200);
        Color accentBlue = new Color(60, 90, 160, 80);

        int bgW = 240, bgH = 200;
        int bgX = (tw - bgW) / 2;
        int bgY = (th - bgH) / 2;

        g.setColor(bgDark);
        g.fillRoundRect(bgX, bgY, bgW, bgH, 14, 14);

        g.setColor(borderDark);
        g.drawRoundRect(bgX, bgY, bgW - 1, bgH - 1, 14, 14);

        g.setColor(accentBlue);
        g.drawRoundRect(bgX + 1, bgY + 1, bgW - 3, bgH - 3, 12, 12);

        g.setColor(new Color(35, 35, 50, 150));
        g.fillRoundRect(bgX + 10, bgY + 30, 220, 1, 0, 0);

        g.setColor(new Color(35, 35, 50, 150));
        g.fillRoundRect(bgX + 10, bgY + 100, 220, 1, 0, 0);

        g.dispose();
        saveImage(img, outputDir + "/add_item_background.png");

        System.out.println("add_item_background.png " + tw + "x" + th + " (power of 2)");
        System.out.println("  Background:   (" + bgX + "," + bgY + ") " + bgW + "x" + bgH);
    }

    private static void createInventorySelectTexture(String outputDir) throws IOException {
        int tw = 512;
        int th = 512;

        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        Color bgDark = new Color(15, 15, 22, 230);
        Color borderDark = new Color(45, 45, 60, 220);
        Color accentBlue = new Color(70, 100, 180, 100);
        Color slotBg = new Color(25, 25, 35, 200);

        int bgW = 248, bgH = 200;
        int bgX = (tw - bgW) / 2;
        int bgY = (th - bgH) / 2;

        g.setColor(bgDark);
        g.fillRoundRect(bgX, bgY, bgW, bgH, 12, 12);

        g.setColor(borderDark);
        g.drawRoundRect(bgX, bgY, bgW - 1, bgH - 1, 12, 12);

        g.setColor(accentBlue);
        g.drawRoundRect(bgX + 1, bgY + 1, bgW - 3, bgH - 3, 10, 10);

        g.setColor(new Color(35, 35, 50, 150));
        g.fillRoundRect(bgX + 8, bgY + 22, 232, 1, 0, 0);

        int slotSize = 18;
        int slotGap = 2;
        int startX = bgX + 12;
        int startY = bgY + 30;
        
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = startX + col * (slotSize + slotGap);
                int sy = startY + row * (slotSize + slotGap);
                
                g.setColor(slotBg);
                g.fillRoundRect(sx, sy, slotSize, slotSize, 4, 4);
                
                g.setColor(new Color(50, 50, 70, 100));
                g.drawRoundRect(sx, sy, slotSize - 1, slotSize - 1, 4, 4);
            }
        }

        g.dispose();
        saveImage(img, outputDir + "/inventory_select_background.png");

        System.out.println("inventory_select_background.png " + tw + "x" + th + " (power of 2)");
        System.out.println("  Background:   (" + bgX + "," + bgY + ") " + bgW + "x" + bgH);
        System.out.println("  Slots: 9x4 grid starting at (" + startX + ", " + startY + "), slot size 18x18");
    }

    private static void createFinalButtonsTexture(String outputDir) throws IOException {
        int tw = 256;
        int th = 256;
        
        BufferedImage img = createImage(tw, th);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, tw, th);

        Color darkBase = new Color(25, 25, 35, 160);
        Color blueHover = new Color(80, 120, 200, 100);

        // [+]/[-] normal
        g.setColor(new Color(25, 25, 35, 120));
        g.fillRoundRect(0, 0, 22, 22, 22, 22);
        g.setColor(new Color(60, 60, 80, 80));
        g.drawRoundRect(0, 0, 21, 21, 21, 21);

        // [+] hover
        g.setColor(darkBase);
        g.fillRoundRect(25, 0, 22, 22, 22, 22);
        g.setColor(blueHover);
        g.fillRoundRect(25, 0, 22, 22, 22, 22);

        // [-] hover (red)
        g.setColor(new Color(35, 20, 20, 180));
        g.fillRoundRect(50, 0, 22, 22, 22, 22);
        g.setColor(new Color(200, 60, 60, 120));
        g.fillRoundRect(50, 0, 22, 22, 22, 22);

        // category button
        g.setColor(darkBase);
        g.fillRoundRect(0, 24, 62, 27, 10, 10);
        g.setColor(blueHover);
        g.fillRoundRect(0, 24, 62, 27, 10, 10);

        // item slot hover
        g.setColor(darkBase);
        g.fillRoundRect(0, 75, 30, 30, 12, 12);
        g.setColor(blueHover);
        g.fillRoundRect(0, 75, 30, 30, 12, 12);

        // inventory slot hover (18x18)
        g.setColor(new Color(96, 144, 192, 128));
        g.fillRoundRect(32, 75, 18, 18, 4, 4);
        g.setColor(new Color(120, 180, 240, 180));
        g.drawRoundRect(32, 75, 17, 17, 4, 4);

        // page button
        g.setColor(darkBase);
        g.fillRoundRect(0, 107, 52, 17, 10, 10);
        g.setColor(blueHover);
        g.fillRoundRect(0, 107, 52, 17, 10, 10);

        // amount buttons (with hover states)
        // -10 button (22x14)
        g.setColor(darkBase);
        g.fillRoundRect(0, 126, 22, 14, 6, 6);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(0, 126, 21, 13, 6, 6);
        
        g.setColor(new Color(35, 40, 65, 200));
        g.fillRoundRect(24, 126, 22, 14, 6, 6);
        g.setColor(new Color(80, 130, 220, 180));
        g.drawRoundRect(24, 126, 21, 13, 6, 6);
        
        // -1 button (18x14)
        g.setColor(darkBase);
        g.fillRoundRect(48, 126, 18, 14, 6, 6);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(48, 126, 17, 13, 6, 6);
        
        g.setColor(new Color(35, 40, 65, 200));
        g.fillRoundRect(68, 126, 18, 14, 6, 6);
        g.setColor(new Color(80, 130, 220, 180));
        g.drawRoundRect(68, 126, 17, 13, 6, 6);
        
        // +1 button (18x14)
        g.setColor(darkBase);
        g.fillRoundRect(88, 126, 18, 14, 6, 6);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(88, 126, 17, 13, 6, 6);
        
        g.setColor(new Color(35, 40, 65, 200));
        g.fillRoundRect(108, 126, 18, 14, 6, 6);
        g.setColor(new Color(80, 130, 220, 180));
        g.drawRoundRect(108, 126, 17, 13, 6, 6);
        
        // +10 button (22x14)
        g.setColor(darkBase);
        g.fillRoundRect(128, 126, 22, 14, 6, 6);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(128, 126, 21, 13, 6, 6);
        
        g.setColor(new Color(35, 40, 65, 200));
        g.fillRoundRect(152, 126, 22, 14, 6, 6);
        g.setColor(new Color(80, 130, 220, 180));
        g.drawRoundRect(152, 126, 21, 13, 6, 6);

        // quantity panel confirm (32x14)
        g.setColor(new Color(25, 25, 35, 160));
        g.fillRoundRect(0, 150, 32, 14, 7, 7);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(0, 150, 31, 13, 7, 7);

        g.setColor(darkBase);
        g.fillRoundRect(34, 150, 32, 14, 7, 7);
        g.setColor(blueHover);
        g.fillRoundRect(34, 150, 32, 14, 7, 7);

        // quantity panel cancel (32x14)
        g.setColor(new Color(25, 25, 35, 160));
        g.fillRoundRect(68, 150, 32, 14, 7, 7);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(68, 150, 31, 13, 7, 7);

        g.setColor(new Color(35, 20, 20, 180));
        g.fillRoundRect(102, 150, 32, 14, 7, 7);
        g.setColor(new Color(200, 60, 60, 120));
        g.fillRoundRect(102, 150, 32, 14, 7, 7);

        // small confirm/cancel button (60x16)
        g.setColor(new Color(25, 25, 35, 160));
        g.fillRoundRect(134, 150, 60, 16, 8, 8);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(134, 150, 59, 15, 8, 8);

        g.setColor(darkBase);
        g.fillRoundRect(196, 150, 60, 16, 8, 8);
        g.setColor(blueHover);
        g.fillRoundRect(196, 150, 60, 16, 8, 8);

        // select inventory button (60x14) - after category select
        g.setColor(new Color(25, 25, 35, 160));
        g.fillRoundRect(124, 196, 60, 14, 7, 7);
        g.setColor(new Color(50, 50, 70, 100));
        g.drawRoundRect(124, 196, 59, 13, 7, 7);

        g.setColor(darkBase);
        g.fillRoundRect(186, 196, 60, 14, 7, 7);
        g.setColor(blueHover);
        g.fillRoundRect(186, 196, 60, 14, 7, 7);

        // scroll button (56x20) - for category scroll in shop
        g.setColor(darkBase);
        g.fillRoundRect(0, 172, 56, 20, 10, 10);
        g.setColor(blueHover);
        g.fillRoundRect(58, 172, 56, 20, 10, 10);

        // category select
        g.setColor(new Color(25, 25, 35, 120));
        g.fillRoundRect(0, 196, 120, 14, 8, 8);
        g.setColor(new Color(80, 120, 200, 60));
        g.fillRoundRect(0, 196, 120, 14, 8, 8);

        // delete category (RED)
        g.setColor(new Color(40, 15, 15, 200));
        g.fillRoundRect(0, 214, 200, 18, 10, 10);
        g.setColor(new Color(200, 60, 60, 200));
        g.fillRoundRect(0, 214, 200, 18, 10, 10);

        // small scroll
        g.setColor(new Color(25, 25, 35, 200));
        g.fillRoundRect(0, 234, 20, 20, 12, 12);
        g.setColor(new Color(80, 120, 200, 200));
        g.fillRoundRect(0, 234, 20, 20, 12, 12);

        // back button
        g.setColor(new Color(25, 25, 35, 200));
        g.fillRoundRect(22, 234, 100, 20, 12, 12);
        g.setColor(new Color(80, 120, 200, 200));
        g.fillRoundRect(22, 234, 100, 20, 12, 12);

        // vertical add item button - normal state (40x40)
        g.setColor(new Color(30, 30, 45, 230));
        g.fillRoundRect(150, 0, 40, 40, 10, 10);
        g.setColor(new Color(50, 55, 80, 180));
        g.drawRoundRect(150, 0, 39, 39, 10, 10);

        // vertical add item button - hover state (40x40)
        g.setColor(new Color(35, 40, 60, 240));
        g.fillRoundRect(192, 0, 40, 40, 10, 10);
        g.setColor(new Color(80, 130, 220, 200));
        g.drawRoundRect(192, 0, 39, 39, 10, 10);

        g.dispose();
        saveImage(img, outputDir + "/buttons.png");
        
        System.out.println("buttons.png " + tw + "x" + th);
        System.out.println("\nNew layout (2px gaps between rows):");
        System.out.println("(0, 0)   22x22  - [+]/[-] normal");
        System.out.println("(25, 0)   22x22  - [+] hover (blue)");
        System.out.println("(50, 0)   22x22  - [-] hover (red)");
        System.out.println("(0, 24)  62x27  - category button");
        System.out.println("(0, 75)   30x30  - item slot hover");
        System.out.println("(32, 75)  18x18  - inventory slot hover");
        System.out.println("(0, 107) 52x17  - page button");
        System.out.println("(0, 126)  22x14  - amount -10 normal");
        System.out.println("(24, 126) 22x14  - amount -10 hover");
        System.out.println("(48, 126) 18x14  - amount -1 normal");
        System.out.println("(68, 126) 18x14  - amount -1 hover");
        System.out.println("(88, 126) 18x14  - amount +1 normal");
        System.out.println("(108, 126) 18x14 - amount +1 hover");
        System.out.println("(128, 126) 22x14 - amount +10 normal");
        System.out.println("(152, 126) 22x14 - amount +10 hover");
        System.out.println("(0, 150) 32x14  - confirm normal (quantity panel)");
        System.out.println("(34, 150) 32x14  - confirm hover (quantity panel)");
        System.out.println("(68, 150) 32x14  - cancel normal (quantity panel)");
        System.out.println("(102, 150) 32x14 - cancel hover (quantity panel)");
        System.out.println("(134,150) 60x16 - small confirm/cancel normal");
        System.out.println("(196,150) 60x16 - small confirm/cancel hover");
        System.out.println("(0, 172)   56x20  - scroll button normal");
        System.out.println("(58, 172)  56x20  - scroll button hover");
        System.out.println("(0, 196) 120x14 - category select");
        System.out.println("(124,196) 60x14 - select inventory normal");
        System.out.println("(186,196) 60x14 - select inventory hover");
        System.out.println("(0, 234) 20x20  - small scroll");
        System.out.println("(22,234) 100x20 - back button");
    }

    private static void createRowHoverTextures(String outputDir) throws IOException {
        Color[] fillColors = {
            new Color(80, 120, 200, 100),   // Row 1: Blue
            new Color(80, 180, 120, 100),   // Row 2: Green
            new Color(220, 160, 60, 100),   // Row 3: Gold/Orange
            new Color(160, 100, 220, 100)   // Row 4: Purple
        };
        Color[] borderColors = {
            new Color(120, 160, 240, 180),
            new Color(120, 220, 160, 180),
            new Color(255, 200, 80, 180),
            new Color(200, 140, 255, 180)
        };
        Color baseColor = new Color(25, 25, 35, 160);

        for (int i = 0; i < 4; i++) {
            int size = (i == 2) ? 38 : 30;
            int padding = (i == 2) ? 4 : 0;

            BufferedImage img = createImage(size, size);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(baseColor);
            g.fillRoundRect(padding, padding, 30, 30, 12, 12);

            g.setColor(fillColors[i]);
            g.fillRoundRect(padding, padding, 30, 30, 12, 12);

            g.setColor(borderColors[i]);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(padding, padding, padding + 29, padding + 29, 12, 12);

            g.dispose();
            saveImage(img, outputDir + "/row" + (i + 1) + "_hover.png");
        }
        System.out.println("Generated 4 row hover textures (row1-4_hover.png)");
    }
}