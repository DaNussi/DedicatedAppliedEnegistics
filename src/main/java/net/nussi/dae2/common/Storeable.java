package net.nussi.dae2.common;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Storeable {
    void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) ;
    void saveAdditional(CompoundTag tag, HolderLookup.Provider registries);
}
