package net.nussi.dae2.block;


import appeng.api.networking.GridFlags;
import appeng.api.networking.IManagedGridNode;
import appeng.api.storage.IStorageProvider;
import appeng.me.ManagedGridNode;
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


public class InterDimensionalInterfaceBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IManagedGridNode mainNode = new ManagedGridNode(this, InterDimensionalInterfaceListener.INSTANCE);
    private final InterDimensionalInterfaceStorage storage = new InterDimensionalInterfaceStorage();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }

    // TODO: Implement destruction of mainNode when block entity gets destroyed

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
