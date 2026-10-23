package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.CrucibleMold;
import com.gregtech.gregtech.api.addon.GregTechMaterials;
import com.gregtech.gregtech.api.addon.platform.AddonItems;
import com.gregtech.gregtech.api.addon.platform.BehaviorState;
import com.gregtech.gregtech.api.addon.platform.BoundPart;
import com.gregtech.gregtech.api.addon.platform.MachineBehaviorContext;
import com.gregtech.gregtech.api.material.GTValues;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
    protected static final int MB_PER_GAS_UNIT = 100;
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

    static long temperature(BehaviorState state) {
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
            long accepted = mold.fillMold(material, amount, temperature, moldSide);
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
        if (lid == null) return false;
        var partContext = lid.context();
        return partContext != null && partContext.state().getBoolean("open");
    }

    private static BoundPart lidPart(MachineBehaviorContext context) {
        for (BoundPart part : context.parts()) {
            if (LID_PART.equals(part.name())) return part;
        }
        return null;
    }

    /**
     * Shared start of a tick. Without the lid the pool keeps cooling as if open, and its gas is lost.
     * Returns false when the structure is not formed and the rest of the tick should not run.
     */
    protected boolean formed(MachineBehaviorContext context) {
        if (context.structureFormed()) return true;
        unformed(context.state());
        context.working(false);
        return false;
    }

    /** Cools at the open-lid rate and vents stored gas while the lid is missing. */
    void unformed(BehaviorState state) {
        drift(state, true);
        vent(state);
    }

    /** Turns the main energy buffer into kelvin. Returns true when any heat arrived. */
    protected static boolean heat(MachineBehaviorContext context, long perKelvin, long maxK) {
        long heat = context.energy();
        if (heat <= 0) return false;
        BehaviorState state = context.state();
        context.consume(heat);
        long carry = state.getLong(K_HEAT_CARRY) + heat;
        long kelvin = Math.max(1L, perKelvin);
        state.setLong(K_TEMP, Math.min(maxK, temperature(state) + carry / kelvin));
        state.setLong(K_HEAT_CARRY, carry % kelvin);
        context.working(true);
        return true;
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
    static long drawArgon(ArgonTank argon, long units) {
        if (units <= 0) return 0;
        long wanted = Math.min(units, MAX_GAS_UNITS) * MB_PER_GAS_UNIT;
        int use = (int) Math.min(wanted, argon.availableMillibuckets());
        use -= use % MB_PER_GAS_UNIT;
        if (use <= 0 || !argon.drainMillibuckets(use)) return 0;
        return use / MB_PER_GAS_UNIT;
    }

    private interface SlotSink {
        void set(ItemStack stack);
    }

    /** Melts one item from the menu slot, then from the open lid's top-feed slot. */
    protected boolean meltCharge(MachineBehaviorContext context, long temperature) {
        BehaviorState state = context.state();
        if (meltFrom(state, temperature, context.item(CHARGE_SLOT),
                stack -> context.setItem(CHARGE_SLOT, stack))) {
            return true;
        }
        BoundPart lid = lidPart(context);
        if (lid == null || lid.context() == null || lid.slots() <= 0 || !lidOpen(context)) return false;
        var lidContext = lid.context();
        return meltFrom(state, temperature, lid.item(0), stack -> lidContext.setItem(0, stack));
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
