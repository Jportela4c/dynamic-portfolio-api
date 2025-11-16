package com.portfolio.api.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {

    @NotNull(message = "Client ID is required")
    @Positive(message = "Client ID must be positive")
    private Long clienteId;

    @NotNull(message = "Value is required")
    @Positive(message = "Value must be positive")
    private BigDecimal valor;

    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    private Integer prazoMeses;

    @NotBlank(message = "Product type is required")
    private String tipoProduto;
}
