package shop.pomelo.mod.gui;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import shop.pomelo.mod.PomelosShopMod;

public class ShopMenu extends AbstractContainerMenu {
    private final Player player;

    public ShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(0));
    }

    public ShopMenu(int containerId, Inventory playerInventory, Container container) {
        super(PomelosShopMod.SHOP_MENU.get(), containerId);
        this.player = playerInventory.player;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Player getPlayer() {
        return player;
    }
}
