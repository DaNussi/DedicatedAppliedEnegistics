package net.nussi.dae2.block;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class InterDimensionalInterfaceListener implements IGridNodeListener<InterDimensionalInterfaceBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final InterDimensionalInterfaceListener INSTANCE = new InterDimensionalInterfaceListener();

    @Override
    public void onSaveChanges(InterDimensionalInterfaceBlockEntity nodeOwner, IGridNode node) {

    }

    @Override
    public void onInWorldConnectionChanged(InterDimensionalInterfaceBlockEntity nodeOwner, IGridNode node) {
        IGridNodeListener.super.onInWorldConnectionChanged(nodeOwner, node);
    }

    @Override
    public void onOwnerChanged(InterDimensionalInterfaceBlockEntity nodeOwner, IGridNode node) {
        IGridNodeListener.super.onOwnerChanged(nodeOwner, node);
    }

    @Override
    public void onGridChanged(InterDimensionalInterfaceBlockEntity nodeOwner, IGridNode node) {
        LOGGER.info("Grid changed");
    }

    @Override
    public void onStateChanged(InterDimensionalInterfaceBlockEntity nodeOwner, IGridNode node, State state) {
        IGridNodeListener.super.onStateChanged(nodeOwner, node, state);
    }
}
