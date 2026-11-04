package com.lombinaxmasson.ssite.machine;

/** Pure JVM checks of both chemical-looping batch ledgers, fluid venting and carrier wear. */
public final class ChemicalLoopingTests {
    private ChemicalLoopingTests() {}

    public static void main(String[] args) {
        fuelBedRequiresIgnition(); charcoalBatchClosesTheLedger(); cokeBatchUsesTheHostValue();
        ashFullRefusesTheCharge(); blockedMagnetiteKeepsTheBatch(); kuShortagePausesProgress();
        fullHeatBufferPausesProgress(); missingReceiverVentsBothFluids();
        airBedRequiresVent(); airBatchRegeneratesTheCarrier(); carrierWearIsDeterministic();
        combinedHeatMatchesTheHostFuelTable(); idleBedsNeverDuplicate();
        System.out.println("ChemicalLoopingTests: all 13 checks passed.");
    }

    private static void fuelBedRequiresIgnition() {
        FuelRig rig = new FuelRig();
        rig.lit = false;
        rig.fuel = 1;
        rig.carrier = 1;
        rig.step();
        check(rig.state == FuelReactorCore.IDLE && rig.last.status() == FuelReactorCore.ST_NOT_LIT,
                "an unlit fuel bed does not start");
        check(rig.fuel == 1 && rig.carrier == 1, "an unlit bed reserves nothing");
    }

    private static void charcoalBatchClosesTheLedger() {
        FuelRig rig = new FuelRig();
        rig.fuel = 1;
        rig.carrier = 1;
        rig.receiver = true;
        rig.heatSink = Long.MAX_VALUE;
        rig.run(FuelReactorCore.BATCH_TICKS + 1);
        check(rig.magnetite == 1 && rig.ash == 1, "one charcoal batch releases one magnetite and one ash");
        check(rig.consumedKu == FuelReactorCore.KU_PER_TICK * FuelReactorCore.BATCH_TICKS,
                "the blower consumes exactly four KU per working tick");
        check(rig.generated == ChemicalLoopingCore.FuelKind.CHARCOAL.heat(),
                "the fuel bed credits charcoal's share of the host heat value");
        check(rig.pushedCo2 + rig.co2 == FuelReactorCore.CO2_PER_BATCH
                        && rig.pushedWater + rig.water == FuelReactorCore.WATER_PER_BATCH,
                "carbon dioxide and water leave the batch exactly once");
    }

    private static void cokeBatchUsesTheHostValue() {
        FuelRig rig = new FuelRig();
        rig.inputBatch = FuelReactorCore.BATCH_COKE;
        rig.fuel = 1;
        rig.carrier = 1;
        rig.heatSink = Long.MAX_VALUE;
        rig.run(FuelReactorCore.BATCH_TICKS + 1);
        check(rig.generated == ChemicalLoopingCore.FuelKind.COKE.heat(),
                "coke credits its larger host heat value once");
    }

    private static void ashFullRefusesTheCharge() {
        FuelRig rig = new FuelRig();
        rig.fuel = 1;
        rig.carrier = 1;
        rig.ashSlot = 64;
        rig.step();
        check(rig.last.status() == FuelReactorCore.ST_ASH_FULL && rig.fuel == 1 && rig.carrier == 1,
                "a full ash slot refuses the charge without consuming it");
    }

    private static void blockedMagnetiteKeepsTheBatch() {
        FuelRig rig = new FuelRig();
        rig.fuel = 1;
        rig.carrier = 1;
        rig.run(FuelReactorCore.BATCH_TICKS);
        check(rig.state == FuelReactorCore.DONE, "the batch waits in its finished state");
        rig.outputSlot = 64;
        rig.step();
        check(rig.state == FuelReactorCore.DONE && rig.magnetite == 0
                        && rig.last.status() == FuelReactorCore.ST_OUTPUT_BLOCKED,
                "a full magnetite slot keeps the finished batch pending");
        rig.outputSlot = 0;
        rig.step();
        check(rig.magnetite == 1 && rig.state == FuelReactorCore.IDLE,
                "freeing the magnetite slot commits the batch exactly once");
    }

    private static void kuShortagePausesProgress() {
        FuelRig rig = new FuelRig();
        rig.fuel = 1;
        rig.carrier = 1;
        rig.run(50);
        long progress = rig.progress;
        rig.ku = 0;
        rig.run(20);
        check(rig.progress == progress && rig.last.status() == FuelReactorCore.ST_WAIT_KU,
                "lost KU pauses progress without losing the batch");
        rig.ku = 10_000;
        rig.run(10);
        check(rig.progress == progress + 10, "restored KU resumes the same batch");
    }

