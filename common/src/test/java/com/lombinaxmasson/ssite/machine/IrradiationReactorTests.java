package com.lombinaxmasson.ssite.machine;

/** Pure JVM checks of the batch engine both loaders call from the vessel behavior. */
public final class IrradiationReactorTests {
    private IrradiationReactorTests() {}

    public static void main(String[] args) {
        batchConsumesExactlyFourPellets(); missingInputsStayIdle(); blockedOutputRefusesBatch();
        kuShortagePausesWithoutLosingProgress(); heatBudgetIsConservedOverABatch();
        missingHeatSinkOverheatsAndDamagesTooling(); repairClearsTheFaultAndDropsTheBatch();
        productIsCommittedOnce(); outputBlockedAtCompletionKeepsTheBatch();
        storedProgressResumesWithoutCatchingUp(); temperatureCoolsWhenIdle();
        System.out.println("IrradiationReactorTests: all 11 checks passed.");
    }

    private static void batchConsumesExactlyFourPellets() {
        Rig rig = new Rig();
        rig.targets = 6;
        rig.step();
        check(rig.state == IrradiationReactorCore.WORK && rig.targets == 2, "one batch consumes exactly four pellets");
        check(rig.batchStarts == 1 && rig.output == 0, "no product exists before the batch completes");
    }

    private static void missingInputsStayIdle() {
        Rig rig = new Rig();
        rig.targets = 3;
        rig.step();
        check(rig.state == IrradiationReactorCore.IDLE && rig.last.status() == IrradiationReactorCore.ST_WAIT_INPUT,
                "an incomplete pellet batch is not started");
        rig.targets = 0;
        rig.step();
        check(rig.batchStarts == 0 && rig.targets == 0, "no material is consumed without a full batch");
    }

