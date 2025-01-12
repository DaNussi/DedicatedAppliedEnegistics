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
import appeng.me.Grid;
import appeng.me.helpers.MachineSource;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.item.ItemStack;
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
        if(container == cellInventory) {
            ItemStack importItemStack = cellInventory.getItem(0);

            if(importItemStack.isEmpty()) {
                importBufferInventory.clearTargetStorage();
            } else {
                StorageCell cell = StorageCells.getCellInventory(importItemStack, null);
                importBufferInventory.setTargetStorage(cell);
            }

            ItemStack exportItemStack = cellInventory.getItem(1);

            if(exportItemStack.isEmpty()) {
                exportBufferInventory.clearTargetStorage();
            } else {
                StorageCell cell = StorageCells.getCellInventory(exportItemStack, null);
                exportBufferInventory.setTargetStorage(cell);
            }

        }
    }

    @Override
    public void tick() {
        AEKey exportItem = exportBufferInventory.getAvailableStacks().getFirstKey();
        if (exportItem != null) {
            boolean successTempTransger = transferItems(exportBufferInventory, importBufferInventory, exportItem, 1);
            if (!successTempTransger) LOGGER.error("Failed to transfer items from export buffer to import buffer. Items lost: " + exportItem);
        }



        AEKey importItem = importBufferInventory.getAvailableStacks().getFirstKey();
        if (importItem != null) {
            if(!blockEntity.getMainNode().isActive()) {
                LOGGER.warn("InterDimensionalInterfaceBlockEntity is not active. Skipping item transfer.");
                return;
            }

            if(!blockEntity.getMainNode().isReady()) {
                LOGGER.warn("InterDimensionalInterfaceBlockEntity is not ready. Skipping item transfer.");
                return;
            }

            if(!blockEntity.getMainNode().isOnline()) {
                LOGGER.warn("InterDimensionalInterfaceBlockEntity is not online. Skipping item transfer.");
                return;
            }

            if(!blockEntity.getMainNode().isPowered()) {
                LOGGER.warn("InterDimensionalInterfaceBlockEntity is not powered. Skipping item transfer.");
                return;
            }

            IGrid grid = blockEntity.getMainNode().getGrid();
            if(grid == null) {
                LOGGER.warn("No grid found for InterDimensionalInterfaceBlockEntity. Skipping item transfer.");
                return;
            }
            IStorageService storageService = grid.getService(IStorageService.class);
            MEStorage networkStorage = storageService.getInventory();
            boolean success = transferItems(importBufferInventory, networkStorage, importItem, 1);
            if (!success) LOGGER.error("Failed to transfer items from import buffer to network. Items lost: " + importItem);
        }

    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return blockEntity.getActionableNode();
    }

    public boolean transferItems(MEStorage from, MEStorage to, AEKey what, long amount) {

        long extractAmount = from.extract(what, amount, Actionable.SIMULATE, new MachineSource(this));

        long importAmount = to.insert(what, extractAmount, Actionable.SIMULATE, new MachineSource(this));

        long finalExtractAmount = from.extract(what, importAmount, Actionable.MODULATE, new MachineSource(this));

        long finalImportAmount = to.insert(what, finalExtractAmount, Actionable.MODULATE, new MachineSource(this));

        return finalImportAmount == finalExtractAmount;
    }
}
