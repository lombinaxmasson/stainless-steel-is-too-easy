package com.lombinaxmasson.ssite.machine;

import gregtech.api.energy.EnergyType;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.material.GTValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/** One conserved heat reservoir supplies the tower's physical rear HU outlet. */
public final class FluidizedBedCore {
    public static final String MACHINE_TYPE = "fluidized_bed_combustor";
    public static final int FUEL_SLOT = 0, ASH_SLOT = 1, ENABLE_TOGGLE = 0;
    public static final long MAX_OUTPUT = 2304, HEAT_CAPACITY = 576_000, KU_CAPACITY = 6_400;
    public static final long TRANSFER_TIME = 40;
    public static final int FUEL_YIELD_PERCENT = 150;
    public static final int DEFAULT_POWER = 2;
    public static final String K_FORMED = "structure.formed", K_STORED = "heat.stored", K_OUTPUT = "heat.output",
            K_GENERATED = "heat.generated", K_LOSS = "heat.loss", K_PENDING = "fuel.pending",
            K_POWER = "burner.power", K_STATUS = "burner.status", K_ENABLED = "burner.enabled",
            K_KU = "blower.ku", K_RATE = "heat.rate", K_INITIALIZED = "burner.initialized";
    public static final int OFF = 0, UNFORMED = 1, NO_BLOW = 2, ASH_FULL = 3, NO_FUEL = 4,
            BURNING = 5, HEAT_FULL = 6, REDSTONE = 7;
    private static final Set<String> FORMS = Set.of("", "dust", "gem", "gemFlawless", "gemExquisite", "block", "blockGem", "blockDust", "blockRaw");

    private FluidizedBedCore() {}

    @FunctionalInterface public interface FuelResolver { Fuel resolve(ItemStack stack); }
    public record Fuel(long heat, ItemStack ash) {}

    /** The actual transition engine uses this narrow boundary, also exercised without a Minecraft world. */
    public interface Access {
        boolean formed(); boolean enabled(); boolean redstone(); int power();
        long stored(); long pending(); void pending(long value);
        long ku(); boolean consumeKu(long amount);
        boolean consumeHeat(long amount); long generate(long amount);
        long emit(long amount);
        /** Atomically reserves ash and consumes one fuel, or 0 / -1 for no fuel / full ash. */
        long takeFuel();
    }

    public record Tick(long generated, long output, long loss, int status) {}

    public static int power(long value) { return (int) Math.max(1, Math.min(4, value)); }
    public static long outputRate(int power) { return 576L * power(power); }
    public static long kuPerTick(int power) { return 8L * power(power); }
    public static long heatFromFurnaceTicks(long ticks) {
        // Original FluidBed fuel recipes already multiply the ordinary 25 HU/t fuel scale by three.
        // This machine credits 150% of that recipe heat, before reservoir losses.
        return Math.min(32_000L, Math.max(0L, ticks)) * 25L * 3L * FUEL_YIELD_PERCENT / 100L;
    }

    public static long heatForMaterial(String material, long units) {
        if (units < GTValues.U || units % GTValues.U != 0 || units / GTValues.U > 64) return 0;
        // OriginalFluidBedFuelRows / GT6 Loader_Fuels values per U. Furnace item hooks can be
        // missing for native powders, or give coal blocks a compression bonus absent from FluidBed.
        long ticks = switch (material) {
            case "Coal", "Charcoal", "LigniteCoke" -> 1600;
            case "Coke", "CoalCoke" -> 3200;
            case "Lignite" -> 800;
            case "Anthracite" -> 4800;
            case "PetroleumCoke", "PetCoke" -> 6400;
            default -> 0;
        };
        return heatFromFurnaceTicks(ticks) * (units / GTValues.U);
    }

    public static Tick advance(Access access) {
        long rate = outputRate(access.power()), sent = 0;
        if (access.formed()) {
            long budget = Math.min(rate, Math.max(0, access.stored()) / TRANSFER_TIME);
            if (budget > 0) sent = access.emit(budget);
        }
        long loss = Math.min(Math.max(0, access.stored()), Math.max(1, access.stored() / 20_000));
        if (loss > 0 && !access.consumeHeat(loss)) throw new IllegalStateException("Heat store refused a bounded loss");
        if (!access.formed()) return new Tick(0, sent, loss, UNFORMED);
        if (!access.enabled()) return new Tick(0, sent, loss, OFF);
        if (access.redstone()) return new Tick(0, sent, loss, REDSTONE);
        long ku = kuPerTick(access.power());
        if (access.ku() < ku) return new Tick(0, sent, loss, NO_BLOW);
        long room = Math.max(0, HEAT_CAPACITY - access.stored());
        if (room < rate) return new Tick(0, sent, loss, HEAT_FULL);
        if (access.pending() <= 0) {
            long fuel = access.takeFuel();
            if (fuel <= 0) return new Tick(0, sent, loss, fuel < 0 ? ASH_FULL : NO_FUEL);
            access.pending(fuel);
        }
        long burn = Math.min(rate, Math.min(room, access.pending()));
        if (burn <= 0 || !access.consumeKu(ku)) return new Tick(0, sent, loss, NO_BLOW);
        long added = access.generate(burn);
        if (added < 0 || added > burn) throw new IllegalStateException("Invalid generated heat result");
        access.pending(access.pending() - added);
        return new Tick(added, sent, loss, BURNING);
    }

