package shop.pomelo.mod.gui;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;
import shop.pomelo.mod.PomelosShopMod;
import shop.pomelo.mod.item.ModItems;

public class WalletMenu extends AbstractContainerMenu {
    private static final int WALLET_SLOTS = 5;
    private static final int PLAYER_INV_START = WALLET_SLOTS;
    private static final int HOTBAR_START = PLAYER_INV_START + 27;
    private static final int TOTAL_SLOTS = HOTBAR_START + 9;

    // 钱包槽位坐标（左侧竖排，y轴整体居中：总高度166，槽位块114px，(166-114)/2=26）
    private static final int[] WALLET_SLOT_X = {44, 44, 44, 44, 44};
    private static final int[] WALLET_SLOT_Y = {26, 50, 74, 98, 122};

    private final SimpleContainer walletContainer;
    private final ItemStack walletItem;

    public WalletMenu(int containerId, Inventory playerInventory) {
        this(PomelosShopMod.WALLET_MENU.get(), containerId, playerInventory);
    }

    public WalletMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory) {
        super(menuType, containerId);

        this.walletItem = ItemStack.EMPTY;
        this.walletContainer = new SimpleContainer(WALLET_SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                WalletMenu.this.slotsChanged(this);
            }
        };

        // 钱包插槽（5个）- 左侧竖排
        for (int i = 0; i < WALLET_SLOTS; i++) {
            this.addSlot(new Slot(walletContainer, i, WALLET_SLOT_X[i], WALLET_SLOT_Y[i]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // 阻止钱包放入钱包槽位
                    return stack.getItem() != ModItems.WALLET.get();
                }
            });
        }

        // 玩家物品栏（27个）- 右侧区域
        int invStartX = 118;
        int invStartY = 66;
        int slotSize = 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    invStartX + col * slotSize, invStartY + row * slotSize));
            }
        }

        // 快捷栏（9个）
        int hotbarStartY = 122;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                invStartX + col * slotSize, hotbarStartY));
        }
    }

    /**
     * 带钱包物品的构造函数，用于持久化存储
     */
    public WalletMenu(int containerId, Inventory playerInventory, SimpleContainer walletContainer, ItemStack walletItem) {
        super(PomelosShopMod.WALLET_MENU.get(), containerId);
        this.walletContainer = walletContainer;
        this.walletItem = walletItem;

        // 钱包插槽（5个）- 左侧竖排
        for (int i = 0; i < WALLET_SLOTS; i++) {
            this.addSlot(new Slot(walletContainer, i, WALLET_SLOT_X[i], WALLET_SLOT_Y[i]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // 阻止钱包放入钱包槽位
                    return stack.getItem() != ModItems.WALLET.get();
                }
            });
        }

        // 玩家物品栏（27个）- 右侧区域
        int invStartX = 118;
        int invStartY = 66;
        int slotSize = 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    invStartX + col * slotSize, invStartY + row * slotSize));
            }
        }

        // 快捷栏（9个）
        int hotbarStartY = 122;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                invStartX + col * slotSize, hotbarStartY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemStack = slotItem.copy();

            if (slotIndex < WALLET_SLOTS) {
                // 从钱包槽位移到玩家物品栏
                if (!this.moveItemStackTo(slotItem, PLAYER_INV_START, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家物品栏移到钱包槽位
                if (!this.moveItemStackTo(slotItem, 0, WALLET_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * 关闭界面时保存钱包内容到物品
     */
    @Override
    public void removed(Player player) {
        super.removed(player);
        
        // 如果有钱包物品，保存内容
        if (!walletItem.isEmpty()) {
            // 从容器创建 ItemContainerContents
            NonNullList<ItemStack> items = NonNullList.withSize(WALLET_SLOTS, ItemStack.EMPTY);
            for (int i = 0; i < WALLET_SLOTS; i++) {
                items.set(i, walletContainer.getItem(i));
            }
            ItemContainerContents contents = ItemContainerContents.fromItems(items);
            walletItem.set(DataComponents.CONTAINER, contents);
        }
    }

    public SimpleContainer getWalletContainer() {
        return walletContainer;
    }

    public ItemStack getWalletItem() {
        return walletItem;
    }
}