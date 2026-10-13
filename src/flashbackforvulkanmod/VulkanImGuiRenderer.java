package flashbackforvulkanmod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.moulberry.flashback.editor.ui.ReplayUI;
import imgui.moulberry90.ImDrawData;
import imgui.moulberry90.ImFontAtlas;
import imgui.moulberry90.ImGuiIO;
import imgui.moulberry90.ImVec4;
import imgui.moulberry90.type.ImInt;
import net.minecraft.class_10142;
import net.minecraft.class_10366;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_293;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * ImGui renderer used instead of CustomImGuiImplGl3 when VulkanMod is installed.
 * <p>
 * CustomImGuiImplGl3 issues raw OpenGL calls (shaders, VAOs, glDrawElements) which don't exist under
 * VulkanMod. This draws the same ImDrawData through Minecraft's RenderSystem / BufferBuilder pipeline,
 * which VulkanMod implements.
 */
public final class VulkanImGuiRenderer {

    private static final int GL_TEXTURE_2D = 3553;
    private static final int GL_RGBA = 6408;
    private static final int GL_UNSIGNED_BYTE = 5121;
    private static final int GL_LINEAR = 9729;
    private static final int GL_CLAMP_TO_EDGE = 33071;
    private static final int IMGUI_BACKEND_FLAGS_RENDERER_HAS_VTX_OFFSET = 8;

    private static int fontTexture = -1;
    private static final ImVec4 clipRect = new ImVec4();
    private static ByteBuffer vertexCopy = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

    private VulkanImGuiRenderer() {
    }

    public static boolean init() {
        ImGuiIO io = ReplayUI.getIO();
        io.setBackendRendererName("flashback_impl_vulkanmod");
        // Multi-viewport isn't supported: secondary OS windows would need their own OpenGL contexts
        io.addBackendFlags(IMGUI_BACKEND_FLAGS_RENDERER_HAS_VTX_OFFSET);
        return true;
    }

    public static void shutdown() {
        destroyFontsTexture();
        ImGuiIO io = ReplayUI.getIO();
        io.setBackendRendererName(null);
        io.removeBackendFlags(IMGUI_BACKEND_FLAGS_RENDERER_HAS_VTX_OFFSET);
    }

    public static void newFrame() {
        if (fontTexture == -1) {
            updateFontsTexture();
        }
    }

    public static boolean createDeviceObjects() {
        return true;
    }

