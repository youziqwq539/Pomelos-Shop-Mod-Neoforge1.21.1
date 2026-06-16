package shop.pomelo.mod.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import shop.pomelo.mod.gui.WalletMenu;

public class WalletItem extends Item {
    private static final int WALLET_SLOTS = 5;

    public WalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 从钱包物品读取内容
            ItemContainerContents contents = itemStack.get(DataComponents.CONTAINER);
            SimpleContainer walletContainer = new SimpleContainer(WALLET_SLOTS);
            
            // 加载已存储的物品
            if (contents != null) {
                // 使用 NonNullList 接收 copyInto
                NonNullList<ItemStack> items = NonNullList.withSize(WALLET_SLOTS, ItemStack.EMPTY);
                contents.copyInto(items);
                for (int i = 0; i < WALLET_SLOTS; i++) {
                    walletContainer.setItem(i, items.get(i));
                }
            }

            // 打开钱包界面
            player.openMenu(new net.minecraft.world.MenuProvider() {
                @Override
                public net.minecraft.network.chat.Component getDisplayName() {
                    return net.minecraft.network.chat.Component.translatable("item.pomeloshopmod.wallet");
                }

                @Override
                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                        int containerId, net.minecraft.world.entity.player.Inventory playerInventory,
                        net.minecraft.world.entity.player.Player player) {
                    return new WalletMenu(containerId, playerInventory, walletContainer, itemStack);
                }
            });
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }
}