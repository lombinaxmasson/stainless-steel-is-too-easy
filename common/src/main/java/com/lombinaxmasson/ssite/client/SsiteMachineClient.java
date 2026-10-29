package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.VarFurnaceCore;
import com.lombinaxmasson.ssite.machine.FluidizedBedCore;
import com.mojang.logging.LogUtils;
import gregtech.api.addon.platform.BehaviorMachineClient;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

/** Client entry point: GregTech loads this from META-INF/services on the client only. */
public final class SsiteMachineClient implements BehaviorMachineClient {
    private static final Set<String> TYPES;

    static {
        Set<String> types = new LinkedHashSet<>();
        for (AodTier tier : AodTier.values()) { types.add(tier.machineType()); types.add(tier.largeMachineType()); }
        types.add(VarFurnaceCore.MACHINE_TYPE);
        types.add(FluidizedBedCore.MACHINE_TYPE);
        TYPES = Set.copyOf(types);
    }

    private final SmallFurnaceRenderer renderer;
    private final FluidizedBedRenderer fluidizedBedRenderer;

    public SsiteMachineClient() {
        MeshSink sink = null;
        for (MeshSink found : ServiceLoader.load(MeshSink.class)) {
            sink = found;
            break;
        }
        if (sink == null) throw new IllegalStateException("ssite: no MeshSink service for this loader");
        LogUtils.getLogger().info("[ssite] OBJ machine renderer using {}", sink.getClass().getName());
        renderer = new SmallFurnaceRenderer(sink);
        fluidizedBedRenderer = new FluidizedBedRenderer(sink);
    }

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public Set<String> machineTypes() {
        return TYPES;
    }

    @Override
    public BlockEntityRenderer<BlockEntity> renderer(String machineType) {
        if (FluidizedBedCore.MACHINE_TYPE.equals(machineType)) return fluidizedBedRenderer;
        return TYPES.contains(machineType) ? renderer : null;
    }
}
