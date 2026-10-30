package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Bounded HU routing. The individual machines own every stored and transmitted unit. */
public final class HeatTransportCore {
    public static final String DUCT_TYPE = "heat_duct", PLATE_TYPE = "heating_plate";
    public static final int MAX_DUCTS = 12, MAX_PLATES = 9;
    public static final long PIPE_THROUGHPUT = 73_728, PLATE_OUTPUT = 4_096;
    public static final int READY = 0, NO_PLATE = 1, TOO_LONG = 2, INVALID_PLATES = 3,
            LOOP = 4, UNLOADED = 5, ALREADY_TICKED = 6, NOT_LEADER = 7, NO_HEAT = 8, TOPOLOGY_CHANGED = 9;
    public static final String K_STATUS = "heat_network.status", K_OUTPUT = "heat_network.output",
            K_LOSS = "heat_network.loss", K_DUCTS = "heat_network.ducts", K_PLATES = "heat_network.plates",
            K_STORED = "heat_network.stored", K_PROCESSED = "heat_network.processed",
            K_PROCESSED_SET = "heat_network.processed_set";
    private static final Comparator<Node> NODE_ORDER = Comparator.comparingLong(node -> node.pos().asLong());

    private HeatTransportCore() {}

    public enum Kind { DUCT, PLATE }

    /** Implemented on the host block entity by the addon's narrowly scoped mixin. */
    public interface RuntimeAccess {
        long thermalStored();
        boolean thermalConsume(long amount);
        /** Sends from this host's real buffer and deducts only the accepted HU. */
        long thermalEmit(BlockPos target, long amount);
        BehaviorState thermalState();
    }

    /** No Minecraft server or block entity is required to exercise the routing/accounting engine. */
    public interface Node {
        BlockPos pos();
        Kind kind();
        /** Stable identity of the real store; replacing a block at the same position is a change. */
        default Object identity() { return this; }
        long stored();
        /** The implementation deducts the accepted amount from this node's own store. */
        long emit(BlockPos target, long amount);
        boolean consume(long amount);
        long processedTick();
        void processedTick(long tick);
        void report(int status, long output, long loss, int ducts, int plates);
    }

    public interface World {
        boolean loaded(BlockPos pos);
        /** Must not load a chunk; this method is called only after loaded(pos). */
        Node node(BlockPos pos);
        long gameTime();
    }

    public record Result(int status, long output, long loss, long debited, int ducts, int plates) {}

    private record Network(List<Node> nodes, List<Node> ducts, List<Node> plates,
                           Map<BlockPos, Integer> distances, int status) {}

    public static boolean isTransport(String type) {
        return DUCT_TYPE.equals(type) || PLATE_TYPE.equals(type);
    }

    /** Plates join only through their horizontal edges; their top face is exclusively an HU output. */
    public static boolean connected(Node first, Node second, Direction side) {
        if (first == null || second == null || side == null || !first.pos().relative(side).equals(second.pos()))
            return false;
        return side != Direction.UP && side != Direction.DOWN
                || first.kind() == Kind.DUCT && second.kind() == Kind.DUCT;
    }

    /** Pure topology inspection: no markers, storage, or receiver callbacks are changed. */
    public static Result inspect(World world, BlockPos start) {
        if (!world.loaded(start)) return new Result(UNLOADED, 0, 0, 0, 0, 0);
        Node origin = world.node(start);
        if (origin == null) return new Result(NO_PLATE, 0, 0, 0, 0, 0);
        Network network = discover(world, origin);
        return new Result(network.status(), 0, 0, 0, network.ducts().size(), network.plates().size());
    }

    public static Result inspect(Level level, BlockPos start) { return inspect(new LevelWorld(level), start); }
    public static boolean canReceive(World world, BlockPos start) { return inspect(world, start).status() == READY; }
    public static boolean canReceive(Level level, BlockPos start) { return inspect(level, start).status() == READY; }

    public static String statusTranslationKey(int status) {
        String suffix = switch (status) {
            case READY -> "ready";
            case NO_PLATE -> "no_plate";
            case TOO_LONG -> "too_long";
            case INVALID_PLATES -> "invalid_plates";
            case LOOP -> "loop";
            case UNLOADED -> "unloaded";
            case ALREADY_TICKED -> "already_ticked";
            case NOT_LEADER -> "not_leader";
            case NO_HEAT -> "no_heat";
            case TOPOLOGY_CHANGED -> "changed";
            default -> "unknown";
        };
        return "ssite.heat_transport.status." + suffix;
    }

