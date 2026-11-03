package com.lombinaxmasson.ssite.machine;

/** Pure JVM checks of the P-03A ion-implanter and P-03B electron-beam ledgers. */
public final class AdvancedProcessTests {
    private AdvancedProcessTests() {}

    public static void main(String[] args) {
        ionRequiresEnvironmentBeforeCharging(); ionStartsAndChargesOnce();
        ionMissingInputKeepsCharge(); ionMissingArgonKeepsItems();
        ionEnergyPauseResumesWithoutRecharging(); ionEnvironmentLossPausesWithoutRecharging();
        ionBlockedOutputRefusesBatch(); ionCompletionWaitsForOutputAndCommitsOnce();
        ionSavedProgressResumes(); ionFullBatchProducesOneWafer();
        ebeamWhitelistKeepsTemplate(); ebeamUnknownTemplateRejected();
        ebeamEnvironmentLossPauses(); ebeamBlockedOutputRefusesBatch();
        ebeamEnergyPauseResumes(); ebeamFullBatchProducesOneWafer();
        System.out.println("AdvancedProcessTests: all 16 checks passed.");
    }

    private static void ionRequiresEnvironmentBeforeCharging() {
        IonRig rig = new IonRig();
        rig.step(false);
        check(rig.last.status() == IonImplanterCore.ST_ENV_INVALID && rig.progress == 0,
                "an unavailable process environment stops before a batch starts");
        check(rig.wafer == 1 && rig.dopant == 1 && rig.argon == IonImplanterCore.ARGON_PER_BATCH
                && rig.batches == 0, "no material is charged while the environment is unavailable");
    }

    private static void ionStartsAndChargesOnce() {
        IonRig rig = new IonRig();
        rig.step(true);
        check(rig.state == IonImplanterCore.WORK && rig.progress == 1,
                "a qualified environment starts the first tick");
        check(rig.wafer == 0 && rig.dopant == 0 && rig.argon == 0 && rig.batches == 1,
                "one batch charges one wafer, one dopant and one argon unit");
        long chargedEnergy = rig.consumedEnergy;
        rig.run(10, true);
        check(rig.batches == 1 && rig.wafer == 0 && rig.argon == 0
                        && rig.consumedEnergy == chargedEnergy + 10 * IonImplanterCore.EU_PER_TICK,
                "continuing ticks never re-charge materials");
    }

    private static void ionMissingInputKeepsCharge() {
        IonRig rig = new IonRig();
        rig.dopant = 0;
        rig.step(true);
        check(rig.last.status() == IonImplanterCore.ST_WAIT_INPUT && rig.wafer == 1
                        && rig.argon == IonImplanterCore.ARGON_PER_BATCH && rig.batches == 0,
                "a missing dopant does not consume the wafer or argon");
    }

    private static void ionMissingArgonKeepsItems() {
        IonRig rig = new IonRig();
        rig.argon = 0;
        rig.step(true);
        check(rig.last.status() == IonImplanterCore.ST_WAIT_FLUID
                        && rig.wafer == 1 && rig.dopant == 1 && rig.batches == 0,
                "a missing argon charge leaves the solid inputs untouched");
    }

    private static void ionEnergyPauseResumesWithoutRecharging() {
        IonRig rig = new IonRig();
        rig.step(true);
        long progress = rig.progress;
        rig.energy = 0;
        rig.run(20, true);
        check(rig.progress == progress && rig.last.status() == IonImplanterCore.ST_WAIT_ENERGY,
                "an EU shortage pauses the same batch");
        rig.energy = 1_000_000;
        rig.step(true);
        check(rig.progress == progress + 1 && rig.batches == 1,
                "restored EU resumes without re-charging materials");
    }

    private static void ionEnvironmentLossPausesWithoutRecharging() {
        IonRig rig = new IonRig();
        rig.step(true);
        long progress = rig.progress;
        rig.run(50, false);
        check(rig.progress == progress && rig.last.status() == IonImplanterCore.ST_ENV_INVALID,
                "losing the environment pauses progress at a tick boundary");
        check(rig.batches == 1 && rig.wafer == 0 && rig.dopant == 0,
                "the paused batch keeps its already charged materials");
        rig.step(true);
        check(rig.progress == progress + 1 && rig.batches == 1,
                "a restored environment resumes the same batch");
    }

