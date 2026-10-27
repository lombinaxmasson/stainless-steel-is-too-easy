package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;

import java.util.List;

/** M6 rare-earth materials. Cards 2 and 3 append their own entries here. */
public final class RareEarthMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("Xenotime")
                    .displayName("Xenotime").color(0xC8C878).chemicalFormula("YPO4")
                    .meltingPointKelvin(700).boilingPointKelvin(1900).density(4.50F)
                    .dust().oreByproducts("RareEarth", "Phosphorus").build(),
            MaterialDeclaration.builder("BastnasiteCalcine")
                    .displayName("Bastnasite Calcine").color(0xC87840).chemicalFormula("CeO2·RE2O3")
                    .meltingPointKelvin(1200).boilingPointKelvin(3200).density(5.20F).dust().build(),
            MaterialDeclaration.builder("MixedRareEarthSolution")
                    .displayName("Mixed Rare Earth Solution").color(0xA8B878)
                    .chemicalFormula("RE2(SO4)3·RECl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(380).density(1.35F).liquid().build(),
            MaterialDeclaration.builder("LanthanumOxide")
                    .displayName("Lanthanum Oxide").color(0xF0F0E0).chemicalFormula("La2O3")
                    .meltingPointKelvin(2588).boilingPointKelvin(4200).density(6.51F).dust().build(),
            MaterialDeclaration.builder("CeriumOxide")
                    .displayName("Cerium Dioxide").color(0xFFF4C8).chemicalFormula("CeO2")
                    .meltingPointKelvin(2873).boilingPointKelvin(3800).density(7.22F).dust().build(),
            MaterialDeclaration.builder("PraseodymiumOxide")
                    .displayName("Praseodymium Oxide").color(0x98C060).chemicalFormula("Pr2O3")
                    .meltingPointKelvin(2473).boilingPointKelvin(3900).density(6.77F).dust().build(),
            MaterialDeclaration.builder("NeodymiumOxide")
                    .displayName("Neodymium Oxide").color(0xC8B8E8).chemicalFormula("Nd2O3")
                    .meltingPointKelvin(2503).boilingPointKelvin(3900).density(7.24F).dust().build(),
            MaterialDeclaration.builder("SamariumOxide")
                    .displayName("Samarium Oxide").color(0xE8D8A0).chemicalFormula("Sm2O3")
                    .meltingPointKelvin(2603).boilingPointKelvin(4100).density(7.74F).dust().build(),
            MaterialDeclaration.builder("EuropiumOxide")
                    .displayName("Europium Oxide").color(0xF0C8D8).chemicalFormula("Eu2O3")
                    .meltingPointKelvin(2623).boilingPointKelvin(3900).density(7.42F).dust().build(),
            MaterialDeclaration.builder("GadoliniumOxide")
                    .displayName("Gadolinium Oxide").color(0xE0E0E8).chemicalFormula("Gd2O3")
                    .meltingPointKelvin(2693).boilingPointKelvin(4200).density(7.41F).dust().build(),
            MaterialDeclaration.builder("TerbiumOxide")
                    .displayName("Terbium Oxide").color(0xD8C8A0).chemicalFormula("Tb2O3")
                    .meltingPointKelvin(2673).boilingPointKelvin(3900).density(8.27F).dust().build(),
            MaterialDeclaration.builder("DysprosiumOxide")
                    .displayName("Dysprosium Oxide").color(0xC8D0D8).chemicalFormula("Dy2O3")
                    .meltingPointKelvin(2673).boilingPointKelvin(3900).density(7.81F).dust().build(),
            MaterialDeclaration.builder("YttriumOxide")
                    .displayName("Yttrium Oxide").color(0xE8E8E0).chemicalFormula("Y2O3")
                    .meltingPointKelvin(2693).boilingPointKelvin(4300).density(5.01F).dust().build(),
            MaterialDeclaration.builder("LutetiumOxide")
                    .displayName("Lutetium Oxide").color(0xD0D8E8).chemicalFormula("Lu2O3")
                    .meltingPointKelvin(2763).boilingPointKelvin(4200).density(9.42F).dust().build(),
            MaterialDeclaration.builder("HeavyRareEarthResidue")
                    .displayName("Heavy Rare Earth Residue").color(0xB8B8A0)
                    .chemicalFormula("(Ho,Er,Tm,Yb)2O3")
                    .meltingPointKelvin(2650).boilingPointKelvin(3900).density(8.60F).dust().build(),
            MaterialDeclaration.builder("LightMiddleP204Extract")
                    .displayName("Light-Middle P204 Extract").color(0xC8B070)
                    .chemicalFormula("(RE)·P204")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(0.98F).liquid().build(),
            MaterialDeclaration.builder("MiddleRareEarthP204Extract")
                    .displayName("Middle P204 Extract").color(0xC0A868)
                    .chemicalFormula("(Sm-Gd)·P204")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(0.97F).liquid().build(),
            MaterialDeclaration.builder("LightRareEarthChloride")
                    .displayName("Light Rare Earth Chloride").color(0xD8E8B8)
                    .chemicalFormula("LaCl3·CeCl3·PrCl3·NdCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.30F).liquid().build(),
            MaterialDeclaration.builder("MiddleRareEarthChloride")
                    .displayName("Middle Rare Earth Chloride").color(0xD0E0B0)
                    .chemicalFormula("SmCl3·EuCl3·GdCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.30F).liquid().build(),
            MaterialDeclaration.builder("HeavyRareEarthChloride")
                    .displayName("Heavy Rare Earth Chloride").color(0xC8D8A8)
                    .chemicalFormula("TbCl3·DyCl3·YCl3·LuCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.32F).liquid().build(),
            MaterialDeclaration.builder("LightRareEarthRaffinate")
                    .displayName("Cerium-Free Light Rare Earth Liquor").color(0xD8E8C0)
                    .chemicalFormula("LaCl3·PrCl3·NdCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.28F).liquid().build(),
            MaterialDeclaration.builder("PrNdRaffinate")
                    .displayName("Pr-Nd Chloride Liquor").color(0xC8D8B0)
                    .chemicalFormula("PrCl3·NdCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.29F).liquid().build(),
            MaterialDeclaration.builder("PrP204Extract")
                    .displayName("Praseodymium P204 Extract").color(0xB8A060)
                    .chemicalFormula("Pr·P204")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(0.96F).liquid().build(),
            MaterialDeclaration.builder("SmGdChloride")
                    .displayName("Sm-Gd Chloride Liquor").color(0xD0D8A8)
                    .chemicalFormula("SmCl3·GdCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.30F).liquid().build(),
            MaterialDeclaration.builder("SmP204Extract")
                    .displayName("Samarium P204 Extract").color(0xB0A058)
                    .chemicalFormula("Sm·P204")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(0.96F).liquid().build(),
            MaterialDeclaration.builder("TbDyLuChloride")
                    .displayName("Tb-Dy-Lu Chloride Liquor").color(0xC0C8A0)
                    .chemicalFormula("TbCl3·DyCl3·LuCl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.31F).liquid().build(),
            MaterialDeclaration.builder("TbDyP204Extract")
                    .displayName("Tb-Dy P204 Extract").color(0xA89858)
                    .chemicalFormula("(Tb,Dy)·P204")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(0.97F).liquid().build(),
            MaterialDeclaration.builder("LanthanumFluoride")
                    .displayName("Lanthanum Fluoride").color(0xE8E8D8).chemicalFormula("LaF3")
                    .meltingPointKelvin(1766).boilingPointKelvin(2600).density(5.94F).dust().build(),
            MaterialDeclaration.builder("CeriumFluoride")
                    .displayName("Cerium Fluoride").color(0xF0F0D0).chemicalFormula("CeF3")
                    .meltingPointKelvin(1703).boilingPointKelvin(2500).density(6.16F).dust().build(),
            MaterialDeclaration.builder("PraseodymiumFluoride")
                    .displayName("Praseodymium Fluoride").color(0x98B860).chemicalFormula("PrF3")
                    .meltingPointKelvin(1663).boilingPointKelvin(2500).density(6.27F).dust().build(),
            MaterialDeclaration.builder("NeodymiumFluoride")
                    .displayName("Neodymium Fluoride").color(0xC8B8E0).chemicalFormula("NdF3")
                    .meltingPointKelvin(1653).boilingPointKelvin(2500).density(6.51F).dust().build(),
            MaterialDeclaration.builder("SamariumFluoride")
                    .displayName("Samarium Fluoride").color(0xE0D0A0).chemicalFormula("SmF3")
                    .meltingPointKelvin(1573).boilingPointKelvin(2400).density(6.93F).dust().build(),
            MaterialDeclaration.builder("EuropiumFluoride")
                    .displayName("Europium Fluoride").color(0xE8C0D0).chemicalFormula("EuF3")
                    .meltingPointKelvin(1543).boilingPointKelvin(2400).density(6.50F).dust().build(),
            MaterialDeclaration.builder("GadoliniumFluoride")
                    .displayName("Gadolinium Fluoride").color(0xD8D8E0).chemicalFormula("GdF3")
                    .meltingPointKelvin(1503).boilingPointKelvin(2400).density(7.05F).dust().build(),
            MaterialDeclaration.builder("TerbiumFluoride")
                    .displayName("Terbium Fluoride").color(0xD0C098).chemicalFormula("TbF3")
                    .meltingPointKelvin(1443).boilingPointKelvin(2400).density(7.30F).dust().build(),
            MaterialDeclaration.builder("DysprosiumFluoride")
                    .displayName("Dysprosium Fluoride").color(0xC0C8D0).chemicalFormula("DyF3")
                    .meltingPointKelvin(1423).boilingPointKelvin(2400).density(7.46F).dust().build(),
            MaterialDeclaration.builder("YttriumFluoride")
                    .displayName("Yttrium Fluoride").color(0xE0E0D8).chemicalFormula("YF3")
                    .meltingPointKelvin(1663).boilingPointKelvin(2500).density(4.01F).dust().build(),
            MaterialDeclaration.builder("LutetiumFluoride")
                    .displayName("Lutetium Fluoride").color(0xC8D0E0).chemicalFormula("LuF3")
                    .meltingPointKelvin(1453).boilingPointKelvin(2400).density(8.33F).dust().build(),
            MaterialDeclaration.builder("NeodymiumIronBoron")
                    .displayName("Neodymium-Iron-Boron").color(0x808890).chemicalFormula("Nd2Fe14B")
                    .meltingPointKelvin(1600).boilingPointKelvin(3000).density(7.50F).metal().build(),
            MaterialDeclaration.builder("SamariumCobalt")
                    .displayName("Samarium-Cobalt").color(0x9098A0).chemicalFormula("SmCo5")
                    .meltingPointKelvin(1600).boilingPointKelvin(3000).density(8.30F).metal().build(),
            MaterialDeclaration.builder("YttriumAluminiumGarnet")
                    .displayName("Yttrium Aluminium Garnet (YAG)").color(0xF0F0E0)
                    .chemicalFormula("Y3Al5O12")
                    .meltingPointKelvin(2200).boilingPointKelvin(3400).density(4.55F).dust().build()
    );

    private RareEarthMaterials() {}
}
