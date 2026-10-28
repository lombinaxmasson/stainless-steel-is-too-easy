package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.LayoutPartContributor;
import gregtech.api.addon.platform.PartBehavior;
import gregtech.api.addon.platform.PartContext;
import gregtech.api.addon.platform.PartHit;
import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

/** The hinged lid above an AOD converter or VAR furnace. Its saved {@code open} flag gates top charging and gas retention. */
public final class SsiteLidPart implements LayoutPartContributor {
    private static final String OPEN = "open";
    private static final String MODE = "lid.mode";

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public Set<String> partNames() {
        return Set.of(MoltenPool.LID_PART, LargeAodStructure.LID, LargeAodStructure.ARGON, LargeAodStructure.OXYGEN);
    }

    @Override
    public PartBehavior create(String partName) {
        if (LargeAodStructure.ARGON.equals(partName) || LargeAodStructure.OXYGEN.equals(partName))
            return ArgonCrucibleBehavior.gasPort(partName);
        if (!MoltenPool.LID_PART.equals(partName) && !LargeAodStructure.LID.equals(partName)) {
            throw new IllegalArgumentException("Unknown AOD part: " + partName);
        }
        return new LidBehavior();
    }

    public static final class LidBehavior implements PartBehavior {
        /** How far below the open lid a thrown stack is still caught. */
        private static final int SUCTION_DEPTH = 1;

        @Override public void tick(PartContext context) {
            int mode = context.state().getInt(MODE);
            if (mode != 0) setOpen(context, redstoneOpen(mode, context.level().hasNeighborSignal(context.pos()), false));
            absorb(context);
        }

        static boolean redstoneOpen(int mode, boolean signal, boolean manualOpen) {
            return mode == 1 ? signal : mode == 2 ? !signal : manualOpen;
        }

        /** Only what the furnace below can melt; an unbound lid takes nothing, so its one slot cannot jam. */
        @Override
        public boolean canInsert(PartContext context, int slot, ItemStack stack) {
            return slot == 0 && context.state().getBoolean(OPEN)
                    && context.controller() instanceof MoltenPool.Holder holder
                    && holder.pool().isCharge(stack);
        }

        @Override
        public boolean canExtract(PartContext context, int slot) {
            return context.state().getBoolean(OPEN) && (!context.name().equals(LargeAodStructure.LID) || slot == 1);
        }

        @Override
        public boolean onUse(PartContext context, Player player, InteractionHand hand, PartHit hit) {
            if (player.isShiftKeyDown()) {
                int mode = (context.state().getInt(MODE) + 1) % 3;
                context.state().setInt(MODE, mode);
                tick(context);
                player.displayClientMessage(Component.translatable("ssite.aod.lid.mode." + mode), true);
                return true;
            }
            int mode = context.state().getInt(MODE);
            if (mode == 0) setOpen(context, !context.state().getBoolean(OPEN));
            else player.displayClientMessage(Component.translatable("ssite.aod.lid.mode." + mode), true);
            return true;
        }

        /**
         * GT6 crucible intake: a dropped stack falls through the open lid and waits in the charge
         * slot. A closed or unbound lid takes nothing, so a hopper cannot bypass the seal either.
         */
        private static void absorb(PartContext context) {
            if (context.level().isClientSide || !context.state().getBoolean(OPEN)) return;
            if (context.slots() < 1 || !context.item(0).isEmpty()) return;
            if (!(context.controller() instanceof MoltenPool.Holder holder)) return;
            var pos = context.pos();
            AABB area = new AABB(pos.getX(), pos.getY() - SUCTION_DEPTH, pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            for (ItemEntity entity : context.level().getEntitiesOfClass(ItemEntity.class, area)) {
                if (entity.isRemoved()) continue;
                ItemStack stack = entity.getItem();
                if (stack.isEmpty() || !holder.pool().isCharge(stack)) continue;
                context.setItem(0, stack.copy());
                entity.discard();
                return;
            }
        }

        private static void setOpen(PartContext context, boolean open) {
            if (context.state().getBoolean(OPEN) == open) return;
            context.state().setBoolean(OPEN, open);
            context.level().playSound(null, context.pos(),
                    open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.7F, open ? 1.1F : 0.9F);
        }

        @Override
        public List<Component> describe(PartContext context) {
            return List.of(Component.translatable(
                    context.state().getBoolean(OPEN) ? "ssite.aod.lid.open" : "ssite.aod.lid.closed"),
                    Component.translatable("ssite.aod.lid.mode." + context.state().getInt(MODE)));
        }
    }
}
