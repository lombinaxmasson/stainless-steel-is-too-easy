package com.lombinaxmasson.ssite.machine;

import gregtech.api.machine.BlockFace;
import gregtech.api.addon.recipe.CrucibleMold;
import gregtech.api.addon.material.GregTechMaterials;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.BoundPart;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.material.GTValues;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * A lidded crucible pool fed with argon: the part the AOD converter and the VAR furnace share.
 * Pools are stored in GT units. Gas is stored in whole units, one unit per recipe U of gas.
 * Only the argon tank touches loader fluid types, so that part stays in the platform behavior.
 */
public abstract class MoltenPool {
    public static final String LID_PART = "aod_lid";
    public static final int ARGON_TANK = 0;
    public static final int CHARGE_SLOT = 0;

    protected static final long U = GTValues.U;
    protected static final long AMBIENT_K = 293L;
    protected static final long MAX_GAS_UNITS = 64L;

    static final String K_TEMP = "temp.k";
    static final String K_HEAT_CARRY = "heat.carry";
    static final String K_DRIFT = "drift.ticks";
    static final String K_ARGON = "gas.argon";

    /** The loader half of the argon tank. */
    public interface ArgonTank {
        int availableMillibuckets();

        boolean drainMillibuckets(int amount);
    }

    /** The loader behavior that owns a pool; the lid asks it what may fall in. */
    public interface Holder {
        MoltenPool pool();
    }

    protected record Charge(String material, String poolKey, long meltAtK) {}

    /** A pool a faucet can pour, or a solid leftover a spade rakes back out as dust. */
    protected record Pool(String material, String poolKey) {}

    /** One non-empty pool, in GT units, for inspection tooltips and sensors. */
    public record Content(String material, long amount) {}

    private final List<Charge> charges;
    private final List<Pool> pours;
    private final List<Pool> skims;
    private final long fallbackMeltK;

    /**
     * {@code pours} are offered to a mold in order. Every other charge pool must be listed in
     * {@code skims}, or it could only fill up.
     */
    protected MoltenPool(List<Charge> charges, List<Pool> pours, List<Pool> skims, long fallbackMeltK) {
        this.charges = List.copyOf(charges);
        this.pours = List.copyOf(pours);
        this.skims = List.copyOf(skims);
        this.fallbackMeltK = fallbackMeltK;
        List<String> drained = new ArrayList<>();
        for (Pool pool : pours) drained.add(pool.poolKey());
        for (Pool pool : skims) drained.add(pool.poolKey());
        for (Charge charge : charges) {
            if (!drained.contains(charge.poolKey())) {
                throw new IllegalStateException("Pool " + charge.poolKey() + " has no way out");
            }
        }
    }

    public abstract void tick(MachineBehaviorContext context, ArgonTank argon);

