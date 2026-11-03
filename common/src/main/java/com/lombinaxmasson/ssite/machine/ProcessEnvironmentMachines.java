package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.addon.machine.LayoutPart;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P-04 cleanroom machines and structural parts.
 *
 * <p>The controller owns room detection, the filter vent supplies cleanliness and the thermostat
 * supplies the stable temperature band. The casing and isolation mount are ordinary structure
 * blocks: the casing closes the shell and the mount reduces registered machine vibration. None of
 * these machines is a recipe machine; their display values live in the behaviour cores.</p>
 */
public final class ProcessEnvironmentMachines implements AddonMachineContributor {
    public static final String CONTROLLER = "cleanroom_controller";
    public static final String VENT = "cleanroom_filter_vent";
    public static final String THERMOSTAT = "cleanroom_thermostat";
    public static final String CASING = "cleanroom_casing";
    public static final String ISOLATION = "vibration_isolation_mount";

    /**
     * Machines that vibrate only while working. The list is explicit: terrain, entities and
     * unlisted machines never contribute, so a room check stays a bounded local scan.
     */
    private static final Map<String, Integer> VIBRATION_SOURCES = vibrationSources();

    private static Map<String, Integer> vibrationSources() {
        Map<String, Integer> sources = new LinkedHashMap<>();
        for (AodTier tier : AodTier.values()) {
            sources.put(SsiteAddon.MOD_ID + ":" + tier.machineType(), 3);
            sources.put(SsiteAddon.MOD_ID + ":" + tier.largeMachineType(), 3);
        }
        sources.put(SsiteAddon.MOD_ID + ":" + VarFurnaceCore.MACHINE_TYPE, 3);
        sources.put(SsiteAddon.MOD_ID + ":" + FluidizedBedCore.MACHINE_TYPE, 3);
        sources.put(SsiteAddon.MOD_ID + ":" + IrradiationReactorCore.MACHINE_TYPE, 2);
        sources.put(SsiteAddon.MOD_ID + ":" + PrecisionMachines.MACHINE_TYPE, 2);
        sources.put(SsiteAddon.MOD_ID + ":" + FiberOpticMachines.MACHINE_TYPE, 1);
        return Map.copyOf(sources);
    }

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(controller(), vent(), thermostat());
    }

    public List<LayoutPart> layoutParts() {
        return List.of(
                LayoutPart.builder(CASING, "StainlessSteel").hardness(8)
                        .texture("block/machine/cleanroom_casing").build(),
                LayoutPart.builder(ISOLATION, "StainlessSteel").hardness(6)
                        .texture("block/machine/vibration_isolation_mount").build());
    }

    public static boolean isController(String blockId) {
        return id(CONTROLLER).equals(blockId);
    }

    public static boolean isVent(String blockId) {
        return id(VENT).equals(blockId);
    }

    public static boolean isThermostat(String blockId) {
        return id(THERMOSTAT).equals(blockId);
    }

    public static boolean isIsolationMount(String blockId) {
        return id(ISOLATION).equals(blockId);
    }

    public static boolean isVibrationSource(String blockId) {
        return VIBRATION_SOURCES.containsKey(blockId);
    }

    public static int vibrationAmplitude(String blockId) {
        return VIBRATION_SOURCES.getOrDefault(blockId, 0);
    }

    private static String id(String path) {
        return SsiteAddon.MOD_ID + ":" + path;
    }

    private static BehaviorMachine controller() {
        return BehaviorMachine.builder(MachineCasing.builder(CONTROLLER, "StainlessSteel")
                        .machineType(CONTROLLER).tier(3).build())
                // The controller does not gate room service on power; the vent and thermostat do.
                .energy(EnergyInput.builder(EnergyType.EU, 1).anyPacket().capacity(100_000L).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .progress(80, 40)
                .clientState(CleanroomControllerCore.K_STATUS, CleanroomControllerCore.K_VALID,
                        CleanroomControllerCore.K_REASON, CleanroomControllerCore.K_REVISION,
                        CleanroomControllerCore.K_CLEAN, CleanroomControllerCore.K_TEMPERATURE,
                        CleanroomControllerCore.K_VIBRATION, CleanroomControllerCore.K_VOLUME,
                        CleanroomControllerCore.K_VENTS, CleanroomControllerCore.K_LEAKS,
                        CleanroomControllerCore.K_FILTER)
                .io(MachineIo.builder().build())
                .build();
    }

    private static BehaviorMachine vent() {
        return BehaviorMachine.builder(MachineCasing.builder(VENT, "StainlessSteel")
                        .machineType(VENT).tier(2).build())
                .energy(EnergyInput.builder(EnergyType.EU, (int) CleanroomVentCore.EU_PER_TICK)
                        .anyPacket().capacity(200_000L).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .progress(80, 40)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .clientState(CleanroomVentCore.K_STATUS, CleanroomVentCore.K_RUNNING,
                        CleanroomVentCore.K_WEAR, CleanroomVentCore.K_REMAINING)
                .io(MachineIo.builder().itemInputs(MachineFace.FRONT).build())
                .build();
    }

    private static BehaviorMachine thermostat() {
        return BehaviorMachine.builder(MachineCasing.builder(THERMOSTAT, "StainlessSteel")
                        .machineType(THERMOSTAT).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.EU, (int) CleanroomThermostatCore.EU_PER_TICK)
                        .anyPacket().capacity(200_000L).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .progress(80, 40)
                .fluid(8000, 152, 8, BehaviorMachine.SlotRole.INPUT)
                .clientState(CleanroomThermostatCore.K_STATUS,
                        CleanroomThermostatCore.K_RUNNING, CleanroomThermostatCore.K_COOLANT)
                .io(MachineIo.builder().fluidInputs(MachineFace.FRONT).build())
                .build();
    }
}
