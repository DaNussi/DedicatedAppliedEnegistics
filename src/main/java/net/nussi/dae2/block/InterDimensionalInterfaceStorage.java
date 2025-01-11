package net.nussi.dae2.block;

import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.nussi.dae2.common.Storeable;
import net.nussi.dae2.common.UnlimitedStorage;

public class InterDimensionalInterfaceStorage implements IStorageProvider, Storeable, IItemHandler {
    private final UnlimitedStorage buffer = new UnlimitedStorage(Component.translatable("block.dae2.inter_dimensional_interface").getString(64));

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(buffer);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag bufferData = tag.getCompound("buffer");
        buffer.loadAdditional(bufferData, registries);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag bufferData = new CompoundTag();
        buffer.saveAdditional(bufferData, registries);
        tag.put("buffer", bufferData);
    }

    public UnlimitedStorage getBuffer() {
        return buffer;
    }

    @Override
    public int getSlots() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return null;
    }

    @Override
    public ItemStack insertItem(int i, ItemStack itemStack, boolean b) {
        return null;
    }

    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        return null;
    }

    @Override
    public int getSlotLimit(int i) {
        return 0;
    }

    @Override
    public boolean isItemValid(int i, ItemStack itemStack) {
        return false;
    }
}
