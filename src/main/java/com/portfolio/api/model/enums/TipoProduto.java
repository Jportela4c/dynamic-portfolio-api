package com.portfolio.api.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Investment product types aligned with Open Finance Brasil (OFB) official taxonomy.
 *
 * Maps to multiple OFB API specifications:
 * - Bank Fixed Incomes: CDB, RDB, LCI, LCA (EnumInvestmentType)
 * - Credit Fixed Incomes: CRI, CRA (EnumInvestmentType)
 * - Funds: RENDA_FIXA, ACOES, MULTIMERCADO, CAMBIAL (anbimaCategory)
 * - Treasury Titles: TESOURO_SELIC, TESOURO_PREFIXADO, TESOURO_IPCA (derived from productName)
 * - Variable Incomes: VARIABLE_INCOME (stocks, ETFs - identified by ticker)
 */
@Schema(description = "Tipo de produto financeiro (OFB taxonomy)")
public enum TipoProduto {

    // Bank Fixed Incomes - OFB EnumInvestmentType
    @Schema(description = "Certificado de Depósito Bancário")
    @JsonProperty("CDB")
    CDB,

    @Schema(description = "Recibo de Depósito Bancário")
    @JsonProperty("RDB")
    RDB,

    @Schema(description = "Letra de Crédito Imobiliário")
    @JsonProperty("LCI")
    LCI,

    @Schema(description = "Letra de Crédito do Agronegócio")
    @JsonProperty("LCA")
    LCA,

    // Funds - OFB anbimaCategory
    @Schema(description = "Fundo de Renda Fixa")
    @JsonProperty("RENDA_FIXA")
    RENDA_FIXA,

    @Schema(description = "Fundo de Ações")
    @JsonProperty("ACOES")
    ACOES,

    @Schema(description = "Fundo Multimercado")
    @JsonProperty("MULTIMERCADO")
    MULTIMERCADO,

    @Schema(description = "Fundo Cambial")
    @JsonProperty("CAMBIAL")
    CAMBIAL,

    @Schema(description = "Fundo de Investimento Imobiliário")
    @JsonProperty("FII")
    FII,

    // Treasure Titles - Derived categories from OFB investmentName patterns
    @Schema(description = "Tesouro Selic (LFT)")
    @JsonProperty("TESOURO_SELIC")
    TESOURO_SELIC,

    @Schema(description = "Tesouro Prefixado (LTN/NTN-F)")
    @JsonProperty("TESOURO_PREFIXADO")
    TESOURO_PREFIXADO,

    @Schema(description = "Tesouro IPCA+ (NTN-B)")
    @JsonProperty("TESOURO_IPCA")
    TESOURO_IPCA,

    @Schema(description = "Tesouro RendA+ (NTN-B1)")
    @JsonProperty("TESOURO_RENDA_MAIS")
    TESOURO_RENDA_MAIS,

    @Schema(description = "Tesouro Educa+")
    @JsonProperty("TESOURO_EDUCA_MAIS")
    TESOURO_EDUCA_MAIS,

    // Credit Fixed Incomes - OFB Credit Fixed Incomes API
    @Schema(description = "Certificado de Recebíveis Imobiliários")
    @JsonProperty("CRI")
    CRI,

    @Schema(description = "Certificado de Recebíveis do Agronegócio")
    @JsonProperty("CRA")
    CRA,

    // Variable Incomes - Stocks, ETFs
    @Schema(description = "Renda Variável (Ações, ETFs)")
    @JsonProperty("VARIABLE_INCOME")
    VARIABLE_INCOME,

    // Additional types for compatibility
    @Schema(description = "Poupança")
    @JsonProperty("POUPANCA")
    POUPANCA,

    @Schema(description = "Tipo desconhecido ou indisponível")
    @JsonProperty("UNKNOWN")
    UNKNOWN;

    /**
     * Checks if this product type is a bank fixed income (CDB, RDB, LCI, LCA).
     */
    public boolean isBankFixedIncome() {
        return this == CDB || this == RDB || this == LCI || this == LCA;
    }

    /**
     * Checks if this product type is a fund.
     */
    public boolean isFund() {
        return this == RENDA_FIXA || this == ACOES || this == MULTIMERCADO || this == CAMBIAL || this == FII;
    }

    /**
     * Checks if this product type is a treasure title.
     */
    public boolean isTreasureTitle() {
        return this == TESOURO_SELIC || this == TESOURO_PREFIXADO || this == TESOURO_IPCA
                || this == TESOURO_RENDA_MAIS || this == TESOURO_EDUCA_MAIS;
    }

    /**
     * Checks if this product type is a credit fixed income (CRI, CRA).
     */
    public boolean isCreditFixedIncome() {
        return this == CRI || this == CRA;
    }

    /**
     * Checks if this product type is a variable income (stocks, ETFs).
     */
    public boolean isVariableIncome() {
        return this == VARIABLE_INCOME;
    }
}
