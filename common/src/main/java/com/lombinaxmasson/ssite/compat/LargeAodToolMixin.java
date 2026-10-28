package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.LargeAodBuilding;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.engine.multiblock.MultiblockToolTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/** Dispatch the wand before ordinary plane copying or opening the GUI. */
@Mixin(targets = "gregtech.content.blockentity.machine.BehaviorMachineBlockEntity", remap = false)
public abstract class LargeAodToolMixin extends BlockEntity implements MultiblockToolTarget {
    protected LargeAodToolMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public boolean containsToolPosition(BlockPos pos) {
        var view = (BehaviorMachineClientView) this;
        var tier = view.addonId().equals("ssite") ? LargeAodStructure.tier(view.machineType()) : null;
        if (tier == null) return false;
        if (worldPosition.equals(pos)) return true;
        var facing = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        return LargeAodStructure.preview(tier).keySet().stream().anyMatch(cell ->
                LargeAodStructure.world(worldPosition, facing, cell.getX(), cell.getY(), cell.getZ()).equals(pos));
    }

    @Override public long useMultiblockTool(UseOnContext context, List<Component> messages) {
        var view = (BehaviorMachineClientView) this;
        var tier = view.addonId().equals("ssite") ? LargeAodStructure.tier(view.machineType()) : null;
        return tier == null ? 0 : LargeAodBuilding.use(this, tier, context, messages);
    }
}
