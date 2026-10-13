package flashbackforvulkanmod.mixin;

import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flashback's blit shaders declare no uniforms, which makes VulkanMod's GLSL converter emit an empty (invalid)
 * uniform block and abort the resource reload. Point them at this mod's equivalents, which declare one.
 */
@Mixin(targets = "com.moulberry.flashback.visuals.ShaderManager", remap = false)
public class ShaderManagerMixin {

    @Shadow @Final @Mutable
    public static class_10156 blitScreenRoundAlpha;

    @Shadow @Final @Mutable
    public static class_10156 blitScreenFlip;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void useVulkanShaders(CallbackInfo ci) {
        blitScreenRoundAlpha = new class_10156(class_2960.method_60654("flashback_for_vulkanmod:core/blit_screen_round_alpha"), class_290.field_29336, class_10149.field_53930);
        blitScreenFlip = new class_10156(class_2960.method_60654("flashback_for_vulkanmod:core/blit_screen_flip"), class_290.field_29336, class_10149.field_53930);
    }
}
