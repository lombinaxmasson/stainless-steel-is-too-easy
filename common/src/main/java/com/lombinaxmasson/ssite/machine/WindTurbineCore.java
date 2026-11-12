package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** One placed wind turbine. The host saves and drops the installed rotor in its input slot. */
public final class WindTurbineCore {
    public static final String MACHINE_TYPE = "wind_turbine";
    public static final String K_ROTOR = "wind.rotor";
    public static final String K_SPEED = "wind.speed";
    static final int ROTOR_SLOT = 0;

    private static final ResourceLocation ROTOR = ResourceLocation.tryBuild("ssite", "fiberglass_wind_rotor");
    private static final String MESSAGE = "ssite.wind_turbine.";
    private static final String K_BRAKED = "wind.braked";
    private final WindTurbineEnvironment environment = new WindTurbineEnvironment();
    private WindTurbinePower.StormBrake brake;
    private long lastTick = Long.MIN_VALUE;
    private double wind;
    private int power;
    private String status = "no_rotor";

    static boolean isRotor(ItemStack stack) {
        return !stack.isEmpty() && ROTOR.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    void tick(MachineBehaviorContext context) {
        if (!(context.level() instanceof ServerLevel level)) return;
        boolean installed = isRotor(context.inventory().snapshot(ROTOR_SLOT));
        context.state().setBoolean(K_ROTOR, installed);
        if (!installed) {
            if (brake != null) clearRotor(context);
            return;
        }
        if (brake == null) brake = new WindTurbinePower.StormBrake(context.state().getBoolean(K_BRAKED));
        long now = level.getGameTime();
        if (lastTick != now - 1) brake.interrupt();
        lastTick = now;

        WindTurbineEnvironment.Reading reading = environment.sample(level, context.pos(), context.facing());
        wind = reading.wind();
        if (reading.status() != WindTurbineEnvironment.CLEAR) {
            brake.interrupt();
            stop(context, switch (reading.status()) {
                case WindTurbineEnvironment.BLOCKED -> "blocked";
                case WindTurbineEnvironment.INDOORS -> "indoors";
                case WindTurbineEnvironment.OVERLAP -> "overlap";
                case WindTurbineEnvironment.WRONG_DIMENSION -> "wrong_dimension";
                default -> "unloaded";
            });
            return;
        }
        boolean braked = brake.sample(wind);
        context.state().setBoolean(K_BRAKED, braked);
        if (braked) {
            stop(context, "storm");
            return;
        }
        power = WindTurbinePower.availableRu(wind);
        if (power == 0) {
            stop(context, "calm");
            return;
        }
        status = "running";
        context.state().setLong(K_SPEED, Math.round(Math.min(wind, 1.0) * 40) * 25);
        context.panel().working(true);

        var energy = context.energy();
        if (energy.mainCapacity() != WindTurbinePower.BUFFER_RU) {
            energy.setMainCapacity(WindTurbinePower.BUFFER_RU);
        }
        // Two packets minus one unit preserve the fractional tick's remainder after a whole packet is emitted.
        energy.generateMain(power);
        BlockPos rear = context.pos().relative(context.facing().getOpposite());
        if (level.hasChunkAt(rear)) energy.emitFromFaces(WindTurbinePower.RATED_RU, 1);
    }

    boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        if (context.level().isClientSide) return true;
        ItemStack held = player.getItemInHand(hand);
        if (!isRotor(held)) {
            message(player, "status", Math.round(wind * 100), power, Component.translatable(MESSAGE + status));
            return true;
        }
        if (!context.inventory().snapshot(ROTOR_SLOT).isEmpty()) {
            message(player, "occupied");
            return true;
        }
        ItemStack rotor = held.copy();
        rotor.setCount(1);
        context.inventory().set(ROTOR_SLOT, rotor);
        if (!player.getAbilities().instabuild) held.shrink(1);
        context.state().setBoolean(K_ROTOR, true);
        message(player, "installed");
        return true;
    }

    boolean onWrench(MachineBehaviorContext context, Player player) {
        // Only the synced visual state is available during a client-side tool check.
        if (context.level().isClientSide) return context.state().getBoolean(K_ROTOR);
        ItemStack rotor = context.inventory().snapshot(ROTOR_SLOT);
        if (rotor.isEmpty()) return false;
        context.inventory().set(ROTOR_SLOT, ItemStack.EMPTY);
        clearRotor(context);
        if (!player.getInventory().add(rotor)) player.drop(rotor, false);
        message(player, "removed");
        return true;
    }

    void beforeBreak() { environment.clear(); }

    private void clearRotor(MachineBehaviorContext context) {
        environment.clear();
        brake = null;
        wind = 0;
        context.state().setBoolean(K_BRAKED, false);
        context.state().setBoolean(K_ROTOR, false);
        // A positive cap makes the host discard old inertia at the end of its next tick.
        context.energy().setMainCapacity(1);
        stop(context, "no_rotor");
    }

    private void stop(MachineBehaviorContext context, String reason) {
        power = 0;
        status = reason;
        context.state().setLong(K_SPEED, 0);
        context.panel().working(false);
    }

    private static void message(Player player, String key, Object... arguments) {
        player.displayClientMessage(Component.translatable(MESSAGE + key, arguments), true);
    }
}
