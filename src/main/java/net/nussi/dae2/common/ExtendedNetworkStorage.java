package net.nussi.dae2.common;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import net.minecraft.network.chat.Component;
import net.nussi.dae2.block.InterDimensionalInterfaceStorage;

/**
 * ExtendedNetworkStorage
 * This class is a wrapper for NetworkStorage
 * It ensures that a InterDimensionalInterface cannot transfer items to and from itself
 */
public class ExtendedNetworkStorage implements MEStorage {
    private final NetworkStorage networkStorage;

    public ExtendedNetworkStorage(NetworkStorage networkStorage) {
        this.networkStorage = networkStorage;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return networkStorage.isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(source.machine().isEmpty()) return networkStorage.insert(what, amount, mode, source);
        IActionHost host = source.machine().get();

        if(! (host instanceof InterDimensionalInterfaceStorage)) return networkStorage.insert(what, amount, mode, source);
        InterDimensionalInterfaceStorage blockEntityStorage = (InterDimensionalInterfaceStorage) host;
        StorageMounts storageMounts = new StorageMounts(blockEntityStorage);


        for (MEStorage storage : storageMounts.toList()) {
            networkStorage.unmount(storage);
        }

        long result = networkStorage.insert(what, amount, mode, source);

        for (var entry : storageMounts.toHashMap().entrySet()) {
            int priority = entry.getKey();
            for (MEStorage storage : entry.getValue()) {
                networkStorage.mount(priority, storage);
            }
        }
        return result;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(source.machine().isEmpty()) return networkStorage.extract(what, amount, mode, source);
        IActionHost host = source.machine().get();

        if(! (host instanceof InterDimensionalInterfaceStorage)) return networkStorage.extract(what, amount, mode, source);
        InterDimensionalInterfaceStorage blockEntityStorage = (InterDimensionalInterfaceStorage) host;
        StorageMounts storageMounts = new StorageMounts(blockEntityStorage);


        for (MEStorage storage : storageMounts.toList()) {
            networkStorage.unmount(storage);
        }

        long result = networkStorage.extract(what, amount, mode, source);

        for (var entry : storageMounts.toHashMap().entrySet()) {
            int priority = entry.getKey();
            for (MEStorage storage : entry.getValue()) {
                networkStorage.mount(priority, storage);
            }
        }
        return result;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        networkStorage.getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        return networkStorage.getDescription();
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return networkStorage.getAvailableStacks();
    }


}
