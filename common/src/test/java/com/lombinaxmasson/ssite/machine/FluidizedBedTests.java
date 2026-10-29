package com.lombinaxmasson.ssite.machine;

import gregtech.api.multiblock.CellMatch;
import gregtech.api.material.GTValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Pure JVM checks of the same conservative transition engine called by both runtime behaviors. */
public final class FluidizedBedTests {
    private static final long ORIGINAL_COAL_HEAT = 120_000;
    private static final long COAL_HEAT = 180_000;

    private FluidizedBedTests() {}
    public static void main(String[] args) {
        fuelScale(); materialFuelScale(); noFormationNoFuelOrOutput(); noBlowerNoFuel(); fullAshDoesNotConsume();
        outputsShareOneBudget(); rejectionDoesNotStarveOtherPlates(); residualHeatAndStop();
        pendingFuelResumesWithoutDoubleAsh(); fullHeatStopsNewCharges(); longRunConservation();
        finiteFuelDeliveredYield(); reconstructionPreservesEnergy(); structureCoordinates();
        System.out.println("FluidizedBedTests: all 14 checks passed.");
    }

    private static void fuelScale() {
        check(COAL_HEAT * 2 == ORIGINAL_COAL_HEAT * 3, "coal yields 150 percent of the original fluidized-bed recipe");
        check(FluidizedBedCore.heatFromFurnaceTicks(1600) == COAL_HEAT, "coal: 1600 ticks yields 180000 HU");
        check(FluidizedBedCore.heatFromFurnaceTicks(3200) == 360_000, "coal coke: 3200 ticks yields 360000 HU");
        check(FluidizedBedCore.heatFromFurnaceTicks(800) == 90_000, "lignite: 800 ticks yields 90000 HU");
        check(FluidizedBedCore.heatFromFurnaceTicks(6400) == 720_000, "petroleum coke: 6400 ticks yields 720000 HU");
        check(FluidizedBedCore.heatFromFurnaceTicks(0) == 0, "zero-value fuel rejected");
        check(FluidizedBedCore.heatFromFurnaceTicks(Long.MAX_VALUE) == 3_600_000, "bounded external burn values");
        check(FluidizedBedCore.outputRate(4) == 2304 && FluidizedBedCore.kuPerTick(4) == 32, "maximum output and blower use");
    }
    private static void materialFuelScale() {
        check(FluidizedBedCore.heatForMaterial("Coal", GTValues.U) == COAL_HEAT, "one U of coal uses the original recipe yield");
        check(FluidizedBedCore.heatForMaterial("Charcoal", GTValues.U) == COAL_HEAT, "one U of charcoal matches coal heat");
        check(FluidizedBedCore.heatForMaterial("Coal", 9 * GTValues.U) == 9 * COAL_HEAT,
                "nine-U coal block yields 1620000 HU without a furnace compression bonus");
        check(FluidizedBedCore.heatForMaterial("CoalCoke", GTValues.U) == 360_000
                && FluidizedBedCore.heatForMaterial("Coke", GTValues.U) == 360_000, "coal-coke names share one canonical yield");
        check(FluidizedBedCore.heatForMaterial("Lignite", GTValues.U) == 90_000, "lignite material yield is 90000 HU per U");
        check(FluidizedBedCore.heatForMaterial("LigniteCoke", GTValues.U) == COAL_HEAT, "lignite coke retains the original 1600-tick scale");
        check(FluidizedBedCore.heatForMaterial("Anthracite", GTValues.U) == 540_000, "anthracite retains the original 4800-tick scale");
        check(FluidizedBedCore.heatForMaterial("PetroleumCoke", GTValues.U) == 720_000
                && FluidizedBedCore.heatForMaterial("PetCoke", GTValues.U) == 720_000, "petroleum-coke names share one canonical yield");
        check(FluidizedBedCore.heatForMaterial("PetCoke", 9 * GTValues.U) == 6_480_000,
                "nine-U petroleum coke preserves all material heat beyond the old whole-item burn-time clamp");
        check(FluidizedBedCore.heatForMaterial("Coal", 64 * GTValues.U) == 64 * COAL_HEAT, "maximum supported whole-material amount is conserved");
        for (long units : new long[]{-GTValues.U, 0, GTValues.U / 9, GTValues.U + GTValues.U / 9, 65 * GTValues.U, Long.MAX_VALUE})
            check(FluidizedBedCore.heatForMaterial("Coal", units) == 0, "fractional, nonpositive and oversized material amounts are rejected");
        check(FluidizedBedCore.heatForMaterial("Wood", GTValues.U) == 0, "unknown fuel material has no heat value");
    }
    private static void noFormationNoFuelOrOutput() {
        Rig rig = new Rig(); rig.formed = false; rig.heat = 100_000;
        var tick = conserved(rig);
        check(tick.status() == FluidizedBedCore.UNFORMED && tick.output() == 0, "broken structure emits nothing");
        check(rig.fuel == 20 && rig.ash == 0 && rig.pending == 0, "broken structure consumes nothing");
        check(tick.loss() > 0, "isolated structure still loses stored heat");
    }
    private static void noBlowerNoFuel() {
        Rig rig = new Rig(); rig.ku = 0;
        var tick = conserved(rig);
        check(tick.status() == FluidizedBedCore.NO_BLOW && rig.fuel == 20 && rig.ash == 0, "KU shortage keeps fuel and ash unchanged");
    }
    private static void fullAshDoesNotConsume() {
        Rig rig = new Rig(); rig.ash = 64;
        var tick = conserved(rig);
        check(tick.status() == FluidizedBedCore.ASH_FULL, "full ash stops charging");
        check(rig.fuel == 20 && rig.pending == 0 && tick.generated() == 0, "full ash cannot eat a fuel item");
    }
    private static void outputsShareOneBudget() {
        Rig rig = new Rig(); rig.heat = 150_000; rig.enabled = false; rig.power = 4;
        var tick = conserved(rig);
        check(tick.output() == 2304, "one budget across nine plates");
        for (long received : rig.received) check(received == 256, "equal per-plate heat share");
        check(Arrays.stream(rig.received).sum() == tick.output(), "meter equals real accepted heat");
    }
    private static void rejectionDoesNotStarveOtherPlates() {
        Rig rig = new Rig(); rig.heat = 150_000; rig.enabled = false; rig.power = 4;
        rig.limits[0] = 0; rig.limits[1] = 3;
        var tick = conserved(rig);
        check(rig.received[0] == 0 && rig.received[1] == 3, "reject and partial accept are honored");
        for (int i = 2; i < 9; i++) check(rig.received[i] == 256, "later plates always get their share");
        check(tick.output() == 7 * 256 + 3, "unaccepted heat remains in reservoir");
    }
    private static void residualHeatAndStop() {
        Rig rig = new Rig(); rig.heat = 100_000; rig.pending = 20_000; rig.enabled = false;
        int fuel = rig.fuel; long pending = rig.pending;
        long first = conserved(rig).output();
        for (int i = 0; i < 30; i++) {
            var tick = conserved(rig);
            check(tick.generated() == 0 && tick.output() > 0, "stopped burner still releases residual heat");
        }
        check(rig.fuel == fuel && rig.pending == pending, "stop preserves not-yet-burnt fuel energy");
        rig.enabled = true; rig.redstone = true;
        var inhibited = conserved(rig);
        check(inhibited.status() == FluidizedBedCore.REDSTONE && inhibited.generated() == 0, "redstone inhibits combustion only");
        check(inhibited.output() > 0 && first >= inhibited.output(), "cooling outlet decays with reservoir");
    }
    private static void pendingFuelResumesWithoutDoubleAsh() {
        Rig rig = new Rig();
        conserved(rig); int fuel = rig.fuel, ash = rig.ash; long pending = rig.pending;
        check(pending > 0 && fuel == 19 && ash == 1, "one charge reserves ash once");
        rig.formed = false; conserved(rig);
        check(rig.pending == pending && rig.fuel == fuel, "disassembly preserves pending charge");
        rig.formed = true; conserved(rig);
        check(rig.pending < pending && rig.fuel == fuel && rig.ash == ash, "resume burns existing charge without duplicating products");
    }
    private static void fullHeatStopsNewCharges() {
        Rig rig = new Rig(); Arrays.fill(rig.limits, 0); rig.heat = FluidizedBedCore.HEAT_CAPACITY;
        var tick = conserved(rig);
        check(tick.status() == FluidizedBedCore.HEAT_FULL && rig.fuel == 20, "back-pressure stops fuel at reservoir capacity");
    }
    private static void longRunConservation() {
        Rig rig = new Rig(); rig.fuel = 1000; rig.ashLimit = 10_000; rig.ku = 1_000_000;
        long initialKu = rig.ku, output = 0, loss = 0, generated = 0, blowerUse = 0;
        for (int i = 0; i < 1500; i++) {
            rig.power = i % 4 + 1; rig.limits[i % 9] = i % 3 == 0 ? 0 : Long.MAX_VALUE;
            var tick = conserved(rig); output += tick.output(); loss += tick.loss();
            generated += tick.generated();
            if (tick.status() == FluidizedBedCore.BURNING) blowerUse += FluidizedBedCore.kuPerTick(rig.power);
        }
        check((1000L-rig.fuel) * COAL_HEAT == rig.heat + rig.pending + output + loss, "aggregate fuel ledger closes across throttling");
        check(generated == rig.heat + output + loss, "generated heat is stored, delivered or explicitly lost");
        check(Arrays.stream(rig.received).sum() == output, "all nine receivers agree with the aggregate output meter");
        check(rig.ash == 1000-rig.fuel, "one ash per consumed charge, independent of ticks and nine outputs");
        check(initialKu - rig.ku == blowerUse && blowerUse > 0, "KU ledger closes across power changes and partial fuel ticks");
        check(output > 0 && loss > 0, "long run exercises useful output and thermal loss");
    }
    private static void finiteFuelDeliveredYield() {
        int charges = 4;
        for (int power = 1; power <= 4; power++) {
            Rig rig = new Rig(); rig.power = power; rig.fuel = charges;
            long initialKu = rig.ku, output = 0, loss = 0, generated = 0, blowerUse = 0;
            boolean draining = false;
            for (int ticks = 0; ticks < 10_000 && (rig.fuel > 0 || rig.pending > 0 || rig.heat > 0); ticks++) {
                if (rig.fuel == 0 && rig.pending == 0) {
                    rig.enabled = false;
                    draining = true;
                }
                var tick = conserved(rig);
                output += tick.output(); loss += tick.loss(); generated += tick.generated();
                if (tick.status() == FluidizedBedCore.BURNING) blowerUse += FluidizedBedCore.kuPerTick(power);
                if (draining) check(tick.generated() == 0, "stopped burner only releases previously stored heat");
            }
            check(draining && rig.fuel == 0 && rig.pending == 0 && rig.heat == 0, "finite fuel finishes and residual heat drains within the tick budget");
            check(rig.ash == charges && generated == charges * COAL_HEAT, "all finite fuel is burned once with one ash per charge");
            check(output + loss == charges * COAL_HEAT && loss > 0, "delivered yield accounts for all startup and cooldown losses");
            check(Arrays.stream(rig.received).sum() == output, "finite-fuel delivery is the sum of actual receiver receipts");
            check(initialKu - rig.ku == blowerUse, "cooldown consumes no additional blower KU");
            check(output > charges * ORIGINAL_COAL_HEAT && output < charges * COAL_HEAT,
                    "net delivered heat exceeds the original recipe while remaining below the gross 150 percent yield");
            System.out.printf(java.util.Locale.ROOT,
                    "FluidizedBedTests: power %d, %d coal -> %d delivered HU, %d lost HU, %d KU; actual yield %.3f%% of original.%n",
                    power, charges, output, loss, blowerUse, 100.0 * output / (charges * ORIGINAL_COAL_HEAT));
        }
    }
    private static void reconstructionPreservesEnergy() {
        Rig first = new Rig(); for (int i = 0; i < 16; i++) conserved(first);
        Rig restored = new Rig(first);
        for (int i = 0; i < 80; i++) {
            var a = conserved(first); var b = conserved(restored);
            check(a.equals(b) && first.heat == restored.heat && first.pending == restored.pending
                    && first.fuel == restored.fuel && first.ash == restored.ash, "restored state produces the same energy and products");
        }
    }
    private static void structureCoordinates() {
        Set<BlockPos> seen = new HashSet<>(); int plates = 0;
        for (var cell : FluidizedBedStructure.layout().cells()) {
            BlockPos p = new BlockPos(cell.right(), cell.up(), cell.back());
            check(seen.add(p), "layout has unique cells");
            check(!p.equals(BlockPos.ZERO), "controller is not a claimed part");
            if (cell.match() instanceof CellMatch.Part part && part.name().equals(FluidizedBedStructure.HEAT_PLATE)) {
                check(p.getY() == 1 && p.getX() >= 5 && p.getX() <= 7 && p.getZ() >= 1 && p.getZ() <= 3, "plates occupy one exact plane");
                plates++;
            }
            check(!(p.getX() >= 5 && p.getY() >= 2), "structure leaves furnace mounting space free");
        }
        check(plates == 9, "exactly nine plate positions");
        for (Direction facing : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}) {
            Set<BlockPos> targets = new HashSet<>();
            for (int i = 0; i < 9; i++) targets.add(FluidizedBedStructure.heatTarget(BlockPos.ZERO,facing,i));
            check(targets.size() == 9 && targets.stream().allMatch(p -> p.getY() == 2), "four orientations keep unique adjacent targets");
        }
    }

    private static FluidizedBedCore.Tick conserved(Rig rig) {
        long heat = rig.heat, pending = rig.pending, ku = rig.ku; int fuel = rig.fuel, ash = rig.ash;
        var result = FluidizedBedCore.advance(rig);
        check(heat + result.generated() - result.output() - result.loss() == rig.heat, "per-tick heat ledger closes");
        check(pending + (fuel-rig.fuel)*COAL_HEAT - result.generated() == rig.pending, "fuel reserve ledger closes");
        check(rig.ash - ash == fuel - rig.fuel, "per-tick ash ledger closes");
        check(ku - rig.ku == (result.status() == FluidizedBedCore.BURNING ? FluidizedBedCore.kuPerTick(rig.power) : 0),
                "only combustion ticks consume their selected blower KU");
        check(rig.heat >= 0 && rig.heat <= FluidizedBedCore.HEAT_CAPACITY && rig.pending >= 0, "storage stays in range");
        return result;
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }

    private static final class Rig implements FluidizedBedCore.Access {
        boolean formed = true, enabled = true, redstone;
        int power = 2, fuel = 20, ash, ashLimit = 64;
        long heat, pending, ku = 100_000;
        long[] limits = new long[9], received = new long[9];
        Rig() { Arrays.fill(limits, Long.MAX_VALUE); }
        Rig(Rig other) {
            formed=other.formed; enabled=other.enabled; redstone=other.redstone; power=other.power;
            fuel=other.fuel; ash=other.ash; ashLimit=other.ashLimit; heat=other.heat; pending=other.pending; ku=other.ku;
            limits=other.limits.clone(); received=other.received.clone();
        }
        public boolean formed() { return formed; } public boolean enabled() { return enabled; }
        public boolean redstone() { return redstone; } public int power() { return power; }
        public long stored() { return heat; } public long pending() { return pending; }
        public void pending(long value) { pending=value; } public long ku() { return ku; }
        public boolean consumeKu(long value) { if (value>ku) return false; ku-=value; return true; }
        public boolean consumeHeat(long value) { if (value>heat) return false; heat-=value; return true; }
        public long generate(long value) { long add=Math.min(value,FluidizedBedCore.HEAT_CAPACITY-heat); heat+=add; return add; }
        public long emit(int plate,long amount) { long out=Math.min(amount,Math.min(heat,limits[plate])); heat-=out; received[plate]+=out; return out; }
        public long takeFuel() { if (fuel<=0) return 0; if (ash>=ashLimit) return -1; fuel--; ash++; return COAL_HEAT; }
    }
}