    private static void blockedOutputRefusesBatch() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.output = 64;
        rig.step();
        check(rig.state == IrradiationReactorCore.IDLE && rig.last.status() == IrradiationReactorCore.ST_OUTPUT_BLOCKED,
                "a full output slot blocks the batch");
        check(rig.targets == 4, "a blocked output does not consume the pellets");
    }

    private static void kuShortagePausesWithoutLosingProgress() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.step();
        rig.run(100);
        long progress = rig.progress;
        check(rig.consumedKu == progress * IrradiationReactorCore.KU_PER_TICK,
                "each working tick debits exactly one KU charge");
        rig.ku = 0;
        rig.run(50);
        check(rig.progress == progress && rig.last.status() == IrradiationReactorCore.ST_WAIT_KU,
                "lost KU stops progress without resetting the batch");
        rig.ku = 1_000_000;
        rig.run(10);
        check(rig.progress == progress + 10, "restored KU resumes the same batch");
    }

    private static void heatBudgetIsConservedOverABatch() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.run(IrradiationReactorCore.BATCH_TICKS + 2);
        check(rig.output == IrradiationReactorCore.BATCH_TARGETS, "a full batch yields one blanket per pellet");
        check(rig.generated == IrradiationReactorCore.HEAT_PER_TICK * IrradiationReactorCore.BATCH_TICKS,
                "the batch generates its fixed heat budget");
        check(rig.generated == rig.emitted + rig.stored + rig.discarded,
                "generated heat equals the amount sent plus the amount still buffered");
        check(rig.emitted <= IrradiationReactorCore.EMIT_PER_TICK * IrradiationReactorCore.BATCH_TICKS,
                "the outlet never exceeds its per-tick rate");
    }

    private static void missingHeatSinkOverheatsAndDamagesTooling() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.heatSink = false;
        rig.run(2_000);
        check(rig.toolingDamaged && rig.state == IrradiationReactorCore.FAULT && rig.sawFault,
                "a full buffer without an outlet ends in a shield fault");
        check(rig.progress == 0, "the damaged batch is dropped instead of resuming");
        check(rig.output == 0, "an overheated batch produces nothing");
    }

    private static void repairClearsTheFaultAndDropsTheBatch() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.heatSink = false;
        rig.run(2_000);
        check(rig.state == IrradiationReactorCore.FAULT && rig.down, "the fault waits for the shield to be seen missing");
        rig.formed = true;
        rig.heatSink = true;
        rig.step();
        check(rig.state != IrradiationReactorCore.FAULT, "rebuilding the shield clears the fault");
        check(rig.temperature == IrradiationReactorCore.AMBIENT_K, "the repaired vessel cools to ambient");
    }

    private static void productIsCommittedOnce() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.heatSink = true;
        rig.run(IrradiationReactorCore.BATCH_TICKS + 40);
        check(rig.output == IrradiationReactorCore.BATCH_TARGETS && rig.commits == 1,
                "the finished batch is committed exactly once");
        rig.run(100);
        check(rig.commits == 1 && rig.output == IrradiationReactorCore.BATCH_TARGETS,
                "an idle vessel never duplicates the finished batch");
    }

    private static void outputBlockedAtCompletionKeepsTheBatch() {
        Rig rig = new Rig();
        rig.targets = 4;
        rig.step();
        rig.progress = IrradiationReactorCore.BATCH_TICKS - 1;
        rig.output = 62;
        rig.step();
        check(rig.state == IrradiationReactorCore.DONE, "the completed batch waits in the finished state");
        rig.step();
        check(rig.state == IrradiationReactorCore.DONE && rig.last.status() == IrradiationReactorCore.ST_OUTPUT_BLOCKED,
                "a blocked output at completion keeps the batch pending");
        check(rig.commits == 0 && rig.output == 62, "nothing is written while the output is full");
        rig.output = 0;
        rig.step();
        check(rig.commits == 1 && rig.output == IrradiationReactorCore.BATCH_TARGETS,
                "freeing the output commits the pending batch once");
    }

    private static void storedProgressResumesWithoutCatchingUp() {
        Rig rig = new Rig();
        rig.state = IrradiationReactorCore.WORK;
        rig.progress = 900;
        rig.formed = true;
        rig.run(1);
        check(rig.progress == 901, "a saved batch resumes from its stored progress");
        check(rig.generated == IrradiationReactorCore.HEAT_PER_TICK, "an unloaded vessel does not catch up on skipped ticks");
    }

    private static void temperatureCoolsWhenIdle() {
        Rig rig = new Rig();
        rig.temperature = 600;
        rig.step();
        check(rig.temperature == 600 - IrradiationReactorCore.COOL_K, "an idle vessel cools every tick");
        rig.temperature = IrradiationReactorCore.AMBIENT_K;
        rig.step();
        check(rig.temperature == IrradiationReactorCore.AMBIENT_K, "cooling stops at ambient");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Rig implements IrradiationReactorCore.Access {
        boolean formed = true, heatSink = true, down, toolingDamaged, sawFault;
        long ku = 1_000_000, stored, temperature = IrradiationReactorCore.AMBIENT_K;
        int state = IrradiationReactorCore.IDLE, targets, output, batchStarts, commits;
        long progress, consumedKu, generated, emitted;
        /** Heat the rig could not store; the engine must never create more than it stores. */
        long discarded;
        IrradiationReactorCore.Tick last = new IrradiationReactorCore.Tick(0, 0, 0, 0, 0);

        void step() {
            last = IrradiationReactorCore.advance(this);
            if (last.status() == IrradiationReactorCore.ST_FAULT) sawFault = true;
        }
        void run(long ticks) { for (long i = 0; i < ticks; i++) step(); }

        @Override public boolean formed() { return formed; }
        @Override public long stored() { return stored; }
        @Override public long capacity() { return IrradiationReactorCore.HEAT_CAPACITY; }
        @Override public long add(long amount) {
            long accepted = Math.min(amount, Math.max(0, capacity() - stored));
            stored += accepted;
            generated += accepted;
            discarded += amount - accepted;
            return accepted;
        }
        @Override public long emit(long amount) {
            if (!heatSink) return 0;
            long sent = Math.min(amount, stored);
            stored -= sent;
            emitted += sent;
            return sent;
        }
        @Override public long ku() { return ku; }
        @Override public boolean consumeKu(long amount) {
            if (ku < amount) return false;
            ku -= amount;
            consumedKu += amount;
            return true;
        }
        @Override public int state() { return state; }
        @Override public void state(int value) { state = value; }
        @Override public long progress() { return progress; }
        @Override public void progress(long value) { progress = value; }
        @Override public long temperature() { return temperature; }
        @Override public void temperature(long kelvin) { temperature = kelvin; }
        @Override public boolean down() { return down; }
        @Override public void down(boolean value) { down = value; }
        @Override public int startBatch() {
            if (targets < IrradiationReactorCore.BATCH_TARGETS) return 1;
            if (output + IrradiationReactorCore.BATCH_TARGETS > 64) return 2;
            targets -= IrradiationReactorCore.BATCH_TARGETS;
            batchStarts++;
            return 0;
        }
        @Override public boolean commitProduct() {
            if (output + IrradiationReactorCore.BATCH_TARGETS > 64) return false;
            output += IrradiationReactorCore.BATCH_TARGETS;
            commits++;
            return true;
        }
        @Override public boolean damageTooling() {
            toolingDamaged = true;
            formed = false;
            return true;
        }
    }
}
