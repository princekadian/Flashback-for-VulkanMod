package com.mojang.blaze3d.systems;
import net.minecraft.*;
public class RenderSystem {
    public static void assertOnRenderThread() {}
    public static void enableBlend() {}
    public static void disableBlend() {}
    public static void blendFuncSeparate(int a, int b, int c, int d) {}
    public static void defaultBlendFunc() {}
    public static void enableCull() {}
    public static void disableCull() {}
    public static void disableDepthTest() {}
    public static void depthMask(boolean b) {}
    public static void colorMask(boolean r, boolean g, boolean b, boolean a) {}
    public static void viewport(int x, int y, int w, int h) {}
    public static void setProjectionMatrix(org.joml.Matrix4f m, class_10366 t) {}
    public static void backupProjectionMatrix() {}
    public static void restoreProjectionMatrix() {}
    public static org.joml.Matrix4fStack getModelViewStack() { throw new AssertionError(); }
    public static class_5944 setShader(class_10156 s) { throw new AssertionError(); }
    public static void setShaderTexture(int unit, int id) {}
    public static float[] getShaderColor() { throw new AssertionError(); }
    public static void setShaderColor(float r, float g, float b, float a) {}
    public static void enableScissor(int x, int y, int w, int h) {}
    public static void disableScissor() {}
    public static class_289 renderThreadTesselator() { throw new AssertionError(); }
}
