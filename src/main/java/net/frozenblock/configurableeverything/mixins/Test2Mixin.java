package net.frozenblock.configurableeverything.mixins;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HytaleServer.class)
public class Test2Mixin {

    @Inject(method = "boot", at = @At("TAIL"))
    private void boot(CallbackInfo ci) {
        HytaleLogger.get("SKIBIDI").atInfo().log("TOILET");
    }
}
