package net.nussi.dae2.block;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class InterDimensionalInterfaceStorage implements MEStorage {
    private final Map<AEKey, Long> inventory = new HashMap<>();

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
        return Component.translatable("block.dae2.inter_dimensional_interface");
    }
}
