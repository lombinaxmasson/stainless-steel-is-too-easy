package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.platform.LayoutPartContributor;
import com.gregtech.gregtech.api.addon.platform.PartBehavior;
import com.gregtech.gregtech.api.addon.platform.PartContext;
import com.gregtech.gregtech.api.addon.platform.PartHit;
import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

/** The hinged lid above an AOD converter. Its saved {@code open} flag gates top charging and gas retention. */
public final class SsiteLidPart implements LayoutPartContributor {
    private static final String OPEN = "open";

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public Set<String> partNames() {
        return Set.of(AodConverterCore.LID_PART);
    }

    @Override
    public PartBehavior create(String partName) {
        if (!AodConverterCore.LID_PART.equals(partName)) {
            throw new IllegalArgumentException("Unknown AOD part: " + partName);
        }
        return new LidBehavior();
    }

    public static final class LidBehavior implements PartBehavior {
        @Override
        public boolean canInsert(PartContext context, int slot, ItemStack stack) {
            return context.state().getBoolean(OPEN) && AodConverterCore.isCharge(stack);
        }

        @Override
        public boolean canExtract(PartContext context, int slot) {
            return context.state().getBoolean(OPEN);
        }

        @Override
        public boolean onUse(PartContext context, Player player, InteractionHand hand, PartHit hit) {
            boolean open = !context.state().getBoolean(OPEN);
            context.state().setBoolean(OPEN, open);
            context.level().playSound(null, context.pos(),
                    open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.7F, open ? 1.1F : 0.9F);
            return true;
        }

        @Override
        public List<Component> describe(PartContext context) {
            return List.of(Component.translatable(
                    context.state().getBoolean(OPEN) ? "ssite.aod.lid.open" : "ssite.aod.lid.closed"));
        }
    }
}
