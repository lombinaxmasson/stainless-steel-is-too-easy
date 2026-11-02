package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.IrradiationReactorCore;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import gregtech.content.blockentity.machine.MultiblockPortBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Inspects the vessel or the heat port bound to it; Jade stays an optional client add-on. */
public enum IrradiationReactorJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "irradiation_reactor");
    private static final String DATA = "ssite_irradiation_reactor";

    @Override public ResourceLocation getUid() { return UID; }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        if (entity instanceof MultiblockPortBlockEntity part) entity = part.sensorTarget();
        if (!(entity instanceof BehaviorMachineBlockEntity machine) || !"ssite".equals(machine.addonId())
                || !IrradiationReactorCore.MACHINE_TYPE.equals(machine.machineType())) return;
        var state = machine.state();
        CompoundTag data = new CompoundTag();
        data.putBoolean("formed", machine.structureFormed());
        for (String key : new String[]{IrradiationReactorCore.K_STATUS, IrradiationReactorCore.K_PROGRESS,
                IrradiationReactorCore.K_TEMPERATURE, IrradiationReactorCore.K_STORED,
                IrradiationReactorCore.K_GENERATED, IrradiationReactorCore.K_EMITTED}) {
            data.putLong(key, state.getLong(key));
        }
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        if (data.isEmpty()) return;
        boolean formed = data.getBoolean("formed");
        tooltip.add(Component.translatable(formed ? "jade.ssite.aod.formed" : "jade.ssite.aod.incomplete")
                .withStyle(formed ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable("jade.ssite.irradiation_reactor.status."
                + data.getLong(IrradiationReactorCore.K_STATUS)));
        tooltip.add(Component.translatable("jade.ssite.irradiation_reactor.progress",
                data.getLong(IrradiationReactorCore.K_PROGRESS), IrradiationReactorCore.BATCH_TICKS));
        tooltip.add(Component.translatable("jade.ssite.irradiation_reactor.temperature",
                data.getLong(IrradiationReactorCore.K_TEMPERATURE), IrradiationReactorCore.OVERHEAT_K)
                .withStyle(data.getLong(IrradiationReactorCore.K_TEMPERATURE) >= IrradiationReactorCore.OVERHEAT_K
                        ? ChatFormatting.RED : ChatFormatting.AQUA));
        tooltip.add(Component.translatable("jade.ssite.irradiation_reactor.heat",
                data.getLong(IrradiationReactorCore.K_GENERATED), data.getLong(IrradiationReactorCore.K_EMITTED),
                data.getLong(IrradiationReactorCore.K_STORED)).withStyle(ChatFormatting.GOLD));
    }
}
