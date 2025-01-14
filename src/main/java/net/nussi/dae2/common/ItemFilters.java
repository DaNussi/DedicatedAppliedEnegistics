package net.nussi.dae2.common;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class ItemFilters {
    /**
     * Filters for items handled by {@link StorageCells}
     */
    public static IAEItemFilter STORAGE_CELL_FILTER = new IAEItemFilter() {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if(stack.isEmpty()) return false;
            if(stack.getCount() != 1) return false;
            return StorageCells.isCellHandled(stack);
        }
    };
}
