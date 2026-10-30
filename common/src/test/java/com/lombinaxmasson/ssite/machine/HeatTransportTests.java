package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** In-memory topology and energy ledgers exercise the production routing engine without a server. */
public final class HeatTransportTests {
    private HeatTransportTests() {}

    public static void main(String[] args) {
        singlePlateRateAndRemainder(); ninePlatesShareOneStore(); exactLinearRouteLoss();
        splitRoundingDoesNotMultiplyLoss(); partialReceiverChargesOnlyAcceptedHeat();
        blockedReceiverKeepsItsShare(); multipleEntriesShareOneTick(); noLeaderOrSecondTickTransfer();
        changedTopologyCannotClaimAnotherBudget(); reentrantReceiverCannotDuplicateHeat();
        rejectLongWideVerticalAndDisconnectedGroups(); rejectPipeLoopButAllowPlateGrid();
        missingChunkNeverLoadsOrTransfers(); cutPipeRetainsHeatAndAllowsDownstreamRemainder();
        integerLimitsStayBounded(); verticalPlateSidesAreNotNetworkEdges(); invalidNetworksRefuseInput();
        inspectionDoesNotClaimATick(); firstReceiverDisconnectsRoute(); firstReceiverUnloadsRoute();
        System.out.println("HeatTransportTests: all 20 checks passed.");
    }

    private static void singlePlateRateAndRemainder() {
        Rig rig = new Rig();
        Cell plate = rig.plate(0, 0, 0, 10_000);
        rig.receiver(plate.pos().above());
        var first = rig.tickAll();
        check(first.output == 4096 && first.loss == 0 && plate.heat == 5904, "single plate is capped at 4096 HU/t");
        check(rig.tickAll().output == 0 && plate.heat == 5904, "same-tick calls cannot duplicate plate output");
        rig.time++;
        check(rig.tickAll().output == 4096 && plate.heat == 1808, "a new tick restores the rate budget");
        rig.time++;
        check(rig.tickAll().output == 1808 && plate.heat == 0, "the last partial buffer drains without loss");
    }

    private static void ninePlatesShareOneStore() {
        Rig rig = new Rig();
        rig.grid();
        rig.at(0, 0, 0).heat = 9000;
        var tick = rig.tickAll();
        check(tick.output == 9000 && tick.loss == 0 && rig.stored() == 0, "nine plates share one injected buffer without heat loss");
        for (long amount : rig.received.values()) check(amount == 1000, "low-power heat reaches every member of the plate group");
    }

    private static void exactLinearRouteLoss() {
        for (int length : new int[]{1, 3, 12}) {
            Rig rig = line(length, 1000);
            var tick = rig.tickAll();
            check(tick.output == 1000 - 10L * length && tick.loss == 10L * length,
                    length + " duct route must lose exactly its linear one-percent-per-duct share");
            check(rig.stored() == 0 && tick.debited == 1000, "all routed heat is received or explicitly lost");
        }
    }

    private static void splitRoundingDoesNotMultiplyLoss() {
        Rig rig = new Rig(); rig.grid(); rig.duct(-1, 0, 1, 10_000);
        var tick = rig.tickAll();
        check(tick.output == 9900 && tick.loss == 100 && rig.stored() == 0,
                "splitting one source over nine plates must not round the same route loss nine times");
        for (long amount : rig.received.values()) check(amount == 1100, "single-duct source is shared evenly over nine plates");
    }

    private static void partialReceiverChargesOnlyAcceptedHeat() {
        Rig rig = line(3, 1000);
        rig.limits.put(new BlockPos(0, 1, 0), 17L);
        var tick = rig.tickAll();
        check(tick.output == 17 && tick.loss == 1 && tick.debited == 18 && rig.stored() == 982,
                "loss must be recomputed from actual partial acceptance, not from offered heat");
    }

