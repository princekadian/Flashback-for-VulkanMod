# Flashback for VulkanMod

**Download:** [Modrinth](https://modrinth.com/mod/flashback-for-vulkanmod)

Add-on for [Flashback](https://modrinth.com/mod/flashback) that makes it work with
[VulkanMod](https://modrinth.com/mod/vulkanmod) on Minecraft 1.21.4.

This mod contains no Flashback code. It changes Flashback at load time with Mixin, and only
when VulkanMod is installed; without VulkanMod it does nothing.

## Requirements
- Minecraft 1.21.4, Fabric Loader 0.15.10+
- Flashback 0.39.10 (install it separately)
- VulkanMod 0.5.6+

## What it fixes
- **Replay editor UI**: Flashback draws its ImGui interface with raw OpenGL. It is now drawn
  through Minecraft's renderer, which VulkanMod supports.
- **Video export**: frames are read back with VulkanMod instead of OpenGL `glReadPixels`.
  Export renders into its own offscreen target at the export resolution.
- **Startup crash**: Flashback's blit shaders declare no uniforms, which VulkanMod's shader
  converter can't compile. This mod provides equivalent shaders that it can compile.

## Known limits
- With VulkanMod the editor always uses Flashback's "Underlay" sizing: the game fills the
  window behind the editor panels.
- Keep the game window open (not minimized) while exporting.

## Building
Run `build.sh` from Git Bash (needs JDK 21 and a Prism Launcher install that has Minecraft 1.21.4, Fabric,
Flashback 0.39.10 and VulkanMod 0.5.6). It writes `build/flashback-for-vulkanmod-1.0.0.jar`.

Flashback is made by Moulberry and is not affiliated with this project.