    private static void fullHeatBufferPausesProgress() {
        FuelRig rig = new FuelRig();
        rig.fuel = 1;
        rig.carrier = 1;
        rig.stored = FuelReactorCore.HEAT_CAPACITY;
        rig.step();
        check(rig.last.status() == FuelReactorCore.ST_HEAT_FULL && rig.progress == 0,
                "a full heat buffer stops the batch before it spends KU");
        check(rig.consumedKu == 0, "no KU is spent while the heat buffer is full");
    }

    private static void missingReceiverVentsBothFluids() {
        FuelRig rig = new FuelRig();
        rig.fuel = 1;
        rig.carrier = 1;
        rig.receiver = false;
        rig.run(FuelReactorCore.BATCH_TICKS + 2);
        check(rig.co2 == 0 && rig.water == 0 && rig.ventedCo2 == FuelReactorCore.CO2_PER_BATCH
                        && rig.ventedWater == FuelReactorCore.WATER_PER_BATCH,
                "without a receiver the bed vents both fluids instead of stopping");
    }

    private static void airBedRequiresVent() {
        AirRig rig = new AirRig();
        rig.vent = false;
        rig.input = 1;
        rig.step();
        check(rig.last.status() == AirReactorCore.ST_NO_VENT && rig.input == 1,
                "the air bed refuses to start without an installed vent");
    }

    private static void airBatchRegeneratesTheCarrier() {
        AirRig rig = new AirRig();
        rig.input = 1;
        rig.heatSink = Long.MAX_VALUE;
        rig.run(AirReactorCore.BATCH_TICKS + 1);
        check(rig.output == 1 && rig.batches == 1, "one magnetite unit returns as one hematite unit");
        check(rig.generated == AirReactorCore.HEAT_PER_BATCH,
                "the air bed credits its own fixed regeneration heat");
        check(rig.consumedKu == AirReactorCore.KU_PER_TICK * AirReactorCore.BATCH_TICKS,
                "the air bed consumes eight KU per working tick");
    }

    private static void carrierWearIsDeterministic() {
        AirRig rig = new AirRig();
        rig.input = AirReactorCore.WEAR_PERIOD;
        rig.heatSink = Long.MAX_VALUE;
        rig.run((long) AirReactorCore.WEAR_PERIOD * (AirReactorCore.BATCH_TICKS + 1));
        check(rig.batches == AirReactorCore.WEAR_PERIOD, "every magnetite unit starts one batch");
        check(rig.output == AirReactorCore.WEAR_PERIOD - 1,
                "one batch per wear period consumes the carrier without returning hematite");
        check(rig.wearBatches == 1, "exactly one wear batch occurs in the period");
    }

    private static void combinedHeatMatchesTheHostFuelTable() {
        check(ChemicalLoopingCore.FuelKind.CHARCOAL.heat() + AirReactorCore.HEAT_PER_BATCH == 40_000,
                "charcoal fuel-bed heat plus regeneration heat equals the host 40,000 HU/U");
        check(ChemicalLoopingCore.FuelKind.COKE.heat() + AirReactorCore.HEAT_PER_BATCH == 80_000,
                "coke fuel-bed heat plus regeneration heat equals the host 80,000 HU/U");
    }

