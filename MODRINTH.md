# Modrinth project setup

## Project fields
- **Name:** Flashback for VulkanMod
- **Summary:** Makes the Flashback replay mod work with VulkanMod: replay editor, video export and shaders.
- **Project type:** Mod
- **Environment:** Client only (required on client, unsupported on server)
- **Categories:** Utility, Optimization (optional)
- **License:** MIT
- **URL slug:** `flashback-for-vulkanmod` (gives https://modrinth.com/mod/flashback-for-vulkanmod)
- **Source code:** https://github.com/princekadian/Flashback-for-VulkanMod
- **Issues:** https://github.com/princekadian/Flashback-for-VulkanMod/issues

## Version upload
- **File:** `build/flashback-for-vulkanmod-1.0.0.jar`
- **Version number:** 1.0.0
- **Loaders:** Fabric
- **Game versions:** 1.21.4
- **Dependencies (Required):** Flashback (0.39.10), VulkanMod (0.5.6 or newer)
- **Changelog:** First release.

## Description (paste into the project body)

Unofficial add-on that makes [Flashback](https://modrinth.com/mod/flashback) work with
[VulkanMod](https://modrinth.com/mod/vulkanmod) on Minecraft 1.21.4.

Without it, Flashback can't run under VulkanMod: the game fails to load Flashback's shaders,
the replay editor can't draw its interface, and video export reads frames with OpenGL.

### What it fixes
- **Startup:** provides Vulkan-compatible versions of Flashback's blit shaders.
- **Replay editor:** draws Flashback's editor interface through Minecraft's renderer, which
  VulkanMod supports.
- **Video export:** reads exported frames back with VulkanMod and renders the export at
  the resolution you choose.

### Requirements
- Minecraft 1.21.4 with Fabric Loader
- [Flashback](https://modrinth.com/mod/flashback) **0.39.10**
- [VulkanMod](https://modrinth.com/mod/vulkanmod) **0.5.6** or newer

### Known limits
- With VulkanMod the editor always uses Flashback's **Underlay** sizing: the game fills the
  window behind the editor panels.
- Keep the game window open (not minimized) while exporting.
- Only Flashback 0.39.10 is supported. Newer Flashback versions need an update of this add-on.

### Notes
This add-on contains none of Flashback's code; it changes Flashback while the game loads, and
only when VulkanMod is installed. Flashback is made by Moulberry. This project isn't affiliated
with Flashback or VulkanMod; please report problems with it here, not to them.
