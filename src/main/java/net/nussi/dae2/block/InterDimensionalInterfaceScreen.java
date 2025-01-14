package net.nussi.dae2.block;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class InterDimensionalInterfaceScreen extends AbstractContainerScreen<InterDimensionalInterfaceMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InterDimensionalInterfaceScreen(InterDimensionalInterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        renderTransparentBackground(pGuiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void init() {
        super.init();


//        addRenderableWidget(
//                Button.builder(Component.literal("Test"), (button) -> button.setMessage(Component.literal("Test2")))
//                        .pos(width / 2, height / 2)
//                        .size(200, 20)
//                        .build()
//        );

    }
}
