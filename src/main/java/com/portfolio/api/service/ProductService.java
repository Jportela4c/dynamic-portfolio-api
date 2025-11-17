package com.portfolio.api.service;

import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getRecommendedProducts(String riskProfile) {
        return productRepository.findByPerfilAdequadoAndAtivoTrue(riskProfile);
    }

    public Optional<Product> findMatchingProduct(TipoProduto tipo, BigDecimal valor, Integer prazoMeses) {
        return productRepository
                .findFirstByTipoAndAtivoTrueAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeDesc(
                        tipo, valor, prazoMeses
                );
    }
}
