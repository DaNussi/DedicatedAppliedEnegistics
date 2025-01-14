package net.nussi.dae2.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import com.mojang.logging.LogUtils;
import net.nussi.dae2.block.InterDimensionalInterfaceBlockEntity;
import net.nussi.dae2.block.InterDimensionalInterfaceStorage;
import net.nussi.dae2.common.StorageMounts;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for NetworkStorage
 * This mixin enures that a InterDimensionalInterface cannot transfer items to and from itself
 */
@Mixin(NetworkStorage.class)
public abstract class NetworkStorageMixin {
    @Shadow
    public abstract void unmount(MEStorage inventory);

    @Shadow
    public abstract void mount(int priority, MEStorage inventory);

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "insert", at = @At("HEAD"))
    public void insertHead(AEKey what, long amount, Actionable type, IActionSource src, CallbackInfoReturnable<Long> cir) {
//        if (src.machine().isPresent()) {
//            IActionHost host = src.machine().get();
//            if (host instanceof InterDimensionalInterfaceStorage blockEntityStorage) {
//                StorageMounts storageMounts = new StorageMounts(blockEntityStorage);
//                for (MEStorage storage : storageMounts.toList()) {
//                    unmount(storage);
//                }
//            }
//        }
    }

    @Inject(method = "insert", at = @At("RETURN"))
    public void insertReturn(AEKey what, long amount, Actionable type, IActionSource src, CallbackInfoReturnable<Long> cir) {
//        if (src.machine().isPresent()) {
//            IActionHost host = src.machine().get();
//            if (host instanceof InterDimensionalInterfaceStorage blockEntityStorage) {
//                StorageMounts storageMounts = new StorageMounts(blockEntityStorage);
//                for (var entry : storageMounts.toHashMap().entrySet()) {
//                    int priority = entry.getKey();
//                    for (MEStorage storage : entry.getValue()) {
//                        mount(priority, storage);
//                    }
//                }
//            }
//        }
    }

}
