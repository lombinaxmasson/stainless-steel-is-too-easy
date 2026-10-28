package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** The host's EMI/JEI assembly catalog currently omits addon behavior machines. */
@Mixin(targets = "gregtech.content.multiblock.ControllerStructureLayouts", remap = false)
public abstract class LargeAodPreviewMixin {
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void ssite$layouts(CallbackInfoReturnable<Map<Block, Map<BlockPos, Block>>> result) {
        var layouts = new LinkedHashMap<>(result.getReturnValue());
        for (AodTier tier : AodTier.values()) {
            var id = ResourceLocation.tryBuild("ssite", tier.largeMachineType());
            if (BuiltInRegistries.BLOCK.containsKey(id))
                layouts.put(BuiltInRegistries.BLOCK.get(id), LargeAodStructure.preview(tier));
        }
        result.setReturnValue(Collections.unmodifiableMap(layouts));
    }
}
