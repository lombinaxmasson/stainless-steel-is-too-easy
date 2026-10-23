package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.platform.BehaviorMachineContributor;
import com.gregtech.gregtech.api.addon.platform.MachineBehavior;
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
        for (AodTier tier : AodTier.values()) types.add(tier.machineType());
        types.add(VarFurnaceCore.MACHINE_TYPE);
        return types;
    }

    @Override
    public MachineBehavior create(String machineType) {
        if (VarFurnaceCore.MACHINE_TYPE.equals(machineType)) return new ArgonCrucibleBehavior(VarFurnaceCore.INSTANCE);
        AodTier tier = AodTier.byMachineType(machineType);
        if (tier == null) throw new IllegalArgumentException("Unknown AOD machine type: " + machineType);
        return new ArgonCrucibleBehavior(new AodConverterCore(tier));
    }
}
