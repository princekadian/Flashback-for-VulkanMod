package flashbackforvulkanmod.mixin;

import flashbackforvulkanmod.VulkanCompat;
import net.minecraft.class_1011;
import net.minecraft.class_276;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces the glReadPixels + pixel buffer object frame readback used during export. */
@Mixin(targets = "com.moulberry.flashback.exporting.SaveableFramebuffer", remap = false)
public class SaveableFramebufferMixin {

    @Inject(method = "startDownload", at = @At("HEAD"), cancellable = true)
    private void startDownload(class_276 target, int width, int height, CallbackInfo ci) {
        VulkanCompat.startDownload(this, target, width, height);
        ci.cancel();
    }

    @Inject(method = "finishDownload", at = @At("HEAD"), cancellable = true)
    private void finishDownload(int width, int height, CallbackInfoReturnable<class_1011> cir) {
        cir.setReturnValue(VulkanCompat.finishDownload(this));
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void close(CallbackInfo ci) {
        VulkanCompat.closeDownload(this);
        ci.cancel();
    }
}
