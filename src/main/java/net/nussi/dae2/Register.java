package net.nussi.dae2;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nussi.dae2.block.InterDimensionalInterfaceBlock;
import net.nussi.dae2.block.InterDimensionalInterfaceBlockEntity;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.function.Supplier;

public class Register {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dae2.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dae2.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dae2.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Dae2.MODID);


    public static final DeferredBlock<Block> INTER_DIMENSIONAL_INTERFACE_BLOCK = BLOCKS.registerBlock("inter_dimensional_interface", InterDimensionalInterfaceBlock::new);

    public static final DeferredItem<BlockItem> INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("inter_dimensional_interface", INTER_DIMENSIONAL_INTERFACE_BLOCK);

    public static final Supplier<BlockEntityType<InterDimensionalInterfaceBlockEntity>> INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("inter_dimensional_interface", () -> create(InterDimensionalInterfaceBlockEntity.class, BlockEntityType.Builder.of(InterDimensionalInterfaceBlockEntity::new, INTER_DIMENSIONAL_INTERFACE_BLOCK.get()).build(null), (AEBaseEntityBlock<InterDimensionalInterfaceBlockEntity>) INTER_DIMENSIONAL_INTERFACE_BLOCK.get()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DAE2_TAB = CREATIVE_MODE_TABS.register("dae2_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.dae2")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get());
    }).build());


    public static <T extends AEBaseBlockEntity> BlockEntityType<T> create(Class<T> entityClass,
                                                                          BlockEntityType<T> type,
                                                                          AEBaseEntityBlock<T> block) {

        // If the block entity classes implement specific interfaces, automatically register them
        // as tickers with the blocks that create that entity.
        BlockEntityTicker<T> serverTicker = null;
        if (ServerTickingBlockEntity.class.isAssignableFrom(entityClass)) {
            serverTicker = (level, pos, state, entity) -> {
                ((ServerTickingBlockEntity) entity).serverTick();
            };
        }
        BlockEntityTicker<T> clientTicker = null;
        if (ClientTickingBlockEntity.class.isAssignableFrom(entityClass)) {
            clientTicker = (level, pos, state, entity) -> {
                ((ClientTickingBlockEntity) entity).clientTick();
            };
        }

        block.setBlockEntity(entityClass, type, clientTicker, serverTicker);

        return type;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
