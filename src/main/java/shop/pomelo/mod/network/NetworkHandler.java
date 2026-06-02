package shop.pomelo.mod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import shop.pomelo.mod.ShopConfig;
import shop.pomelo.mod.gui.ShopScreen;
import shop.pomelo.mod.shop.BannedItemsManager;
import shop.pomelo.mod.shop.CategoryManager;
import shop.pomelo.mod.shop.ShopManager;

public class NetworkHandler {
    public static void handleShopAction(ShopActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (packet.action() == ShopActionPacket.Action.BUY) {
                    ShopManager.getInstance().buyItem(serverPlayer, packet.itemId(), packet.amount());
                } else if (packet.action() == ShopActionPacket.Action.SELL) {
                    ShopManager.getInstance().sellItem(serverPlayer, packet.itemId(), packet.amount());
                }

                int money = ShopManager.getInstance().getPlayerMoney(serverPlayer);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new SyncMoneyPacket(money));
            }
        });
    }

    public static void handleSyncMoney(SyncMoneyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof LocalPlayer) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof ShopScreen shopScreen) {
                    shopScreen.setPlayerMoney(packet.money());
                }
            }
        });
    }
    
    public static void handleListItem(ListItemPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (ShopConfig.requiresAdminListItem() && !serverPlayer.hasPermissions(2)) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.permission_denied")
                    );
                    return;
                }

                if (BannedItemsManager.getInstance().isBanned(packet.itemStack())) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.item_banned")
                    );
                    return;
                }

                String itemId = ShopManager.getInstance().listItem(
                    serverPlayer,
                    packet.itemStack(),
                    packet.price(),
                    packet.sellPrice(),
                    packet.amount(),
                    packet.categoryId()
                );
                
                if (itemId != null) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "shop.pomeloshopmod.listed",
                            packet.itemStack().getHoverName(),
                            packet.amount(),
                            packet.price()
                        )
                    );
                    
                    java.util.List<shop.pomelo.mod.shop.ShopItem> items = 
                        new java.util.ArrayList<>(ShopManager.getInstance().getAllItems());
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        serverPlayer, 
                        new SyncShopItemsPacket(items)
                    );
                }
            }
        });
    }
    
    public static void handleCreateCategory(CreateCategoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (ShopConfig.requiresAdminCreateCategory() && !serverPlayer.hasPermissions(2)) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.permission_denied")
                    );
                    return;
                }

                CategoryManager.getInstance().addCategory(
                    packet.categoryId(),
                    packet.displayName(),
                    serverPlayer.getUUID(),
                    serverPlayer.getName().getString()
                );
                
                serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "shop.pomeloshopmod.category_created",
                        packet.displayName()
                    )
                );
                
                java.util.List<shop.pomelo.mod.shop.ShopCategory> categories = 
                    new java.util.ArrayList<>(CategoryManager.getInstance().getCategories());
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer, 
                    new SyncCategoriesPacket(categories)
                );
            }
        });
    }
    
    public static void handleSyncShopItems(SyncShopItemsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof LocalPlayer) {
                try {
                    ShopManager.getInstance().clearItems();
                    for (var item : packet.items()) {
                        ShopManager.getInstance().addItem(item);
                    }
                    
                    if (Minecraft.getInstance().screen instanceof ShopScreen shopScreen) {
                        shopScreen.refreshItems();
                    }
                } catch (Exception e) {
                    shop.pomelo.mod.PomelosShopMod.LOGGER.error("Error syncing shop items", e);
                }
            }
        });
    }
    
    public static void handleSyncCategories(SyncCategoriesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof LocalPlayer) {
                try {
                    CategoryManager.getInstance().clearCategories();
                    for (var category : packet.categories()) {
                        CategoryManager.getInstance().addCategoryWithoutSave(category);
                    }

                    if (Minecraft.getInstance().screen instanceof ShopScreen shopScreen) {
                        shopScreen.refreshItems();
                    }
                } catch (Exception e) {
                    shop.pomelo.mod.PomelosShopMod.LOGGER.error("Error syncing categories", e);
                }
            }
        });
    }
    
    public static void handleDeleteItem(DeleteItemPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (ShopConfig.requiresAdminDeleteItem() && !serverPlayer.hasPermissions(2)) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.permission_denied")
                    );
                    return;
                }

                boolean success = ShopManager.getInstance().removeListing(packet.itemId(), serverPlayer);
                
                if (success) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "shop.pomeloshopmod.item_deleted"
                        )
                    );
                    
                    java.util.List<shop.pomelo.mod.shop.ShopItem> items = 
                        new java.util.ArrayList<>(ShopManager.getInstance().getAllItems());
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        serverPlayer, 
                        new SyncShopItemsPacket(items)
                    );
                } else {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "shop.pomeloshopmod.delete_failed"
                        )
                    );
                }
            }
        });
    }
    
    public static void handleDeleteCategory(DeleteCategoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (ShopConfig.requiresAdminDeleteCategory() && !serverPlayer.hasPermissions(2)) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.permission_denied")
                    );
                    return;
                }

                boolean success = CategoryManager.getInstance().removeCategory(packet.categoryId());
                
                if (success) {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "shop.pomeloshopmod.category_deleted"
                        )
                    );
                    
                    java.util.List<shop.pomelo.mod.shop.ShopCategory> categories = 
                        new java.util.ArrayList<>(CategoryManager.getInstance().getCategories());
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        serverPlayer, 
                        new SyncCategoriesPacket(categories)
                    );
                    
                    java.util.List<shop.pomelo.mod.shop.ShopItem> items = 
                        new java.util.ArrayList<>(ShopManager.getInstance().getAllItems());
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        serverPlayer, 
                        new SyncShopItemsPacket(items)
                    );
                } else {
                    serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "shop.pomeloshopmod.category_delete_failed"
                        )
                    );
                }
            }
        });
    }

    public static void handleOpenShop(OpenShopPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                java.util.List<shop.pomelo.mod.shop.ShopItem> items =
                    new java.util.ArrayList<>(ShopManager.getInstance().getAllItems());
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new SyncShopItemsPacket(items)
                );

                java.util.List<shop.pomelo.mod.shop.ShopCategory> categories =
                    new java.util.ArrayList<>(CategoryManager.getInstance().getCategories());
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new SyncCategoriesPacket(categories)
                );

                serverPlayer.openMenu(new net.minecraft.world.MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.title");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                        return new shop.pomelo.mod.gui.ShopMenu(containerId, playerInventory);
                    }
                });

                int money = ShopManager.getInstance().getPlayerMoney(serverPlayer);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new SyncMoneyPacket(money));
            }
        });
    }
}
