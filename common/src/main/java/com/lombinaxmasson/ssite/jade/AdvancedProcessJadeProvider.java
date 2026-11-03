package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.ElectronBeamCore;
import com.lombinaxmasson.ssite.machine.IonImplanterCore;
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

/** Shows the advanced process stop reason and the P-04 environment revision for both machines. */
public enum AdvancedProcessJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "advanced_process");
    private static final String DATA = "ssite_advanced_process";

    @Override public ResourceLocation getUid() { return UID; }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        if (!(entity instanceof BehaviorMachineBlockEntity machine) || !"ssite".equals(machine.addonId())) return;
        String type = machine.machineType();
        String statusKey, progressKey, reasonKey, revisionKey;
        long max;
        if (IonImplanterCore.MACHINE_TYPE.equals(type)) {
            statusKey = IonImplanterCore.K_STATUS;
            progressKey = IonImplanterCore.K_PROGRESS;
            reasonKey = IonImplanterCore.K_ENV_REASON;
            revisionKey = IonImplanterCore.K_ENV_REVISION;
            max = IonImplanterCore.TICKS_PER_BATCH;
        } else if (ElectronBeamCore.MACHINE_TYPE.equals(type)) {
            statusKey = ElectronBeamCore.K_STATUS;
            progressKey = ElectronBeamCore.K_PROGRESS;
            reasonKey = ElectronBeamCore.K_ENV_REASON;
            revisionKey = ElectronBeamCore.K_ENV_REVISION;
            max = ElectronBeamCore.TICKS_PER_BATCH;
        } else {
            return;
        }
        var state = machine.state();
        CompoundTag data = new CompoundTag();
        data.putString("type", type);
        data.putLong("status", state.getLong(statusKey));
        data.putLong("progress", state.getLong(progressKey));
        data.putLong("max", max);
        data.putString("reason", state.getString(reasonKey));
        data.putLong("revision", state.getLong(revisionKey));
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        if (data.isEmpty()) return;
        long status = data.getLong("status");
        String type = data.getString("type");
        tooltip.add(Component.translatable("ssite." + type + ".status." + status)
                .withStyle(status == IonImplanterCore.ST_ENV_INVALID ? ChatFormatting.RED
                        : status == IonImplanterCore.ST_WORKING ? ChatFormatting.GREEN
                        : ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("jade.ssite.advanced_process.env",
                Component.translatable("ssite.advanced_process.env." + data.getString("reason"))));
        tooltip.add(Component.translatable("jade.ssite.advanced_process.progress",
                data.getLong("progress"), data.getLong("max")));
        tooltip.add(Component.translatable("jade.ssite.advanced_process.revision",
                data.getLong("revision")).withStyle(ChatFormatting.DARK_AQUA));
    }
}
