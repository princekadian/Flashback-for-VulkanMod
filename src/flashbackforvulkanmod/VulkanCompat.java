package flashbackforvulkanmod;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_6367;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Vulkan-compatible replacements for the OpenGL-only parts of Flashback, called from the mixins.
 * Anything that touches VulkanMod types lives in {@link VulkanModBridge}.
 */
public final class VulkanCompat {

    public static final boolean ACTIVE = FabricLoader.getInstance().isModLoaded("vulkanmod");

    // SaveableFramebuffer -> frame read back by startDownload, handed out by finishDownload
    private static final Map<Object, class_1011> PENDING_DOWNLOADS = new IdentityHashMap<>();
    private static boolean exporting = false;

    private VulkanCompat() {
    }

    // ---- SaveableFramebuffer (replaces the glReadPixels + pixel buffer object readback) ----

    public static void startDownload(Object framebuffer, class_276 target, int width, int height) {
        if (PENDING_DOWNLOADS.containsKey(framebuffer)) {
            throw new IllegalStateException("Can't start downloading while already downloading");
        }

        class_1011 image = new class_1011(class_1011.class_1012.field_4997, width, height, false);
        try {
            int textureId = target.method_30277();
            // Leave the target's render pass so its colour image can be copied out
            target.method_1240();
            VulkanModBridge.downloadTexture(textureId, image.field_4988, width, height);
        } catch (Throwable t) {
            image.close();
            throw t;
        }
        PENDING_DOWNLOADS.put(framebuffer, image);
    }

    public static class_1011 finishDownload(Object framebuffer) {
        class_1011 image = PENDING_DOWNLOADS.remove(framebuffer);
        if (image == null) {
            throw new IllegalStateException("Can't finish downloading before download has started");
        }
        return image;
    }

    public static void closeDownload(Object framebuffer) {
        class_1011 image = PENDING_DOWNLOADS.remove(framebuffer);
        if (image != null) {
            image.close();
        }
    }

    // ---- ExportJob ----

    /** True while {@link #runExport} is running the original export, so the run() hook doesn't recurse. */
    public static boolean isExporting() {
        return exporting;
    }

    /**
     * Under VulkanMod the main render target is the swapchain itself, so it can't be resized to the
     * export resolution or sampled. Export into a real offscreen target instead and restore afterwards.
     */
    public static void runExport(int width, int height, Runnable originalRun) {
        class_310 minecraft = class_310.method_1551();
        class_276 original = minecraft.field_1689;
        class_6367 exportTarget = new class_6367(Math.max(1, width), Math.max(1, height), true);
        minecraft.field_1689 = exportTarget;
        exporting = true;
        try {
            originalRun.run();
        } finally {
            exporting = false;
            minecraft.field_1689 = original;
            exportTarget.method_1238();
            minecraft.method_15993();
        }
    }

    /**
     * Flashback's export loop renders and presents frames outside of Minecraft#runTick, where VulkanMod
     * normally begins its frames. Make sure VulkanMod is recording before each exported frame.
     */
    public static void ensureFrame() {
        if (ACTIVE) {
            VulkanModBridge.ensureFrame();
        }
    }
}
