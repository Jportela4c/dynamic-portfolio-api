package com.portfolio.api.controller;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.service.ProductService;
import com.portfolio.api.service.TelemetryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductRecommendationController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.portfolio\\.api\\.security\\..*"
    ))
class ProductRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private TelemetryService telemetryService;

    @Test
    void shouldReturnRecommendedProductsForConservativeProfile() throws Exception {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setNome("CDB Caixa 2026");
        product1.setTipo(TipoProduto.CDB);
        product1.setRentabilidade(new BigDecimal("0.10"));
        product1.setRisco("Baixo");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setNome("LCI Itaú 2025");
        product2.setTipo(TipoProduto.LCI);
        product2.setRentabilidade(new BigDecimal("0.09"));
        product2.setRisco("Baixo");

        List<Product> products = Arrays.asList(product1, product2);

        when(productService.getRecommendedProducts("CONSERVADOR")).thenReturn(products);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/produtos-recomendados/Conservador")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nome").value("CDB Caixa 2026"))
            .andExpect(jsonPath("$[0].tipo").value("CDB"))
            .andExpect(jsonPath("$[0].rentabilidade").value(0.10))
            .andExpect(jsonPath("$[0].risco").value("Baixo"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].nome").value("LCI Itaú 2025"))
            .andExpect(jsonPath("$[1].tipo").value("LCI"))
            .andExpect(jsonPath("$[1].rentabilidade").value(0.09))
            .andExpect(jsonPath("$[1].risco").value("Baixo"));

        verify(productService).getRecommendedProducts("CONSERVADOR");
        verify(telemetryService).recordMetric(eq("produtos-recomendados"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnRecommendedProductsForModerateProfile() throws Exception {
        // Arrange
        Product product = new Product();
        product.setId(3L);
        product.setNome("Fundo Multimercado XP");
        product.setTipo(TipoProduto.MULTIMERCADO);
        product.setRentabilidade(new BigDecimal("0.14"));
        product.setRisco("Médio");

        List<Product> products = Arrays.asList(product);

        when(productService.getRecommendedProducts("MODERADO")).thenReturn(products);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/produtos-recomendados/Moderado")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].nome").value("Fundo Multimercado XP"))
            .andExpect(jsonPath("$[0].tipo").value("FundoMultimercado"))
            .andExpect(jsonPath("$[0].rentabilidade").value(0.14))
            .andExpect(jsonPath("$[0].risco").value("Médio"));

        verify(productService).getRecommendedProducts("MODERADO");
        verify(telemetryService).recordMetric(eq("produtos-recomendados"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnRecommendedProductsForAggressiveProfile() throws Exception {
        // Arrange
        Product product1 = new Product();
        product1.setId(4L);
        product1.setNome("Fundo Ações BTG");
        product1.setTipo(TipoProduto.ACOES);
        product1.setRentabilidade(new BigDecimal("0.18"));
        product1.setRisco("Alto");

        Product product2 = new Product();
        product2.setId(5L);
        product2.setNome("ACOES Shopping Center");
        product2.setTipo(TipoProduto.ACOES);
        product2.setRentabilidade(new BigDecimal("0.16"));
        product2.setRisco("Alto");

        List<Product> products = Arrays.asList(product1, product2);

        when(productService.getRecommendedProducts("AGRESSIVO")).thenReturn(products);
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/produtos-recomendados/Agressivo")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(4))
            .andExpect(jsonPath("$[0].nome").value("Fundo Ações BTG"))
            .andExpect(jsonPath("$[0].tipo").value("FundoAcoes"))
            .andExpect(jsonPath("$[1].id").value(5))
            .andExpect(jsonPath("$[1].nome").value("ACOES Shopping Center"))
            .andExpect(jsonPath("$[1].tipo").value("ACOES"));

        verify(productService).getRecommendedProducts("AGRESSIVO");
        verify(telemetryService).recordMetric(eq("produtos-recomendados"), anyLong(), eq(true), eq(200));
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() throws Exception {
        // Arrange
        when(productService.getRecommendedProducts("CONSERVADOR")).thenReturn(Collections.emptyList());
        doNothing().when(telemetryService).recordMetric(anyString(), anyLong(), anyBoolean(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/produtos-recomendados/Conservador")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(productService).getRecommendedProducts("CONSERVADOR");
        verify(telemetryService).recordMetric(eq("produtos-recomendados"), anyLong(), eq(true), eq(200));
    }
}
