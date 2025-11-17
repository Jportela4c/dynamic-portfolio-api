package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.ProductResponse;
import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.service.ProductService;
import com.portfolio.api.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Produtos Recomendados", description = "Endpoints para consulta de produtos recomendados por perfil de risco")
public class ProductRecommendationController {

    private final ProductService productService;
    private final TelemetryService telemetryService;

    public ProductRecommendationController(ProductService productService,
                                           TelemetryService telemetryService) {
        this.productService = productService;
        this.telemetryService = telemetryService;
    }

    @Operation(
        summary = "Consultar produtos recomendados",
        description = "Retorna produtos de investimento recomendados para um perfil de risco específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Perfil de risco inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/produtos-recomendados/{perfil}")
    public ResponseEntity<List<ProductResponse>> getRecommendedProducts(
        @Parameter(description = "Perfil de risco do cliente", example = "Moderado", required = true)
        @PathVariable PerfilRisco perfil) {
        long startTime = System.currentTimeMillis();

        try {
            List<Product> products = productService.getRecommendedProducts(perfil.name());

            List<ProductResponse> response = products.stream()
                    .map(p -> ProductResponse.builder()
                            .id(p.getId())
                            .nome(p.getNome())
                            .tipo(p.getTipo())
                            .rentabilidade(p.getRentabilidade())
                            .risco(p.getRisco())
                            .build())
                    .collect(Collectors.toList());

            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("produtos-recomendados", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("produtos-recomendados", responseTime, false, 500);
            throw e;
        }
    }
}