    public static void initialize(BehaviorState state) {
        if (!state.getBoolean(K_INITIALIZED)) {
            state.setBoolean(K_INITIALIZED, true);
            state.setLong(K_POWER, DEFAULT_POWER);
        }
    }

    public static void tick(MachineBehaviorContext context, FuelResolver fuelResolver) {
        BehaviorState state = context.state(); initialize(state);
        int power = power(state.getLong(K_POWER));
        Tick result = advance(new Access() {
            public boolean formed() { return context.structure().formed(); }
            public boolean enabled() { return context.panel().toggle(ENABLE_TOGGLE); }
            public boolean redstone() { return context.level().hasNeighborSignal(context.pos()); }
            public int power() { return power; }
            public long stored() { return context.energy().mainStored(); }
            public long pending() { return Math.max(0, state.getLong(K_PENDING)); }
            public void pending(long value) { state.setLong(K_PENDING, Math.max(0, value)); }
            public long ku() { return context.energy().auxiliaryStored(EnergyType.KU); }
            public boolean consumeKu(long amount) { return context.energy().consumeAuxiliary(EnergyType.KU, amount); }
            public boolean consumeHeat(long amount) { return context.energy().consumeMain(amount); }
            public long generate(long amount) { return context.energy().generateMain(amount); }
            public long emit(long amount) {
                BlockPos target = FluidizedBedStructure.heatOutletTarget(context.pos(), context.facing());
                if (!context.level().hasChunkAt(target)
                        || !HeatTransportConnections.isType(context.level().getBlockState(target).getBlock(), HeatTransportMachines.DUCT)) return 0;
                // The target is immediately beyond the formed tower's rear outlet, not a remote furnace.
                return context.energy().emitTo(target, context.facing(), 1, amount).transferredUnits();
            }
            public long takeFuel() { return consumeOne(context, fuelResolver); }
        });
        state.setBoolean(K_FORMED, context.structure().formed());
        state.setBoolean(K_ENABLED, context.panel().toggle(ENABLE_TOGGLE));
        state.setLong(K_POWER, power); state.setLong(K_RATE, outputRate(power));
        state.setLong(K_STORED, context.energy().mainStored()); state.setLong(K_KU, context.energy().auxiliaryStored(EnergyType.KU));
        state.setLong(K_GENERATED, result.generated()); state.setLong(K_OUTPUT, result.output());
        state.setLong(K_LOSS, result.loss()); state.setLong(K_STATUS, result.status());
        context.panel().setProgress((float) context.energy().mainStored() / HEAT_CAPACITY);
        context.panel().working(result.generated() > 0 || result.output() > 0);
    }

    private static long consumeOne(MachineBehaviorContext context, FuelResolver resolver) {
        ItemStack input = context.inventory().snapshot(FUEL_SLOT).copy();
        Fuel fuel = resolver.resolve(input);
        if (fuel == null || fuel.heat() <= 0 || fuel.ash().isEmpty()) return 0;
        ItemStack ash = context.inventory().snapshot(ASH_SLOT).copy();
        ItemStack product = fuel.ash();
        // Products are generated plain material items; require exact item plus no custom data on the destination.
        if (!ash.isEmpty() && (!sameStack(ash, product) || ash.getCount() + product.getCount() > ash.getMaxStackSize())) return -1;
        ItemStack merged = ash.isEmpty() ? product.copy() : ash;
        if (!ash.isEmpty()) merged.grow(product.getCount());
        if (merged.getCount() > merged.getMaxStackSize()) return -1;
        input.shrink(1);
        context.inventory().set(ASH_SLOT, merged);
        context.inventory().set(FUEL_SLOT, input);
        return fuel.heat();
    }

    /** Platform wrapper supplies the version-specific component comparison. */
    public static boolean sameStack(ItemStack a, ItemStack b) { return FluidizedBedBehavior.sameStack(a, b); }

    /** Only solid coal-family material forms are admitted; no wooden machines, containers or item recursion. */
    public static boolean acceptedFuel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(Items.COAL_BLOCK)) return true;
        var composition = AddonItems.composition(stack);
        return FORMS.contains(composition.prefix()) && composition.units().size() == 1
                && heatForMaterial(composition.units().get(0).material(), composition.units().get(0).amount()) > 0;
    }

    public static Fuel fuel(ItemStack stack) {
        if (!acceptedFuel(stack)) return null;
        var composition = AddonItems.composition(stack);
        long units = composition.units().size() == 1 ? composition.units().get(0).amount() : GTValues.U;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) units = GTValues.U;
        if (stack.is(Items.COAL_BLOCK)) units = 9 * GTValues.U;
        String material = stack.is(Items.CHARCOAL) ? "Charcoal"
                : stack.is(Items.COAL) || stack.is(Items.COAL_BLOCK) ? "Coal"
                : composition.units().get(0).material();
        long heat = heatForMaterial(material, units);
        if (heat <= 0) return null;
        int ashCount = (int) Math.max(1, units / GTValues.U);
        boolean charcoal = stack.is(Items.CHARCOAL) || composition.units().size() == 1
                && composition.units().get(0).material().equals("Charcoal");
        ItemStack ash = AddonItems.stack("dustTiny", charcoal ? "Ashes" : "DarkAshes", ashCount);
        return ash.isEmpty() ? null : new Fuel(heat, ash);
    }
}
