package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.media.RecordCatalog;
import com.lombinaxmasson.ssite.media.RecordData;
import com.lombinaxmasson.ssite.media.RecordServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Named/SRG aliases bridge Forge's development and production methods without a global refmap. */
@Mixin(JukeboxBlock.class)
public abstract class RecordJukeboxSignalMixin {
    @Inject(method = {"getSignal", "m_6378_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void ssite$importedSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction,
                                      CallbackInfoReturnable<Integer> result) {
        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox
                && RecordData.isRecord(jukebox.getItem(0))) {
            Level level = jukebox.getLevel();
            result.setReturnValue(level != null && RecordServer.isPlaying(level, pos) ? 15 : 0);
        }
    }

    @Inject(method = {"getAnalogOutputSignal", "m_6782_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void ssite$importedComparator(BlockState state, Level level, BlockPos pos,
                                          CallbackInfoReturnable<Integer> result) {
        if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox
                && RecordData.isRecord(jukebox.getItem(0))) {
            result.setReturnValue(RecordData.get(jukebox.getItem(0)) == null ? 0 : RecordCatalog.COMPARATOR_OUTPUT);
        }
    }
}
