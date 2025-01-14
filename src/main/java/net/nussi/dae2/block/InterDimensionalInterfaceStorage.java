package net.nussi.dae2.block;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.NetworkStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.nussi.dae2.common.ExtendedNetworkStorage;
import net.nussi.dae2.common.ProxyStorage;
import net.nussi.dae2.common.SimpleCellContainer;
import net.nussi.dae2.common.Storeable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class InterDimensionalInterfaceStorage implements IStorageProvider, Storeable, ContainerListener, Tickable, IActionHost {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final InterDimensionalInterfaceBlockEntity blockEntity;
    private final ProxyStorage importBufferInventory;
    private final ProxyStorage exportBufferInventory;
    private final SimpleCellContainer cellInventory;

    public InterDimensionalInterfaceStorage(InterDimensionalInterfaceBlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.importBufferInventory = new ProxyStorage();
        this.exportBufferInventory = new ProxyStorage();

        this.cellInventory = new SimpleCellContainer(2);
        this.cellInventory.addListener(this);
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
//        storageMounts.mount(importBufferInventory);
        storageMounts.mount(exportBufferInventory);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag cellInventoryTag = tag.getCompound("CellInventory");
        cellInventory.deserializeNBT(registries, cellInventoryTag);

        containerChanged(cellInventory);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag cellInventoryTag = cellInventory.serializeNBT(registries);
        tag.put("CellInventory", cellInventoryTag);
    }

    public Container getCellInventory() {
        return cellInventory;
    }

    public MEStorage getImportBufferInventory() {
        return importBufferInventory;
    }

    public MEStorage getExportBufferInventory() {
        return exportBufferInventory;
    }

    @Override
    public void containerChanged(Container container) {
        if (container == cellInventory) {
            ItemStack importItemStack = cellInventory.getItem(0);

            if (importItemStack.isEmpty()) {
                importBufferInventory.clearTargetStorage();
            } else {
                StorageCell cell = StorageCells.getCellInventory(importItemStack, null);
                importBufferInventory.setTargetStorage(cell);
            }

            ItemStack exportItemStack = cellInventory.getItem(1);

            if (exportItemStack.isEmpty()) {
                exportBufferInventory.clearTargetStorage();
            } else {
                StorageCell cell = StorageCells.getCellInventory(exportItemStack, null);
                exportBufferInventory.setTargetStorage(cell);
            }

        }
    }

    @Override
    public void tick() {
        if (!blockEntity.getMainNode().isActive()) {
            LOGGER.warn("InterDimensionalInterfaceBlockEntity is not active. Skipping item transfer.");
            return;
        }

        var exportItem = exportBufferInventory.getAvailableStacks().getFirstEntry();
        if (exportItem != null) {
            try {
                // TODO: Implement item transfer over network
                long amount = transferItems(exportBufferInventory, importBufferInventory, exportItem.getKey(), exportItem.getLongValue(), false, "ExportBuffer", "ImportBuffer");
            } catch (Exception e) {
                LOGGER.error("Failed to transfer items.", e);
            }

        }


        var importItem = importBufferInventory.getAvailableStacks().getFirstEntry();
        if (importItem != null) {
            IGrid grid = blockEntity.getMainNode().getGrid();
            if (grid == null) {
                LOGGER.error("Main node grid is null.");
                return;
            }
            IStorageService storageService = grid.getService(IStorageService.class);
            MEStorage baseStorage = storageService.getInventory();
            if (baseStorage instanceof NetworkStorage networkStorage) {
                ExtendedNetworkStorage extendedNetworkStorage = new ExtendedNetworkStorage(networkStorage);
                try {
                    long amount = transferItems(importBufferInventory, extendedNetworkStorage, importItem.getKey(), importItem.getLongValue(), false, "ImportBuffer", "NetworkStorage");
                } catch (Exception e) {
                    LOGGER.error("Failed to transfer items.", e);
                }

            } else {
                LOGGER.error("Grid inventory is not of type NetworkStorage. Maybe there is a version mismatch.");
            }
        }
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return blockEntity.getActionableNode();
    }

    /**
     * Transfer items from one storage to another.
     * @param from source MEStorage
     * @param to target MEStorage
     * @param what to transfer
     * @param amount the amount of items to transfer.
     * @return the amount of items that have been transferred successfully.
     * @throws Exception if simulation failes in exact mode or if the transfer fails.
     */
    public long transferItems(MEStorage from, MEStorage to, AEKey what, long amount) throws Exception {
        return transferItems(from, to, what, amount, false);
    }

    /**
     * Transfer items from one storage to another.
     * @param from source MEStorage
     * @param to target MEStorage
     * @param what to transfer
     * @param amount the amount of items to transfer.
     * @param exact if true, the transfer will fail if the exact amount of items cannot be transferred.
     * @return the amount of items that have been transferred successfully.
     * @throws Exception if simulation failes in exact mode or if the transfer fails.
     */
    public long transferItems(MEStorage from, MEStorage to, AEKey what, long amount, boolean exact) throws Exception {
        return transferItems(from, to, what, amount, exact, from.getDescription().getString(64), to.getDescription().getString(64));

    }

    /**
     * Transfer items from one storage to another.
     * @param from source MEStorage
     * @param to target MEStorage
     * @param what to transfer
     * @param amount the amount of items to transfer.
     * @param exact if true, the transfer will fail if the exact amount of items cannot be transferred.
     * @param fromName the name to use in log messages.
     * @param toName the name to use in log messages.
     * @return the amount of items that have been transferred successfully.
     * @throws Exception if simulation failes in exact mode or if the transfer fails.
     */
    public long transferItems(MEStorage from, MEStorage to, AEKey what, long amount, boolean exact, String fromName, String toName) throws Exception {
        MachineSource source = new MachineSource(this);
        String whatName = what.getDisplayName().getString(64);

        long extractAmount = from.extract(what, amount, Actionable.SIMULATE, source);

        long importAmount = to.insert(what, extractAmount, Actionable.SIMULATE, source);

        if (importAmount != extractAmount && exact)
            throw new Exception("Failed to transfer " + whatName + " from " + fromName + " to " + toName + " in exact mode. Simulated extraction of " + extractAmount + " items and import of " + importAmount + " items.");

        long finalExtractAmount = from.extract(what, importAmount, Actionable.MODULATE, source);

        long finalImportAmount = to.insert(what, finalExtractAmount, Actionable.MODULATE, source);

        long difference = finalExtractAmount - finalImportAmount;

        LOGGER.info("Transferred {} {} from {} to {}.", finalImportAmount, whatName, fromName, toName);

        if (difference > 0) {
            LOGGER.warn("Failed to transfer {} {} from {} to {}. Trying to transfer remaining {} items back to source.", amount, whatName, fromName, toName, difference);

            long fixAmount = from.insert(what, difference, Actionable.MODULATE, source);

            long remainingAmount = difference - fixAmount;

            if (remainingAmount > 0) {
                LOGGER.error("Failed to transfer {} {} from {} to {}. Tried to fix issue by transferring {} items back to source. Managed to fix {} items. Remaining {} items have been lost.", amount, whatName, fromName, toName, difference, fixAmount, remainingAmount);
                throw new Exception("Failed to transfer " + whatName + " from " + fromName + " to " + toName + ". Tried to fix issue by transferring " + difference + " items back to source. Managed to fix " + fixAmount + " items. Remaining " + remainingAmount + " items have been lost.");
            }
        }

        return finalImportAmount;
    }
}