    private static void idleBedsNeverDuplicate() {
        FuelRig fuel = new FuelRig();
        fuel.run(100);
        check(fuel.magnetite == 0 && fuel.ash == 0 && fuel.consumedKu == 0,
                "an empty fuel bed never produces a batch");
        AirRig air = new AirRig();
        air.run(100);
        check(air.output == 0 && air.consumedKu == 0, "an empty air bed never produces hematite");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FuelRig implements FuelReactorCore.Access {
        boolean lit = true, receiver;
        long stored, heatSink, ku = 10_000, consumedKu, generated, pushedCo2, pushedWater, ventedCo2, ventedWater;
        long progress;
        int state = FuelReactorCore.IDLE, batch = FuelReactorCore.BATCH_NONE;
        int inputBatch = FuelReactorCore.BATCH_CHARCOAL;
        int fuel, carrier, ashSlot, outputSlot, magnetite, ash, co2, water;
        FuelReactorCore.Tick last = new FuelReactorCore.Tick(FuelReactorCore.ST_IDLE, 0, 0);

        void step() { last = FuelReactorCore.advance(this); }
        void run(long ticks) { for (long i = 0; i < ticks; i++) step(); }

        @Override public boolean lit() { return lit; }
        @Override public long stored() { return stored; }
        @Override public long add(long amount) { stored += amount; generated += amount; return amount; }
        @Override public long emit(long amount) {
            long sent = Math.min(amount, Math.max(0, heatSink)); heatSink -= sent; stored -= sent; return sent;
        }
        @Override public long ku() { return ku; }
        @Override public boolean consumeKu(long amount) {
            if (ku < amount) return false;
            ku -= amount; consumedKu += amount; return true;
        }
        @Override public int state() { return state; }
        @Override public void state(int value) { state = value; }
        @Override public int batch() { return batch; }
        @Override public void batch(int value) { batch = value; }
        @Override public long progress() { return progress; }
        @Override public void progress(long value) { progress = value; }
        @Override public int tankAmount(int tank) { return tank == FuelReactorCore.CO2_TANK ? co2 : water; }
        @Override public int tankCapacity(int tank) {
            return tank == FuelReactorCore.CO2_TANK ? FuelReactorCore.CO2_CAPACITY : FuelReactorCore.WATER_CAPACITY;
        }
        @Override public int fillTank(int tank, int amount) {
            int room = tankCapacity(tank) - tankAmount(tank);
            int filled = Math.min(amount, room);
            if (tank == FuelReactorCore.CO2_TANK) co2 += filled; else water += filled;
            return filled;
        }
        @Override public int pushTank(int tank, int amount) {
            int moved = Math.min(amount, tankAmount(tank));
            if (tank == FuelReactorCore.CO2_TANK) { co2 -= moved; pushedCo2 += moved; }
            else { water -= moved; pushedWater += moved; }
            return moved;
        }
        @Override public void ventTank(int tank, int amount) {
            int drained = Math.min(amount, tankAmount(tank));
            if (tank == FuelReactorCore.CO2_TANK) { co2 -= drained; ventedCo2 += drained; }
            else { water -= drained; ventedWater += drained; }
        }
        @Override public boolean hasReceiver() { return receiver; }

        @Override public int startBatch() {
            if (!lit) return 5;
            if (fuel <= 0 || inputBatch == FuelReactorCore.BATCH_NONE) return 1;
            if (carrier <= 0) return 2;
            if (ashSlot >= 64) return 3;
            if (outputSlot >= 64) return 4;
            fuel--; carrier--; batch = inputBatch;
            return 0;
        }

        @Override public boolean commitBatch() {
            ChemicalLoopingCore.FuelKind kind = FuelReactorCore.kind(batch);
            if (kind == null || ashSlot >= 64 || outputSlot >= 64) return false;
            FuelReactorCore.fillWithoutBackpressure(this, FuelReactorCore.CO2_TANK, FuelReactorCore.CO2_PER_BATCH);
            FuelReactorCore.fillWithoutBackpressure(this, FuelReactorCore.WATER_TANK, FuelReactorCore.WATER_PER_BATCH);
            ash++; ashSlot++; magnetite++; outputSlot++;
            return true;
        }
    }

    private static final class AirRig implements AirReactorCore.Access {
        boolean vent = true;
        long stored, heatSink = Long.MAX_VALUE, ku = 1_000_000, consumedKu, generated, progress, batches;
        boolean wear;
        int state = AirReactorCore.IDLE, input, output, wearBatches;
        AirReactorCore.Tick last = new AirReactorCore.Tick(AirReactorCore.ST_IDLE, 0, 0);

        void step() { last = AirReactorCore.advance(this); }
        void run(long ticks) { for (long i = 0; i < ticks; i++) step(); }

        @Override public boolean ventInstalled() { return vent; }
        @Override public long stored() { return stored; }
        @Override public long add(long amount) { stored += amount; generated += amount; return amount; }
        @Override public long emit(long amount) {
            long sent = Math.min(amount, Math.max(0, heatSink)); heatSink -= sent; stored -= sent; return sent;
        }
        @Override public long ku() { return ku; }
        @Override public boolean consumeKu(long amount) {
            if (ku < amount) return false;
            ku -= amount; consumedKu += amount; return true;
        }
        @Override public int state() { return state; }
        @Override public void state(int value) { state = value; }
        @Override public long progress() { return progress; }
        @Override public void progress(long value) { progress = value; }
        @Override public long batches() { return batches; }
        @Override public void batches(long value) { batches = value; }
        @Override public boolean wear() { return wear; }
        @Override public void wear(boolean value) { wear = value; }

        @Override public int startBatch() {
            if (!vent) return 1;
            if (input <= 0) return 2;
            long next = batches + 1;
            boolean wearing = next % AirReactorCore.WEAR_PERIOD == 0;
            if (!wearing && output >= 64) return 3;
            input--; batches = next; wear = wearing;
            if (wearing) wearBatches++;
            return 0;
        }

        @Override public boolean commitBatch() {
            if (wear) return true;
            if (output >= 64) return false;
            output++;
            return true;
        }
    }
}
