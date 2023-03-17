
package net.ccbluex.liquidbounce.injection.forge.mixins.crash;

import net.minecraft.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class MixinCrashReport {
/*
    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void injectCrashEnv(CallbackInfo callbackInfo) {
		  wdl.WDLHooks.onCrashReportPopulateEnvironment((CrashReport) (Object) this);
    }
*/
}