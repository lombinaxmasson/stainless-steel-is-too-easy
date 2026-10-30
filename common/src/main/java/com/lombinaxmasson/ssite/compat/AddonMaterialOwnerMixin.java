package com.lombinaxmasson.ssite.compat;

import gregtech.api.mod.ModData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Reuse the owner's metadata while GT6CE installs multiple materials from the same addon. */
@Mixin(targets = "gregtech.engine.addon.AddonMaterials", remap = false)
public abstract class AddonMaterialOwnerMixin {
    @Redirect(method = {"installDefinitions", "linkAddons"},
            at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;)Lgregtech/api/mod/ModData;"),
            require = 2)
    private static ModData ssite$reuseMaterialOwner(String id, String name) {
        // GT6CE 1.0.2 constructs ModData for every material, but its new registry rejects
        // duplicate mod IDs. Keep that global validation intact and reuse ownership metadata
        // only at these two material-installation sites; do not change material declarations.
        synchronized (ModData.class) {
            ModData existing = ModData.MODS.get(id);
            return existing != null ? existing : new ModData(id, name);
        }
    }
}
