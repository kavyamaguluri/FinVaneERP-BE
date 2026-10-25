package com.finvane.model.financialmodel;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialModelDTO {

    private boolean hasData;
    private int numYears;
    private SectorBenchmarkDTO sector;
    private List<FinancialYearDTO> historicalFYs;
    private List<FinancialYearDTO> projectedFYs;
    private BigDecimal growthRateUsedPct;
    private boolean usedSectorGrowth;
    private SegmentsDTO segments;
    private UnitEconomicsDTO unitEconomics;
    private BenchmarkComparisonDTO benchmarkComparison;
    private AssumptionsDTO assumptions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RangeVal {
        private Double low;
        private Double mid;
        private Double high;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorBenchmarkDTO {
        private String key;
        private String label;
        private RangeVal grossMargin;
        private RangeVal ebitdaMargin;
        private RangeVal netMargin;
        private Double revenueGrowth;
        private Integer dso;
        private Integer dpo;
        private Integer dio;
        private Double assetTurnover;
        private String unitEconomicsNote;
        private String unitEconomicsBasis;
        private RangeVal evEbitdaMultiple;
        private RangeVal peRatio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinancialYearDTO {
        private int startYear;
        private String label;
        private String from;
        private String to;
        private BigDecimal revenue;
        private BigDecimal cogs;
        private BigDecimal grossProfit;
        private BigDecimal indirectIncomeTotal;
        private BigDecimal indirectExpenseTotal;
        private BigDecimal depreciation;
        private BigDecimal ebitda;
        private BigDecimal ebit;
        private BigDecimal tax;
        private BigDecimal netProfit;
        private BigDecimal totalAssets;
        private BigDecimal equity;
        private BigDecimal debt;
        private BigDecimal currentAssets;
        private BigDecimal currentLiabilities;
        private int salesInvoiceCount;
        private BigDecimal purchaseTotal;
        private boolean isProjected;
        private FinancialRatiosDTO ratios;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinancialRatiosDTO {
        private Double grossMarginPct;
        private Double ebitdaMarginPct;
        private Double netMarginPct;
        private Double currentRatio;
        private Double quickRatio;
        private Double debtEquity;
        private Double roe;
        private Double roce;
        private Double assetTurnover;
        private Double dso;
        private Double dpo;
        private Double dio;
        private Double c2c;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SegmentsDTO {
        private List<CategorySegmentDTO> byCategory;
        private List<LocationSegmentDTO> byLocation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySegmentDTO {
        private String segment;
        private BigDecimal revenue;
        private Long qty;
        private Double sharePct;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationSegmentDTO {
        private String segment;
        private BigDecimal revenue;
        private Long invoiceCount;
        private Double sharePct;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitEconomicsDTO {
        private BigDecimal averageOrderValue;
        private BigDecimal revenuePerCustomer;
        private Integer distinctCustomers;
        private Integer salesInvoiceCount;
        private Long unitsSold;
        private BigDecimal costPerUnit;
        private BigDecimal revenuePerUnit;
        private BigDecimal grossMarginPerUnit;
        private Long activeHeadcount;
        private BigDecimal revenuePerEmployee;
        private BigDecimal payrollCostForPeriod;
        private Double payrollCostPctOfRevenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenchmarkComparisonDTO {
        private String sectorLabel;
        private BenchmarkFlagDTO grossMargin;
        private BenchmarkFlagDTO ebitdaMargin;
        private BenchmarkFlagDTO netMargin;
        private BenchmarkFlagDTO dso;
        private BenchmarkFlagDTO dio;
        private String unitEconomicsNote;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenchmarkFlagDTO {
        private Double value;
        private String status;
        private RangeVal band;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssumptionsDTO {
        private int assumedTaxRatePct;
        private boolean assumedTaxRateAppliesToProjectionOnly;
        private Double fallbackGrowthPct;
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValuationReportDTO {
        private boolean hasData;
        private DcfValuationDTO dcf;
        private ComparablesValuationDTO comparables;
        private BookValueDTO bookValue;
        private RangeValuationDTO range;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DcfValuationDTO {
        private Double wacc;
        private Double terminalGrowth;
        private List<DcfRowDTO> rows;
        private BigDecimal terminalValue;
        private BigDecimal pvTerminalValue;
        private BigDecimal sumPvFcf;
        private BigDecimal enterpriseValue;
        private BigDecimal netDebt;
        private BigDecimal equityValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DcfRowDTO {
        private String label;
        private BigDecimal fcf;
        private Double discountFactor;
        private BigDecimal pvFcf;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparablesValuationDTO {
        private String basisFY;
        private BigDecimal ebitda;
        private BigDecimal netProfit;
        private BigDecimal revenue;
        private RangeVal evEbitdaMultiple;
        private ValuationRangeDTO evEbitdaValuation;
        private ValuationRangeDTO equityFromEvEbitda;
        private RangeVal peRatio;
        private ValuationRangeDTO peValuation;
        private RangeVal revenueMultiple;
        private ValuationRangeDTO evRevenueValuation;
        private ValuationRangeDTO equityFromRevenue;
        private Double controlPremiumPct;
        private RangeVal transactionMultiple;
        private ValuationRangeDTO evTransactionValuation;
        private ValuationRangeDTO equityFromTransaction;
        private boolean usedSector;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValuationRangeDTO {
        private BigDecimal low;
        private BigDecimal mid;
        private BigDecimal high;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookValueDTO {
        private String basisFY;
        private BigDecimal equityValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RangeValuationDTO {
        private BigDecimal low;
        private BigDecimal mid;
        private BigDecimal high;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CmaReportDTO {
        private boolean hasData;
        private List<CmaOperatingRowDTO> operatingStatement;
        private List<CmaBalanceSheetRowDTO> balanceSheetAnalysis;
        private List<CmaRatioRowDTO> ratioAnalysis;
        private List<CmaMpbfRowDTO> mpbf;
        private String dscrCaveat;
        private String mpbfCaveat;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CmaOperatingRowDTO {
        private String label;
        private BigDecimal revenue;
        private BigDecimal totalExpenses;
        private BigDecimal ebitda;
        private BigDecimal depreciation;
        private BigDecimal pbt;
        private BigDecimal tax;
        private BigDecimal pat;
        private BigDecimal cashAccruals;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CmaBalanceSheetRowDTO {
        private String label;
        private BigDecimal currentAssets;
        private BigDecimal currentLiabilities;
        private BigDecimal netWorkingCapital;
        private BigDecimal totalDebt;
        private BigDecimal netWorth;
        private BigDecimal totalAssets;
        private Double debtEquity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CmaRatioRowDTO {
        private String label;
        private Double currentRatio;
        private Double debtEquity;
        private Double roce;
        private Double netMarginPct;
        private Double assetTurnover;
        private Double dscr;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CmaMpbfRowDTO {
        private String label;
        private BigDecimal projectedTurnover;
        private BigDecimal workingCapitalGap;
        private BigDecimal marginMinimum;
        private BigDecimal mpbf;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorOptionDTO {
        private String key;
        private String label;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CorporateFinanceDataDTO {
        private FinancialModelDTO model;
        private ValuationReportDTO valuation;
        private CmaReportDTO cma;
        private int years;
        private String sectorKey;
        private List<SectorOptionDTO> sectors;
    }
}
