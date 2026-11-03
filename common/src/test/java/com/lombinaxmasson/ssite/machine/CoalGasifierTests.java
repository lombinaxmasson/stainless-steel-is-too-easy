package com.lombinaxmasson.ssite.machine;

/** Pure JVM checks of the gasifier reservation, pause and completion transitions. */
public final class CoalGasifierTests {
    private CoalGasifierTests() {}

    public static void main(String[] args) {
        coalBatchConsumesOneChargeAndSteam(); cokeBatchSavesSteamAndTime();
        missingChargeStaysIdle(); missingSteamRefusesStart();
        fullRawTankRefusesStart(); blockedResidueRefusesStart();
        euShortagePausesWithoutLosingProgress(); completedBatchWaitsForOutputRoom();
        storedProgressResumesWithoutCatchingUp(); idleVesselNeverDuplicatesTheBatch();
        System.out.println("CoalGasifierTests: all 10 checks passed.");
    }

    private static void coalBatchConsumesOneChargeAndSteam() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.step();
        check(rig.state == CoalGasifierCore.WORK && rig.batch == CoalGasifierCore.BATCH_COAL,
                "a coal charge starts a working batch");
        check(rig.solid == 0 && rig.steam == 0, "the batch reserves exactly one solid and its steam");
        rig.run(CoalGasifierCore.COAL_TICKS - 2);
        check(rig.state == CoalGasifierCore.WORK && rig.raw == 0, "no raw gas exists before the batch ends");
        rig.step();
        check(rig.state == CoalGasifierCore.DONE && rig.raw == 0, "the finished batch waits before releasing gas");
        rig.step();
        check(rig.state == CoalGasifierCore.IDLE, "the released batch returns the machine to idle");
        check(rig.raw == CoalGasifierCore.RAW_GAS, "one coal charge releases the fixed raw-gas volume");
        check(rig.residue == 1 && rig.commits == 1, "the residue is written once with the batch");
    }

    private static void cokeBatchSavesSteamAndTime() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COKE_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COKE;
        rig.run(CoalGasifierCore.COKE_TICKS);
        check(rig.state == CoalGasifierCore.DONE, "a coke charge completes in its shorter batch time");
        rig.step();
        check(rig.raw == CoalGasifierCore.RAW_GAS && rig.steam == 0,
                "coke uses less steam for the same raw-gas volume");
    }

    private static void missingChargeStaysIdle() {
        Rig rig = new Rig();
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.step();
        check(rig.state == CoalGasifierCore.IDLE && rig.last.status() == CoalGasifierCore.ST_WAIT_INPUT,
                "an empty charge slot keeps the machine idle");
        check(rig.steam == CoalGasifierCore.COAL_STEAM && rig.batchStarts == 0,
                "no steam is reserved without a solid charge");
    }

    private static void missingSteamRefusesStart() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM - 1;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.step();
        check(rig.state == CoalGasifierCore.IDLE && rig.last.status() == CoalGasifierCore.ST_WAIT_MEDIUM,
                "insufficient steam refuses the batch");
        check(rig.solid == 1 && rig.batchStarts == 0, "the solid is not consumed when steam is short");
    }

    private static void fullRawTankRefusesStart() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.raw = CoalGasifierCore.RAW_GAS_CAPACITY - CoalGasifierCore.RAW_GAS + 1;
        rig.step();
        check(rig.state == CoalGasifierCore.IDLE && rig.last.status() == CoalGasifierCore.ST_OUTPUT_BLOCKED,
                "a raw tank without a full batch of room refuses the charge");
        check(rig.solid == 1 && rig.steam == CoalGasifierCore.COAL_STEAM, "a blocked tank reserves nothing");
    }

    private static void blockedResidueRefusesStart() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.residue = 64;
        rig.step();
        check(rig.last.status() == CoalGasifierCore.ST_OUTPUT_BLOCKED && rig.batchStarts == 0,
                "a full residue slot refuses the charge");
        check(rig.solid == 1, "the solid stays in the input slot");
    }

    private static void euShortagePausesWithoutLosingProgress() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.run(50);
        long progress = rig.progress;
        check(rig.consumedEu == progress * CoalGasifierCore.EU_PER_TICK,
                "each working tick debits exactly one EU charge");
        rig.eu = 0;
        rig.run(20);
        check(rig.progress == progress && rig.last.status() == CoalGasifierCore.ST_WAIT_EU,
                "lost EU pauses progress without resetting the batch");
        rig.eu = 1_000_000;
        rig.run(10);
        check(rig.progress == progress + 10, "restored EU resumes the same batch");
    }

    private static void completedBatchWaitsForOutputRoom() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.step();
        rig.progress = CoalGasifierCore.COAL_TICKS - 1;
        rig.step();
        check(rig.state == CoalGasifierCore.DONE, "the batch reaches its finished state");
        rig.raw = CoalGasifierCore.RAW_GAS_CAPACITY - CoalGasifierCore.RAW_GAS + 1;
        rig.step();
        check(rig.state == CoalGasifierCore.DONE && rig.last.status() == CoalGasifierCore.ST_OUTPUT_BLOCKED,
                "a full tank keeps the finished batch pending");
        check(rig.commits == 0, "nothing is written while the raw tank is full");
        rig.raw = 0;
        rig.step();
        check(rig.commits == 1 && rig.raw == CoalGasifierCore.RAW_GAS,
                "freeing the tank releases the pending batch exactly once");
    }

    private static void storedProgressResumesWithoutCatchingUp() {
        Rig rig = new Rig();
        rig.state = CoalGasifierCore.WORK;
        rig.batch = CoalGasifierCore.BATCH_COAL;
        rig.progress = 100;
        rig.step();
        check(rig.progress == 101, "a saved batch resumes from its stored progress");
        check(rig.consumedEu == CoalGasifierCore.EU_PER_TICK, "an unloaded machine does not catch up on skipped ticks");
    }

    private static void idleVesselNeverDuplicatesTheBatch() {
        Rig rig = new Rig();
        rig.solid = 1;
        rig.steam = CoalGasifierCore.COAL_STEAM;
        rig.inputBatch = CoalGasifierCore.BATCH_COAL;
        rig.run(CoalGasifierCore.COAL_TICKS + 5);
        check(rig.commits == 1 && rig.raw == CoalGasifierCore.RAW_GAS, "the batch is committed exactly once");
        rig.solid = 0;
        rig.run(100);
        check(rig.commits == 1 && rig.residue == 1, "an idle machine never duplicates the residue or gas");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Rig implements CoalGasifierCore.Access {
        long eu = 1_000_000, steam, raw, progress, consumedEu;
        int state = CoalGasifierCore.IDLE, batch = CoalGasifierCore.BATCH_NONE;
        int solid, residue, inputBatch = CoalGasifierCore.BATCH_NONE, batchStarts, commits;
        CoalGasifierCore.Tick last = new CoalGasifierCore.Tick(0);

        void step() { last = CoalGasifierCore.advance(this); }
        void run(long ticks) { for (long i = 0; i < ticks; i++) step(); }

        @Override public long eu() { return eu; }
        @Override public boolean consumeEu(long amount) {
            if (eu < amount) return false;
            eu -= amount;
            consumedEu += amount;
            return true;
        }
        @Override public int state() { return state; }
        @Override public void state(int value) { state = value; }
        @Override public int batch() { return batch; }
        @Override public void batch(int value) { batch = value; }
        @Override public long progress() { return progress; }
        @Override public void progress(long value) { progress = value; }

        @Override public int startBatch() {
            if (solid <= 0 || inputBatch == CoalGasifierCore.BATCH_NONE) return 1;
            long needed = CoalGasifierCore.steamFor(inputBatch);
            if (steam < needed) return 2;
            if (CoalGasifierCore.RAW_GAS_CAPACITY - raw < CoalGasifierCore.RAW_GAS) return 3;
            if (residue >= 64) return 4;
            solid--;
            steam -= needed;
            batch = inputBatch;
            batchStarts++;
            return 0;
        }

        @Override public boolean commitBatch() {
            if (CoalGasifierCore.RAW_GAS_CAPACITY - raw < CoalGasifierCore.RAW_GAS || residue >= 64) return false;
            raw += CoalGasifierCore.RAW_GAS;
            residue++;
            commits++;
            return true;
        }
    }
}
