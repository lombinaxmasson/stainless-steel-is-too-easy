package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.FluidizedBedCore;
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

/** Inspects either the control panel or one of its bound heating/structural parts. */
public enum FluidizedBedJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "fluidized_bed");
    private static final String DATA = "ssite_fluidized_bed";

    @Override public ResourceLocation getUid() { return UID; }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        if (entity instanceof MultiblockPortBlockEntity part) entity = part.sensorTarget();
        if (!(entity instanceof BehaviorMachineBlockEntity machine) || !"ssite".equals(machine.addonId())
                || !FluidizedBedCore.MACHINE_TYPE.equals(machine.machineType())) return;
        var state = machine.state();
        CompoundTag data = new CompoundTag();
        data.putBoolean("formed", machine.structureFormed());
        for (String key : new String[]{FluidizedBedCore.K_POWER, FluidizedBedCore.K_STATUS,
                FluidizedBedCore.K_STORED, FluidizedBedCore.K_OUTPUT, FluidizedBedCore.K_KU,
                FluidizedBedCore.K_PENDING}) data.putLong(key, state.getLong(key));
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        if (data.isEmpty()) return;
        int power = FluidizedBedCore.power(data.getLong(FluidizedBedCore.K_POWER));
        tooltip.add(Component.translatable(data.getBoolean("formed")
                ? "jade.ssite.aod.formed" : "jade.ssite.aod.incomplete")
                .withStyle(data.getBoolean("formed") ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.status." + data.getLong(FluidizedBedCore.K_STATUS)));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.power", power, FluidizedBedCore.outputRate(power)));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.fuel_yield", FluidizedBedCore.FUEL_YIELD_PERCENT));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.output", data.getLong(FluidizedBedCore.K_OUTPUT))
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.stored", data.getLong(FluidizedBedCore.K_STORED)));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.blower", FluidizedBedCore.kuPerTick(power), data.getLong(FluidizedBedCore.K_KU)));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.pending", data.getLong(FluidizedBedCore.K_PENDING)));
        tooltip.add(Component.translatable("jade.ssite.fluidized_bed.controls").withStyle(ChatFormatting.GRAY));
    }
}
