package net.nussi.dae2.block;


import appeng.api.networking.*;
import appeng.api.storage.IStorageProvider;
import appeng.api.util.AECableType;
import appeng.me.ManagedGridNode;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dae2.Register;
import org.slf4j.Logger;

import java.util.Set;
import java.util.UUID;


public class InterDimensionalInterfaceBlockEntity extends BlockEntity implements IGridConnectedBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IManagedGridNode mainNode = new ManagedGridNode(this, BlockEntityNodeListener.INSTANCE);
    private final InterDimensionalInterfaceStorage storage = new InterDimensionalInterfaceStorage();

    private CompoundTag initData = null;
    private HolderLookup.Provider initRegistries = null;
    private boolean isInitialized = false;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }


    public void tick() {
        if(!isInitialized) initialize();
    }

    private void initialize() {
        Level level = getLevel();

        if(level == null) return;
        if(level.isClientSide()) return;

        mainNode.setInWorldNode(true);
        mainNode.addService(IStorageProvider.class, storage);
        mainNode.setExposedOnSides(Set.of(Direction.values()));
        mainNode.setFlags(GridFlags.REQUIRE_CHANNEL);
        mainNode.setVisualRepresentation(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get());
        mainNode.create(getLevel(), getBlockPos());

        if(initData != null) loadAdditionalInternal(initData, initRegistries);

        this.isInitialized = true;
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
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }


}