    private static void blockedReceiverKeepsItsShare() {
        Rig rig = new Rig(); rig.grid(); rig.at(0, 0, 0).heat = 9000;
        rig.limits.put(new BlockPos(0, 1, 0), 0L);
        var tick = rig.tickAll();
        check(tick.output == 8000 && tick.loss == 0 && rig.stored() == 1000, "refused HU remains in the source store");
        for (var row : rig.received.entrySet())
            check(row.getValue() == (row.getKey().equals(new BlockPos(0, 1, 0)) ? 0 : 1000),
                    "one refused outlet does not starve or overload the other outlets");
    }

    private static void multipleEntriesShareOneTick() {
        Rig rig = new Rig(); rig.grid();
        Cell first = rig.duct(-1, 0, 1, 5000), second = rig.duct(3, 0, 1, 100_000);
        var tick = rig.tickAll();
        check(first.heat < 5000 && second.heat < 100_000, "multiple buffered entries participate in one transfer");
        check(tick.output == 9 * 4096 && tick.debited <= HeatTransportCore.PIPE_THROUGHPUT,
                "source count does not multiply either plate or network throughput");
        for (long amount : rig.received.values()) check(amount == 4096, "each plate keeps its own 4096 HU/t cap");
        long stored = rig.stored(), received = rig.receivedTotal();
        for (int i = 0; i < 10; i++) rig.tickAll();
        check(rig.stored() == stored && rig.receivedTotal() == received, "calling each inlet repeatedly cannot claim a fresh budget");
    }

    private static void noLeaderOrSecondTickTransfer() {
        Rig rig = line(2, 5000);
        Cell nonLeader = rig.cells.values().stream().max(Comparator.comparingLong(cell -> cell.pos.asLong())).orElseThrow();
        var result = HeatTransportCore.tick(rig, nonLeader.pos);
        check(result.status() == HeatTransportCore.NOT_LEADER && rig.stored() == 5000 && rig.receivedTotal() == 0,
                "a non-leader call does not transfer or consume energy");
        check(rig.tickAll().output > 0, "the actual leader remains able to process after a non-leader call");
    }

    private static void changedTopologyCannotClaimAnotherBudget() {
        Rig rig = new Rig(); Cell plate = rig.plate(0, 0, 0, 10_000); rig.receiver(plate.pos.above());
        check(rig.tickAll().output == 4096, "initial component transferred");
        rig.duct(-1, 0, 0, 10_000);
        long before = rig.stored(), received = rig.receivedTotal();
        rig.tickAll();
        check(rig.stored() == before && rig.receivedTotal() == received, "a new lower-coordinate leader cannot replay a processed component");
        rig.time++;
        check(rig.tickAll().output == 4096, "the merged component resumes on the next tick");
    }

    private static void reentrantReceiverCannotDuplicateHeat() {
        Rig rig = line(1, 1000);
        rig.callback = () -> rig.tickAll();
        var tick = rig.tickAll();
        check(tick.output == 990 && tick.loss == 10 && rig.receivedTotal() == 990,
                "receiver callbacks cannot recursively re-enter an unclaimed transfer budget");
    }

    private static void firstReceiverDisconnectsRoute() { changedDuringEmit(false); }

    private static void firstReceiverUnloadsRoute() { changedDuringEmit(true); }

    private static void changedDuringEmit(boolean unload) {
        Rig rig = new Rig(); rig.grid();
        rig.duct(-3, 0, 1, 10_000); rig.duct(-2, 0, 1, 0); rig.duct(-1, 0, 1, 0);
        BlockPos middle = new BlockPos(-2, 0, 1);
        rig.callback = () -> { if (unload) rig.unloaded.add(middle); else rig.cells.remove(middle); };
        var tick = rig.tickAll();
        check(tick.output > 0 && rig.received.values().stream().filter(value -> value > 0).count() == 1,
                "only the first receiver may receive heat when its callback " + (unload ? "unloads" : "cuts") + " the route");
        long expectedDebit = (tick.output * 100 + 96) / 97;
        check(tick.debited == expectedDebit && tick.loss == expectedDebit - tick.output,
                "the accepted first packet must retain its exact route-loss debit after topology changes");
        long remaining = rig.stored(), received = rig.receivedTotal();
        rig.tickAll();
        check(rig.stored() == remaining && rig.receivedTotal() == received,
                "a receiver-triggered topology change must not create another same-tick budget");
    }

