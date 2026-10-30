package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.ObjMultiblocks;
import com.lombinaxmasson.ssite.machine.HeatTransportMachines;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.content.blockentity.machine.MultiblockPortBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.List;

/** Unformed cubes also serve EMI/JEI previews; only a formed world structure hides them. */
@EventBusSubscriber(modid = "ssite", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LargeAodBlockModels {
    private LargeAodBlockModels() {}

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        for (var block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals("ssite") || !(ObjMultiblocks.isPart(id.getPath())
                    || HeatTransportMachines.isMachine(id.getPath()))) continue;
            for (var state : block.getStateDefinition().getPossibleStates()) {
                var key = BlockModelShaper.stateToModelLocation(id, state);
                var original = event.getModels().get(key);
                if (original != null) event.getModels().put(key, new FormedModel(original));
            }
        }
    }

    private static final class FormedModel extends BakedModelWrapper<BakedModel> {
        private static final ModelProperty<Boolean> FORMED = new ModelProperty<>();

        private FormedModel(BakedModel original) { super(original); }

        @Override public ModelData getModelData(BlockAndTintGetter level, BlockPos pos,
                                                BlockState state, ModelData data) {
            var result = super.getModelData(level, pos, state, data);
            boolean formed = Integer.valueOf(LargeAodStructure.FORMED_DESIGN)
                    .equals(result.get(MultiblockPortBlockEntity.DESIGN));
            if (level.getBlockEntity(pos) instanceof BehaviorMachineClientView view)
                formed = HeatTransportMachines.isMachine(view.machineType()) || view.getBoolean("structure.formed");
            return result.derive().with(FORMED, formed).build();
        }

        @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
                                                  ModelData data, RenderType layer) {
            return Boolean.TRUE.equals(data.get(FORMED)) ? List.of()
                    : originalModel.getQuads(state, side, random, data, layer);
        }
    }
}
