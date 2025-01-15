package net.nussi.dae2.common;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.network.chat.Component;

public class ProxyStorageCell implements StorageCell {
    private StorageCell targetStorage;

    public ProxyStorageCell() {
        this.targetStorage = null;
    }

    public ProxyStorageCell(StorageCell targetStorage) {
        this.targetStorage = targetStorage;
    }

    public void setTargetStorage(StorageCell targetStorage) {
        this.targetStorage = targetStorage;
    }

    public void clearTargetStorage() {
        this.targetStorage = null;
    }

    @Override
    public CellState getStatus() {
        if (targetStorage == null)
            return CellState.ABSENT;
        else
            return targetStorage.getStatus();
    }

    @Override
    public double getIdleDrain() {
        if (targetStorage == null)
            return 0;
        else
            return targetStorage.getIdleDrain();
    }

    @Override
    public boolean canFitInsideCell() {
        if (targetStorage == null)
            return StorageCell.super.canFitInsideCell();
        else
            return targetStorage.canFitInsideCell();
    }

    @Override
    public void persist() {
        if(targetStorage != null)
            targetStorage.persist();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (targetStorage == null)
            return StorageCell.super.isPreferredStorageFor(what, source);
        else
            return targetStorage.isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (targetStorage == null)
            return StorageCell.super.insert(what, amount, mode, source);
        else
            return targetStorage.insert(what, amount, mode, source);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (targetStorage == null)
            return StorageCell.super.extract(what, amount, mode, source);
        else
            return targetStorage.extract(what, amount, mode, source);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (targetStorage == null)
            StorageCell.super.getAvailableStacks(out);
        else
            targetStorage.getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        if (targetStorage == null)
            return Component.empty();
        else
            return targetStorage.getDescription();
    }

    @Override
    public KeyCounter getAvailableStacks() {
        if (targetStorage == null)
            return StorageCell.super.getAvailableStacks();
        else
            return targetStorage.getAvailableStacks();
    }
}