    private static void rejectLongWideVerticalAndDisconnectedGroups() {
        Rig tooLong = line(13, 1000);
        rejected(tooLong, HeatTransportCore.TOO_LONG, "13-duct route");
        Rig wide = new Rig();
        for (int x = 0; x < 4; x++) { Cell p = wide.plate(x, 0, 0, x == 0 ? 1000 : 0); wide.receiver(p.pos.above()); }
        rejected(wide, HeatTransportCore.INVALID_PLATES, "four-wide group");
        Rig ten = new Rig(); ten.grid(); ten.at(0, 0, 0).heat = 1000; ten.plate(3, 0, 1, 0);
        rejected(ten, HeatTransportCore.INVALID_PLATES, "ten-plate group");
        Rig vertical = new Rig(); vertical.plate(0, 0, 0, 1000); vertical.plate(0, 1, 0, 0);
        vertical.duct(-1, 0, 0, 0); vertical.duct(-1, 1, 0, 0); vertical.receiver(new BlockPos(0, 2, 0));
        rejected(vertical, HeatTransportCore.INVALID_PLATES, "different plate levels joined through side pipes");
        Rig separate = new Rig(); separate.plate(0, 0, 0, 1000); separate.duct(1, 0, 0, 0); separate.plate(2, 0, 0, 0);
        separate.receiver(new BlockPos(0, 1, 0)); separate.receiver(new BlockPos(2, 1, 0));
        rejected(separate, HeatTransportCore.INVALID_PLATES, "two plate groups linked only by a pipe");
    }

    private static void rejectPipeLoopButAllowPlateGrid() {
        Rig loop = new Rig();
        loop.plate(-1, 0, 0, 0); loop.receiver(new BlockPos(-1, 1, 0));
        loop.duct(0, 0, 0, 1000); loop.duct(1, 0, 0, 0); loop.duct(1, 0, 1, 0); loop.duct(0, 0, 1, 0);
        rejected(loop, HeatTransportCore.LOOP, "pipe ring");
        Rig grid = new Rig(); grid.grid(); grid.at(0, 0, 0).heat = 1000;
        check(grid.tickAll().output == 1000, "a plate grid's square adjacency is not a forbidden pipe loop");
    }

    private static void missingChunkNeverLoadsOrTransfers() {
        Rig rig = line(2, 1000);
        rig.unloaded.add(new BlockPos(-1, 0, 1));
        rejected(rig, HeatTransportCore.UNLOADED, "unloaded neighboring chunk boundary");
        rig.unloaded.clear(); rig.time++;
        check(rig.tickAll().output == 980, "a loaded boundary allows the intact network to resume");
    }

    private static void cutPipeRetainsHeatAndAllowsDownstreamRemainder() {
        Rig rig = line(3, 1000); rig.at(0, 0, 0).heat = 900;
        rig.cells.remove(new BlockPos(-2, 0, 0));
        var tick = rig.tickAll();
        check(rig.at(-3, 0, 0).heat == 1000, "disconnected upstream pipe does not send through empty space");
        check(tick.output == 900 && tick.loss == 0 && rig.at(0, 0, 0).heat == 0,
                "downstream plate releases its already stored heat after the upstream pipe is cut");
        rig.duct(-2, 0, 0, 0); rig.time++;
        check(rig.tickAll().output == 970 && rig.stored() == 0, "restoring the real path resumes only the remaining upstream heat");
    }

