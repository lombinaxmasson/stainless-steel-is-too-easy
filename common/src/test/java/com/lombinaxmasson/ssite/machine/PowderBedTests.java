package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.material.PowderManufacturingMaterials;

/** Pure JVM checks of the P-05 powder-bed batch, gas and dual-settlement ledger. */
public final class PowderBedTests {
    private PowderBedTests() {}

    public static void main(String[] args) {
        reservationPrecedesCharging(); startChargesOnceAndDrainsGasByProgress();
        outputBlockedRefusesBatch(); reclaimBlockedHoldsFinishedBatch();
        completionCommitsOnce(); savedProgressResumesWithoutRecharging();
        energyShortagePausesWithoutRecharging(); gasShortagePausesWithoutRecharging();
        parallelUpgradeScalesBudget(); parallelNeedsBothParts();
        whitelistRejectsOtherForms();
        System.out.println("PowderBedTests: all 11 checks passed.");
    }

    private static void reservationPrecedesCharging() {
        Rig rig = new Rig();
        rig.powder = 3;
        rig.step();
        check(rig.last.status() == PowderBedCore.ST_WAIT_POWDER && rig.batches == 0
                        && rig.powder == 3 && rig.argon == PowderBedCore.ARGON_PER_BATCH,
                "a short powder charge is refused before anything is consumed");
        rig.powder = 4;
        rig.argon = PowderBedCore.ARGON_PER_BATCH - 1;
        rig.step();
        check(rig.last.status() == PowderBedCore.ST_WAIT_ARGON && rig.powder == 4 && rig.batches == 0,
                "a short argon charge is refused before the powder is consumed");
        rig.argon = PowderBedCore.ARGON_PER_BATCH;
        rig.blankOut = 64;
        rig.step();
        check(rig.last.status() == PowderBedCore.ST_OUTPUT_BLOCKED && rig.powder == 4 && rig.batches == 0,
                "a blocked blank output is refused before the powder is consumed");
        rig.blankOut = 0;
        rig.step();
        check(rig.last.status() == PowderBedCore.ST_WORKING && rig.batches == 1 && rig.powder == 0,
                "a fully reserved batch charges the powder once");
    }

    private static void startChargesOnceAndDrainsGasByProgress() {
        Rig rig = new Rig();
        rig.run(100);
        check(rig.batches == 1 && rig.powder == 0,
                "one batch charges the powder once");
        check(rig.consumedEnergy == 100 * PowderBedCore.EU_PER_TICK,
                "energy is consumed once per working tick");
        check(rig.argon == PowderBedCore.ARGON_PER_BATCH - 100 * PowderBedCore.argonPerTick(1),
                "argon is drained by progress rather than at the start");
        check(rig.progress == 100, "progress advances one tick per working tick");
    }

    private static void outputBlockedRefusesBatch() {
        Rig rig = new Rig();
        rig.reclaimOut = 64;
        rig.step();
        check(rig.last.status() == PowderBedCore.ST_OUTPUT_BLOCKED && rig.powder == 4
                        && rig.argon == PowderBedCore.ARGON_PER_BATCH && rig.batches == 0,
                "a blocked reclaim output refuses the batch before charging");
    }

    private static void reclaimBlockedHoldsFinishedBatch() {
        Rig rig = new Rig();
        rig.state = PowderBedCore.WORK;
        rig.parallel = 1;
        rig.progress = PowderBedCore.TICKS_PER_BATCH - 1;
        rig.argonRemaining = PowderBedCore.argonPerTick(1);
        rig.reclaimOut = 64;
        rig.step();
        check(rig.state == PowderBedCore.DONE && rig.commits == 0 && rig.blankOut == 0,
                "the finished batch waits when the reclaim output is blocked");
        rig.step();
        check(rig.last.status() == PowderBedCore.ST_OUTPUT_BLOCKED && rig.commits == 0,
                "a blocked reclaim output leaves the whole batch pending");
        rig.reclaimOut = 0;
        rig.step();
        check(rig.blankOut == 1 && rig.reclaimOut == 2 && rig.commits == 1,
                "freeing the reclaim output settles the blank and the reclaim powder together");
        rig.run(20);
        check(rig.commits == 1 && rig.blankOut == 1 && rig.reclaimOut == 2,
                "an idle machine never duplicates the settled batch");
    }

    private static void completionCommitsOnce() {
        Rig rig = new Rig();
        rig.run((int) PowderBedCore.TICKS_PER_BATCH + 5);
        check(rig.blankOut == 1 && rig.reclaimOut == 2 && rig.commits == 1 && rig.batches == 1,
                "one full batch yields one blank and two reclaim powder");
    }

    private static void savedProgressResumesWithoutRecharging() {
        Rig rig = new Rig();
        rig.state = PowderBedCore.WORK;
        rig.parallel = 1;
        rig.progress = 123;
        rig.argonRemaining = 1000;
        rig.step();
        check(rig.progress == 124 && rig.batches == 0 && rig.powder == 4,
                "a saved batch resumes from its stored progress without re-charging");
    }

    private static void energyShortagePausesWithoutRecharging() {
        Rig rig = new Rig();
        rig.step();
        long progress = rig.progress;
        rig.energy = 0;
        rig.run(20);
        check(rig.progress == progress && rig.last.status() == PowderBedCore.ST_WAIT_ENERGY
                        && rig.batches == 1 && rig.powder == 0,
                "an EU shortage pauses the same batch without re-charging");
        rig.energy = 1_000_000;
        rig.step();
        check(rig.progress == progress + 1 && rig.batches == 1,
                "restored EU resumes the same batch");
    }

