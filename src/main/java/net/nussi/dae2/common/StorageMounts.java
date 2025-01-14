package net.nussi.dae2.common;

import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StorageMounts implements IStorageMounts {
    private HashMap<Integer, List<MEStorage>> storageMounts = new HashMap<>();

    public StorageMounts() {
    }

    public StorageMounts(IStorageProvider storageProvider) {
        storageProvider.mountInventories(this);
    }

    @Override
    public void mount(MEStorage inventory, int priority) {
        if(storageMounts.containsKey(priority)) {
            storageMounts.get(priority).add(inventory);
        } else {
            storageMounts.put(priority, List.of(inventory));
        }
    }

    public List<MEStorage> toList() {
        return storageMounts.values().stream().reduce(new LinkedList<>(), (a, b) -> {
            a.addAll(b);
            return a;
        });
    }

    public HashMap<Integer, List<MEStorage>> toHashMap() {
        return storageMounts;
    }

    public static StorageMounts from(IStorageProvider storageProvider) {
        return new StorageMounts(storageProvider);
    }
}
