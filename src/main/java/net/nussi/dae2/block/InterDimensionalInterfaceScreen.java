package net.nussi.dae2.block;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.nussi.dae2.Dae2;
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


}
