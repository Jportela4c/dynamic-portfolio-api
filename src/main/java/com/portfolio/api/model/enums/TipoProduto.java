package com.portfolio.api.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Investment product types aligned with Open Finance Brasil (OFB) official taxonomy.
 *
 * Maps to multiple OFB API specifications:
 * - Bank Fixed Incomes: CDB, RDB, LCI, LCA (EnumInvestmentType)
 * - Funds: RENDA_FIXA, ACOES, MULTIMERCADO, CAMBIAL (anbimaCategory)
 * - Treasure Titles: TESOURO_SELIC, TESOURO_PREFIXADO, TESOURO_IPCA (derived from investmentName)
 * - Variable Incomes: Identified by ticker, not included here
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
    @Schema(description = "Fundo de Renda Fixa (ANBIMA classification)")
    @JsonProperty("RENDA_FIXA")
    RENDA_FIXA,

    @Schema(description = "Fundo de Ações (ANBIMA classification)")
    @JsonProperty("ACOES")
    ACOES,

    @Schema(description = "Fundo Multimercado (ANBIMA classification)")
    @JsonProperty("MULTIMERCADO")
    MULTIMERCADO,

    @Schema(description = "Fundo Cambial (ANBIMA classification)")
    @JsonProperty("CAMBIAL")
    CAMBIAL,

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
     * Checks if this product type is a fund (RENDA_FIXA, ACOES, MULTIMERCADO, CAMBIAL).
     */
    public boolean isFund() {
        return this == RENDA_FIXA || this == ACOES || this == MULTIMERCADO || this == CAMBIAL;
    }

    /**
     * Checks if this product type is a treasure title.
     */
    public boolean isTreasureTitle() {
        return this == TESOURO_SELIC || this == TESOURO_PREFIXADO || this == TESOURO_IPCA
                || this == TESOURO_RENDA_MAIS || this == TESOURO_EDUCA_MAIS;
    }
}