    private static void integerLimitsStayBounded() {
        Rig rig = new Rig(); Cell plate = rig.plate(0, 0, 0, Long.MAX_VALUE); rig.receiver(plate.pos.above());
        check(rig.tickAll().output == 4096 && plate.heat == Long.MAX_VALUE - 4096, "large stored values do not overflow transfer math");
        check(HeatTransportCore.delivered(Long.MAX_VALUE, 12) == HeatTransportCore.PIPE_THROUGHPUT * 88 / 100,
                "untrusted gross requests are bounded before multiplication");
        check(HeatTransportCore.delivered(-1, 1) == 0 && HeatTransportCore.delivered(1000, 13) == 0,
                "negative amounts and unsupported routes yield no output");
        Rig tiny = line(1, 1);
        check(tiny.tickAll().output == 0 && tiny.stored() == 1, "untransferable rounding residue is not fabricated or silently deleted");
    }

    private static void verticalPlateSidesAreNotNetworkEdges() {
        Rig rig = new Rig();
        Cell low = rig.plate(0, 0, 0, 1000), high = rig.plate(0, 1, 0, 500);
        rig.receiver(high.pos.above());
        check(!HeatTransportCore.connected(low, high, Direction.UP)
                        && !HeatTransportCore.connected(high, low, Direction.DOWN),
                "vertical plate neighbors must not silently merge separate heating surfaces");
        check(HeatTransportCore.inspect(rig, low.pos).plates() == 1 && HeatTransportCore.inspect(rig, high.pos).plates() == 1,
                "stacked plates remain distinct one-plate components");
        var tick = rig.tickAll();
        check(tick.output == 500 && tick.loss == 0 && low.heat == 1000 && high.heat == 0,
                "one heating surface cannot use another transport block as an arbitrary receiver");
    }

    private static void invalidNetworksRefuseInput() {
        Rig valid = line(3, 0);
        check(HeatTransportCore.canReceive(valid, new BlockPos(-3, 0, 0)), "intact loaded duct route accepts HU");
        valid.cells.remove(new BlockPos(-2, 0, 0));
        check(!HeatTransportCore.canReceive(valid, new BlockPos(-3, 0, 0)), "broken upstream route must reject new HU immediately");
        Rig longRoute = line(13, 0);
        check(!HeatTransportCore.canReceive(longRoute, new BlockPos(-13, 0, 0)), "overlength route rejects input");
        Rig absent = line(1, 0); absent.unloaded.add(new BlockPos(-1, 0, 1));
        check(!HeatTransportCore.canReceive(absent, new BlockPos(-1, 0, 0)), "unloaded boundary rejects input without loading it");
    }

    private static void inspectionDoesNotClaimATick() {
        Rig rig = line(3, 1000);
        for (int i = 0; i < 5; i++) {
            check(HeatTransportCore.inspect(rig, new BlockPos(-3, 0, 0)).status() == HeatTransportCore.READY,
                    "pure topology inspection reports a ready route");
            check(HeatTransportCore.canReceive(rig, new BlockPos(-3, 0, 0)), "repeated packet simulation remains allowed");
        }
        check(rig.stored() == 1000 && rig.receivedTotal() == 0
                        && rig.cells.values().stream().allMatch(cell -> cell.tick == Long.MIN_VALUE && cell.status == -1),
                "inspection/simulated input must not consume heat, publish a tick or update processing markers");
        check(rig.tickAll().output == 970, "inspection cannot suppress the first real transport tick");
    }

    private static Rig line(int ducts, long heat) {
        Rig rig = new Rig(); Cell plate = rig.plate(0, 0, 0, 0); rig.receiver(plate.pos.above());
        for (int x = -ducts; x < 0; x++) rig.duct(x, 0, 0, x == -ducts ? heat : 0);
        return rig;
    }

