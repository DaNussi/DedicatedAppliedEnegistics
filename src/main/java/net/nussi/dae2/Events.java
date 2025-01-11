package net.nussi.dae2;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.nussi.dae2.block.InterDimensionalInterfaceScreen;

public class Events {

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Register.INTER_DIMENSIONAL_INTERFACE_BLOCK_MENU.get(), InterDimensionalInterfaceScreen::new);
    }
}
