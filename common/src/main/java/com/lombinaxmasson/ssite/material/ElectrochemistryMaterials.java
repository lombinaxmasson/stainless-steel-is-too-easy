package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;

import java.util.List;

/**
 * M9-07 electrochemical materials and battery parts.
 *
 * <p>{@code CathodeMix} and {@code AnodeMix} are the blended active material, binder and conductive
 * carbon. The card's slurry and drying stages are folded into a dry blend: the host Laminator needs
 * two item inputs and has no fluid output, so a slurry fluid could not be coated on a foil there.</p>
 *
 * <p>Sheet and plate parts declare {@code polymer()} only to unlock the generated {@code foil} and
 * {@code plate} forms through {@link MaterialFormRequest}; the feature has no other effect.</p>
 */
public final class ElectrochemistryMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("LithiumCobaltOxide")
                    .displayName("Lithium Cobalt Oxide").color(0x35406E).chemicalFormula("LiCoO2")
                    .meltingPointKelvin(1400).boilingPointKelvin(2500).density(5.05F).dust().build(),
            MaterialDeclaration.builder("CathodeMix")
                    .displayName("Cathode Mix").color(0x3A3A48).chemicalFormula("LiCoO2·(C2H2F2)n·C")
                    .meltingPointKelvin(450).density(3.20F).dust().build(),
            MaterialDeclaration.builder("CathodeSheet")
                    .displayName("Cathode Sheet").color(0x9AA6B8).chemicalFormula("Al·LiCoO2·(C2H2F2)n·C")
                    .meltingPointKelvin(933).density(3.00F).polymer().build(),
            MaterialDeclaration.builder("AnodeMix")
                    .displayName("Anode Mix").color(0x1E1E22).chemicalFormula("C·(C2H2F2)n")
                    .meltingPointKelvin(450).density(1.90F).dust().build(),
            MaterialDeclaration.builder("AnodeSheet")
                    .displayName("Anode Sheet").color(0xB06A3A).chemicalFormula("Cu·C·(C2H2F2)n")
                    .meltingPointKelvin(1358).boilingPointKelvin(2800).density(4.50F).polymer().build(),
            MaterialDeclaration.builder("PolyolefinSeparator")
                    .displayName("Polyolefin Separator").color(0xF2F2F2).chemicalFormula("(C2H4)n")
                    .meltingPointKelvin(415).density(0.94F).polymer().build(),
            MaterialDeclaration.builder("LithiumHexafluorophosphate")
                    .displayName("Lithium Hexafluorophosphate").color(0xE8F0F8).chemicalFormula("LiPF6")
                    .meltingPointKelvin(473).density(1.50F).dust().build(),
            MaterialDeclaration.builder("EthyleneCarbonate")
                    .displayName("Ethylene Carbonate").color(0xF0F8F0).chemicalFormula("C3H4O3")
                    .meltingPointKelvin(310).boilingPointKelvin(521).density(1.32F).liquid().build(),
            MaterialDeclaration.builder("LithiumElectrolyte")
                    .displayName("Lithium Electrolyte").color(0xD8F0E8).chemicalFormula("LiPF6·C3H4O3")
                    .meltingPointKelvin(250).boilingPointKelvin(500).density(1.25F).liquid().build(),
            MaterialDeclaration.builder("CarbonPaper")
                    .displayName("Carbon Paper").color(0x22242A).chemicalFormula("C")
                    .meltingPointKelvin(3800).boilingPointKelvin(4300).density(1.60F).polymer().build(),
            MaterialDeclaration.builder("MembraneElectrodeAssembly")
                    .displayName("Membrane Electrode Assembly").color(0xC8C8B8)
                    .chemicalFormula("(C2F4)n(C7F13O5S)m·C·Pt")
                    .meltingPointKelvin(470).density(1.90F).polymer().build(),
            MaterialDeclaration.builder("BipolarPlate")
                    .displayName("Bipolar Plate").color(0x30302E).chemicalFormula("C·(C2H2F2)n")
                    .meltingPointKelvin(450).density(1.80F).polymer().build(),
            MaterialDeclaration.builder("VanadiumElectrolyte")
                    .displayName("Vanadium Electrolyte").color(0x3E5FA8).chemicalFormula("VOSO4·H2SO4")
                    .meltingPointKelvin(270).boilingPointKelvin(400).density(1.35F).liquid().build());

    public static final List<MaterialFormRequest> FORMS = List.of(
            new MaterialFormRequest("CathodeSheet", "foil"),
            new MaterialFormRequest("AnodeSheet", "foil"),
            new MaterialFormRequest("PolyolefinSeparator", "foil"),
            new MaterialFormRequest("CarbonPaper", "foil"),
            new MaterialFormRequest("MembraneElectrodeAssembly", "plate"),
            new MaterialFormRequest("BipolarPlate", "plate"));

    private ElectrochemistryMaterials() {}
}
