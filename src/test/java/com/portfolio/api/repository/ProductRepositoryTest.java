package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Product;
import com.portfolio.api.model.enums.TipoProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product conservativeProduct;
    private Product moderateProduct;
    private Product aggressiveProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        conservativeProduct = new Product();
        conservativeProduct.setNome("CDB Conservador");
        conservativeProduct.setTipo(TipoProduto.CDB);
        conservativeProduct.setRentabilidade(new BigDecimal("0.10"));
        conservativeProduct.setRisco("Baixo");
        conservativeProduct.setValorMinimo(new BigDecimal("1000.00"));
        conservativeProduct.setPrazoMinimoMeses(6);
        conservativeProduct.setPrazoMaximoMeses(24);
        conservativeProduct.setPerfilAdequado("Conservador");
        conservativeProduct.setAtivo(true);
        productRepository.save(conservativeProduct);

        moderateProduct = new Product();
        moderateProduct.setNome("LCI Moderado");
        moderateProduct.setTipo(TipoProduto.LCI);
        moderateProduct.setRentabilidade(new BigDecimal("0.12"));
        moderateProduct.setRisco("Médio");
        moderateProduct.setValorMinimo(new BigDecimal("5000.00"));
        moderateProduct.setPrazoMinimoMeses(12);
        moderateProduct.setPrazoMaximoMeses(36);
        moderateProduct.setPerfilAdequado("Moderado");
        moderateProduct.setAtivo(true);
        productRepository.save(moderateProduct);

        aggressiveProduct = new Product();
        aggressiveProduct.setNome("Fundo Agressivo");
        aggressiveProduct.setTipo(TipoProduto.FUNDO_ACOES);
        aggressiveProduct.setRentabilidade(new BigDecimal("0.18"));
        aggressiveProduct.setRisco("Alto");
        aggressiveProduct.setValorMinimo(new BigDecimal("10000.00"));
        aggressiveProduct.setPrazoMinimoMeses(24);
        aggressiveProduct.setPrazoMaximoMeses(60);
        aggressiveProduct.setPerfilAdequado("Agressivo");
        aggressiveProduct.setAtivo(true);
        productRepository.save(aggressiveProduct);
    }

    @Test
    void shouldFindProductsByRiskProfile() {
        List<Product> conservativeProducts = productRepository.findByPerfilAdequadoAndAtivoTrue("Conservador");

        assertThat(conservativeProducts).hasSize(1);
        assertThat(conservativeProducts.get(0).getNome()).isEqualTo("CDB Conservador");
    }

    @Test
    void shouldFindProductsByTypeAndMinimumValue() {
        List<Product> products = productRepository.findByTipoAndAtivoTrueAndValorMinimoLessThanEqual(
                TipoProduto.CDB,
                new BigDecimal("2000.00")
        );

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getNome()).isEqualTo("CDB Conservador");
    }

    @Test
    void shouldNotFindProductsAboveMinimumValue() {
        List<Product> products = productRepository.findByTipoAndAtivoTrueAndValorMinimoLessThanEqual(
                TipoProduto.CDB,
                new BigDecimal("500.00")
        );

        assertThat(products).isEmpty();
    }

    @Test
    void shouldFindBestProductByProfitability() {
        Optional<Product> product = productRepository
                .findFirstByTipoAndAtivoTrueAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeDesc(
                        TipoProduto.CDB,
                        new BigDecimal("5000.00"),
                        12
                );

        assertThat(product).isPresent();
        assertThat(product.get().getNome()).isEqualTo("CDB Conservador");
        assertThat(product.get().getRentabilidade()).isEqualByComparingTo(new BigDecimal("0.10"));
    }

    @Test
    void shouldNotFindProductWhenTermTooShort() {
        Optional<Product> product = productRepository
                .findFirstByTipoAndAtivoTrueAndValorMinimoLessThanEqualAndPrazoMinimoMesesLessThanEqualOrderByRentabilidadeDesc(
                        TipoProduto.LCI,
                        new BigDecimal("10000.00"),
                        6
                );

        assertThat(product).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoActiveProducts() {
        conservativeProduct.setAtivo(false);
        productRepository.save(conservativeProduct);

        List<Product> products = productRepository.findByPerfilAdequadoAndAtivoTrue("Conservador");

        assertThat(products).isEmpty();
    }

    @Test
    void shouldFindMultipleProductsForSameProfile() {
        Product anotherModerate = new Product();
        anotherModerate.setNome("LCA Moderado");
        anotherModerate.setTipo(TipoProduto.LCA);
        anotherModerate.setRentabilidade(new BigDecimal("0.11"));
        anotherModerate.setRisco("Médio");
        anotherModerate.setValorMinimo(new BigDecimal("3000.00"));
        anotherModerate.setPrazoMinimoMeses(6);
        anotherModerate.setPrazoMaximoMeses(24);
        anotherModerate.setPerfilAdequado("Moderado");
        anotherModerate.setAtivo(true);
        productRepository.save(anotherModerate);

        List<Product> products = productRepository.findByPerfilAdequadoAndAtivoTrue("Moderado");

        assertThat(products).hasSize(2);
    }
}
