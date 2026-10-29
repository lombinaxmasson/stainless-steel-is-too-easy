package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.VarFurnaceCore;
import com.lombinaxmasson.ssite.machine.FluidizedBedCore;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/** Culling boxes for the OBJ furnaces, which are drawn far outside their controller cell. */
public final class MachineRenderBounds {
    private MachineRenderBounds() {}

    /** Null when this block entity is not one of the addon's OBJ machines. */
    @Nullable
    public static AABB of(BlockEntity entity, @Nullable BlockPos at) {
        if (!(entity instanceof BehaviorMachineClientView view) || !"ssite".equals(view.addonId())) return null;
        BlockPos pos = at == null ? entity.getBlockPos() : at;
        String type = view.machineType();
        if (FluidizedBedCore.MACHINE_TYPE.equals(type)) return new AABB(pos).inflate(10.0D);
        if (LargeAodStructure.tier(type) != null) return new AABB(pos).inflate(5.0D);
        if (AodTier.byMachineType(type) != null || VarFurnaceCore.MACHINE_TYPE.equals(type))
            return new AABB(pos).inflate(2.0D);
        return null;
    }
}
