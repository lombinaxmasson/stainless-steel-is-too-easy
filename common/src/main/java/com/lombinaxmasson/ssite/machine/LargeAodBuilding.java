package com.lombinaxmasson.ssite.machine;

import gregtech.engine.multiblock.MultiblockToolRules;
import gregtech.engine.tool.GTToolHelper;
import gregtech.engine.tool.GTToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

/** One click fills the fixed layout, consuming inventory through normal item placement. */
public final class LargeAodBuilding {
    private LargeAodBuilding() {}

    public static long use(BlockEntity controller, AodTier tier, UseOnContext use, List<Component> messages) {
        return use(controller, LargeAodStructure.preview(tier), "ssite.aod", use, messages);
    }

    public static long use(BlockEntity controller, Map<BlockPos, Block> layout, String prefix,
                           UseOnContext use, List<Component> messages) {
        var level = use.getLevel();
        if (level.isClientSide || use.getPlayer() == null) return 0;
        boolean builder = GTToolHelper.matchesTool(use.getItemInHand(), GTToolType.BUILDER_WAND);
        if (!builder && !GTToolHelper.matchesTool(use.getItemInHand(), GTToolType.MAGNIFYING_GLASS)) return 0;
        var facing = controller.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        int placed = 0, missing = 0;
        for (var cell : layout.entrySet()) {
            BlockPos local = cell.getKey();
            BlockPos pos = LargeAodStructure.world(controller.getBlockPos(), facing,
                    local.getX(), local.getY(), local.getZ());
            Block expected = cell.getValue();
            if (!level.hasChunkAt(pos)) { missing++; continue; }
            if (expected == Blocks.AIR ? level.getBlockState(pos).isAir() : level.getBlockState(pos).is(expected)) continue;
            if (builder && expected != Blocks.AIR && place(use, pos, expected)) placed++;
            else missing++;
        }
        messages.add(builder ? Component.translatable(prefix + ".build", placed, missing)
                : Component.translatable(prefix + ".inspect", missing));
        // Consume even an empty-inventory click; never fall through to copying controllers.
        return builder ? MultiblockToolRules.BUILDER_COST : MultiblockToolRules.MAGNIFIER_COST;
    }

    private static boolean place(UseOnContext use, BlockPos pos, Block expected) {
        var level = use.getLevel();
        var player = use.getPlayer();
        if (player == null || !level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || !level.mayInteract(player, pos) || !level.getBlockState(pos).canBeReplaced()) return false;
        ItemStack source = ItemStack.EMPTY;
        if (player.getAbilities().instabuild) source = new ItemStack(expected);
        else for (int slot = player.getInventory().getContainerSize() - 1; slot >= 0; slot--) {
            var candidate = player.getInventory().getItem(slot);
            if (candidate.is(expected.asItem())) { source = candidate; break; }
        }
        if (source.isEmpty() || !player.mayUseItemAt(pos, Direction.UP, source)) return false;
        ItemStack copy = source.copy();
        ItemStack held = player.getItemInHand(use.getHand());
        var placement = new UseOnContext(level, player, use.getHand(), copy,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        boolean accepted;
        try {
            // Includes the loader's placement/protection events and cancellation rollback.
            accepted = copy.useOn(placement).consumesAction();
        } finally {
            if (player.getItemInHand(use.getHand()) == copy) player.setItemInHand(use.getHand(), held);
        }
        if (!accepted || !level.getBlockState(pos).is(expected)) return false;
        if (!player.getAbilities().instabuild) source.shrink(Math.max(0, source.getCount() - copy.getCount()));
        return true;
    }
}
