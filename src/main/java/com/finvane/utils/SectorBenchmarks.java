package com.finvane.utils;

import com.finvane.model.financialmodel.FinancialModelDTO.*;

import java.util.*;

public final class SectorBenchmarks {

    private SectorBenchmarks() {}

    private static final Map<String, SectorBenchmarkDTO> SECTORS_MAP = new LinkedHashMap<>();

    static {
        addSector(SectorBenchmarkDTO.builder()
                .key("it_ites")
                .label("Information Technology / IT-Enabled Services")
                .grossMargin(RangeVal.builder().low(55.0).mid(65.0).high(75.0).build())
                .ebitdaMargin(RangeVal.builder().low(15.0).mid(22.0).high(30.0).build())
                .netMargin(RangeVal.builder().low(10.0).mid(15.0).high(20.0).build())
                .revenueGrowth(18.0)
                .dso(60).dpo(30).dio(5)
                .assetTurnover(1.8)
                .unitEconomicsNote("Revenue per billable employee and utilization rate are the primary economic drivers.")
                .unitEconomicsBasis("employee")
                .evEbitdaMultiple(RangeVal.builder().low(10.0).mid(13.0).high(16.0).build())
                .peRatio(RangeVal.builder().low(18.0).mid(22.0).high(28.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("saas")
                .label("SaaS / Software Products")
                .grossMargin(RangeVal.builder().low(70.0).mid(80.0).high(88.0).build())
                .ebitdaMargin(RangeVal.builder().low(5.0).mid(18.0).high(30.0).build())
                .netMargin(RangeVal.builder().low(0.0).mid(10.0).high(18.0).build())
                .revenueGrowth(35.0)
                .dso(45).dpo(30).dio(0)
                .assetTurnover(1.2)
                .unitEconomicsNote("LTV:CAC ratio and net revenue retention typically matter more than gross margin alone.")
                .unitEconomicsBasis("employee")
                .evEbitdaMultiple(RangeVal.builder().low(15.0).mid(20.0).high(25.0).build())
                .peRatio(RangeVal.builder().low(25.0).mid(30.0).high(40.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("ecommerce")
                .label("E-commerce & Online Retail")
                .grossMargin(RangeVal.builder().low(20.0).mid(32.0).high(45.0).build())
                .ebitdaMargin(RangeVal.builder().low(-5.0).mid(5.0).high(12.0).build())
                .netMargin(RangeVal.builder().low(-8.0).mid(2.0).high(8.0).build())
                .revenueGrowth(28.0)
                .dso(5).dpo(45).dio(35)
                .assetTurnover(2.5)
                .unitEconomicsNote("Contribution margin per order after logistics/returns is the key unit economic lens.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(8.0).mid(11.0).high(15.0).build())
                .peRatio(RangeVal.builder().low(20.0).mid(25.0).high(35.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("fmcg")
                .label("FMCG & Consumer Goods")
                .grossMargin(RangeVal.builder().low(35.0).mid(42.0).high(50.0).build())
                .ebitdaMargin(RangeVal.builder().low(10.0).mid(15.0).high(20.0).build())
                .netMargin(RangeVal.builder().low(6.0).mid(9.0).high(13.0).build())
                .revenueGrowth(14.0)
                .dso(30).dpo(50).dio(45)
                .assetTurnover(2.2)
                .unitEconomicsNote("Trade distributor margin vs retail pricing power dictates working-capital and margin health.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(14.0).mid(18.0).high(24.0).build())
                .peRatio(RangeVal.builder().low(22.0).mid(28.0).high(35.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("manufacturing")
                .label("Manufacturing (General / Engineering)")
                .grossMargin(RangeVal.builder().low(22.0).mid(30.0).high(38.0).build())
                .ebitdaMargin(RangeVal.builder().low(8.0).mid(13.0).high(18.0).build())
                .netMargin(RangeVal.builder().low(3.0).mid(7.0).high(11.0).build())
                .revenueGrowth(12.0)
                .dso(75).dpo(60).dio(60)
                .assetTurnover(1.4)
                .unitEconomicsNote("Plant capacity utilization and raw material pass-through timing are the dominant levers.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(7.0).mid(9.0).high(12.0).build())
                .peRatio(RangeVal.builder().low(12.0).mid(16.0).high(22.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("pharma")
                .label("Pharmaceuticals & Healthcare Products")
                .grossMargin(RangeVal.builder().low(45.0).mid(55.0).high(65.0).build())
                .ebitdaMargin(RangeVal.builder().low(16.0).mid(22.0).high(28.0).build())
                .netMargin(RangeVal.builder().low(10.0).mid(14.0).high(18.0).build())
                .revenueGrowth(15.0)
                .dso(70).dpo(60).dio(75)
                .assetTurnover(1.1)
                .unitEconomicsNote("Batch-level manufacturing yield and regulatory compliance spend shape the gross spread.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(12.0).mid(16.0).high(20.0).build())
                .peRatio(RangeVal.builder().low(18.0).mid(24.0).high(30.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("healthcare_services")
                .label("Healthcare Services (Clinics / Diagnostics)")
                .grossMargin(RangeVal.builder().low(50.0).mid(60.0).high(70.0).build())
                .ebitdaMargin(RangeVal.builder().low(14.0).mid(20.0).high(26.0).build())
                .netMargin(RangeVal.builder().low(8.0).mid(12.0).high(16.0).build())
                .revenueGrowth(16.0)
                .dso(30).dpo(45).dio(15)
                .assetTurnover(1.3)
                .unitEconomicsNote("Revenue per bed/center and doctor cost share of revenue are standard diagnostic KPIs.")
                .unitEconomicsBasis("employee")
                .evEbitdaMultiple(RangeVal.builder().low(11.0).mid(15.0).high(19.0).build())
                .peRatio(RangeVal.builder().low(18.0).mid(22.0).high(28.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("retail_trading")
                .label("Retail & Trading (Brick-and-Mortar)")
                .grossMargin(RangeVal.builder().low(18.0).mid(26.0).high(34.0).build())
                .ebitdaMargin(RangeVal.builder().low(4.0).mid(8.0).high(12.0).build())
                .netMargin(RangeVal.builder().low(1.5).mid(4.0).high(7.0).build())
                .revenueGrowth(11.0)
                .dso(10).dpo(45).dio(50)
                .assetTurnover(3.0)
                .unitEconomicsNote("Sales per square foot and inventory turns govern profitability in physical retail.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(6.0).mid(8.5).high(12.0).build())
                .peRatio(RangeVal.builder().low(12.0).mid(16.0).high(22.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("textiles_apparel")
                .label("Textiles & Apparel")
                .grossMargin(RangeVal.builder().low(25.0).mid(35.0).high(45.0).build())
                .ebitdaMargin(RangeVal.builder().low(8.0).mid(12.0).high(16.0).build())
                .netMargin(RangeVal.builder().low(2.5).mid(5.5).high(9.0).build())
                .revenueGrowth(10.0)
                .dso(65).dpo(55).dio(70)
                .assetTurnover(1.5)
                .unitEconomicsNote("Order-to-delivery lead time and fabric wastage % drive operating margin swings.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(6.0).mid(8.0).high(11.0).build())
                .peRatio(RangeVal.builder().low(10.0).mid(14.0).high(18.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("auto_components")
                .label("Auto & Auto Components")
                .grossMargin(RangeVal.builder().low(20.0).mid(28.0).high(35.0).build())
                .ebitdaMargin(RangeVal.builder().low(9.0).mid(13.0).high(17.0).build())
                .netMargin(RangeVal.builder().low(3.5).mid(6.5).high(10.0).build())
                .revenueGrowth(13.0)
                .dso(65).dpo(60).dio(40)
                .assetTurnover(1.7)
                .unitEconomicsNote("OEM customer concentration and tooling amortization dominate unit cost economics.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(7.0).mid(9.5).high(13.0).build())
                .peRatio(RangeVal.builder().low(12.0).mid(17.0).high(24.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("logistics")
                .label("Logistics & Supply Chain")
                .grossMargin(RangeVal.builder().low(12.0).mid(18.0).high(25.0).build())
                .ebitdaMargin(RangeVal.builder().low(5.0).mid(9.0).high(13.0).build())
                .netMargin(RangeVal.builder().low(1.5).mid(4.0).high(7.0).build())
                .revenueGrowth(15.0)
                .dso(55).dpo(40).dio(5)
                .assetTurnover(2.0)
                .unitEconomicsNote("Fleet utilization, fuel cost indexation, and deadhead miles determine trip margins.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(6.5).mid(9.0).high(12.5).build())
                .peRatio(RangeVal.builder().low(12.0).mid(16.0).high(22.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("real_estate_construction")
                .label("Real Estate & Construction")
                .grossMargin(RangeVal.builder().low(20.0).mid(28.0).high(38.0).build())
                .ebitdaMargin(RangeVal.builder().low(10.0).mid(16.0).high(22.0).build())
                .netMargin(RangeVal.builder().low(4.0).mid(9.0).high(14.0).build())
                .revenueGrowth(12.0)
                .dso(90).dpo(75).dio(120)
                .assetTurnover(0.7)
                .unitEconomicsNote("Project completion velocity and cost-to-complete variance dictate ROCE.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(6.0).mid(8.0).high(11.0).build())
                .peRatio(RangeVal.builder().low(9.0).mid(13.0).high(18.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("hospitality_food")
                .label("Hospitality & Food Services")
                .grossMargin(RangeVal.builder().low(55.0).mid(64.0).high(72.0).build())
                .ebitdaMargin(RangeVal.builder().low(10.0).mid(16.0).high(22.0).build())
                .netMargin(RangeVal.builder().low(3.0).mid(8.0).high(13.0).build())
                .revenueGrowth(16.0)
                .dso(10).dpo(35).dio(12)
                .assetTurnover(1.6)
                .unitEconomicsNote("RevPAR / table turns and prime cost (food + labor) are the operational compass.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(8.0).mid(11.0).high(15.0).build())
                .peRatio(RangeVal.builder().low(15.0).mid(20.0).high(28.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("financial_services")
                .label("Financial Services / NBFC")
                .grossMargin(RangeVal.builder().low(60.0).mid(72.0).high(82.0).build())
                .ebitdaMargin(RangeVal.builder().low(25.0).mid(35.0).high(45.0).build())
                .netMargin(RangeVal.builder().low(12.0).mid(20.0).high(28.0).build())
                .revenueGrowth(18.0)
                .dso(45).dpo(25).dio(0)
                .assetTurnover(0.4)
                .unitEconomicsNote("Net interest margin (NIM) and credit-cost provisions outweigh operating overhead.")
                .unitEconomicsBasis("employee")
                .evEbitdaMultiple(RangeVal.builder().low(8.0).mid(12.0).high(16.0).build())
                .peRatio(RangeVal.builder().low(12.0).mid(16.0).high(22.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("edtech")
                .label("EdTech / Education Services")
                .grossMargin(RangeVal.builder().low(50.0).mid(65.0).high(78.0).build())
                .ebitdaMargin(RangeVal.builder().low(0.0).mid(14.0).high(25.0).build())
                .netMargin(RangeVal.builder().low(-5.0).mid(8.0).high(18.0).build())
                .revenueGrowth(24.0)
                .dso(20).dpo(30).dio(0)
                .assetTurnover(1.5)
                .unitEconomicsNote("Student acquisition cost vs cohort renewal rate is the primary venture health metric.")
                .unitEconomicsBasis("employee")
                .evEbitdaMultiple(RangeVal.builder().low(10.0).mid(14.0).high(20.0).build())
                .peRatio(RangeVal.builder().low(18.0).mid(24.0).high(35.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("agri_processing")
                .label("Agriculture & Agri-Processing")
                .grossMargin(RangeVal.builder().low(15.0).mid(22.0).high(30.0).build())
                .ebitdaMargin(RangeVal.builder().low(5.0).mid(8.5).high(13.0).build())
                .netMargin(RangeVal.builder().low(1.5).mid(4.0).high(7.5).build())
                .revenueGrowth(10.0)
                .dso(45).dpo(40).dio(50)
                .assetTurnover(2.0)
                .unitEconomicsNote("Crop seasonality, cold-chain storage loss %, and commodity procurement timing govern margins.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(5.5).mid(7.5).high(10.5).build())
                .peRatio(RangeVal.builder().low(10.0).mid(13.0).high(18.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("chemicals_industrial")
                .label("Chemicals & Industrial Products")
                .grossMargin(RangeVal.builder().low(28.0).mid(36.0).high(45.0).build())
                .ebitdaMargin(RangeVal.builder().low(12.0).mid(17.0).high(23.0).build())
                .netMargin(RangeVal.builder().low(6.0).mid(10.0).high(14.0).build())
                .revenueGrowth(13.0)
                .dso(60).dpo(55).dio(45)
                .assetTurnover(1.3)
                .unitEconomicsNote("Specialty vs commodity mix and environmental/effluent compliance costs shape return profiles.")
                .unitEconomicsBasis("unit")
                .evEbitdaMultiple(RangeVal.builder().low(8.0).mid(11.0).high(15.0).build())
                .peRatio(RangeVal.builder().low(14.0).mid(18.0).high(24.0).build())
                .build());

        addSector(SectorBenchmarkDTO.builder()
                .key("media_entertainment")
                .label("Media & Entertainment")
                .grossMargin(RangeVal.builder().low(35.0).mid(48.0).high(60.0).build())
                .ebitdaMargin(RangeVal.builder().low(8.0).mid(15.0).high(22.0).build())
                .netMargin(RangeVal.builder().low(2.0).mid(8.0).high(14.0).build())
                .revenueGrowth(14.0)
                .dso(75).dpo(45).dio(5)
                .assetTurnover(1.4)
                .unitEconomicsNote("Content IP ownership vs agency fee margins determines whether enterprise value compounds.")
                .unitEconomicsBasis("employee")
                .evEbitdaMultiple(RangeVal.builder().low(7.0).mid(10.0).high(14.0).build())
                .peRatio(RangeVal.builder().low(12.0).mid(17.0).high(24.0).build())
                .build());
    }

    private static void addSector(SectorBenchmarkDTO s) {
        SECTORS_MAP.put(s.getKey(), s);
    }

    public static List<SectorOptionDTO> listSectors() {
        List<SectorOptionDTO> list = new ArrayList<>();
        for (SectorBenchmarkDTO s : SECTORS_MAP.values()) {
            list.add(new SectorOptionDTO(s.getKey(), s.getLabel()));
        }
        return list;
    }

    public static SectorBenchmarkDTO findSector(String key) {
        if (key == null || key.isBlank()) return null;
        return SECTORS_MAP.get(key);
    }
}