    public static void tick(MachineBehaviorContext context) {
        if (context.level().isClientSide) return;
        Result result = tick(new LevelWorld(context.level()), context.pos());
        if (result.status() != NOT_LEADER && result.status() != ALREADY_TICKED)
            context.panel().working(result.output() > 0);
    }

    /**
     * The minimum-coordinate node owns this component's one transfer budget for this game tick.
     * Processed markers also prevent a newly merged/reconfigured component receiving a second budget.
     */
    public static Result tick(World world, BlockPos start) {
        if (!world.loaded(start)) return new Result(UNLOADED, 0, 0, 0, 0, 0);
        Node origin = world.node(start);
        if (origin == null) return new Result(NO_PLATE, 0, 0, 0, 0, 0);
        long tick = world.gameTime();
        if (origin.processedTick() == tick) return new Result(ALREADY_TICKED, 0, 0, 0, 0, 0);
        Network network = discover(world, origin);
        if (network.status() != READY) return finish(network, tick, network.status(), 0, 0);
        if (!network.nodes().get(0).pos().equals(start))
            return new Result(NOT_LEADER, 0, 0, 0, network.ducts().size(), network.plates().size());
        for (Node node : network.nodes()) {
            if (node.processedTick() == tick) return finish(network, tick, ALREADY_TICKED, 0, 0);
        }
        // Claim every member before calling an external receiver: re-entrant callbacks cannot reroute it.
        for (Node node : network.nodes()) node.processedTick(tick);

        long affordable = affordableOutput(network, PIPE_THROUGHPUT);
        int count = network.plates().size();
        long shared = Math.min(affordable, PLATE_OUTPUT * count);
        if (shared == 0) return finish(network, tick, NO_HEAT, 0, 0);
        long remainingBudget = PIPE_THROUGHPUT, output = 0, loss = 0;
        Map<BlockPos, Long> acceptedBySource = new HashMap<>(), chargedBySource = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Node plate = network.plates().get(i);
            BlockPos target = plate.pos().above();
            // Discovery checked all neighbors; recheck immediately before invoking an external block.
            if (!world.loaded(target)) continue;
            Node targetNode = world.node(target);
            if (targetNode != null) continue;
            long remainingOffer = shared / count + (i < shared % count ? 1 : 0);
            for (Node source : network.nodes()) {
                if (remainingOffer <= 0 || remainingBudget <= 0) break;
                int distance = network.distances().getOrDefault(source.pos(), 0);
                long gross = Math.min(Math.max(0, source.stored()), remainingBudget);
                long priorAccepted = acceptedBySource.getOrDefault(source.pos(), 0L);
                long priorCharge = chargedBySource.getOrDefault(source.pos(), 0L);
                // Round a source's total charge once, so splitting it over nine plates does not
                // create nine rounding penalties. Previously paid fractions stay within this tick.
                long offer = Math.min(remainingOffer, delivered(gross + priorCharge, distance) - priorAccepted);
                if (offer <= 0) continue;
                int currentStatus = currentTopology(world, network);
                if (currentStatus != READY) return finish(network, tick, currentStatus, output, loss);
                if (!world.loaded(target)) return finish(network, tick, UNLOADED, output, loss);
                if (world.node(target) != null) break;
                long accepted = source.emit(target, offer);
                if (accepted < 0 || accepted > offer)
                    throw new IllegalStateException("Heat receiver accepted outside the offered amount");
                if (accepted == 0) {
                    currentStatus = currentTopology(world, network);
                    if (currentStatus != READY) return finish(network, tick, currentStatus, output, loss);
                    continue;
                }
                long cumulativeAccepted = priorAccepted + accepted;
                long cumulativeCharge = charged(cumulativeAccepted, distance);
                long charge = cumulativeCharge - priorCharge, routeLoss = charge - accepted;
                if (routeLoss > 0 && !source.consume(routeLoss))
                    throw new IllegalStateException("Heat source refused its reserved transmission loss");
                acceptedBySource.put(source.pos(), cumulativeAccepted);
                chargedBySource.put(source.pos(), cumulativeCharge);
                remainingBudget -= charge;
                remainingOffer -= accepted;
                output += accepted;
                loss += routeLoss;
                // A receiver may synchronously break a duct, replace a plate, or unload a chunk.
                // Complete this packet's accounting, then stop before using any of the stale routes.
                currentStatus = currentTopology(world, network);
                if (currentStatus != READY) return finish(network, tick, currentStatus, output, loss);
            }
        }
        return finish(network, tick, READY, output, loss);
    }

    /** Linear one-percent loss per pipe in the actual route, rounded toward conserving the source. */
    public static long delivered(long gross, int ducts) {
        if (gross <= 0 || ducts < 0 || ducts > MAX_DUCTS) return 0;
        long bounded = Math.min(gross, PIPE_THROUGHPUT);
        return bounded * (100 - ducts) / 100;
    }

    public static long charged(long accepted, int ducts) {
        if (accepted <= 0) return 0;
        if (ducts < 0 || ducts > MAX_DUCTS || accepted > PIPE_THROUGHPUT)
            throw new IllegalArgumentException("Invalid heat route charge");
        int efficiency = 100 - ducts;
        return (accepted * 100 + efficiency - 1) / efficiency;
    }

    private static long affordableOutput(Network network, long budget) {
        long total = 0;
        for (Node source : network.nodes()) {
            long gross = Math.min(Math.max(0, source.stored()), budget);
            total += delivered(gross, network.distances().getOrDefault(source.pos(), 0));
            budget -= gross;
            if (budget <= 0) break;
        }
        return total;
    }

    /**
     * Checking the same real nodes and every connecting boundary proves that the previously validated
     * graph and all its distances still apply. It also detects additions without rebuilding a BFS.
     */
    private static int currentTopology(World world, Network network) {
        Map<BlockPos, Node> expected = new HashMap<>();
        for (Node node : network.nodes()) expected.put(node.pos(), node);
        for (Node previous : network.nodes()) {
            if (!world.loaded(previous.pos())) return UNLOADED;
            Node current = world.node(previous.pos());
            if (current == null || current.kind() != previous.kind() || current.identity() != previous.identity())
                return TOPOLOGY_CHANGED;
            for (Direction side : Direction.values()) {
                if (current.kind() == Kind.PLATE && (side == Direction.UP || side == Direction.DOWN)) continue;
                BlockPos neighborPos = current.pos().relative(side);
                if (!world.loaded(neighborPos)) return UNLOADED;
                Node neighbor = world.node(neighborPos);
                if (!connected(current, neighbor, side)) continue;
                Node oldNeighbor = expected.get(neighborPos);
                if (oldNeighbor == null || neighbor.kind() != oldNeighbor.kind()
                        || neighbor.identity() != oldNeighbor.identity()) return TOPOLOGY_CHANGED;
            }
        }
        return READY;
    }

    private static Result finish(Network network, long tick, int status, long output, long loss) {
        for (Node node : network.nodes()) {
            node.processedTick(tick);
            node.report(status, output, loss, network.ducts().size(), network.plates().size());
        }
        return new Result(status, output, loss, output + loss, network.ducts().size(), network.plates().size());
    }

    private static Network discover(World world, Node start) {
        Map<BlockPos, Node> found = new LinkedHashMap<>();
        ArrayDeque<Node> pending = new ArrayDeque<>();
        found.put(start.pos().immutable(), start);
        pending.add(start);
        int ductCount = start.kind() == Kind.DUCT ? 1 : 0, plateCount = start.kind() == Kind.PLATE ? 1 : 0;
        int status = READY;
        search:
        while (!pending.isEmpty()) {
            Node node = pending.removeFirst();
            for (Direction side : Direction.values()) {
                if (node.kind() == Kind.PLATE && (side == Direction.UP || side == Direction.DOWN)) continue;
                BlockPos neighbor = node.pos().relative(side);
                if (!world.loaded(neighbor)) { status = UNLOADED; break search; }
                if (found.containsKey(neighbor)) continue;
                Node next = world.node(neighbor);
                if (!connected(node, next, side)) continue;
                found.put(neighbor.immutable(), next);
                pending.addLast(next);
                if (next.kind() == Kind.DUCT && ++ductCount > MAX_DUCTS) { status = TOO_LONG; break search; }
                if (next.kind() == Kind.PLATE && ++plateCount > MAX_PLATES) { status = INVALID_PLATES; break search; }
            }
        }
        List<Node> nodes = new ArrayList<>(found.values());
        nodes.sort(NODE_ORDER);
        List<Node> ducts = nodes.stream().filter(node -> node.kind() == Kind.DUCT).toList();
        List<Node> plates = nodes.stream().filter(node -> node.kind() == Kind.PLATE).toList();
        Map<BlockPos, Integer> distances = new HashMap<>();
        if (status != READY) return new Network(nodes, ducts, plates, distances, status);
        if (!validPlateGroup(plates)) return new Network(nodes, ducts, plates, distances, INVALID_PLATES);

        // Contract the flat plate group into one terminal. Its ordinary square grid is not a pipe loop.
        int twicePipeEdges = 0, terminalEdges = 0;
        for (Node duct : ducts) {
            boolean touchesPlates = false;
            for (Direction side : Direction.values()) {
                Node neighbor = found.get(duct.pos().relative(side));
                if (!connected(duct, neighbor, side)) continue;
                if (neighbor.kind() == Kind.DUCT) twicePipeEdges++;
                else touchesPlates = true;
            }
            if (touchesPlates) terminalEdges++;
        }
        int vertices = ducts.size() + (plates.isEmpty() ? 0 : 1);
        if (twicePipeEdges / 2 + terminalEdges >= vertices)
            return new Network(nodes, ducts, plates, distances, LOOP);
        if (plates.isEmpty()) return new Network(nodes, ducts, plates, distances, NO_PLATE);

        ArrayDeque<Node> breadth = new ArrayDeque<>();
        for (Node plate : plates) { distances.put(plate.pos(), 0); breadth.add(plate); }
        while (!breadth.isEmpty()) {
            Node node = breadth.removeFirst();
            for (Direction side : Direction.values()) {
                Node next = found.get(node.pos().relative(side));
                if (!connected(node, next, side) || distances.containsKey(next.pos())) continue;
                distances.put(next.pos(), distances.get(node.pos()) + (next.kind() == Kind.DUCT ? 1 : 0));
                breadth.addLast(next);
            }
        }
        return new Network(nodes, ducts, plates, distances, READY);
    }

    private static boolean validPlateGroup(List<Node> plates) {
        if (plates.isEmpty()) return true;
        Set<BlockPos> positions = new HashSet<>();
        int y = plates.get(0).pos().getY(), minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE,
                maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Node plate : plates) {
            BlockPos pos = plate.pos();
            if (pos.getY() != y) return false;
            minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
            positions.add(pos);
        }
        if (maxX - minX > 2 || maxZ - minZ > 2) return false;
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(plates.get(0).pos());
        Set<BlockPos> visited = new HashSet<>();
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (!visited.add(pos)) continue;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(side);
                if (positions.contains(next) && !visited.contains(next)) pending.addLast(next);
            }
        }
        return visited.size() == positions.size();
    }

    private record LevelWorld(Level level) implements World {
        @Override public boolean loaded(BlockPos pos) { return level.hasChunkAt(pos); }
        @Override public long gameTime() { return level.getGameTime(); }
        @Override public Node node(BlockPos pos) {
            if (!loaded(pos)) return null;
            BlockEntity entity = level.getBlockEntity(pos);
            if (!(entity instanceof RuntimeAccess access) || !(entity instanceof BehaviorMachineClientView view)
                    || !"ssite".equals(view.addonId()) || !isTransport(view.machineType())) return null;
            return new RuntimeNode(entity, access, DUCT_TYPE.equals(view.machineType()) ? Kind.DUCT : Kind.PLATE);
        }
    }

    private record RuntimeNode(BlockEntity entity, RuntimeAccess access, Kind kind) implements Node {
        @Override public BlockPos pos() { return entity.getBlockPos(); }
        @Override public Object identity() { return entity; }
        @Override public long stored() { return entity.isRemoved() ? 0 : access.thermalStored(); }
        private boolean live() {
            Level level = entity.getLevel();
            return !entity.isRemoved() && level != null && level.hasChunkAt(pos()) && level.getBlockEntity(pos()) == entity;
        }
        @Override public long emit(BlockPos target, long amount) {
            return live() ? access.thermalEmit(target, amount) : 0;
        }
        // A successful emit can remove its own source through a receiver callback. The old host
        // still owns this packet's reserved loss, even though it can no longer start another emit.
        @Override public boolean consume(long amount) { return access.thermalConsume(amount); }
        @Override public long processedTick() {
            BehaviorState state = access.thermalState();
            return state.getBoolean(K_PROCESSED_SET) ? state.getLong(K_PROCESSED) : Long.MIN_VALUE;
        }
        @Override public void processedTick(long tick) {
            if (!live()) return;
            access.thermalState().setBoolean(K_PROCESSED_SET, true);
            access.thermalState().setLong(K_PROCESSED, tick);
        }
        @Override public void report(int status, long output, long loss, int ducts, int plates) {
            if (!live()) return;
            BehaviorState state = access.thermalState();
            state.setInt(K_STATUS, status); state.setLong(K_OUTPUT, output); state.setLong(K_LOSS, loss);
            state.setInt(K_DUCTS, ducts); state.setInt(K_PLATES, plates); state.setLong(K_STORED, stored());
        }
    }
}
