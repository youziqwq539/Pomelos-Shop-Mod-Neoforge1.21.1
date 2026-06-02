package shop.pomelo.mod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.config.ModConfig;
import shop.pomelo.mod.command.ShopCommand;
import shop.pomelo.mod.gui.ShopMenu;
import shop.pomelo.mod.network.CreateCategoryPacket;
import shop.pomelo.mod.network.DeleteCategoryPacket;
import shop.pomelo.mod.network.DeleteItemPacket;
import shop.pomelo.mod.network.ListItemPacket;
import shop.pomelo.mod.network.NetworkHandler;
import shop.pomelo.mod.network.OpenShopPacket;
import shop.pomelo.mod.network.ShopActionPacket;
import shop.pomelo.mod.network.SyncCategoriesPacket;
import shop.pomelo.mod.network.SyncMoneyPacket;
import shop.pomelo.mod.network.SyncShopItemsPacket;
import shop.pomelo.mod.shop.BannedItemsManager;
import shop.pomelo.mod.shop.ShopManager;

@Mod(PomelosShopMod.MODID)
public class PomelosShopMod {
    public static final String MODID = "pomeloshopmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<ShopMenu>> SHOP_MENU = MENUS.register("shop", 
        () -> new MenuType<ShopMenu>(ShopMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public PomelosShopMod(IEventBus modEventBus, ModContainer modContainer) {
        MENUS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(ShopConfig::onLoad);
        modContainer.registerConfig(ModConfig.Type.SERVER, ShopConfig.SPEC);
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Pomelo's Shop Mod starting...");
        
        java.nio.file.Path dataDir = event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data").resolve("pomeloshopmod");
        
        java.nio.file.Path shopDataPath = dataDir.resolve("shop.json");
        ShopManager.getInstance().setDataPath(shopDataPath);
        ShopManager.getInstance().loadFromFile(shopDataPath);
        ShopManager.getInstance().startAutoSave();
        LOGGER.info("Shop data loaded and auto-save started");
        
        shop.pomelo.mod.shop.CategoryManager.initialize(event.getServer());
        LOGGER.info("Category manager initialized");

        java.nio.file.Path bannedItemsPath = dataDir.resolve("banned_items.json");
        BannedItemsManager.getInstance().loadFromFile(bannedItemsPath);
        LOGGER.info("Banned items manager initialized");
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Pomelo's Shop Mod stopping...");
        ShopManager.getInstance().stopAutoSave();

        java.nio.file.Path shopDataPath = event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data").resolve("pomeloshopmod").resolve("shop.json");
        ShopManager.getInstance().saveToFile(shopDataPath);
        LOGGER.info("Shop data saved");

        shop.pomelo.mod.shop.CategoryManager.getInstance().flushSave();
        LOGGER.info("Categories saved");
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ShopCommand.register(event.getDispatcher());
        LOGGER.info("Shop commands registered");
    }
    
    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0.0");
        
        registrar.playToServer(ShopActionPacket.TYPE, ShopActionPacket.STREAM_CODEC, 
            NetworkHandler::handleShopAction);
        registrar.playToClient(SyncMoneyPacket.TYPE, SyncMoneyPacket.STREAM_CODEC, 
            NetworkHandler::handleSyncMoney);
        registrar.playToServer(ListItemPacket.TYPE, ListItemPacket.STREAM_CODEC,
            NetworkHandler::handleListItem);
        registrar.playToServer(CreateCategoryPacket.TYPE, CreateCategoryPacket.STREAM_CODEC,
            NetworkHandler::handleCreateCategory);
        registrar.playToClient(SyncShopItemsPacket.TYPE, SyncShopItemsPacket.STREAM_CODEC,
            NetworkHandler::handleSyncShopItems);
        registrar.playToClient(SyncCategoriesPacket.TYPE, SyncCategoriesPacket.STREAM_CODEC,
            NetworkHandler::handleSyncCategories);
        registrar.playToServer(DeleteItemPacket.TYPE, DeleteItemPacket.STREAM_CODEC,
            NetworkHandler::handleDeleteItem);
        registrar.playToServer(DeleteCategoryPacket.TYPE, DeleteCategoryPacket.STREAM_CODEC,
            NetworkHandler::handleDeleteCategory);
        registrar.playToServer(OpenShopPacket.TYPE, OpenShopPacket.STREAM_CODEC,
            NetworkHandler::handleOpenShop);

        LOGGER.info("Network packets registered");
    }
}
