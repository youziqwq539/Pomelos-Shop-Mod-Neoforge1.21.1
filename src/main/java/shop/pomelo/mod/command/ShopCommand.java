package shop.pomelo.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.shop.BannedItemsManager;
import shop.pomelo.mod.shop.CategoryManager;
import shop.pomelo.mod.shop.ShopManager;

public class ShopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shop")
            .requires(source -> source.getEntity() instanceof ServerPlayer)
            .executes(ShopCommand::openShop)
            .then(Commands.literal("balance")
                .executes(ShopCommand::showBalance))
            .then(Commands.literal("sell")
                .then(Commands.argument("price", IntegerArgumentType.integer(1))
                    .executes(context -> listItem(context, IntegerArgumentType.getInteger(context, "price"), 1))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> listItem(context, 
                            IntegerArgumentType.getInteger(context, "price"),
                            IntegerArgumentType.getInteger(context, "amount"))))))
            .then(Commands.literal("category")
                .then(Commands.literal("create")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                            .executes(ShopCommand::createCategory))))
                .then(Commands.literal("list")
                    .executes(ShopCommand::listCategories)))
            .then(Commands.literal("add")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ShopCommand::addMoney))))
            .then(Commands.literal("set")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ShopCommand::setMoney))))
            .then(Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(ShopCommand::reloadShop))
            .then(Commands.literal("clear")
                .requires(source -> source.hasPermission(2))
                .executes(ShopCommand::clearShop))
            .then(Commands.literal("ban")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("item", ResourceLocationArgument.id())
                    .executes(ShopCommand::banItem)))
            .then(Commands.literal("unban")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("item", ResourceLocationArgument.id())
                    .executes(ShopCommand::unbanItem)))
            .then(Commands.literal("banlist")
                .requires(source -> source.hasPermission(2))
                .executes(ShopCommand::banlist)));
    }

    private static int openShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            java.util.List<shop.pomelo.mod.shop.ShopItem> items = 
                new java.util.ArrayList<>(shop.pomelo.mod.shop.ShopManager.getInstance().getAllItems());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player, 
                new shop.pomelo.mod.network.SyncShopItemsPacket(items)
            );
            
            java.util.List<shop.pomelo.mod.shop.ShopCategory> categories = 
                new java.util.ArrayList<>(shop.pomelo.mod.shop.CategoryManager.getInstance().getCategories());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player, 
                new shop.pomelo.mod.network.SyncCategoriesPacket(categories)
            );
            
            player.openMenu(new net.minecraft.world.MenuProvider() {
                @Override
                public net.minecraft.network.chat.Component getDisplayName() {
                    return net.minecraft.network.chat.Component.translatable("shop.pomeloshopmod.title");
                }

                @Override
                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                    return new shop.pomelo.mod.gui.ShopMenu(containerId, playerInventory);
                }
            });

            int money = ShopManager.getInstance().getPlayerMoney(player);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new shop.pomelo.mod.network.SyncMoneyPacket(money));
            
            return 1;
        } catch (Exception e) {
            shop.pomelo.mod.PomelosShopMod.LOGGER.error("Error opening shop", e);
            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Error opening shop: " + e.getMessage()));
            return 0;
        }
    }

    private static int showBalance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            int money = ShopManager.getInstance().getPlayerMoney(player);
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.balance", money));
            return 1;
        } catch (Exception e) {
            shop.pomelo.mod.PomelosShopMod.LOGGER.error("Error showing balance", e);
            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int addMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        
        ShopManager.getInstance().addMoney(target, amount);
        int newBalance = ShopManager.getInstance().getPlayerMoney(target);
        
        context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.money_added", 
            target.getDisplayName(), amount, newBalance), true);
        
        return 1;
    }

    private static int setMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        
        ShopManager.getInstance().setPlayerMoney(target, amount);
        
        context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.money_set", 
            target.getDisplayName(), amount), true);
        
        return 1;
    }
    
    private static int listItem(CommandContext<CommandSourceStack> context, int price, int amount) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack heldItem = player.getMainHandItem();
        
        if (heldItem.isEmpty()) {
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.no_item_in_hand"));
            return 0;
        }
        
        if (heldItem.getCount() < amount) {
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.not_enough_items", amount));
            return 0;
        }

        if (BannedItemsManager.getInstance().isBanned(heldItem)) {
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.item_banned"));
            return 0;
        }
        
        String categoryId = "default";
        
        int sellPrice = price / 2;
        
        String itemId = ShopManager.getInstance().listItem(player, heldItem, price, sellPrice, amount, categoryId);
        
        if (itemId != null) {
            heldItem.shrink(amount);
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.listed",
                heldItem.getHoverName(), amount, price));
            return 1;
        } else {
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.list_failed"));
            return 0;
        }
    }

    private static int reloadShop(CommandContext<CommandSourceStack> context) {
        java.nio.file.Path shopDataPath = context.getSource().getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data").resolve("pomeloshopmod").resolve("shop.json");
        
        ShopManager.getInstance().loadFromFile(shopDataPath);
        context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.reloaded"), true);
        
        return 1;
    }
    
    private static int clearShop(CommandContext<CommandSourceStack> context) {
        ShopManager.getInstance().clearAllItems(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.shop_cleared"), true);
        
        return 1;
    }

    private static int banItem(CommandContext<CommandSourceStack> context) {
        net.minecraft.resources.ResourceLocation itemId = context.getArgument("item", net.minecraft.resources.ResourceLocation.class);
        String idStr = itemId.toString();

        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemId);
        if (item == net.minecraft.world.item.Items.AIR) {
            context.getSource().sendFailure(Component.translatable("shop.pomeloshopmod.ban_invalid_item", idStr));
            return 0;
        }

        BannedItemsManager manager = BannedItemsManager.getInstance();
        if (manager.isBanned(idStr)) {
            context.getSource().sendFailure(Component.translatable("shop.pomeloshopmod.ban_already_banned", idStr));
            return 0;
        }

        manager.banItem(idStr);
        java.nio.file.Path bannedItemsPath = context.getSource().getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data").resolve("pomeloshopmod").resolve("banned_items.json");
        manager.saveToFile(bannedItemsPath);

        String displayName = item.getName(new ItemStack(item)).getString();
        context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.ban_success", displayName, idStr), true);

        ShopManager.getInstance().removeBannedItems(context.getSource().getServer(), manager);

        return 1;
    }

    private static int unbanItem(CommandContext<CommandSourceStack> context) {
        net.minecraft.resources.ResourceLocation itemId = context.getArgument("item", net.minecraft.resources.ResourceLocation.class);
        String idStr = itemId.toString();

        BannedItemsManager manager = BannedItemsManager.getInstance();
        if (!manager.isBanned(idStr)) {
            context.getSource().sendFailure(Component.translatable("shop.pomeloshopmod.unban_not_banned", idStr));
            return 0;
        }

        manager.unbanItem(idStr);
        java.nio.file.Path bannedItemsPath = context.getSource().getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data").resolve("pomeloshopmod").resolve("banned_items.json");
        manager.saveToFile(bannedItemsPath);

        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemId);
        String displayName = item != net.minecraft.world.item.Items.AIR
            ? item.getName(new ItemStack(item)).getString()
            : idStr;
        context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.unban_success", displayName, idStr), true);

        return 1;
    }

    private static int banlist(CommandContext<CommandSourceStack> context) {
        BannedItemsManager manager = BannedItemsManager.getInstance();
        java.util.Set<String> bannedIds = manager.getBannedItemIds();

        if (bannedIds.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.banlist_empty"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("shop.pomeloshopmod.banlist_header", bannedIds.size()), false);
            for (String id : bannedIds) {
                net.minecraft.resources.ResourceLocation loc = net.minecraft.resources.ResourceLocation.parse(id);
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(loc);
                String displayName = item != net.minecraft.world.item.Items.AIR
                    ? item.getName(new ItemStack(item)).getString()
                    : id;
                context.getSource().sendSuccess(() -> Component.literal("  - " + displayName + " (" + id + ")"), false);
            }
        }

        return 1;
    }
    
    private static int createCategory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String categoryId = StringArgumentType.getString(context, "id");
        String categoryName = StringArgumentType.getString(context, "name");
        
        CategoryManager.getInstance().addCategory(
            categoryId,
            categoryName,
            player.getUUID(),
            player.getName().getString()
        );
        
        java.util.List<shop.pomelo.mod.shop.ShopCategory> categories = 
            new java.util.ArrayList<>(CategoryManager.getInstance().getCategories());
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
            player, 
            new shop.pomelo.mod.network.SyncCategoriesPacket(categories)
        );
        
        player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.category_created", categoryName));
        return 1;
    }
    
    private static int listCategories(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        
        java.util.List<shop.pomelo.mod.shop.ShopCategory> categories = CategoryManager.getInstance().getCategories();
        
        if (categories.isEmpty()) {
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.no_categories"));
        } else {
            player.sendSystemMessage(Component.translatable("shop.pomeloshopmod.category_list_header"));
            for (shop.pomelo.mod.shop.ShopCategory category : categories) {
                player.sendSystemMessage(Component.literal("  - " + category.getDisplayName() + " (" + category.getId() + ")"));
            }
        }
        
        return 1;
    }
}
