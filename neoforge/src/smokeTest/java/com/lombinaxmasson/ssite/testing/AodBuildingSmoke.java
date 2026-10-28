package com.lombinaxmasson.ssite.testing;

import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import gregtech.content.item.GTToolItem;
import gregtech.content.multiblock.ControllerStructureLayouts;
import gregtech.engine.material.GTMaterialRegistry;
import gregtech.engine.multiblock.MultiblockTools;
import gregtech.engine.tool.GTToolHelper;
import gregtech.engine.tool.GTToolType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Explicit command in the disposable test server, exercising real native tool dispatch. */
@Mod("ssite_aod_smoke")
public final class AodBuildingSmoke {
    private BlockPos denied;

    public AodBuildingSmoke() {
        NeoForge.EVENT_BUS.addListener(this::commands);
        NeoForge.EVENT_BUS.addListener(this::placement);
    }

    private void placement(BlockEvent.EntityPlaceEvent event) {
        if (event.getPos().equals(denied)) event.setCanceled(true);
    }

    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ssite_aod_smoke").requires(s -> s.hasPermission(4))
                .executes(context -> {
                    run(context.getSource().getLevel());
                    context.getSource().sendSuccess(() -> Component.literal("PASS: AOD preview and wand integration"), true);
                    return 1;
                }));
    }

    private void run(ServerLevel level) {
        var player = FakePlayerFactory.getMinecraft(level);
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(128, 110, -8);
        var wandItem = BuiltInRegistries.ITEM.stream().filter(i -> i instanceof GTToolItem tool
                && tool.toolType() == GTToolType.BUILDER_WAND).findFirst().orElseThrow();
        int index = 0;
        for (var tier : AodTier.values()) for (var facing : Direction.Plane.HORIZONTAL) {
            BlockPos origin = new BlockPos(128 + index++ * 16, 100, 0);
            level.setChunkForced(origin.getX() >> 4, 0, true);
            level.setChunkForced((origin.getX() - 5) >> 4, -1, true);
            level.setChunkForced(origin.getX() >> 4, -1, true);
            level.setChunkForced((origin.getX() - 5) >> 4, 0, true);
            for (var pos : BlockPos.betweenClosed(origin.offset(-5, -2, -5), origin.offset(5, 5, 5)))
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            Block controller = block(tier.largeMachineType());
            level.setBlockAndUpdate(origin, controller.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing));
            var preview = ControllerStructureLayouts.cells(controller);
            check(preview != null && preview.equals(LargeAodStructure.preview(tier)), "native preview includes exact addon layout");
            check(preview.values().stream().filter(b -> b == block(tier.largeCasing())).count() == 21, "preview counts 21 walls");
            player.getInventory().clearContent();
            var wand = GTToolHelper.write(new ItemStack(wandItem), GTMaterialRegistry.get("Steel"), GTMaterialRegistry.get("Wood"));
            player.setItemInHand(InteractionHand.MAIN_HAND, wand);
            player.getInventory().setItem(9, new ItemStack(block(tier.largeCasing()), 21));
            player.getInventory().setItem(10, new ItemStack(block(LargeAodStructure.OXYGEN)));
            player.getInventory().setItem(11, new ItemStack(block(LargeAodStructure.ARGON)));
            player.getInventory().setItem(12, new ItemStack(block(LargeAodStructure.LID)));
            var use = new UseOnContext(level, player, InteractionHand.MAIN_HAND, wand,
                    new BlockHitResult(Vec3.atCenterOf(origin), facing, origin, false));
            denied = LargeAodStructure.world(origin, facing, -1, -1, 1);
            check(MultiblockTools.use(use).consumesAction(), "wand dispatch handled");
            check(level.getBlockState(denied).isAir(), "canceled placement leaves air");
            check(player.getInventory().getItem(9).getCount() == 1, "canceled placement does not consume wall");
            denied = null;
            check(MultiblockTools.use(use).consumesAction(), "second click fills canceled cell");
            for (var cell : preview.entrySet()) {
                var p = cell.getKey();
                check(level.getBlockState(LargeAodStructure.world(origin, facing, p.getX(), p.getY(), p.getZ()))
                        .is(cell.getValue()), "every rotated cell matches");
            }
            for (int slot = 9; slot <= 12; slot++) check(player.getInventory().getItem(slot).isEmpty(), "exact inventory consumption");
            check(player.getMainHandItem() == wand, "wand remains held");
            // No materials: a repair click must not duplicate a controller or eat the obstacle.
            var missing = LargeAodStructure.world(origin, facing, 0, 3, 2);
            level.setBlockAndUpdate(missing, Blocks.STONE.defaultBlockState());
            check(MultiblockTools.use(use).consumesAction(), "blocked click consumed");
            check(level.getBlockState(missing).is(Blocks.STONE), "solid obstruction preserved");
            player.setGameMode(GameType.CREATIVE);
            level.setBlockAndUpdate(missing, Blocks.AIR.defaultBlockState());
            check(MultiblockTools.use(use).consumesAction(), "creative builds without inventory");
            check(level.getBlockState(missing).is(block(LargeAodStructure.LID)), "creative repair placed lid");
            check(player.getMainHandItem() == wand, "creative placement keeps wand");
            player.setGameMode(GameType.SURVIVAL);
        }
    }

    private static Block block(String name) { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("ssite", name)); }
    private static void check(boolean pass, String message) { if (!pass) throw new IllegalStateException(message); }
}
