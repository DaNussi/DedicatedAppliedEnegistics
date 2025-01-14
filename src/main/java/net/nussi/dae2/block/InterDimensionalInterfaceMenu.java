package net.nussi.dae2.block;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nussi.dae2.Register;
import org.slf4j.Logger;

public class InterDimensionalInterfaceMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ContainerLevelAccess containerLevelAccess;
    private final InterDimensionalInterfaceBlockEntity blockEntity;

    public InterDimensionalInterfaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, Minecraft.getInstance().level.getBlockEntity(extraData.readBlockPos()));
    }

    protected InterDimensionalInterfaceMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_MENU.get(), containerId);

        if(blockEntity instanceof InterDimensionalInterfaceBlockEntity convertedBlockEntity) {
            this.blockEntity = convertedBlockEntity;

            this.containerLevelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

            createPlayerHotbar(playerInventory);
            createPlayerInventory(playerInventory);
            createBlockInventory(convertedBlockEntity);

        } else {
            throw new IllegalArgumentException("BlockEntity is not an instance of InterDimensionalInterfaceBlockEntity");
        }
    }


    private void createBlockInventory(InterDimensionalInterfaceBlockEntity blockEntity) {
        addSlot(new Slot(blockEntity.getStorage().getCellInventory(), 0, 100, 50));
        addSlot(new Slot(blockEntity.getStorage().getCellInventory(), 1, 50, 50));
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
            if(!moveItemStackTo(fromStack, 36, 37, false))
                return ItemStack.EMPTY;
        } else if (index < 37) {
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
