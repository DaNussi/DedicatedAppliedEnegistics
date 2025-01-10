package net.nussi.dae2.block;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static net.nussi.dae2.Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY;

public class InterDimensionalInterfaceBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InterDimensionalInterfaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new InterDimensionalInterfaceBlockEntity(blockPos, blockState);
    }

    @SuppressWarnings("unchecked") // Due to generics, an unchecked cast is necessary here.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // You can return different tickers here, depending on whatever factors you want. A common use case would be
        // to return different tickers on the client or server, only tick one side to begin with,
        // or only return a ticker for some blockstates (e.g. when using a "my machine is working" blockstate property).
        return type == INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY.get() ? new BlockEntityTicker<T>() {
            @Override
            public void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
                if (t instanceof InterDimensionalInterfaceBlockEntity blockEntity) {
                    blockEntity.tick();
                }
            }
        } : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide()) return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof InterDimensionalInterfaceBlockEntity interDimensionalInterfaceBlockEntity) {


            Minecraft.getInstance().setScreen(new InterDimensionalInterfaceScreen(level, pos, interDimensionalInterfaceBlockEntity, player));

        } else {
            LOGGER.info("BlockEntity at position {} is not an instance of InterDimensionalInterfaceBlockEntity", pos);
        }

        return InteractionResult.SUCCESS;
    }


}
