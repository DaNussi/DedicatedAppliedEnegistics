package net.nussi.dae2.block;


import appeng.api.inventories.InternalInventory;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IStorageProvider;
import appeng.api.util.AECableType;
import appeng.core.definitions.AEItems;
import appeng.me.ManagedGridNode;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dae2.Register;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;
import java.util.UUID;


public class InterDimensionalInterfaceBlockEntity extends BlockEntity implements IGridConnectedBlockEntity, MenuProvider, IGridTickable, Tickable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IManagedGridNode mainNode = new ManagedGridNode(this, BlockEntityNodeListener.INSTANCE);
    private final InterDimensionalInterfaceStorage storage = new InterDimensionalInterfaceStorage(this);

    private CompoundTag initData = null;
    private HolderLookup.Provider initRegistries = null;
    private boolean isInitialized = false;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }

    private void initialize() {
        Level level = getLevel();

        if(level == null) return;
        if(level.isClientSide()) return;

        mainNode.setInWorldNode(true);
        mainNode.addService(IStorageProvider.class, storage);
        mainNode.addService(IGridTickable.class, this);
        mainNode.setExposedOnSides(Set.of(Direction.values()));
        mainNode.setFlags(GridFlags.REQUIRE_CHANNEL);
        mainNode.setIdlePowerUsage(200);
        mainNode.setTagName("mainNode");
        mainNode.setVisualRepresentation(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get());
        mainNode.create(getLevel(), getBlockPos());
        LOGGER.info("Main node created! Main node is ready: {} | online: {} | active: {} | powered: {}", mainNode.isReady(), mainNode.isOnline(), mainNode.isActive(), mainNode.isPowered());


        if(initData != null) loadAdditionalInternal(initData, initRegistries);

        this.isInitialized = true;
    }

    public InterDimensionalInterfaceStorage getStorage() {
        return storage;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
        if(!isInitialized) {
            this.initData = data;
            this.initRegistries = registries;
        } else {
            loadAdditionalInternal(data, registries);
        }
    }

    public void loadAdditionalInternal(CompoundTag data, HolderLookup.Provider registries) {
        super.loadAdditional(data, registries);

        CompoundTag mainNodeData = data.getCompound("mainNode");
        mainNode.loadFromNBT(mainNodeData);

        CompoundTag storageData = data.getCompound("storage");
        storage.loadAdditional(storageData, registries);


        LOGGER.info("InterDimensionalInterfaceBlockEntity loaded");
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);

        CompoundTag mainNodeData = new CompoundTag();
        mainNode.saveToNBT(mainNodeData);
        data.put("mainNode", mainNodeData);

        CompoundTag storageData = new CompoundTag();
        storage.saveAdditional(storageData, registries);
        data.put("storage", storageData);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
        LOGGER.info("Destroyed main node");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }



    public boolean isInitialized() {
        return isInitialized;
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("block.dae2.inter_dimensional_interface");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new InterDimensionalInterfaceMenu(i, inventory, this);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, 1);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        storage.tick();

        return TickRateModulation.SAME;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedBlockEntity.super.onMainNodeStateChanged(reason);

        LOGGER.info("Main node state changed because {}. Main node state is ready: {} | online: {} | active: {} | powered: {}", reason.toString(), mainNode.isReady(), mainNode.isOnline(), mainNode.isActive(), mainNode.isPowered());
    }

    @Override
    public void tick() {
        if(!isInitialized) initialize();
    }
}
