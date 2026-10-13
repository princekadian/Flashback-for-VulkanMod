package flashbackforvulkanmod.mixin;

import flashbackforvulkanmod.VulkanCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.moulberry.flashback.exporting.ExportJob", remap = false)
public abstract class ExportJobMixin {

    @Shadow
    public abstract void run();

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract int getHeight();

    /** Export into an offscreen target at the export resolution instead of VulkanMod's swapchain-backed main target. */
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void runWithOffscreenTarget(CallbackInfo ci) {
        if (!VulkanCompat.isExporting()) {
            ci.cancel();
            VulkanCompat.runExport(this.getWidth(), this.getHeight(), this::run);
        }
    }

    // Keep VulkanMod recording for the frames the export loop renders and presents itself

    @Inject(method = "doExport", at = {
            @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/class_276;method_1235(Z)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/class_757;method_3192(Lnet/minecraft/class_9779;Z)V")
    })
    private void ensureFrameInExport(CallbackInfo ci) {
        VulkanCompat.ensureFrame();
    }

    @Inject(method = "finishFrame", at = @At("HEAD"))
    private void ensureFrameInFinishFrame(CallbackInfoReturnable<Boolean> cir) {
        VulkanCompat.ensureFrame();
    }
}
