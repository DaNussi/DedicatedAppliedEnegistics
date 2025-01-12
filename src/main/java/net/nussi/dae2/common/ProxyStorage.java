package net.nussi.dae2.common;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;

public class ProxyStorage implements MEStorage {
    private MEStorage targetStorage;

    public ProxyStorage() {
        this.targetStorage = null;
    }

    public ProxyStorage(MEStorage targetStorage) {
        this.targetStorage = targetStorage;
    }

    public void setTargetStorage(MEStorage targetStorage) {
        this.targetStorage = targetStorage;
    }

    public void clearTargetStorage() {
        this.targetStorage = null;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (targetStorage == null) return false;
        return targetStorage.isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if ( targetStorage == null) return 0;
        return targetStorage.insert(what, amount, mode, source);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if ( targetStorage == null) return 0;
        return targetStorage.extract(what, amount, mode, source);
    }

    @Override
    public KeyCounter getAvailableStacks() {
        if ( targetStorage == null) return new KeyCounter();
        return targetStorage.getAvailableStacks();
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if( targetStorage == null) return;
        targetStorage.getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        if ( targetStorage == null) return Component.empty();
        return targetStorage.getDescription();
    }

}
