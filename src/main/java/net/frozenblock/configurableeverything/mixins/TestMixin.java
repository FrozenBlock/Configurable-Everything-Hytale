package net.frozenblock.configurableeverything.mixins;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.EntityRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(EntityRegistration.class)
public class TestMixin {

    @Unique
    private static final HytaleLogger configurableEverythingHY$LOGGER = HytaleLogger.forEnclosingClass();

    @Inject(method = "<init>(Lcom/hypixel/hytale/server/core/modules/entity/EntityRegistration;Ljava/util/function/BooleanSupplier;Ljava/lang/Runnable;)V", at = @At("TAIL"))
    private void poopFart(EntityRegistration registration, BooleanSupplier isEnabled, Runnable unregister, CallbackInfo ci) {
        configurableEverythingHY$LOGGER.atInfo().log("SKIBIDI TOILET RIZZ");
    }
}
