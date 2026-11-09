package com.lombinaxmasson.ssite.forge.media;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.media.RecordCatalog;
import com.lombinaxmasson.ssite.media.RecordData;
import com.lombinaxmasson.ssite.media.RecordServer;
import com.lombinaxmasson.ssite.media.RecordTrack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Generic media items. Imported track data belongs to the stack, never the item registry.
 */
public final class ForgeRecordItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SsiteAddon.MOD_ID);
    public static final RegistryObject<Item> VINYL_RECORD = ITEMS.register(RecordCatalog.VINYL_ITEM,
            Pressing::new);
    public static final RegistryObject<Item> CD_RECORD = ITEMS.register(RecordCatalog.CD_ITEM,
            Pressing::new);

    private ForgeRecordItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private static final class Pressing extends Item {
        private Pressing() {
            super(new Item.Properties().stacksTo(1));
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            ItemStack stack = context.getItemInHand();
            if (!state.is(Blocks.JUKEBOX) || state.getValue(JukeboxBlock.HAS_RECORD)) {
                return InteractionResult.PASS;
            }
            if (RecordData.get(stack) == null) {
                if (level.isClientSide && context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(Component.translatable("message.ssite.record.empty"), true);
                }
                return InteractionResult.FAIL;
            }
            if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)) return InteractionResult.FAIL;
            if (!level.isClientSide) {
                ItemStack inserted = stack.copy();
                inserted.setCount(1);
                jukebox.setRecordWithoutPlaying(inserted);
                level.setBlock(pos, state.setValue(JukeboxBlock.HAS_RECORD, true), 3);
                stack.shrink(1);
                Player player = context.getPlayer();
                if (player != null) player.awardStat(Stats.PLAY_RECORD);
                RecordServer.changed(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            RecordTrack track = RecordData.get(stack);
            if (track == null) tooltip.add(Component.translatable("tooltip.ssite.record.blank"));
            else {
                tooltip.add(Component.literal(track.title()));
                int seconds = track.durationTicks() / 20;
                tooltip.add(Component.translatable("tooltip.ssite.record.duration", seconds / 60,
                        String.format(java.util.Locale.ROOT, "%02d", seconds % 60)));
            }
            super.appendHoverText(stack, level, tooltip, flag);
        }
    }
}
