package net.nussi.dae2.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;

import static net.nussi.dae2.Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY;

public class InterDimensionalInterfaceBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<BlockPos, BlockEntity> blockEntities = new HashMap<>();


    public InterDimensionalInterfaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {

        return new InterDimensionalInterfaceBlockEntity(blockPos, blockState);

//        if(blockEntities.containsKey(blockPos)) {
//            LOGGER.info("Found existing InterDimensionalInterfaceBlockEntity at {}", blockPos);
//            return blockEntities.get(blockPos);
//        } else {
//            BlockEntity blockEntity = new InterDimensionalInterfaceBlockEntity(blockPos, blockState);
//            blockEntities.put(blockPos, blockEntity);
//            LOGGER.info("Created new InterDimensionalInterfaceBlockEntity at {}", blockPos);
//            return blockEntity;
//        }
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
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(! (blockEntity instanceof InterDimensionalInterfaceBlockEntity)) return InteractionResult.PASS;
        InterDimensionalInterfaceBlockEntity parsedBlockEntity = (InterDimensionalInterfaceBlockEntity) blockEntity;

        if(level.isClientSide()) return InteractionResult.SUCCESS;

        if(player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(parsedBlockEntity, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(level.isClientSide()) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof InterDimensionalInterfaceBlockEntity parsedBlockEntity) {
            Container inventory = parsedBlockEntity.getStorage().getCellInventory();
            for (int slotIndex = 0; slotIndex < inventory.getContainerSize(); slotIndex++) {
                ItemStack itemStack = inventory.getItem(slotIndex);
                if (!itemStack.isEmpty()) {
                    ItemStack stack = inventory.getItem(slotIndex);
                    ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                    level.addFreshEntity(entity);
                }
            }

        }

        blockEntities.remove(pos);
        LOGGER.info("Removed InterDimensionalInterfaceBlockEntity at {}", pos);

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
