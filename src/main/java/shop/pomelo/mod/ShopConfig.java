package shop.pomelo.mod;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ShopConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_LIST_ITEM = BUILDER
            .comment("Require admin permission to list items in the shop")
            .define("requireAdminListItem", false);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_CREATE_CATEGORY = BUILDER
            .comment("Require admin permission to create categories")
            .define("requireAdminCreateCategory", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_DELETE_CATEGORY = BUILDER
            .comment("Require admin permission to delete categories")
            .define("requireAdminDeleteCategory", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_DELETE_ITEM = BUILDER
            .comment("Require admin permission to delete items from the shop")
            .define("requireAdminDeleteItem", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_DELETE_OWN_ITEM = BUILDER
            .comment("Require admin permission to delete own listed items (Ctrl + right click)")
            .define("requireAdminDeleteOwnItem", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_EDIT_OWN_ITEM = BUILDER
            .comment("Require admin permission to edit own listed items (Shift + left click)")
            .define("requireAdminEditOwnItem", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_COPY_ITEM = BUILDER
            .comment("Require admin permission to copy item info (Ctrl + C while hovering)")
            .define("requireAdminCopyItem", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_PASTE_ITEM = BUILDER
            .comment("Require admin permission to paste copied item (Ctrl + V)")
            .define("requireAdminPasteItem", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_ADD_ITEM = BUILDER
            .comment("Require admin permission to add item button click")
            .define("requireAdminAddItem", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean requireAdminListItem;
    private static boolean requireAdminCreateCategory;
    private static boolean requireAdminDeleteCategory;
    private static boolean requireAdminDeleteItem;
    private static boolean requireAdminDeleteOwnItem;
    private static boolean requireAdminEditOwnItem;
    private static boolean requireAdminCopyItem;
    private static boolean requireAdminPasteItem;
    private static boolean requireAdminAddItem;

    static void onLoad(final ModConfigEvent event) {
        requireAdminListItem = REQUIRE_ADMIN_LIST_ITEM.get();
        requireAdminCreateCategory = REQUIRE_ADMIN_CREATE_CATEGORY.get();
        requireAdminDeleteCategory = REQUIRE_ADMIN_DELETE_CATEGORY.get();
        requireAdminDeleteItem = REQUIRE_ADMIN_DELETE_ITEM.get();
        requireAdminDeleteOwnItem = REQUIRE_ADMIN_DELETE_OWN_ITEM.get();
        requireAdminEditOwnItem = REQUIRE_ADMIN_EDIT_OWN_ITEM.get();
        requireAdminCopyItem = REQUIRE_ADMIN_COPY_ITEM.get();
        requireAdminPasteItem = REQUIRE_ADMIN_PASTE_ITEM.get();
        requireAdminAddItem = REQUIRE_ADMIN_ADD_ITEM.get();
    }

    public static boolean requiresAdminListItem() {
        return requireAdminListItem;
    }

    public static boolean requiresAdminCreateCategory() {
        return requireAdminCreateCategory;
    }

    public static boolean requiresAdminDeleteCategory() {
        return requireAdminDeleteCategory;
    }

    public static boolean requiresAdminDeleteItem() {
        return requireAdminDeleteItem;
    }

    public static boolean requiresAdminDeleteOwnItem() {
        return requireAdminDeleteOwnItem;
    }

    public static boolean requiresAdminEditOwnItem() {
        return requireAdminEditOwnItem;
    }

    public static boolean requiresAdminCopyItem() {
        return requireAdminCopyItem;
    }

    public static boolean requiresAdminPasteItem() {
        return requireAdminPasteItem;
    }

    public static boolean requiresAdminAddItem() {
        return requireAdminAddItem;
    }
}
