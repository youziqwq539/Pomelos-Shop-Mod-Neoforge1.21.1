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

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean requireAdminListItem;
    private static boolean requireAdminCreateCategory;
    private static boolean requireAdminDeleteCategory;
    private static boolean requireAdminDeleteItem;

    static void onLoad(final ModConfigEvent event) {
        requireAdminListItem = REQUIRE_ADMIN_LIST_ITEM.get();
        requireAdminCreateCategory = REQUIRE_ADMIN_CREATE_CATEGORY.get();
        requireAdminDeleteCategory = REQUIRE_ADMIN_DELETE_CATEGORY.get();
        requireAdminDeleteItem = REQUIRE_ADMIN_DELETE_ITEM.get();
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
}
