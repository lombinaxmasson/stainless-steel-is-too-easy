package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

/** Pure JVM checks of the P-04 room scan, band rules, claims and consumable cores. */
public final class ProcessEnvironmentTests {
    private ProcessEnvironmentTests() {}

    public static void main(String[] args) {
        sealedRoomIsMeasured(); openPassageIsLeak(); unloadedBoundaryRejects();
        blockedControllerFaceRejects(); oversizedSpanRejects(); oversizedVolumeRejects();
        solidObstacleIsNotALeak(); separateControllersShareRoomId();
        claimsDetectOverlapAndExpire(); bandsFollowVentsThermostatsAndMounts();
        advancedProcessGate(); ventInstallsWearsAndReplaces(); ventPausesWithoutEnergy();
        thermostatNeedsCoolantAndEnergy(); revisionSignatureChanges();
        System.out.println("ProcessEnvironmentTests: all 15 checks passed.");
    }

    private static void sealedRoomIsMeasured() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(scan.valid() && scan.reason().equals(ProcessEnvironmentSnapshot.REASON_NONE),
                "a sealed box is a valid room");
        check(scan.volume() == 27, "the scan counts the interior cells");
        check(!scan.roomId().isEmpty(), "a valid room has an id");
    }

    private static void openPassageIsLeak() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0)
                .set(0, 1, -2, CleanroomRoom.Kind.PASSAGE);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(!scan.valid() && scan.reason().equals(ProcessEnvironmentSnapshot.REASON_LEAK),
                "an open door or fluid invalidates the room");
        check(scan.leaks().contains(new BlockPos(0, 1, -2)), "the opening is reported");
        check(scan.volume() == 26, "the leak keeps the measured interior for diagnostics");
    }

    private static void unloadedBoundaryRejects() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0)
                .set(2, 1, -1, CleanroomRoom.Kind.UNLOADED);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(!scan.valid() && scan.reason().equals(ProcessEnvironmentSnapshot.REASON_UNLOADED),
                "an unloaded boundary cell rejects the scan instead of force-loading");
    }

    private static void blockedControllerFaceRejects() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0)
                .set(0, 0, -1, CleanroomRoom.Kind.SEALED);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(!scan.valid() && scan.reason().equals(ProcessEnvironmentSnapshot.REASON_INVALID),
                "a controller whose front is blocked owns no room");
    }

    private static void oversizedSpanRejects() {
        Box room = Box.interior(-8, 0, -3, 8, 2, -1).controller(0, 0, 0);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(!scan.valid() && scan.reason().equals(ProcessEnvironmentSnapshot.REASON_TOO_LARGE),
                "a room wider than the span cap is rejected");
    }

    private static void oversizedVolumeRejects() {
        Box room = Box.interior(-6, 0, -13, 6, 12, -1).controller(0, 0, 0);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(!scan.valid() && scan.reason().equals(ProcessEnvironmentSnapshot.REASON_TOO_LARGE),
                "a room above the volume cap is rejected");
    }

    private static void solidObstacleIsNotALeak() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0)
                .set(0, 1, -2, CleanroomRoom.Kind.SEALED);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        check(scan.valid() && scan.volume() == 26,
                "a machine standing inside the room is an obstacle, not an opening");
    }

    private static void separateControllersShareRoomId() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0);
        CleanroomRoom.Scan left = CleanroomRoom.scan(new BlockPos(-1, 0, 0), Direction.NORTH,
                room, "minecraft:overworld");
        Box second = Box.interior(-1, 0, -3, 1, 2, -1).controller(1, 0, 0);
        CleanroomRoom.Scan right = CleanroomRoom.scan(new BlockPos(1, 0, 0), Direction.NORTH,
                second, "minecraft:overworld");
        check(left.roomId().equals(right.roomId()),
                "two faces opening into the same volume compute one room id");
    }

    private static void claimsDetectOverlapAndExpire() {
        RoomClaims claims = new RoomClaims();
        BlockPos a = new BlockPos(0, 0, 0), b = new BlockPos(1, 0, 0);
        check(!claims.claim("room", a, 0, 200), "the first controller owns the room");
        check(claims.claim("room", b, 10, 200), "a second live controller is an overlap");
        check(claims.liveOwners("room", 10, 200) == 2, "both owners are counted");
        claims.release("room", a);
        check(claims.liveOwners("room", 10, 200) == 1, "removing a controller releases its claim");
        check(!claims.claim("room", a, 500, 200), "an expired claim no longer causes overlap");
    }

    private static void bandsFollowVentsThermostatsAndMounts() {
        check(ProcessEnvironmentRules.cleanLevel(1, 1) == 2, "a running vent reaches the advanced clean band");
        check(ProcessEnvironmentRules.cleanLevel(0, 1) == 1, "an idle installed vent reaches only the basic band");
        check(ProcessEnvironmentRules.cleanLevel(0, 0) == 0, "no vent leaves the clean band at zero");
        check(ProcessEnvironmentRules.temperatureBand(1) == 1, "a running thermostat is the stable band");
        check(ProcessEnvironmentRules.temperatureBand(0) == 0, "no thermostat is not the stable band");
        check(ProcessEnvironmentRules.vibrationContribution(3, 0) == 3, "a close source vibrates at full amplitude");
        check(ProcessEnvironmentRules.vibrationContribution(3, 8) == 1, "distance reduces the contribution");
        check(ProcessEnvironmentRules.vibrationContribution(3, 13) == 0, "a source past the range is ignored");
        check(ProcessEnvironmentRules.vibrationBand(3, 0) == 3, "mounts absent leave the raw band");
        check(ProcessEnvironmentRules.vibrationBand(3, 2) == 1, "two mounts drop the band to the quiet limit");
        check(ProcessEnvironmentRules.vibrationBand(3, 9) == 1, "extra mounts do not reduce below the cap");
    }

    private static void advancedProcessGate() {
        Box room = Box.interior(-1, 0, -3, 1, 2, -1).controller(0, 0, 0);
        CleanroomRoom.Scan scan = scan(room, 0, 0, 0, Direction.NORTH);
        ProcessEnvironmentSnapshot good = ProcessEnvironmentRules.evaluate(scan.roomId(), 4, scan, 1, 1, 0, false);
        check(good.valid() && good.acceptsAdvancedProcess(), "all three bands qualify the advanced process");
        check(good.failureReason().equals(ProcessEnvironmentSnapshot.REASON_NONE), "a qualified room has no failure");
        ProcessEnvironmentSnapshot noisy = ProcessEnvironmentRules.evaluate(scan.roomId(), 4, scan, 1, 1, 3, false);
        check(!noisy.acceptsAdvancedProcess()
                        && noisy.failureReason().equals(ProcessEnvironmentSnapshot.REASON_VIBRATION),
                "vibration above the quiet band blocks the advanced process");
        ProcessEnvironmentSnapshot cold = ProcessEnvironmentRules.evaluate(scan.roomId(), 4, scan, 1, 0, 0, false);
        check(cold.failureReason().equals(ProcessEnvironmentSnapshot.REASON_TEMPERATURE),
                "an unstable temperature blocks the advanced process");
        ProcessEnvironmentSnapshot dirty = ProcessEnvironmentRules.evaluate(scan.roomId(), 4, scan, 0, 1, 0, false);
        check(dirty.failureReason().equals(ProcessEnvironmentSnapshot.REASON_CLEAN),
                "the basic clean band blocks the advanced process");
        ProcessEnvironmentSnapshot overlap = ProcessEnvironmentRules.evaluate(scan.roomId(), 4, scan, 1, 1, 0, true);
        check(!overlap.valid() && overlap.failureReason().equals(ProcessEnvironmentSnapshot.REASON_OVERLAP),
                "an overlap invalidates the room");
    }

    private static void ventInstallsWearsAndReplaces() {
        VentRig rig = new VentRig(2);
        check(rig.step().status() == CleanroomVentCore.ST_RUNNING, "a vent with a cartridge runs");
        check(rig.filters == 1 && rig.installed && rig.wear == 1, "one cartridge is installed and wears");
        rig.wear = CleanroomVentCore.FILTER_TICKS - 1;
        check(rig.step().status() == CleanroomVentCore.ST_RUNNING, "the last wear tick still runs");
        check(rig.step().status() == CleanroomVentCore.ST_CLOGGED && !rig.installed,
                "an exhausted filter stops until replaced");
        check(rig.step().status() == CleanroomVentCore.ST_RUNNING && rig.filters == 0,
                "the next tick installs the spare cartridge");
        check(rig.consumed == 2, "cartridges are consumed once per installation");
    }

    private static void ventPausesWithoutEnergy() {
        VentRig rig = new VentRig(1);
        rig.energy = 0;
        CleanroomVentCore.Step step = rig.step();
        check(step.status() == CleanroomVentCore.ST_NO_ENERGY && step.remainingPermille() == 1000,
                "a power loss pauses the vent with the filter intact");
        check(rig.filters == 0 && rig.consumed == 1, "installation happened before the energy pause");
        rig.energy = 1_000_000;
        check(rig.step().status() == CleanroomVentCore.ST_RUNNING && rig.consumed == 1,
                "restored EU resumes without consuming another cartridge");
    }

    private static void thermostatNeedsCoolantAndEnergy() {
        ThermostatRig rig = new ThermostatRig(0, 1_000_000);
        check(rig.step().status() == CleanroomThermostatCore.ST_NO_COOLANT, "no coolant stops the thermostat");
        rig.coolant = 10;
        rig.energy = 0;
        check(rig.step().status() == CleanroomThermostatCore.ST_NO_ENERGY && rig.drained == 0,
                "no EU stops the thermostat before draining coolant");
        rig.energy = 1_000_000;
        check(rig.step().status() == CleanroomThermostatCore.ST_RUNNING && rig.drained == 1,
                "a running thermostat drains one coolant unit");
    }

    private static void revisionSignatureChanges() {
        ProcessEnvironmentSnapshot a = new ProcessEnvironmentSnapshot(
                "room", true, 2, 1, 0, 1, ProcessEnvironmentSnapshot.REASON_NONE);
        ProcessEnvironmentSnapshot b = new ProcessEnvironmentSnapshot(
                "room", true, 1, 1, 0, 2, ProcessEnvironmentSnapshot.REASON_CLEAN);
        check(!ProcessEnvironmentRules.signature(a, false)
                        .equals(ProcessEnvironmentRules.signature(b, false)),
                "a band change changes the published signature");
        check(ProcessEnvironmentRules.signature(a, false)
                        .equals(ProcessEnvironmentRules.signature(a, false)),
                "an unchanged room keeps its signature and revision");
    }

    private static CleanroomRoom.Scan scan(Box room, int x, int y, int z, Direction facing) {
        return CleanroomRoom.scan(new BlockPos(x, y, z), facing, room, "minecraft:overworld");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /** Interior air box; every cell outside is a solid wall unless overridden. */
    private static final class Box implements CleanroomRoom.Cells {
        private final int minX, minY, minZ, maxX, maxY, maxZ;
        private final Map<BlockPos, CleanroomRoom.Kind> overrides = new HashMap<>();

        private Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }

        static Box interior(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            return new Box(minX, minY, minZ, maxX, maxY, maxZ);
        }

        Box controller(int x, int y, int z) {
            return set(x, y, z, CleanroomRoom.Kind.CONTROLLER);
        }

        Box set(int x, int y, int z, CleanroomRoom.Kind kind) {
            overrides.put(new BlockPos(x, y, z), kind);
            return this;
        }

        @Override public CleanroomRoom.Kind at(BlockPos pos) {
            CleanroomRoom.Kind override = overrides.get(pos);
            if (override != null) return override;
            boolean inside = pos.getX() >= minX && pos.getX() <= maxX
                    && pos.getY() >= minY && pos.getY() <= maxY
                    && pos.getZ() >= minZ && pos.getZ() <= maxZ;
            return inside ? CleanroomRoom.Kind.AIR : CleanroomRoom.Kind.SEALED;
        }
    }

    private static final class VentRig implements CleanroomVentCore.Access {
        int filters;
        long energy = 1_000_000;
        boolean installed;
        int wear;
        int consumed;

        VentRig(int filters) { this.filters = filters; }

        CleanroomVentCore.Step step() {
            return CleanroomVentCore.advance(this);
        }

        @Override public boolean hasFilter() { return filters > 0; }
        @Override public boolean consumeFilter() {
            if (filters <= 0) return false;
            filters--;
            consumed++;
            return true;
        }
        @Override public boolean installed() { return installed; }
        @Override public void installed(boolean value) { installed = value; }
        @Override public int wear() { return wear; }
        @Override public void wear(int value) { wear = value; }
        @Override public boolean hasEnergy(long amount) { return energy >= amount; }
        @Override public boolean consumeEnergy(long amount) {
            if (energy < amount) return false;
            energy -= amount;
            return true;
        }
    }

    private static final class ThermostatRig implements CleanroomThermostatCore.Access {
        int coolant;
        long energy;
        int drained;

        ThermostatRig(int coolant, long energy) {
            this.coolant = coolant;
            this.energy = energy;
        }

        CleanroomThermostatCore.Step step() { return CleanroomThermostatCore.advance(this); }

        @Override public int coolant() { return coolant; }
        @Override public boolean drainCoolant(int amount) {
            if (coolant < amount) return false;
            coolant -= amount;
            drained += amount;
            return true;
        }
        @Override public boolean hasEnergy(long amount) { return energy >= amount; }
        @Override public boolean consumeEnergy(long amount) {
            if (energy < amount) return false;
            energy -= amount;
            return true;
        }
    }
}
