package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonTool;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.machine.MachineFace;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class WindTurbineBehavior implements MachineBehavior {
    private final WindTurbineCore core = new WindTurbineCore();

    @Override public void tick(MachineBehaviorContext context) { core.tick(context); }
    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return core.onUse(context, player, hand);
    }
    @Override public boolean onTool(MachineBehaviorContext context, AddonTool tool, Player player,
                                    InteractionHand hand, MachineFace face) {
        return tool == AddonTool.WRENCH && core.onWrench(context, player);
    }
    @Override public void beforeBreak(MachineBehaviorContext context) { core.beforeBreak(); }
    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) { return false; }
    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) { return false; }
}