    private static void gasShortagePausesWithoutRecharging() {
        Rig rig = new Rig();
        rig.step();
        long progress = rig.progress;
        rig.argon = 0;
        rig.run(20);
        check(rig.progress == progress && rig.last.status() == PowderBedCore.ST_WAIT_ARGON
                        && rig.batches == 1 && rig.powder == 0,
                "an argon shortage pauses the same batch without re-charging");
        rig.argon = 1000;
        rig.step();
        check(rig.progress == progress + 1 && rig.batches == 1,
                "restored argon resumes the same batch");
    }

    private static void parallelUpgradeScalesBudget() {
        Rig rig = new Rig();
        rig.upgrade = true;
        rig.powder = 8;
        rig.argon = 2 * PowderBedCore.ARGON_PER_BATCH;
        rig.step();
        check(rig.parallel == 2 && rig.progress == 1,
                "the module plus cooling assembly starts two real batches");
        check(rig.powder == 0 && rig.consumedEnergy == 2 * PowderBedCore.EU_PER_TICK,
                "two batches charge twice the powder and twice the EU");
        check(rig.argon == 2 * PowderBedCore.ARGON_PER_BATCH - 2 * PowderBedCore.argonPerTick(1),
                "two batches drain twice the argon");
        rig.run((int) PowderBedCore.TICKS_PER_BATCH + 2);
        check(rig.blankOut == 2 && rig.reclaimOut == 4 && rig.commits == 1,
                "two batches settle two blanks and four reclaim powder");
    }

    private static void parallelNeedsBothParts() {
        Rig rig = new Rig();
        rig.powder = 8;
        rig.argon = 2 * PowderBedCore.ARGON_PER_BATCH;
        rig.step();
        check(rig.parallel == 1 && rig.powder == 4,
                "without both kept parts the machine still runs one batch");
    }

    private static void whitelistRejectsOtherForms() {
        String alloy = PowderManufacturingMaterials.ALLOY;
        check(PowderBedCore.acceptsPowder("classifiedPowder", alloy), "the classified powder is accepted");
        check(!PowderBedCore.acceptsPowder("dust", alloy), "ordinary dust is not qualified powder");
        check(!PowderBedCore.acceptsPowder("rawPowder", alloy), "raw powder is not qualified powder");
        check(!PowderBedCore.acceptsPowder("classifiedPowder", "Titanium"), "a second alloy is not accepted");
        check(PowderBedCore.acceptsModule("controlModule", "PrecisionControlModule"),
                "the P-03 control module is accepted");
        check(PowderBedCore.acceptsCooling("coolingAssembly", alloy),
                "the P-05 cooling assembly is accepted");
        check(!PowderBedCore.acceptsCooling("coolingBlock", alloy),
                "the raw cooling block is not the assembled component");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Rig implements PowderBedCore.Access {
        int powder = 4, argon = PowderBedCore.ARGON_PER_BATCH, blankOut, reclaimOut;
        long energy = 1_000_000, consumedEnergy;
        boolean upgrade;
        int state = PowderBedCore.IDLE, batches, commits, parallel, argonRemaining;
        long progress;
        PowderBedCore.Tick last = new PowderBedCore.Tick(PowderBedCore.ST_IDLE, 0);

        void step() {
            last = PowderBedCore.advance(this);
        }

        void run(int ticks) {
            for (int i = 0; i < ticks; i++) step();
        }

        @Override public int powderCount() { return powder; }
        @Override public int argon() { return argon; }
        @Override public boolean drainArgon(int amount) {
            if (argon < amount) return false;
            argon -= amount;
            return true;
        }
        @Override public int blankSpace() { return Math.max(0, 64 - blankOut); }
        @Override public int reclaimSpace() { return Math.max(0, 64 - reclaimOut); }
        @Override public boolean hasUpgrade() { return upgrade; }
        @Override public boolean hasEnergy(long amount) { return energy >= amount; }
        @Override public boolean consumeEnergy(long amount) {
            if (energy < amount) return false;
            energy -= amount;
            consumedEnergy += amount;
            return true;
        }
        @Override public int state() { return state; }
        @Override public void state(int value) { state = value; }
        @Override public long progress() { return progress; }
        @Override public void progress(long value) { progress = value; }
        @Override public int argonRemaining() { return argonRemaining; }
        @Override public void argonRemaining(int value) { argonRemaining = value; }
        @Override public int parallel() { return parallel; }
        @Override public void parallel(int value) { parallel = value; }

        @Override public int startBatch(int count) {
            int needed = PowderBedCore.POWDER_PER_BATCH * count;
            if (powder < needed) return 1;
            if (argon < PowderBedCore.ARGON_PER_BATCH * count) return 3;
            if (blankSpace() < count || reclaimSpace() < PowderBedCore.RECLAIM_PER_BATCH * count) return 2;
            powder -= needed;
            batches++;
            return 0;
        }

        @Override public boolean commitProduct() {
            int count = Math.max(1, parallel);
            if (blankSpace() < count || reclaimSpace() < PowderBedCore.RECLAIM_PER_BATCH * count)
                return false;
            blankOut += count;
            reclaimOut += PowderBedCore.RECLAIM_PER_BATCH * count;
            commits++;
            return true;
        }
    }
}
