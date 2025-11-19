package com.portfolio.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OFBInvestmentDto {
    private String investmentId;
    private String productType;
    private String productName;
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maturityDate;

    private BigDecimal profitability;
    private BigDecimal currentValue;
}
