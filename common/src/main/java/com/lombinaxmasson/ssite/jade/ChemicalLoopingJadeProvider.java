package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.AirReactorCore;
import com.lombinaxmasson.ssite.machine.FuelReactorCore;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Read-only inspection of both chemical-looping beds; Jade remains an optional add-on. */
public enum ChemicalLoopingJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "chemical_looping");
    private static final String DATA = "ssite_chemical_looping";

    @Override public ResourceLocation getUid() { return UID; }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        if (!(entity instanceof BehaviorMachineBlockEntity machine) || !"ssite".equals(machine.addonId())) return;
        String type = machine.machineType();
        CompoundTag data = new CompoundTag();
        data.putString("type", type);
        var state = machine.state();
        if (FuelReactorCore.MACHINE_TYPE.equals(type)) {
            data.putLong(FuelReactorCore.K_STATUS, state.getLong(FuelReactorCore.K_STATUS));
            data.putLong(FuelReactorCore.K_PROGRESS, state.getLong(FuelReactorCore.K_PROGRESS));
            data.putBoolean(FuelReactorCore.K_LIT, state.getBoolean(FuelReactorCore.K_LIT));
            data.putLong(FuelReactorCore.K_STORED, state.getLong(FuelReactorCore.K_STORED));
            data.putLong(FuelReactorCore.K_KU, state.getLong(FuelReactorCore.K_KU));
            data.putLong(FuelReactorCore.K_CO2, state.getLong(FuelReactorCore.K_CO2));
            data.putLong(FuelReactorCore.K_WATER, state.getLong(FuelReactorCore.K_WATER));
            data.putBoolean(FuelReactorCore.K_RECEIVER, state.getBoolean(FuelReactorCore.K_RECEIVER));
        } else if (AirReactorCore.MACHINE_TYPE.equals(type)) {
            data.putLong(AirReactorCore.K_STATUS, state.getLong(AirReactorCore.K_STATUS));
            data.putLong(AirReactorCore.K_PROGRESS, state.getLong(AirReactorCore.K_PROGRESS));
            data.putLong(AirReactorCore.K_STORED, state.getLong(AirReactorCore.K_STORED));
            data.putLong(AirReactorCore.K_KU, state.getLong(AirReactorCore.K_KU));
            data.putLong(AirReactorCore.K_BATCHES, state.getLong(AirReactorCore.K_BATCHES));
            data.putBoolean(AirReactorCore.K_WEAR, state.getBoolean(AirReactorCore.K_WEAR));
            data.putString(AirReactorCore.K_VENT, state.getString(AirReactorCore.K_VENT));
        } else return;
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        String type = data.getString("type");
        if (FuelReactorCore.MACHINE_TYPE.equals(type)) {
            tooltip.add(Component.translatable("ssite.fuel_reactor.status."
                    + data.getLong(FuelReactorCore.K_STATUS)));
            tooltip.add(Component.translatable("jade.ssite.fuel_reactor.progress",
                    data.getLong(FuelReactorCore.K_PROGRESS), FuelReactorCore.BATCH_TICKS));
            tooltip.add(Component.translatable("jade.ssite.fuel_reactor.heat",
                    data.getLong(FuelReactorCore.K_STORED), FuelReactorCore.HEAT_CAPACITY)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("jade.ssite.fuel_reactor.blower",
                    FuelReactorCore.KU_PER_TICK, data.getLong(FuelReactorCore.K_KU)));
            tooltip.add(Component.translatable("jade.ssite.fuel_reactor.outlet",
                    data.getLong(FuelReactorCore.K_CO2), data.getLong(FuelReactorCore.K_WATER),
                    data.getBoolean(FuelReactorCore.K_RECEIVER)
                            ? Component.translatable("jade.ssite.chemical_looping.connected")
                            : Component.translatable("jade.ssite.chemical_looping.venting"))
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("jade.ssite.fuel_reactor.controls").withStyle(ChatFormatting.GRAY));
        } else if (AirReactorCore.MACHINE_TYPE.equals(type)) {
            tooltip.add(Component.translatable("ssite.air_reactor.status."
                    + data.getLong(AirReactorCore.K_STATUS)));
            tooltip.add(Component.translatable("jade.ssite.air_reactor.progress",
                    data.getLong(AirReactorCore.K_PROGRESS), AirReactorCore.BATCH_TICKS));
            tooltip.add(Component.translatable("jade.ssite.air_reactor.heat",
                    data.getLong(AirReactorCore.K_STORED), AirReactorCore.HEAT_CAPACITY)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("jade.ssite.air_reactor.blower",
                    AirReactorCore.KU_PER_TICK, data.getLong(AirReactorCore.K_KU)));
            tooltip.add(Component.translatable("jade.ssite.air_reactor.wear",
                    data.getLong(AirReactorCore.K_BATCHES), AirReactorCore.WEAR_PERIOD,
                    data.getBoolean(AirReactorCore.K_WEAR)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.ssite.air_reactor.controls").withStyle(ChatFormatting.GRAY));
        }
    }
}
