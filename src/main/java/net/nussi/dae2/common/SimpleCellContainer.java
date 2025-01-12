package net.nussi.dae2.common;

import appeng.api.storage.StorageCells;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.UnknownNullability;

public class SimpleCellContainer extends SimpleContainer implements INBTSerializable<CompoundTag> {

    public SimpleCellContainer(int size) {
        super(size);
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        if(stack.isEmpty()) return false;
        if(stack.getCount() != 1) return false;
        return StorageCells.isCellHandled(stack);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ItemStackHandler itemStackHandler = new ItemStackHandler(this.getItems());
        return itemStackHandler.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        ItemStackHandler itemStackHandler = new ItemStackHandler();
        itemStackHandler.deserializeNBT(provider, compoundTag);

        for (int slotIndex = 0; slotIndex < itemStackHandler.getSlots(); slotIndex++) {
            this.clearContent();
            this.setItem(slotIndex, itemStackHandler.getStackInSlot(slotIndex));
        }
    }
}
