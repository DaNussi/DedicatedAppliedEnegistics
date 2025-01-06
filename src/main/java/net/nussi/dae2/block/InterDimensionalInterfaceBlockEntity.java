package net.nussi.dae2.block;


import appeng.api.networking.GridHelper;
import appeng.api.networking.IManagedGridNode;
import appeng.me.ManagedGridNode;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dae2.Register;
import org.slf4j.Logger;

import java.util.function.Consumer;


public class InterDimensionalInterfaceBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IManagedGridNode mainNode = new ManagedGridNode(this, InterDimensionalInterfaceListener.INSTANCE);

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
            mainNode.create(getLevel(), getBlockPos());
        } else {
            LOGGER.warn("InterDimensionalInterfaceBlockEntity created without level");
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InterDimensionalInterfaceBlockEntity blockEntity) {
        if(blockEntity.firstTick) {
            blockEntity.onFirstTick();
            blockEntity.firstTick = false;
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (t instanceof InterDimensionalInterfaceBlockEntity blockEntity) {
            tick(level, blockPos, blockState, blockEntity);
        }
    }
}