    private static void ionBlockedOutputRefusesBatch() {
        IonRig rig = new IonRig();
        rig.output = 64;
        rig.step(true);
        check(rig.last.status() == IonImplanterCore.ST_OUTPUT_BLOCKED
                        && rig.wafer == 1 && rig.dopant == 1 && rig.batches == 0,
                "a full output slot refuses the batch before charging");
    }

    private static void ionCompletionWaitsForOutputAndCommitsOnce() {
        IonRig rig = new IonRig();
        rig.state = IonImplanterCore.WORK;
        rig.progress = IonImplanterCore.TICKS_PER_BATCH - 1;
        rig.output = 64;
        rig.step(true);
        check(rig.state == IonImplanterCore.DONE && rig.commits == 0,
                "the finished batch waits in the done state");
        rig.step(true);
        check(rig.last.status() == IonImplanterCore.ST_OUTPUT_BLOCKED && rig.output == 64,
                "a blocked output keeps the finished wafer pending");
        rig.output = 0;
        rig.step(true);
        check(rig.output == 1 && rig.commits == 1, "freeing the output commits exactly one wafer");
        rig.run(20, true);
        check(rig.commits == 1 && rig.output == 1, "an idle machine never duplicates the finished batch");
    }

    private static void ionSavedProgressResumes() {
        IonRig rig = new IonRig();
        rig.state = IonImplanterCore.WORK;
        rig.progress = 400;
        rig.step(true);
        check(rig.progress == 401, "a saved batch resumes from its stored progress");
    }

    private static void ionFullBatchProducesOneWafer() {
        IonRig rig = new IonRig();
        rig.run(IonImplanterCore.TICKS_PER_BATCH + 2, true);
        check(rig.output == 1 && rig.commits == 1 && rig.batches == 1,
                "one full batch yields one implanted wafer");
    }

    private static void ebeamWhitelistKeepsTemplate() {
        BeamRig rig = new BeamRig();
        rig.step(true);
        check(rig.state == ElectronBeamCore.WORK && rig.progress == 1,
                "the whitelisted template starts the write");
        check(rig.wafer.isEmpty() && rig.template.equals(ElectronBeamCore.TEMPLATE_ITEM_ID),
                "the coated wafer is consumed and the program template is kept");
        check(ElectronBeamCore.OUTPUT_WAFER_ID.equals(rig.lastOutput),
                "the whitelist selects the exposed output id");
    }

    private static void ebeamUnknownTemplateRejected() {
        BeamRig rig = new BeamRig();
        rig.template = "ssite:program_template_unknown";
        rig.step(true);
        check(rig.last.status() == ElectronBeamCore.ST_WAIT_TEMPLATE
                        && rig.wafer.equals(ElectronBeamCore.INPUT_WAFER_ID) && rig.batches == 0,
                "an unknown template never starts a batch");
    }

    private static void ebeamEnvironmentLossPauses() {
        BeamRig rig = new BeamRig();
        rig.step(true);
        long progress = rig.progress;
        rig.run(40, false);
        check(rig.progress == progress && rig.last.status() == ElectronBeamCore.ST_ENV_INVALID
                        && rig.batches == 1,
                "the electron beam pauses without re-charging when the environment is lost");
        rig.step(true);
        check(rig.progress == progress + 1, "the electron beam resumes the same write");
    }

    private static void ebeamBlockedOutputRefusesBatch() {
        BeamRig rig = new BeamRig();
        rig.output = 64;
        rig.step(true);
        check(rig.last.status() == ElectronBeamCore.ST_OUTPUT_BLOCKED
                        && rig.wafer.equals(ElectronBeamCore.INPUT_WAFER_ID) && rig.batches == 0,
                "a blocked output refuses the electron-beam batch");
    }

    private static void ebeamEnergyPauseResumes() {
        BeamRig rig = new BeamRig();
        rig.step(true);
        long progress = rig.progress;
        rig.energy = 0;
        rig.run(10, true);
        check(rig.progress == progress && rig.last.status() == ElectronBeamCore.ST_WAIT_ENERGY,
                "an EU shortage pauses the write");
        rig.energy = 1_000_000;
        rig.step(true);
        check(rig.progress == progress + 1, "restored EU resumes the write");
    }

