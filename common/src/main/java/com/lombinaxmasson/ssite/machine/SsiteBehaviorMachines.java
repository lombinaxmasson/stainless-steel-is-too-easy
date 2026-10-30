package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorMachineContributor;
import gregtech.api.addon.platform.MachineBehavior;
import com.lombinaxmasson.ssite.SsiteAddon;

import java.util.LinkedHashSet;
import java.util.Set;

/** Creates the AOD behavior for a placed block. The platform class holds the argon tank. */
public final class SsiteBehaviorMachines implements BehaviorMachineContributor {
    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public Set<String> machineTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (AodTier tier : AodTier.values()) { types.add(tier.machineType()); types.add(tier.largeMachineType()); }
        types.add(VarFurnaceCore.MACHINE_TYPE);
        types.add(FluidizedBedCore.MACHINE_TYPE);
        types.add(HeatTransportMachines.DUCT);
        types.add(HeatTransportMachines.PLATE);
        return types;
    }

    @Override
    public MachineBehavior create(String machineType) {
        if (FluidizedBedCore.MACHINE_TYPE.equals(machineType)) return new FluidizedBedBehavior();
        if (HeatTransportMachines.isMachine(machineType)) return new HeatTransportBehavior();
        AodTier large = LargeAodStructure.tier(machineType);
        if (large != null) return new ArgonCrucibleBehavior(new LargeAodCore(large));
        if (VarFurnaceCore.MACHINE_TYPE.equals(machineType)) return new ArgonCrucibleBehavior(VarFurnaceCore.INSTANCE);
        AodTier tier = AodTier.byMachineType(machineType);
        if (tier == null) throw new IllegalArgumentException("Unknown AOD machine type: " + machineType);
        return new ArgonCrucibleBehavior(new AodConverterCore(tier));
    }
}