    /** Clears every stored gas when the lid opens, the pool cools or the structure breaks. */
    protected void vent(BehaviorState state) {
        state.setLong(K_ARGON, 0L);
    }

    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return slot == CHARGE_SLOT && chargeOf(stack) != null;
    }

    public boolean isCharge(ItemStack stack) {
        return chargeOf(stack) != null;
    }

    public boolean canFill(MachineBehaviorContext context, int tank) {
        return tank == ARGON_TANK;
    }

    public long temperature(MachineBehaviorContext context) {
        return temperature(context.state());
    }

    public static long temperature(BehaviorState state) {
        long temperature = state.getLong(K_TEMP);
        return temperature > 0 ? temperature : AMBIENT_K;
    }

    public long contentAmount(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        long amount = 0;
        for (Pool pool : pours) amount += state.getLong(pool.poolKey());
        for (Pool pool : skims) amount += state.getLong(pool.poolKey());
        return amount;
    }

    /** Non-empty pools in declaration order: products first, then skimmed leftovers and charges. */
    public List<Content> contents(BehaviorState state) {
        List<Content> rows = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (Pool pool : pours) addContent(rows, seen, state, pool.poolKey(), pool.material());
        for (Pool pool : skims) addContent(rows, seen, state, pool.poolKey(), pool.material());
        for (Charge charge : charges) addContent(rows, seen, state, charge.poolKey(), charge.material());
        return rows;
    }

    private static void addContent(List<Content> rows, List<String> seen, BehaviorState state,
                                   String key, String material) {
        if (seen.contains(key)) return;
        seen.add(key);
        long amount = state.getLong(key);
        if (amount > 0) rows.add(new Content(material, amount));
    }

    /** Whole argon units buffered in the vessel. */
    public long argon(BehaviorState state) {
        return state.getLong(K_ARGON);
    }

    /** Whole oxygen/air units buffered in the vessel; furnaces without air blowing keep zero. */
    public long air(BehaviorState state) {
        return 0L;
    }

    /** Temperature that triggers the failure check, or 0 when this pool has none. */
    public long temperatureLimitK() {
        return 0L;
    }

    /** Offers the pool to a faucet or mold, one material at a time. */
    public boolean pour(MachineBehaviorContext context, CrucibleMold mold, int moldSide) {
        if (mold == null) return false;
        BehaviorState state = context.state();
        long temperature = temperature(context);
        for (Pool target : pours) {
            long amount = state.getLong(target.poolKey());
            if (amount <= 0) continue;
            GregTechMaterials.Material material = material(target.material());
            if (!material.valid()) continue;
            long accepted = mold.fillMold(material, amount, temperature, BlockFace.of(moldSide));
            if (accepted > 0) {
                state.setLong(target.poolKey(), amount - accepted);
                return true;
            }
        }
        return false;
    }

    /** Rakes up to one stack of a leftover that cannot be poured back out to the player as dust. */
    public boolean skim(MachineBehaviorContext context, Player player) {
        if (context.level().isClientSide) return false;
        BehaviorState state = context.state();
        for (Pool pool : skims) {
            long amount = state.getLong(pool.poolKey());
            if (amount <= 0) continue;
            long dusts = Math.min(amount / U, 64L);
            ItemStack stack;
            long taken;
            if (dusts > 0) {
                stack = AddonItems.stack("dust", pool.material(), (int) dusts);
                taken = dusts * U;
            } else {
                long tiny = amount / GTValues.U9;
                stack = tiny > 0 ? AddonItems.stack("dustTiny", pool.material(), (int) tiny) : ItemStack.EMPTY;
                taken = amount;
            }
            if (stack.isEmpty() && amount >= GTValues.U9) continue;
            state.setLong(pool.poolKey(), amount - taken);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack, false);
            return true;
        }
        return false;
    }

    public boolean lidOpen(MachineBehaviorContext context) {
        BoundPart lid = lidPart(context);
        if (lid == null) return true;
        var partContext = lid.context();
        return partContext != null && partContext.state().getBoolean("open");
    }

    private static BoundPart lidPart(MachineBehaviorContext context) {
        for (BoundPart part : context.structure().parts()) {
            if (LID_PART.equals(part.name()) || LargeAodStructure.LID.equals(part.name())) return part;
        }
        return null;
    }

    /**
     * Shared start of a tick. Without the lid the pool keeps cooling as if open, and its gas is lost.
     * Returns false when the structure is not formed and the rest of the tick should not run.
     */
    protected boolean formed(MachineBehaviorContext context) {
        if (context.structure().formed()) return true;
        unformed(context.state());
        context.panel().working(false);
        return false;
    }

    /** Cools at the open-lid rate and vents stored gas while the lid is missing. */
    void unformed(BehaviorState state) {
        drift(state, true);
        vent(state);
    }

    /** Turns the main energy buffer into kelvin. Returns true when any heat arrived. */
    protected static boolean heat(MachineBehaviorContext context, long perKelvin, long maxK) {
        long heat = context.energy().mainStored();
        if (heat <= 0) return false;
        BehaviorState state = context.state();
        if (!context.energy().consumeMain(heat)) return false;
        addHeat(state, heat, perKelvin, maxK);
        context.panel().working(true);
        return true;
    }

    static void addHeat(BehaviorState state, long heat, long perKelvin, long maxK) {
        long kelvin = Math.max(1L, perKelvin);
        long carry = Math.max(0, state.getLong(K_HEAT_CARRY)) % kelvin;
        long remainder = Math.max(0, heat) % kelvin;
        long rise = Math.max(0, heat) / kelvin;
        if (remainder >= kelvin - carry) { rise++; carry = remainder - (kelvin - carry); }
        else carry += remainder;
        long current = temperature(state);
        state.setLong(K_TEMP, Math.min(maxK, rise > Long.MAX_VALUE - current ? Long.MAX_VALUE : current + rise));
        state.setLong(K_HEAT_CARRY, carry);
    }

    enum Failure { NONE, MELTDOWN, EXPLOSION }

    static Failure failure(long temperature, long limit, boolean open) {
        return limit <= 0 || temperature <= limit ? Failure.NONE : open ? Failure.MELTDOWN : Failure.EXPLOSION;
    }

    /** Check before cooling or reacting: an overtemperature tick must not make one final batch. */
    protected boolean overheat(MachineBehaviorContext context, long limit) {
        Failure failure = failure(temperature(context), limit, lidOpen(context));
        if (failure == Failure.NONE) return false;
        if (context.level().isClientSide) return true;
        List<BlockPos> positions = new ArrayList<>(failurePositions(context));
        context.panel().working(false);
        for (BlockPos pos : positions) context.level().destroyBlock(pos, false);
        context.level().destroyBlock(context.pos(), false);
        if (failure == Failure.EXPLOSION) {
            BlockPos pos = context.pos();
            context.level().explode(null, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                    explosionPower(), Level.ExplosionInteraction.BLOCK);
        }
        return true;
    }

    protected float explosionPower() { return 2.0F; }

    protected List<BlockPos> failurePositions(MachineBehaviorContext context) {
        return context.structure().parts().stream().map(BoundPart::pos).toList();
    }

    /** Loses one kelvin every 10 ticks, or every 5 with the lid open. */
    static void drift(BehaviorState state, boolean lidOpen) {
        int ticks = state.getInt(K_DRIFT) + 1;
        if (ticks < (lidOpen ? 5 : 10)) {
            state.setInt(K_DRIFT, ticks);
            return;
        }
        state.setInt(K_DRIFT, 0);
        long temperature = state.getLong(K_TEMP);
        if (temperature > AMBIENT_K) state.setLong(K_TEMP, temperature - 1L);
    }

    /** Draws whole gas units of argon, at most {@code units}. Returns how many were drawn. */
    static long drawArgon(ArgonTank argon, long units, int mbPerGasUnit) {
        if (units <= 0) return 0;
        long wanted = Math.min(units, MAX_GAS_UNITS) * mbPerGasUnit;
        int use = (int) Math.min(wanted, argon.availableMillibuckets());
        use -= use % mbPerGasUnit;
        if (use <= 0 || !argon.drainMillibuckets(use)) return 0;
        return use / mbPerGasUnit;
    }

    private interface SlotSink {
        void set(ItemStack stack);
    }

    /** Already charged items keep melting after the lid closes; only new insertion needs an open lid. */
    protected boolean meltCharge(MachineBehaviorContext context, long temperature) {
        BehaviorState state = context.state();
        if (meltFrom(state, temperature, context.inventory().snapshot(CHARGE_SLOT),
                stack -> context.inventory().set(CHARGE_SLOT, stack))) {
            return true;
        }
        BoundPart lid = lidPart(context);
        if (lid == null || lid.context() == null || lid.slots() <= 0) return false;
        var lidContext = lid.context();
        return meltFrom(state, temperature, lid.item(0), stack -> lidContext.inventory().set(0, stack));
    }

    private boolean meltFrom(BehaviorState state, long temperature, ItemStack stack, SlotSink sink) {
        Charge charge = chargeOf(stack);
        if (charge == null) return false;
        if (temperature < meltPoint(charge)) return false;
        long units = unitOf(stack);
        if (units <= 0) return false;
        ItemStack remaining = stack.copy();
        remaining.shrink(1);
        sink.set(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        state.setLong(charge.poolKey(), state.getLong(charge.poolKey()) + units);
        return true;
    }

    private Charge chargeOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var composition = AddonItems.composition(stack);
        if (composition.units().size() != 1) return null;
        String material = composition.units().get(0).material();
        for (Charge charge : charges) {
            if (charge.material().equals(material)) return charge;
        }
        return null;
    }

    private static long unitOf(ItemStack stack) {
        var composition = AddonItems.composition(stack);
        return composition.units().isEmpty() ? 0L : composition.units().get(0).amount();
    }

    private long meltPoint(Charge charge) {
        if (charge.meltAtK() > 0) return charge.meltAtK();
        GregTechMaterials.Material material = material(charge.material());
        return material.valid() && material.meltingPoint() > 0 ? material.meltingPoint() : fallbackMeltK;
    }

    /** Never null: an unknown name, or a null from the catalog, is the invalid sentinel. */
    static GregTechMaterials.Material material(String name) {
        GregTechMaterials.Material found = GregTechMaterials.find(name);
        return found == null ? GregTechMaterials.invalid() : found;
    }
}