    private static void rejected(Rig rig, int status, String description) {
        long before = rig.stored();
        var tick = rig.tickAll();
        check(tick.output == 0 && tick.loss == 0 && rig.stored() == before, description + " must not move or delete HU");
        check(rig.cells.values().stream().anyMatch(cell -> cell.status == status), description + " reports the specific cause");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private record Ledger(long output, long loss, long debited) {}

    /** The fake world only owns buffers and receivers; it contains no routing or heat-loss algorithm. */
    private static final class Rig implements HeatTransportCore.World {
        final Map<BlockPos, Cell> cells = new LinkedHashMap<>();
        final Map<BlockPos, Long> received = new HashMap<>(), limits = new HashMap<>();
        final Set<BlockPos> unloaded = new HashSet<>();
        long time = 1;
        Runnable callback;
        Cell at(int x, int y, int z) { return cells.get(new BlockPos(x, y, z)); }
        Cell duct(int x, int y, int z, long heat) { return add(x, y, z, HeatTransportCore.Kind.DUCT, heat); }
        Cell plate(int x, int y, int z, long heat) { return add(x, y, z, HeatTransportCore.Kind.PLATE, heat); }
        Cell add(int x, int y, int z, HeatTransportCore.Kind kind, long heat) {
            Cell cell = new Cell(this, new BlockPos(x, y, z), kind, heat); cells.put(cell.pos, cell); return cell;
        }
        void grid() {
            for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) receiver(plate(x, 0, z, 0).pos.above());
        }
        void receiver(BlockPos pos) { received.put(pos, 0L); }
        long stored() { return cells.values().stream().mapToLong(cell -> cell.heat).sum(); }
        long receivedTotal() { return received.values().stream().mapToLong(Long::longValue).sum(); }
        Ledger tickAll() {
            long before = stored(), receivedBefore = receivedTotal(), loss = 0;
            for (BlockPos pos : new ArrayList<>(cells.keySet())) loss += HeatTransportCore.tick(this, pos).loss();
            long output = receivedTotal() - receivedBefore, debit = before - stored();
            check(debit == output + loss, "every actual source debit must equal actual receiver credit plus explicit loss");
            check(cells.values().stream().allMatch(cell -> cell.heat >= 0), "source buffers may not underflow");
            return new Ledger(output, loss, debit);
        }
        @Override public boolean loaded(BlockPos pos) { return !unloaded.contains(pos); }
        @Override public HeatTransportCore.Node node(BlockPos pos) {
            check(loaded(pos), "routing attempted to inspect an unloaded block"); return cells.get(pos);
        }
        @Override public long gameTime() { return time; }
    }

    private static final class Cell implements HeatTransportCore.Node {
        final Rig world;
        final BlockPos pos;
        final HeatTransportCore.Kind kind;
        long heat, tick = Long.MIN_VALUE;
        int status = -1;
        Cell(Rig world, BlockPos pos, HeatTransportCore.Kind kind, long heat) {
            this.world = world; this.pos = pos; this.kind = kind; this.heat = heat;
        }
        @Override public BlockPos pos() { return pos; }
        @Override public HeatTransportCore.Kind kind() { return kind; }
        @Override public long stored() { return heat; }
        @Override public long processedTick() { return tick; }
        @Override public void processedTick(long value) { tick = value; }
        @Override public boolean consume(long amount) {
            if (amount < 0 || amount > heat) return false; heat -= amount; return true;
        }
        @Override public long emit(BlockPos target, long offered) {
            if (!world.received.containsKey(target)) return 0;
            long accepted = Math.min(heat, Math.min(offered, world.limits.getOrDefault(target, Long.MAX_VALUE)));
            check(accepted >= 0, "negative external transfer");
            heat -= accepted;
            world.received.merge(target, accepted, Long::sum);
            if (world.callback != null) { Runnable callback = world.callback; world.callback = null; callback.run(); }
            return accepted;
        }
        @Override public void report(int status, long output, long loss, int ducts, int plates) { this.status = status; }
    }
}
