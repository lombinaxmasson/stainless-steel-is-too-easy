package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.VarFurnaceCore;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GT6CE 0.1.0 checks signal emitted by the nonconducting faucet itself. Use incoming
 * power when its input is one of our crucibles, preserving the faucet's auto-pull switch.
 * No GT implementation classes are linked; the optional injection may retire with a host fix.
 */
@Mixin(targets = "gregtech.content.blockentity.machine.CrucibleFaucetBlockEntity", remap = false)
public abstract class CrucibleFaucetSignalMixin extends BlockEntity {
    protected CrucibleFaucetSignalMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "hasRedstoneIncoming", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssite$incomingPower(CallbackInfoReturnable<Boolean> result) {
        if (level == null || !getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)) return;
        BlockPos input = worldPosition.relative(getBlockState().getValue(HorizontalDirectionalBlock.FACING));
        if (!(level.getBlockEntity(input) instanceof BehaviorMachineClientView view) || !view.addonId().equals("ssite")) return;
        String type = view.machineType();
        if (LargeAodStructure.tier(type) != null || AodTier.byMachineType(type) != null || VarFurnaceCore.MACHINE_TYPE.equals(type))
            result.setReturnValue(level.hasNeighborSignal(worldPosition));
    }
}
