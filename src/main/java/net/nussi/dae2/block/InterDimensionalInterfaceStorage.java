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
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.NetworkStorage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.nussi.dae2.common.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterDimensionalInterfaceStorage implements IStorageProvider, Storeable, ContainerListener, Tickable, IActionHost {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final InterDimensionalInterfaceBlockEntity blockEntity;
    private final ProxyStorageCell importBufferInventory;
    private final ProxyStorageCell exportBufferInventory;
    private final SimpleCellContainer cellInventory;
    private HolderLookup.Provider registries;

    public InterDimensionalInterfaceStorage(InterDimensionalInterfaceBlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.importBufferInventory = new ProxyStorageCell();
        this.exportBufferInventory = new ProxyStorageCell();

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
        this.registries = registries;

        CompoundTag cellInventoryTag = tag.getCompound("CellInventory");
        cellInventory.deserializeNBT(registries, cellInventoryTag);

        containerChanged(cellInventory);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.registries = registries;

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

    public void innerTick() throws Exception {

        if (!blockEntity.getMainNode().isActive()) {
            LOGGER.warn("InterDimensionalInterfaceBlockEntity is not active. Skipping item transfer.");
            return;
        }
        // TODO: Rework functioning code into good code C:

        Channel channel = ConnectionManager.INSTANCE.getChannel();


        String recoverExchangeName = "recovery-" + blockEntity.getNetworkID();
        channel.exchangeDeclare(recoverExchangeName, "fanout", true, false, Map.of());

        String recoveryQueueName = "recovery-" + blockEntity.getNetworkID();
        var recoveryQueueDeclaration = channel.queueDeclare(recoveryQueueName, true, false, false, Map.of());
        recoveryQueueName = recoveryQueueDeclaration.getQueue();
        channel.queueBind(recoveryQueueName, recoverExchangeName, "");

        String importExchangeName = "import-" + blockEntity.getNetworkID();
        Map<String, Object> importExchangeArgs = new HashMap<>();
        importExchangeArgs.put("alternate-exchange", recoverExchangeName);
        channel.exchangeDeclare(importExchangeName, "topic", true, false, importExchangeArgs);

        String importBufferQueueName = "import-buffer-" + blockEntity.getStorageID() + "-" + blockEntity.getNetworkID();
        Map<String, Object> importBufferQueueArgs = new HashMap<>();
        importBufferQueueArgs.put("x-message-ttl", 5000);
        importBufferQueueArgs.put("x-expires", 15000);
        importBufferQueueArgs.put("x-dead-letter-exchange", recoverExchangeName);
        var importBufferQueueDeclaration = channel.queueDeclare(importBufferQueueName, true, false, true, importBufferQueueArgs);
        importBufferQueueName = importBufferQueueDeclaration.getQueue();

        if (List.of(CellState.NOT_EMPTY, CellState.FULL, CellState.TYPES_FULL).contains(exportBufferInventory.getStatus())) {

            try {
                var exportItem = exportBufferInventory.getAvailableStacks().getFirstEntry();

                long extractAmount = exportItem.getLongValue();

                extractAmount = exportBufferInventory.extract(exportItem.getKey(), extractAmount, Actionable.SIMULATE, new MachineSource(this));
                extractAmount = exportBufferInventory.extract(exportItem.getKey(), extractAmount, Actionable.MODULATE, new MachineSource(this));

                String data = itemToString(new AbstractObject2LongMap.BasicEntry<>(exportItem.getKey(), extractAmount));

                channel.basicPublish(importExchangeName, "", new AMQP.BasicProperties(), data.getBytes(StandardCharsets.UTF_8));
                LOGGER.info("Transferred {} {} from export buffer to import exchange.", extractAmount, exportItem.getKey().getDisplayName().getString(64));

            } catch (Exception e) {
                LOGGER.error("Failed to transfer items.", e);
            }
        }


        if (List.of(CellState.EMPTY, CellState.NOT_EMPTY).contains(importBufferInventory.getStatus())) {
            try {
                GetResponse response = channel.basicGet(importBufferQueueName, false);
                if(response == null)
                    response = channel.basicGet(recoveryQueueName, false);

                if (response != null) {


                    long deliveryTag = response.getEnvelope().getDeliveryTag();

                    String data = new String(response.getBody(), StandardCharsets.UTF_8);
                    Object2LongMap.Entry<AEKey> importItem = stringToItem(data);

                    AEKey what = importItem.getKey();

                    long requiredAmount = importItem.getLongValue();
                    long insertedAmount = importBufferInventory.insert(what, requiredAmount, Actionable.SIMULATE, new MachineSource(this));

                    if (insertedAmount != requiredAmount) {
                        LOGGER.warn("Failed to insert items into import buffer because of insufficient space. Re queuing item.");
                        channel.basicNack(deliveryTag, false, true);
                    } else {
                        insertedAmount = importBufferInventory.insert(what, requiredAmount, Actionable.MODULATE, new MachineSource(this));

                        long diff = requiredAmount - insertedAmount;

                        if (diff > 0) {
                            long fixAmount = importBufferInventory.extract(what, insertedAmount, Actionable.MODULATE, new MachineSource(this));

                            long remainingAmount = insertedAmount - fixAmount;

                            LOGGER.error("Failed to insert item into buffer from queue because of insufficient space. {} items have been lost.", remainingAmount);

                            channel.basicNack(deliveryTag, false, false);
                        } else {

                            LOGGER.info("Inserted {} {} into import buffer.", insertedAmount, what.getDisplayName().getString(64));
                            channel.basicAck(deliveryTag, false);
                        }

                    }
                }


            } catch (Exception e) {
                LOGGER.error("Failed to transfer items.", e);
            }
        }


        if (List.of(CellState.NOT_EMPTY, CellState.FULL, CellState.TYPES_FULL).contains(importBufferInventory.getStatus())) {
            try {
                var importItem = importBufferInventory.getAvailableStacks().getFirstEntry();

                IGrid grid = blockEntity.getMainNode().getGrid();
                if (grid == null) {
                    LOGGER.error("Main node grid is null.");
                    return;
                }

                IStorageService storageService = grid.getService(IStorageService.class);
                MEStorage baseStorage = storageService.getInventory();
                if (baseStorage instanceof NetworkStorage networkStorage) {
                    ExtendedNetworkStorage extendedNetworkStorage = new ExtendedNetworkStorage(networkStorage);
                    long amount = transferItems(importBufferInventory, extendedNetworkStorage, importItem.getKey(), importItem.getLongValue(), false, "ImportBuffer", "NetworkStorage");
                } else {
                    LOGGER.error("Grid inventory is not of type NetworkStorage. Maybe there is a version mismatch.");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to transfer items.", e);
            }
        }


    }

    @Override
    public void tick() {
        try {
            innerTick();
        } catch (Exception e) {
            LOGGER.error("Failed to tick InterDimensionalInterfaceStorage", e);
        }
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return blockEntity.getActionableNode();
    }

    public String itemToString(Object2LongMap.Entry<AEKey> entry) {
        CompoundTag compoundData = new CompoundTag();
        compoundData.put("item", entry.getKey().toTagGeneric(registries));
        compoundData.putLong("amount", entry.getLongValue());
        return compoundData.getAsString();
    }

    public Object2LongMap.Entry<AEKey> stringToItem(String data) throws Exception {
        CompoundTag compoundData = TagParser.parseTag(data);
        AEKey item = AEKey.fromTagGeneric(registries, compoundData.getCompound("item"));
        long amount = compoundData.getLong("amount");
        return new AbstractObject2LongMap.BasicEntry<>(item, amount);
    }

    /**
     * Transfer items from one storage to another.
     *
     * @param from   source MEStorage
     * @param to     target MEStorage
     * @param what   to transfer
     * @param amount the amount of items to transfer.
     * @return the amount of items that have been transferred successfully.
     * @throws Exception if simulation failes in exact mode or if the transfer fails.
     */
    public long transferItems(MEStorage from, MEStorage to, AEKey what, long amount) throws Exception {
        return transferItems(from, to, what, amount, false);
    }

    /**
     * Transfer items from one storage to another.
     *
     * @param from   source MEStorage
     * @param to     target MEStorage
     * @param what   to transfer
     * @param amount the amount of items to transfer.
     * @param exact  if true, the transfer will fail if the exact amount of items cannot be transferred.
     * @return the amount of items that have been transferred successfully.
     * @throws Exception if simulation failes in exact mode or if the transfer fails.
     */
    public long transferItems(MEStorage from, MEStorage to, AEKey what, long amount, boolean exact) throws Exception {
        return transferItems(from, to, what, amount, exact, from.getDescription().getString(64), to.getDescription().getString(64));

    }

    /**
     * Transfer items from one storage to another.
     *
     * @param from     source MEStorage
     * @param to       target MEStorage
     * @param what     to transfer
     * @param amount   the amount of items to transfer.
     * @param exact    if true, the transfer will fail if the exact amount of items cannot be transferred.
     * @param fromName the name to use in log messages.
     * @param toName   the name to use in log messages.
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
