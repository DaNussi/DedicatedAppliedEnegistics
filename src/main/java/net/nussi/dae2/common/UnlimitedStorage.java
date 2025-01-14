package net.nussi.dae2.common;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnlimitedStorage implements MEStorage, Storeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<AEKey, Long> inventory = new HashMap<>();
    private String description;

    public UnlimitedStorage(String description) {
        this.description = description;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return false;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mode.isSimulate()) return amount;

        if (!inventory.containsKey(what)) inventory.put(what, 0L);

        long currentAmount = inventory.get(what);
        long insertedAmount = currentAmount + amount;
        inventory.put(what, insertedAmount);

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(!inventory.containsKey(what)) return 0;

        long currentAmount = inventory.get(what);
        long extractAmount = amount;
        if (currentAmount < extractAmount) extractAmount = currentAmount;
        if (mode.isSimulate()) return extractAmount;

        long newAmount = currentAmount - extractAmount;
        if (newAmount == 0) inventory.remove(what);
        else inventory.put(what, newAmount);

        return extractAmount;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (Map.Entry<AEKey, Long> entry : inventory.entrySet()) {
            out.add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Component getDescription() {
        return Component.literal(description);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag inventory = tag.getCompound("inventory");
        int size = tag.getInt("size");


        this.inventory.clear();
        for (int index = 0; index < size; index++) {
            CompoundTag item = inventory.getCompound(String.valueOf(index));
            AEKey key = AEKey.fromTagGeneric(registries, item.getCompound("key"));
            long amount = item.getLong("amount");
            this.inventory.put(key, amount);
            LOGGER.info("Loading item {} with amount {}", key.getDisplayName().getString(64), amount);
        }


        this.description = tag.getString("description");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag inventory = new CompoundTag();

        List<Map.Entry<AEKey, Long>> inventoryEntrySet = this.inventory.entrySet().stream().toList();

        for (int index = 0; index < inventoryEntrySet.size(); index++) {
            Map.Entry<AEKey, Long> entry = inventoryEntrySet.get(index);
            CompoundTag item = new CompoundTag();
            item.put("key", entry.getKey().toTagGeneric(registries));
            item.putLong("amount", entry.getValue());
            inventory.put(String.valueOf(index), item);
            LOGGER.info("Saving item {} with amount {}", entry.getKey().getDisplayName().getString(64), entry.getValue());
        }

        tag.put("inventory", inventory);
        tag.putInt("size", inventory.size());
        tag.putString("description", this.description);
    }

}