    private static void ebeamFullBatchProducesOneWafer() {
        BeamRig rig = new BeamRig();
        rig.run(ElectronBeamCore.TICKS_PER_BATCH + 2, true);
        check(rig.output == 1 && rig.commits == 1 && rig.batches == 1,
                "one full electron-beam write yields one exposed wafer");
    }

    private static ProcessEnvironmentSnapshot environment(boolean valid) {
        return new ProcessEnvironmentSnapshot("test-room", valid, 2, 1, 0,
                42, valid ? ProcessEnvironmentSnapshot.REASON_NONE : ProcessEnvironmentSnapshot.REASON_CLEAN);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class IonRig implements IonImplanterCore.Access {
        int wafer = 1, dopant = 1, argon = IonImplanterCore.ARGON_PER_BATCH, output;
        long energy = 1_000_000, consumedEnergy;
        int state = IonImplanterCore.IDLE, batches, commits;
        long progress;
        IonImplanterCore.Tick last = new IonImplanterCore.Tick(IonImplanterCore.ST_IDLE, 0);

        void step(boolean environmentValid) {
            last = IonImplanterCore.advance(this, environment(environmentValid));
        }

        void run(long ticks, boolean environmentValid) {
            for (long i = 0; i < ticks; i++) step(environmentValid);
        }

        @Override public boolean hasWafer() { return wafer > 0; }
        @Override public boolean hasDopant() { return dopant > 0; }
        @Override public int argon() { return argon; }
        @Override public boolean drainArgon(int amount) {
            if (argon < amount) return false;
            argon -= amount;
            return true;
        }
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
        @Override public int startBatch() {
            if (wafer < 1 || dopant < 1) return 1;
            if (argon < IonImplanterCore.ARGON_PER_BATCH) return 3;
            if (output + 1 > 64) return 2;
            wafer--;
            dopant--;
            argon -= IonImplanterCore.ARGON_PER_BATCH;
            batches++;
            return 0;
        }
        @Override public boolean commitProduct() {
            if (output + 1 > 64) return false;
            output++;
            commits++;
            return true;
        }
    }

    private static final class BeamRig implements ElectronBeamCore.Access {
        String wafer = ElectronBeamCore.INPUT_WAFER_ID, template = ElectronBeamCore.TEMPLATE_ITEM_ID;
        String outputId = "", lastOutput = "";
        int output;
        long energy = 1_000_000;
        int state = ElectronBeamCore.IDLE, batches, commits;
        long progress;
        ElectronBeamCore.Tick last = new ElectronBeamCore.Tick(ElectronBeamCore.ST_IDLE, 0);

        void step(boolean environmentValid) {
            last = ElectronBeamCore.advance(this, environment(environmentValid));
            lastOutput = outputId;
        }

        void run(long ticks, boolean environmentValid) {
            for (long i = 0; i < ticks; i++) step(environmentValid);
        }

        @Override public String waferId() { return wafer; }
        @Override public String templateId() { return template; }
        @Override public String output() { return outputId; }
        @Override public void output(String value) { outputId = value == null ? "" : value; }
        @Override public boolean hasEnergy(long amount) { return energy >= amount; }
        @Override public boolean consumeEnergy(long amount) {
            if (energy < amount) return false;
            energy -= amount;
            return true;
        }
        @Override public int state() { return state; }
        @Override public void state(int value) { state = value; }
        @Override public long progress() { return progress; }
        @Override public void progress(long value) { progress = value; }
        @Override public int startBatch() {
            String out = ElectronBeamCore.TEMPLATE_OUTPUTS.get(template);
            if (out == null) return 3;
            if (!ElectronBeamCore.INPUT_WAFER_ID.equals(wafer)) return 1;
            if (output + 1 > 64) return 2;
            wafer = "";
            outputId = out;
            batches++;
            return 0;
        }
        @Override public boolean commitProduct() {
            if (outputId.isEmpty() || output + 1 > 64) return false;
            output++;
            commits++;
            return true;
        }
    }
}
