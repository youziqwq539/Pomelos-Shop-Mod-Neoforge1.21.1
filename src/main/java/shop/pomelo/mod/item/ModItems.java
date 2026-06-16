package shop.pomelo.mod.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import shop.pomelo.mod.PomelosShopMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, PomelosShopMod.MODID);
    
    public static final DeferredHolder<Item, Item> WALLET = ITEMS.register("wallet", 
        () -> new WalletItem(new Item.Properties().stacksTo(1)));
    
    // 方块币 - 最大堆叠100，与商店钱币系统同步
    public static final DeferredHolder<Item, Item> COIN = ITEMS.register("coin",
        () -> new CoinItem(new Item.Properties()));
    
    // 十纸币 - 最大堆叠99，每个算10个方块币，与商店钱币系统同步
    public static final DeferredHolder<Item, Item> PAPER_MONEY = ITEMS.register("paper_money",
        () -> new PaperMoneyItem(new Item.Properties()));
    
    // 50纸币 - 最大堆叠99，每个算50个方块币，与商店钱币系统同步
    public static final DeferredHolder<Item, Item> FIFTY_PAPER_MONEY = ITEMS.register("fifty_paper_money",
        () -> new MoneyItem(new Item.Properties(), 50));
    
    // 100纸币 - 最大堆叠99，每个算100个方块币，与商店钱币系统同步
    public static final DeferredHolder<Item, Item> HUNDRED_PAPER_MONEY = ITEMS.register("hundred_paper_money",
        () -> new MoneyItem(new Item.Properties(), 100));
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}