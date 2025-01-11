package net.nussi.dae2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.nussi.dae2.Register;

public class InterDimensionalInterfaceMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess containerLevelAccess;
    private final InterDimensionalInterfaceBlockEntity blockEntity;

    public InterDimensionalInterfaceMenu(int containerId, Inventory playerInventory) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_MENU.get(), containerId);

        this.containerLevelAccess = null;
        this.blockEntity = null;

        createPlayerHotbar(playerInventory);
        createPlayerInventory(playerInventory);
    }

    protected InterDimensionalInterfaceMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_MENU.get(), containerId);

        if(blockEntity instanceof InterDimensionalInterfaceBlockEntity) {
            this.blockEntity = (InterDimensionalInterfaceBlockEntity) blockEntity;
        } else {
            throw new IllegalArgumentException("BlockEntity is not an instance of InterDimensionalInterfaceBlockEntity");
        }

        this.containerLevelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        createPlayerHotbar(playerInventory);
        createPlayerInventory(playerInventory);
    }

    private void createPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInv,
                        9 + column + (row * 9),
                        8 + (column * 18),
                        84 + (row * 18)));
            }
        }
    }

    private void createPlayerHotbar(Inventory playerInv) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInv,
                    column,
                    8 + (column * 18),
                    142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot fromSlot = getSlot(index);
        ItemStack fromStack = fromSlot.getItem();

        if(fromStack.getCount() <= 0)
            fromSlot.set(ItemStack.EMPTY);

        if(!fromSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack copyFromStack = fromStack.copy();

        if(index < 36) {
            // We are inside of the player's inventory
            if(!moveItemStackTo(fromStack, 36, 63, false))
                return ItemStack.EMPTY;
        } else if (index < 63) {
            // We are inside of the block entity inventory
            if(!moveItemStackTo(fromStack, 0, 36, false))
                return ItemStack.EMPTY;
        } else {
            System.err.println("Invalid slot index: " + index);
            return ItemStack.EMPTY;
        }

        fromSlot.setChanged();
        fromSlot.onTake(player, fromStack);

        return copyFromStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(containerLevelAccess, player, Register.INTER_DIMENSIONAL_INTERFACE_BLOCK.get());
    }

}
