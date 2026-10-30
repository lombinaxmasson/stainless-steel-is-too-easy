package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.FluidizedBedCore;
import com.lombinaxmasson.ssite.machine.HeatTransportMachines;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the combustor's HU reservoir internal, including the host's active pull path. */
@Mixin(targets = "gregtech.content.blockentity.machine.BehaviorMachineBlockEntity", remap = false)
public abstract class FluidizedBedEnergyMixin {
    @Inject(method = "pollEnergy", at = @At("HEAD"), cancellable = true)
    private void ssite$preventExternalHeatPull(Level level, BlockPos pos, CallbackInfo ci) {
        var machine = (BehaviorMachineClientView) this;
        if ("ssite".equals(machine.addonId())
                && (FluidizedBedCore.MACHINE_TYPE.equals(machine.machineType())
                    || HeatTransportMachines.isMachine(machine.machineType()))) {
            // pollEnergy bypasses MachineBehavior.acceptsEnergy. The separate pollAuxiliary
            // method still pulls KU for the blower; every other machine keeps its normal input.
            ci.cancel();
        }
    }
}
