package net.nussi.dae2.block;


import appeng.api.networking.GridFlags;
import appeng.api.storage.IStorageProvider;
import appeng.blockentity.grid.AENetworkBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dae2.Register;
import org.slf4j.Logger;

import java.util.Set;


public class InterDimensionalInterfaceBlockEntity extends AENetworkBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final InterDimensionalInterfaceStorage storage = new InterDimensionalInterfaceStorage();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY.get(), pos, state);

        getMainNode().addService(IStorageProvider.class, storage);
        getMainNode().setExposedOnSides(Set.of(Direction.values()));
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        getMainNode().setVisualRepresentation(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get());


    }



}
