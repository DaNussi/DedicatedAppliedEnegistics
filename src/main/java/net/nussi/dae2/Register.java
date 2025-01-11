package net.nussi.dae2;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nussi.dae2.block.InterDimensionalInterfaceBlock;
import net.nussi.dae2.block.InterDimensionalInterfaceBlockEntity;
import net.nussi.dae2.block.InterDimensionalInterfaceMenu;

import java.util.function.Supplier;

public class Register {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dae2.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dae2.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dae2.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Dae2.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Dae2.MODID);


    public static final DeferredBlock<Block> INTER_DIMENSIONAL_INTERFACE_BLOCK = BLOCKS.registerBlock("inter_dimensional_interface", InterDimensionalInterfaceBlock::new);

    public static final DeferredItem<BlockItem> INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("inter_dimensional_interface", INTER_DIMENSIONAL_INTERFACE_BLOCK);

    public static final Supplier<BlockEntityType<InterDimensionalInterfaceBlockEntity>> INTER_DIMENSIONAL_INTERFACE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("inter_dimensional_interface", () -> BlockEntityType.Builder.of(InterDimensionalInterfaceBlockEntity::new, INTER_DIMENSIONAL_INTERFACE_BLOCK.get()).build(null));
    public static final Supplier<MenuType<InterDimensionalInterfaceMenu>> INTER_DIMENSIONAL_INTERFACE_BLOCK_MENU = MENU_TYPES.register("inter_dimensional_interface", () -> new MenuType<>(InterDimensionalInterfaceMenu::new, FeatureFlags.DEFAULT_FLAGS));



    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DAE2_TAB = CREATIVE_MODE_TABS.register("dae2_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.dae2")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get());
    }).build());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
        MENU_TYPES.register(eventBus);
    }
}
