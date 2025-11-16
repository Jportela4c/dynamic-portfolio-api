package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.ProductResponse;
import com.portfolio.api.model.entity.Product;
import com.portfolio.api.service.ProductService;
import com.portfolio.api.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductRecommendationController {

    private final ProductService productService;
    private final TelemetryService telemetryService;

    public ProductRecommendationController(ProductService productService,
                                           TelemetryService telemetryService) {
        this.productService = productService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/produtos-recomendados/{perfil}")
    public ResponseEntity<List<ProductResponse>> getRecommendedProducts(@PathVariable String perfil) {
        long startTime = System.currentTimeMillis();

        try {
            List<Product> products = productService.getRecommendedProducts(perfil);

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
