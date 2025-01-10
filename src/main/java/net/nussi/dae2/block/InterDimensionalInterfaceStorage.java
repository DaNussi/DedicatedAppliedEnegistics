package net.nussi.dae2.block;

import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.nussi.dae2.common.Storeable;
import net.nussi.dae2.common.UnlimitedStorage;

public class InterDimensionalInterfaceStorage implements IStorageProvider, Storeable {
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
}
