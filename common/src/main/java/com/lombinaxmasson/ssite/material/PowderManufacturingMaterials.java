package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * P-05 powder-bed forms on the single bootstrap alloy.
 *
 * <p>The first powder route is deliberately limited to {@code StainlessSteel316L}, which the AOD
 * already produces. Ordinary {@code dust} is only the feedstock for milling; it is not accepted by
 * the printer. Each stage is a separate addon prefix so the item id keeps the alloy identity and a
 * later stage cannot silently stand in for an earlier one: raw powder, classified powder, reclaim
 * powder, screen tailings, printed blank, desupported blank, cooling block, printed support and the
 * cooling assembly. The prefix textures reuse the existing addon item icons; no new art is added.</p>
 */
public final class PowderManufacturingMaterials {
    public static final String ALLOY = "StainlessSteel316L";

    public static final List<MaterialDeclaration> MATERIALS = List.of();

    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("rawPowder", "", " Raw Powder", GTValues.U,
                    "item/chip", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("classifiedPowder", "", " Classified Powder", GTValues.U,
                    "item/chip", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("reclaimPowder", "", " Reclaim Powder", GTValues.U,
                    "item/chip", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("screenTailings", "", " Screen Tailings", GTValues.U,
                    "item/chip", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("printedBlank", "", " Printed Blank", GTValues.U * 2,
                    "item/die", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("desupportedBlank", "", " Desupported Blank", GTValues.U * 2,
                    "item/die", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("coolingBlock", "", " Internal-Channel Cooling Block", GTValues.U,
                    "item/wafer", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("printedSupport", "", " Printed Support", GTValues.U / 2,
                    "item/hose", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("coolingAssembly", "", " Cooling Assembly", GTValues.U,
                    "item/hose", List.of(ALLOY), Set.of()));

    private PowderManufacturingMaterials() {}
}
