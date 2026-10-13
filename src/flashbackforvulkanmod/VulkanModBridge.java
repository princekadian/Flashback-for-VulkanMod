package flashbackforvulkanmod;

import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.texture.ImageUtil;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryUtil;

/**
 * The only class that references VulkanMod. Loaded only when {@link VulkanCompat#ACTIVE} is true.
 */
final class VulkanModBridge {

    private static final int VK_FORMAT_B8G8R8A8_UNORM = 44;
    private static final int VK_FORMAT_B8G8R8A8_SRGB = 50;

    private VulkanModBridge() {
    }

    /**
     * Copies a texture's colour image into {@code dst} as tightly packed RGBA rows, flipped vertically
     * so the result matches what Flashback produced with glReadPixels after its Y-flip blit.
     */
    static void downloadTexture(int textureId, long dst, int width, int height) {
        VkGlTexture texture = VkGlTexture.getTexture(textureId);
        if (texture == null || texture.getVulkanImage() == null) {
            throw new IllegalStateException("Flashback: export framebuffer texture " + textureId + " has no Vulkan image");
        }

        VulkanImage image = texture.getVulkanImage();
        if (image.width != width || image.height != height || image.formatSize != 4) {
            throw new IllegalStateException("Flashback: unexpected export framebuffer image " + image.width + "x"
                    + image.height + " (" + image.formatSize + " bytes/pixel), expected " + width + "x" + height);
        }

        // Submit the queued draws (including the blit into this target) and wait for them,
        // the same way VulkanMod's own screenshot code does before downloading
        Renderer.getInstance().flushCmds();
        ImageUtil.downloadTexture(image, dst);

        // Flashback's Y-flip blit already flips for GL's bottom-up readback; VulkanMod reads top-down, so undo it
        long rowSize = (long) width * 4L;
        long tmp = MemoryUtil.nmemAlloc(rowSize);
        try {
            for (int y = 0; y < height / 2; y++) {
                long top = dst + y * rowSize;
                long bottom = dst + (long) (height - 1 - y) * rowSize;
                MemoryUtil.memCopy(top, tmp, rowSize);
                MemoryUtil.memCopy(bottom, top, rowSize);
                MemoryUtil.memCopy(tmp, bottom, rowSize);
            }
        } finally {
            MemoryUtil.nmemFree(tmp);
        }

        if (image.format == VK_FORMAT_B8G8R8A8_UNORM || image.format == VK_FORMAT_B8G8R8A8_SRGB) {
            long end = dst + (long) width * height * 4L;
            for (long ptr = dst; ptr < end; ptr += 4) {
                byte b = MemoryUtil.memGetByte(ptr);
                MemoryUtil.memPutByte(ptr, MemoryUtil.memGetByte(ptr + 2));
                MemoryUtil.memPutByte(ptr + 2, b);
            }
        }
    }

    static void ensureFrame() {
        if (!Renderer.isRecording()) {
            Renderer renderer = Renderer.getInstance();
            renderer.preInitFrame();
            renderer.beginFrame();
        }
    }
}
