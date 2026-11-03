package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.machine.PowderBedCore;
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

/** Shows the powder-bed pause reason, shielding gas, reclaim powder and real batch count. */
public enum PowderBedJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = ResourceLocation.tryBuild("ssite", "powder_bed");
    private static final String DATA = "ssite_powder_bed";

    @Override public ResourceLocation getUid() { return UID; }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var entity = accessor.getBlockEntity();
        if (!(entity instanceof BehaviorMachineBlockEntity machine) || !"ssite".equals(machine.addonId()))
            return;
        if (!PowderBedCore.MACHINE_TYPE.equals(machine.machineType())) return;
        var state = machine.state();
        CompoundTag data = new CompoundTag();
        data.putLong("status", state.getLong(PowderBedCore.K_STATUS));
        data.putLong("progress", state.getLong(PowderBedCore.K_PROGRESS));
        data.putLong("argon", state.getLong(PowderBedCore.K_ARGON));
        data.putLong("reclaim", state.getLong(PowderBedCore.K_RECLAIM));
        data.putLong("parallel", state.getLong(PowderBedCore.K_PARALLEL));
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        if (data.isEmpty()) return;
        long status = data.getLong("status");
        tooltip.add(Component.translatable("ssite.powder_bed.status." + status)
                .withStyle(status == PowderBedCore.ST_WORKING ? ChatFormatting.GREEN
                        : status == PowderBedCore.ST_OUTPUT_BLOCKED ? ChatFormatting.RED
                        : ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("jade.ssite.powder_bed.progress",
                data.getLong("progress"), PowderBedCore.TICKS_PER_BATCH));
        tooltip.add(Component.translatable("jade.ssite.powder_bed.argon", data.getLong("argon")));
        tooltip.add(Component.translatable("jade.ssite.powder_bed.reclaim", data.getLong("reclaim")));
        tooltip.add(Component.translatable("jade.ssite.powder_bed.parallel", data.getLong("parallel"))
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
