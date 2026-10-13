package flashbackforvulkanmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.moulberry.flashback.editor.ui.ReplayUI", remap = false)
public class ReplayUIMixin {

    /**
     * Use Flashback's "underlay" sizing: VulkanMod renders the game straight into the swapchain, so there is
     * no separate game framebuffer that could be shrunk into the editor frame.
     */
    @Inject(method = "shouldModifyViewport", at = @At("HEAD"), cancellable = true)
    private static void shouldModifyViewport(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    /** Hide the "game view focused" outline; with underlay sizing it outlines an empty box in the middle of the game. */
    @ModifyConstant(method = "drawOverlayInternal", constant = @Constant(intValue = -4040669))
    private static int hideFrameBorder(int color) {
        return 0;
    }
}
