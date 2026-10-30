package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyOutput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import java.util.List;

/** Standalone HU transport blocks; their buffers never create fuel energy. */
public final class HeatTransportMachines {
    public static final String DUCT = HeatTransportCore.DUCT_TYPE, PLATE = HeatTransportCore.PLATE_TYPE;
    public static final long DUCT_RATE = HeatTransportCore.PIPE_THROUGHPUT, PLATE_RATE = HeatTransportCore.PLATE_OUTPUT;
    public static final long DUCT_CAPACITY = DUCT_RATE * 40, PLATE_CAPACITY = PLATE_RATE * 40;
    private HeatTransportMachines() {}

    public static boolean isMachine(String type) { return DUCT.equals(type) || PLATE.equals(type); }

    public static List<AddonMachine> machines() {
        return List.of(machine(DUCT, DUCT_RATE, DUCT_CAPACITY, MachineFace.values()),
                machine(PLATE, PLATE_RATE, PLATE_CAPACITY,
                        new MachineFace[]{MachineFace.FRONT, MachineFace.BACK, MachineFace.LEFT, MachineFace.RIGHT}));
    }

    private static BehaviorMachine machine(String id, long rate, long capacity, MachineFace[] inputs) {
        return BehaviorMachine.builder(MachineCasing.builder(id, "Steel").machineType(id).tier(3).build())
                // The host requires a GUI resource even though onUse displays the network status.
                .gui("ssite:textures/gui/aod_converter.png")
                .energy(EnergyInput.builder(EnergyType.HU, rate).anyPacket().capacity(capacity).faces(inputs).build())
                .energyOutput(EnergyOutput.builder(EnergyType.HU, 1, DUCT_RATE).remote().build())
                .clientState(HeatTransportCore.K_STATUS, HeatTransportCore.K_OUTPUT, HeatTransportCore.K_LOSS,
                        HeatTransportCore.K_STORED, HeatTransportCore.K_PLATES, HeatTransportCore.K_DUCTS)
                .build();
    }
}