    public static void updateFontsTexture() {
        RenderSystem.assertOnRenderThread();
        if (fontTexture != -1) {
            TextureUtil.releaseTextureId(fontTexture);
            fontTexture = -1;
        }

        ImFontAtlas fonts = ReplayUI.getIO().getFonts();
        ImInt width = new ImInt();
        ImInt height = new ImInt();
        ByteBuffer pixels = fonts.getTexDataAsRGBA32(width, height);

        int texture = GlStateManager._genTexture();
        GlStateManager._bindTexture(texture);
        GlStateManager._texParameter(GL_TEXTURE_2D, 10241, GL_LINEAR); // min filter
        GlStateManager._texParameter(GL_TEXTURE_2D, 10240, GL_LINEAR); // mag filter
        GlStateManager._texParameter(GL_TEXTURE_2D, 10242, GL_CLAMP_TO_EDGE); // wrap s
        GlStateManager._texParameter(GL_TEXTURE_2D, 10243, GL_CLAMP_TO_EDGE); // wrap t
        GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (IntBuffer) null);
        // VulkanMod sizes the copy as rowLength * height, so 0 ("tightly packed" in GL) would upload nothing
        GlStateManager._pixelStore(3314, width.get()); // unpack row length
        GlStateManager._pixelStore(3316, 0); // unpack skip rows
        GlStateManager._pixelStore(3315, 0); // unpack skip pixels
        GlStateManager._pixelStore(3317, 4); // unpack alignment
        GlStateManager._texSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width.get(), height.get(), GL_RGBA, GL_UNSIGNED_BYTE, MemoryUtil.memAddress(pixels));
        GlStateManager._pixelStore(3314, 0); // row length is global state in VulkanMod; don't leak ours into other uploads

        fontTexture = texture;
        fonts.setTexID(texture);
    }

    public static void destroyFontsTexture() {
        if (fontTexture != -1) {
            TextureUtil.releaseTextureId(fontTexture);
            ReplayUI.getIO().getFonts().setTexID(0);
            fontTexture = -1;
        }
    }

    public static void renderDrawData(ImDrawData drawData) {
        RenderSystem.assertOnRenderThread();

        float displayX = drawData.getDisplayPosX();
        float displayY = drawData.getDisplayPosY();
        float displayWidth = drawData.getDisplaySizeX();
        float displayHeight = drawData.getDisplaySizeY();
        float scaleX = drawData.getFramebufferScaleX();
        float scaleY = drawData.getFramebufferScaleY();

        int framebufferWidth = (int) (displayWidth * scaleX);
        int framebufferHeight = (int) (displayHeight * scaleY);
        if (framebufferWidth <= 0 || framebufferHeight <= 0 || drawData.getCmdListsCount() <= 0) {
            return;
        }

        int vertexSize = ImDrawData.sizeOfImDrawVert();
        int indexSize = ImDrawData.sizeOfImDrawIdx();

        float[] previousShaderColor = RenderSystem.getShaderColor().clone();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 771); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.viewport(0, 0, framebufferWidth, framebufferHeight);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(displayX, displayX + displayWidth,
                displayY + displayHeight, displayY, -1.0f, 1.0f), class_10366.field_54954);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();

        try {
            RenderSystem.setShader(class_10142.field_53880); // position_tex_color

            for (int list = 0; list < drawData.getCmdListsCount(); list++) {
                // imgui-java returns one shared buffer for vertex and index data, so copy the vertices out first
                ByteBuffer vtxData = drawData.getCmdListVtxBufferData(list);
                if (vertexCopy.capacity() < vtxData.remaining()) {
                    vertexCopy = ByteBuffer.allocateDirect(vtxData.remaining() * 2).order(ByteOrder.nativeOrder());
                }
                vertexCopy.clear();
                vertexCopy.put(vtxData).flip();
                ByteBuffer vertices = vertexCopy;
                ByteBuffer indices = drawData.getCmdListIdxBufferData(list).order(ByteOrder.nativeOrder());

                for (int cmd = 0; cmd < drawData.getCmdListCmdBufferSize(list); cmd++) {
                    int elemCount = drawData.getCmdListCmdBufferElemCount(list, cmd);
                    if (elemCount <= 0) {
                        continue;
                    }

                    drawData.getCmdListCmdBufferClipRect(clipRect, list, cmd);
                    float clipMinX = (clipRect.x - displayX) * scaleX;
                    float clipMinY = (clipRect.y - displayY) * scaleY;
                    float clipMaxX = (clipRect.z - displayX) * scaleX;
                    float clipMaxY = (clipRect.w - displayY) * scaleY;
                    if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
                        continue;
                    }

                    // OpenGL convention (origin bottom-left); VulkanMod converts it like it does for vanilla GUI scissor
                    RenderSystem.enableScissor((int) clipMinX, (int) (framebufferHeight - clipMaxY),
                            (int) (clipMaxX - clipMinX), (int) (clipMaxY - clipMinY));
                    RenderSystem.setShaderTexture(0, (int) drawData.getCmdListCmdBufferTextureId(list, cmd));

                    int idxOffset = drawData.getCmdListCmdBufferIdxOffset(list, cmd);
                    int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(list, cmd);

                    class_287 builder = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27379, class_290.field_1575);
                    for (int i = 0; i < elemCount; i++) {
                        int indexPos = (idxOffset + i) * indexSize;
                        int index = indexSize == 2 ? (indices.getShort(indexPos) & 0xFFFF) : indices.getInt(indexPos);
                        int base = (index + vtxOffset) * vertexSize;

                        int abgr = vertices.getInt(base + 16);
                        int argb = (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >>> 16) & 0xFF);

                        builder.method_22912(vertices.getFloat(base), vertices.getFloat(base + 4), 0.0f)
                                .method_22913(vertices.getFloat(base + 8), vertices.getFloat(base + 12))
                                .method_39415(argb);
                    }
                    class_286.method_43433(builder.method_60800()); // drawWithShader
                }
            }
        } finally {
            RenderSystem.disableScissor();
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.setShaderColor(previousShaderColor[0], previousShaderColor[1], previousShaderColor[2], previousShaderColor[3]);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }
}
