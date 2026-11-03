package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.CleanroomControllerCore;
import com.lombinaxmasson.ssite.machine.CleanroomThermostatCore;
import com.lombinaxmasson.ssite.machine.CleanroomVentCore;
import com.lombinaxmasson.ssite.machine.ProcessEnvironmentMachines;
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

/** Shows the P-04 room state, bands and filter wear; the same values come from sneak-use reports. */
public enum ProcessEnvironmentJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "process_environment");
    private static final String DATA = "ssite_process_environment";

    @Override public ResourceLocation getUid() { return UID; }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        if (!(entity instanceof BehaviorMachineBlockEntity machine) || !"ssite".equals(machine.addonId())) return;
        String type = machine.machineType();
        var state = machine.state();
        CompoundTag data = new CompoundTag();
        data.putString("type", type);
        if (ProcessEnvironmentMachines.CONTROLLER.equals(type)) {
            data.putLong("status", state.getLong(CleanroomControllerCore.K_STATUS));
            data.putString("reason", state.getString(CleanroomControllerCore.K_REASON));
            data.putLong("revision", state.getLong(CleanroomControllerCore.K_REVISION));
            data.putLong("clean", state.getLong(CleanroomControllerCore.K_CLEAN));
            data.putLong("temperature", state.getLong(CleanroomControllerCore.K_TEMPERATURE));
            data.putLong("vibration", state.getLong(CleanroomControllerCore.K_VIBRATION));
            data.putLong("volume", state.getLong(CleanroomControllerCore.K_VOLUME));
            data.putLong("vents", state.getLong(CleanroomControllerCore.K_VENTS));
            data.putLong("leaks", state.getLong(CleanroomControllerCore.K_LEAKS));
            data.putLong("filter", state.getLong(CleanroomControllerCore.K_FILTER));
        } else if (ProcessEnvironmentMachines.VENT.equals(type)) {
            data.putLong("status", state.getLong(CleanroomVentCore.K_STATUS));
            data.putLong("remaining", state.getLong(CleanroomVentCore.K_REMAINING));
        } else if (ProcessEnvironmentMachines.THERMOSTAT.equals(type)) {
            data.putLong("status", state.getLong(CleanroomThermostatCore.K_STATUS));
            data.putLong("coolant", state.getLong(CleanroomThermostatCore.K_COOLANT));
        } else {
            return;
        }
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        if (data.isEmpty()) return;
        String type = data.getString("type");
        long status = data.getLong("status");
        if (ProcessEnvironmentMachines.CONTROLLER.equals(type)) {
            tooltip.add(Component.translatable("ssite.cleanroom_controller.status." + status)
                    .withStyle(status == CleanroomControllerCore.ST_VALID ? ChatFormatting.GREEN
                            : status == CleanroomControllerCore.ST_INVALID ? ChatFormatting.RED
                            : ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("jade.ssite.process_environment.env",
                    Component.translatable("ssite.advanced_process.env." + data.getString("reason"))));
            tooltip.add(Component.translatable("jade.ssite.process_environment.bands",
                    data.getLong("clean"), data.getLong("temperature"), data.getLong("vibration")));
            tooltip.add(Component.translatable("jade.ssite.process_environment.room",
                    data.getLong("volume"), data.getLong("vents"), data.getLong("leaks")));
            tooltip.add(Component.translatable("jade.ssite.process_environment.filter",
                    data.getLong("filter") / 10));
            tooltip.add(Component.translatable("jade.ssite.process_environment.revision",
                    data.getLong("revision")).withStyle(ChatFormatting.DARK_AQUA));
        } else if (ProcessEnvironmentMachines.VENT.equals(type)) {
            tooltip.add(Component.translatable("ssite.cleanroom_filter_vent.status." + status));
            tooltip.add(Component.translatable("jade.ssite.process_environment.filter",
                    data.getLong("remaining") / 10));
        } else if (ProcessEnvironmentMachines.THERMOSTAT.equals(type)) {
            tooltip.add(Component.translatable("ssite.cleanroom_thermostat.status." + status));
            tooltip.add(Component.translatable("jade.ssite.process_environment.coolant",
                    data.getLong("coolant")));
        }
    }
}
