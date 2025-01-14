package net.nussi.dae2.common;

import appeng.api.storage.StorageCells;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

public class SimpleCellContainer extends SimpleContainer implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        CompoundTag rootTag = new CompoundTag();

        var items = this.getItems();
        int size = items.size();

        rootTag.putInt("Size", size);

        for (int i = 0; i < size; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                Tag itemCompoundTag = stack.save(provider);
                rootTag.put("Item" + i, itemCompoundTag);
            }
        }

        return rootTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag rootTag) {
        int size = rootTag.getInt("Size");

        for (int i = 0; i < size; i++) {
            if (rootTag.contains("Item" + i)) {
                Tag itemCompoundTag = rootTag.get("Item" + i);
                if( itemCompoundTag == null) {
                    LOGGER.error("Failed to get item stack NBT.");
                    continue;
                }
                var optionalItemStack = ItemStack.parse(provider, itemCompoundTag);
                if(optionalItemStack.isEmpty()) {
                    LOGGER.error("Failed to parse item stack from NBT.");
                    continue;
                }
                ItemStack itemStack = optionalItemStack.get();
                this.setItem(i, itemStack);
            }
        }

    }
}
