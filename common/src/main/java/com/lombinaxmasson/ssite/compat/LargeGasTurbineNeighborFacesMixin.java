package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.LargeGasTurbineRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The hidden native cubes retain their collision but no longer hide adjacent rendered faces. */
@Mixin(targets = "net.minecraft.world.level.block.Block", remap = false)
public abstract class LargeGasTurbineNeighborFacesMixin {
    @Inject(method = {
            "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
            "m_152444_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private static void ssite$revealFaceNextToTurbine(BlockState state, BlockGetter level, BlockPos pos,
                                                     Direction face, BlockPos neighbor,
                                                     CallbackInfoReturnable<Boolean> result) {
        if (LargeGasTurbineRenderer.isModelBlock(level.getBlockState(neighbor).getBlock())
                && LargeGasTurbineRenderer.hidesBlock(level.getBlockEntity(neighbor))) {
            result.setReturnValue(true);
        }
    }
}
