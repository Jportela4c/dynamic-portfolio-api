package com.portfolio.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unified DTO for all OFB investment types.
 * Uses @JsonIgnoreProperties to handle varying field structures across:
 * - Bank Fixed Incomes (CDB, LCI, LCA, RDB)
 * - Credit Fixed Incomes (Debêntures, CRI, CRA)
 * - Funds (Fundos de Investimento)
 * - Treasury Titles (Tesouro Direto)
 * - Variable Incomes (Ações, ETFs)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OFBInvestmentDto {
    // Common fields (all types)
    private String investmentId;

    @JsonProperty("brandName")
    private String brandName;

    @JsonProperty("companyCnpj")
    private String companyCnpj;

    @JsonProperty("isinCode")
    private String isinCode;

    // Bank/Credit Fixed Incomes
    @JsonProperty("investmentType")
    private String investmentType;

    @JsonProperty("issuerInstitutionCnpjNumber")
    private String issuerInstitutionCnpjNumber;

    @JsonProperty("issueUnitPrice")
    private IssueUnitPrice issueUnitPrice;

    @JsonProperty("remuneration")
    private Remuneration remuneration;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    // Credit Fixed Incomes specific
    @JsonProperty("debtorCnpjNumber")
    private String debtorCnpjNumber;

    @JsonProperty("debtorName")
    private String debtorName;

    @JsonProperty("taxExemptProduct")
    private String taxExemptProduct;

    @JsonProperty("voucherPaymentIndicator")
    private String voucherPaymentIndicator;

    @JsonProperty("voucherPaymentPeriodicity")
    private String voucherPaymentPeriodicity;

    // Funds specific
    @JsonProperty("name")
    private String fundName;

    @JsonProperty("cnpjNumber")
    private String fundCnpj;

    @JsonProperty("anbimaCategory")
    private String anbimaCategory;

    @JsonProperty("anbimaClass")
    private String anbimaClass;

    @JsonProperty("anbimaSubclass")
    private String anbimaSubclass;

    // Treasury Titles specific
    @JsonProperty("productName")
    private String productName;

    // Variable Incomes specific
    @JsonProperty("ticker")
    private String ticker;

    // Nested objects
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueUnitPrice {
        private BigDecimal amount;
        private String currency;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Remuneration {
        private String indexer;
        private String indexerAdditionalInfo;

        @JsonProperty("preFixedRate")
        private String preFixedRate;

        @JsonProperty("postFixedIndexerPercentage")
        private String postFixedIndexerPercentage;

        @JsonProperty("rateType")
        private String rateType;

        @JsonProperty("ratePeriodicity")
        private String ratePeriodicity;

        @JsonProperty("calculation")
        private String calculation;
    }

    // Convenience methods for mapper
    public BigDecimal getAmount() {
        return issueUnitPrice != null ? issueUnitPrice.getAmount() : null;
    }

    public LocalDate getMaturityDate() {
        return dueDate;
    }

    /**
     * Returns a human-readable product name based on investment type.
     * Priority: productName > fundName > brandName > investmentType
     */
    public String getProductNameOrDefault() {
        if (productName != null) return productName;
        if (fundName != null) return fundName;
        if (brandName != null) return brandName;
        return investmentType;
    }

    /**
     * Returns the type of investment for categorization.
     * Maps from different type fields depending on the investment category.
     */
    public String getType() {
        if (investmentType != null) return investmentType;
        if (anbimaCategory != null) return "FUND_" + anbimaCategory;
        if (ticker != null) return "VARIABLE_INCOME";
        if (productName != null && productName.contains("Tesouro")) return "TREASURY";
        return "UNKNOWN";
    }
}
