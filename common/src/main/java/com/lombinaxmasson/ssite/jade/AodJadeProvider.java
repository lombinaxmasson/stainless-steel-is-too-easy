package com.lombinaxmasson.ssite.jade;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.MoltenPool;
import com.lombinaxmasson.ssite.machine.VarFurnaceCore;
import gregtech.api.addon.material.GregTechMaterials;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.PartClientView;
import gregtech.api.material.GTValues;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import gregtech.content.blockentity.machine.MultiblockPortBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Crucible-style Jade inspection for the AOD converters, the large AOD and the VAR furnace:
 * temperature, buffered gas, meltdown limit and every non-empty pool. Works on the controller
 * and on the parts bound to it.
 */
public enum AodJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(SsiteAddon.MOD_ID, "aod_furnace");
    private static final String DATA = "ssite_aod";
    private static final String TEMPERATURE = "temperature";
    private static final String LIMIT = "limit";
    private static final String ARGON = "argon";
    private static final String AIR = "air";
    private static final String FORMED = "formed";
    private static final String OPEN = "open";
    private static final String CONTENTS = "contents";
    private static final String MATERIAL = "material";
    private static final String AMOUNT = "amount";

    @Override public ResourceLocation getUid() {
        return UID;
    }

    @Override public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BehaviorMachineBlockEntity machine = machine(accessor.getBlockEntity());
        if (machine == null) return;
        MoltenPool pool = machine.behavior() instanceof MoltenPool.Holder holder ? holder.pool() : null;
        if (pool == null) return;
        BehaviorState state = machine.state();
        CompoundTag data = new CompoundTag();
        data.putBoolean(FORMED, machine.structureFormed());
        data.putBoolean(OPEN, lidOpen(machine));
        data.putLong(TEMPERATURE, MoltenPool.temperature(state));
        data.putLong(ARGON, pool.argon(state));
        data.putLong(AIR, pool.air(state));
        long limit = pool.temperatureLimitK();
        if (limit > 0) data.putLong(LIMIT, limit);
        ListTag contents = new ListTag();
        for (MoltenPool.Content row : pool.contents(state)) {
            CompoundTag entry = new CompoundTag();
            entry.putString(MATERIAL, row.material());
            entry.putLong(AMOUNT, row.amount());
            contents.add(entry);
        }
        data.put(CONTENTS, contents);
        tag.put(DATA, data);
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData().getCompound(DATA);
        if (data.isEmpty()) return;
        long temperature = data.getLong(TEMPERATURE);
        tooltip.add(Component.translatable("jade.ssite.aod.temperature", temperature, temperature - 273)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(data.getBoolean(FORMED)
                        ? "jade.ssite.aod.formed" : "jade.ssite.aod.incomplete")
                .withStyle(data.getBoolean(FORMED) ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable(data.getBoolean(OPEN)
                ? "jade.ssite.aod.lid.open" : "jade.ssite.aod.lid.closed"));
        if (data.contains(LIMIT)) {
            tooltip.add(Component.translatable("jade.ssite.aod.melt_limit", data.getLong(LIMIT)));
            if (temperature > data.getLong(LIMIT))
                tooltip.add(Component.translatable("jade.ssite.aod.warning").withStyle(ChatFormatting.RED));
        }
        long argon = data.getLong(ARGON);
        if (argon > 0) tooltip.add(Component.translatable("jade.ssite.aod.gas.argon", argon));
        long air = data.getLong(AIR);
        if (air > 0) tooltip.add(Component.translatable("jade.ssite.aod.gas.air", air));
        for (Tag entry : data.getList(CONTENTS, Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) entry;
            String material = row.getString(MATERIAL);
            tooltip.add(Component.translatable("jade.ssite.aod.content",
                    units(row.getLong(AMOUNT)), materialName(material), phase(material, temperature)));
        }
    }

    /** The controller behind a clicked part, when that controller is one of the addon's furnaces. */
    private static BehaviorMachineBlockEntity machine(BlockEntity entity) {
        if (entity instanceof MultiblockPortBlockEntity port) entity = port.sensorTarget();
        if (!(entity instanceof BehaviorMachineBlockEntity machine)) return null;
        if (!SsiteAddon.MOD_ID.equals(machine.addonId())) return null;
        String type = machine.machineType();
        return LargeAodStructure.tier(type) != null || AodTier.byMachineType(type) != null
                || VarFurnaceCore.MACHINE_TYPE.equals(type) ? machine : null;
    }

    private static boolean lidOpen(BehaviorMachineBlockEntity machine) {
        var level = machine.getLevel();
        if (level == null) return false;
        BlockPos pos = machine.getBlockPos();
        BlockState state = machine.getBlockState();
        Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        BlockPos lid = LargeAodStructure.tier(machine.machineType()) != null
                ? LargeAodStructure.world(pos, facing, 0, 3, 2) : pos.above();
        return level.getBlockEntity(lid) instanceof PartClientView part && part.getBoolean("open");
    }

    private static Component materialName(String name) {
        var material = GregTechMaterials.find(name);
        return material.valid()
                ? Component.translatableWithFallback(material.translationKey(), name)
                : Component.literal(name);
    }

    private static Component phase(String name, long temperature) {
        var material = GregTechMaterials.find(name);
        String phase = !material.valid() || temperature < material.meltingPoint() ? "solid"
                : temperature >= material.boilingPoint() ? "gas" : "molten";
        return Component.translatable("jade.ssite.aod.phase." + phase);
    }

    private static String units(long amount) {
        long milli = (amount % GTValues.U) * 1000L / GTValues.U;
        return (amount / GTValues.U) + "." + String.format(java.util.Locale.ROOT, "%03d", milli);
    }
}
