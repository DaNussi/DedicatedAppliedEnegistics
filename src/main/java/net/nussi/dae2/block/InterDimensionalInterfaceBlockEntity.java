package net.nussi.dae2.block;


import appeng.api.AECapabilities;
import appeng.api.networking.*;
import appeng.api.storage.IStorageProvider;
import appeng.api.util.AECableType;
import appeng.me.ManagedGridNode;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;


public class InterDimensionalInterfaceBlockEntity extends BlockEntity implements IGridConnectedBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IManagedGridNode mainNode = new ManagedGridNode(this, InterDimensionalInterfaceListener.INSTANCE);
    private final InterDimensionalInterfaceStorage storage = new InterDimensionalInterfaceStorage();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }


    private boolean firstTick = true;
    private void onFirstTick() {
        Level level = getLevel();
        if (level != null) {
            LOGGER.info("InterDimensionalInterfaceBlockEntity created");
            mainNode.setInWorldNode(true);
            mainNode.addService(IStorageProvider.class, storage);
            mainNode.setExposedOnSides(Set.of(Direction.values()));
            mainNode.setFlags(GridFlags.REQUIRE_CHANNEL);
            mainNode.setVisualRepresentation(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get());
            mainNode.create(getLevel(), getBlockPos());


        } else {
            LOGGER.warn("InterDimensionalInterfaceBlockEntity created without level");
        }
    }

    public void tick() {
        if(firstTick) {
            onFirstTick();
            firstTick = false;
        }
    }

    @Override
    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public @Nullable IGridNode getGridNode() {
        return this.mainNode.getNode();
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir) {
        return this.mainNode.getNode();
    }



    @Override
    public void saveChanges() {
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {

    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }


    @Override
    public void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.loadAdditional(data, registries);
        mainNode.loadFromNBT(data);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        mainNode.saveToNBT(data);
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
