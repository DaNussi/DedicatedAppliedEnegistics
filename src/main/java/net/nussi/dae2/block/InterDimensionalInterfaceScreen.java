package net.nussi.dae2.block;

import appeng.client.gui.widgets.OpenGuideButton;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nussi.dae2.Register;
import org.slf4j.Logger;

public class InterDimensionalInterfaceScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Level level;
    private BlockPos position;
    private InterDimensionalInterfaceBlockEntity blockEntity;
    private Player player;

    protected InterDimensionalInterfaceScreen(Level level, BlockPos position, InterDimensionalInterfaceBlockEntity blockEntity, Player player) {
        super(Component.translatable("block.dae2.inter_dimensional_interface"));
        this.position = position;
        this.level = level;
        this.blockEntity = blockEntity;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new AbstractButton(this.width / 2 - 100, this.height / 2 - 25, 200, 20, Component.literal("Test")) {
            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }

            @Override
            public void onPress() {
                close();
            }
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var key = InputConstants.getKey(keyCode, scanCode);

        if(key.getName().equals("key.keyboard.e")) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void close() {
        if(!Minecraft.getInstance().screen.equals(this)) return;
        Minecraft.getInstance().setScreen(null);
    }

}
