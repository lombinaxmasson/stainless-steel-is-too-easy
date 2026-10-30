package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.HeatTransportCore;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum HeatTransportJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "heat_transport");
    @Override public ResourceLocation getUid() { return UID; }
    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof BehaviorMachineBlockEntity machine)
                || !"ssite".equals(machine.addonId()) || !HeatTransportCore.isTransport(machine.machineType())) return;
        CompoundTag data = new CompoundTag();
        data.putLong("stored", machine.storedEnergy());
        data.putInt("status", machine.state().getInt(HeatTransportCore.K_STATUS));
        data.putInt("ducts", machine.state().getInt(HeatTransportCore.K_DUCTS));
        data.putInt("plates", machine.state().getInt(HeatTransportCore.K_PLATES));
        data.putLong("output", machine.state().getLong(HeatTransportCore.K_OUTPUT));
        data.putLong("loss", machine.state().getLong(HeatTransportCore.K_LOSS));
        tag.put("ssite_heat_transport", data);
    }
    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound("ssite_heat_transport");
        if (data.isEmpty()) return;
        tooltip.add(Component.translatable(HeatTransportCore.statusTranslationKey(data.getInt("status"))));
        tooltip.add(Component.translatable("jade.ssite.heat_transport.network", data.getInt("ducts"), data.getInt("plates")));
        tooltip.add(Component.translatable("jade.ssite.heat_transport.output", data.getLong("output"), data.getLong("loss")));
        tooltip.add(Component.translatable("jade.ssite.heat_transport.stored", data.getLong("stored")));
        tooltip.add(Component.translatable("jade.ssite.heat_transport.rules"));
    }
}
