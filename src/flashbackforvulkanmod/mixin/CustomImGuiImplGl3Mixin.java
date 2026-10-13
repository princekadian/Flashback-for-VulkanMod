package flashbackforvulkanmod.mixin;

import flashbackforvulkanmod.VulkanImGuiRenderer;
import imgui.moulberry90.ImDrawData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Flashback's ImGui renderer issues raw OpenGL calls; route everything to the RenderSystem-based renderer. */
@Mixin(targets = "com.moulberry.flashback.editor.ui.CustomImGuiImplGl3", remap = false)
public class CustomImGuiImplGl3Mixin {

    @Inject(method = "init(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private void init(String glslVersion, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(VulkanImGuiRenderer.init());
    }

    @Inject(method = "shutdown", at = @At("HEAD"), cancellable = true)
    private void shutdown(CallbackInfo ci) {
        VulkanImGuiRenderer.shutdown();
        ci.cancel();
    }

    @Inject(method = "newFrame", at = @At("HEAD"), cancellable = true)
    private void newFrame(CallbackInfo ci) {
        VulkanImGuiRenderer.newFrame();
        ci.cancel();
    }

    @Inject(method = "createDeviceObjects", at = @At("HEAD"), cancellable = true)
    private void createDeviceObjects(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(VulkanImGuiRenderer.createDeviceObjects());
    }

    @Inject(method = "renderDrawData", at = @At("HEAD"), cancellable = true)
    private void renderDrawData(ImDrawData drawData, CallbackInfo ci) {
        VulkanImGuiRenderer.renderDrawData(drawData);
        ci.cancel();
    }

    @Inject(method = "updateFontsTexture", at = @At("HEAD"), cancellable = true)
    private void updateFontsTexture(CallbackInfo ci) {
        VulkanImGuiRenderer.updateFontsTexture();
        ci.cancel();
    }

    @Inject(method = {"destroyFontsTexture", "destroyDeviceObjects"}, at = @At("HEAD"), cancellable = true)
    private void destroyFontsTexture(CallbackInfo ci) {
        VulkanImGuiRenderer.destroyFontsTexture();
        ci.cancel();
    }
}
