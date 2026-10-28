package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.MoltenPool;
import gregtech.api.addon.platform.PartClientView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

/**
 * An open AOD lid is passable like an open trapdoor, so a thrown charge drops into the vessel
 * instead of resting on a solid cube. The outline shape stays full, so the lid can still be clicked.
 */
@Mixin(targets = "gregtech.content.block.machine.AddonFunctionalPartBlock", remap = false)
public abstract class LidOpenShapeMixin extends HorizontalDirectionalBlock {
    protected LidOpenShapeMixin(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return ssite$openLid(level, pos) ? Shapes.empty() : super.getCollisionShape(state, level, pos, context);
    }

    private static boolean ssite$openLid(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PartClientView view
                && view.addonId().equals(SsiteAddon.MOD_ID)
                && (view.name().equals(MoltenPool.LID_PART) || view.name().equals(LargeAodStructure.LID))
                && view.getBoolean("open");
    }
}
