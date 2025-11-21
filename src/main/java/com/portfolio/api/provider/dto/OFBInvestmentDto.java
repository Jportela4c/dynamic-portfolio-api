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
        // Bank/Credit Fixed Incomes: Use issueUnitPrice.amount from identification endpoint
        if (issueUnitPrice != null && issueUnitPrice.getAmount() != null) {
            return issueUnitPrice.getAmount();
        }

        // Funds and Treasury: OFB spec requires separate balance endpoints
        // These investment types don't have amount in identification response
        // TODO: Implement balance endpoints for complete data
        return null;
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
        // Bank/Credit Fixed Incomes: CDB, LCI, LCA, RDB
        if (investmentType != null) return investmentType;

        // Funds: RENDA_FIXA, ACOES, MULTIMERCADO, CAMBIAL (direct from ANBIMA category)
        if (anbimaCategory != null) return anbimaCategory;

        // Treasury Titles: Parse from productName
        if (productName != null) {
            if (productName.contains("Selic")) return "TESOURO_SELIC";
            if (productName.contains("Prefixado")) return "TESOURO_PREFIXADO";
            if (productName.contains("IPCA")) return "TESOURO_IPCA";
            if (productName.contains("RendA+") || productName.contains("Renda+")) return "TESOURO_RENDA_MAIS";
            if (productName.contains("Educa+")) return "TESOURO_EDUCA_MAIS";
            if (productName.contains("Tesouro")) return "TESOURO_PREFIXADO"; // Default treasury type
        }

        // Variable Incomes: Stocks, ETFs (TODO: implement when available)
        if (ticker != null) return "VARIABLE_INCOME";

        return "UNKNOWN";
    }
}
