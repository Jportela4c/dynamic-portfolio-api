package com.portfolio.api.repository;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.model.enums.TipoProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InvestmentRepositoryTest {

    @Autowired
    private InvestmentRepository investmentRepository;

    @BeforeEach
    void setUp() {
        investmentRepository.deleteAll();
    }

    @Test
    void shouldFindInvestmentsByClientIdOrderedByDate() {
        LocalDate now = LocalDate.now();

        Investment inv1 = createInvestment(123L, TipoProduto.CDB, new BigDecimal("10000"), now.minusDays(2));
        Investment inv2 = createInvestment(123L, TipoProduto.LCI, new BigDecimal("20000"), now.minusDays(1));
        Investment inv3 = createInvestment(123L, TipoProduto.FUNDO_ACOES, new BigDecimal("15000"), now);
        Investment inv4 = createInvestment(456L, TipoProduto.CDB, new BigDecimal("5000"), now);

        investmentRepository.save(inv1);
        investmentRepository.save(inv2);
        investmentRepository.save(inv3);
        investmentRepository.save(inv4);

        List<Investment> investments = investmentRepository.findByClienteIdOrderByDataDesc(123L);

        assertThat(investments).hasSize(3);
        assertThat(investments.get(0).getTipo()).isEqualTo(TipoProduto.FUNDO_ACOES);
        assertThat(investments.get(1).getTipo()).isEqualTo(TipoProduto.LCI);
        assertThat(investments.get(2).getTipo()).isEqualTo(TipoProduto.CDB);
    }

    @Test
    void shouldReturnEmptyListWhenNoInvestments() {
        List<Investment> investments = investmentRepository.findByClienteIdOrderByDataDesc(999L);

        assertThat(investments).isEmpty();
    }

    @Test
    void shouldCountInvestmentsByClientId() {
        Investment inv1 = createInvestment(123L, TipoProduto.CDB, new BigDecimal("10000"), LocalDate.now());
        Investment inv2 = createInvestment(123L, TipoProduto.LCI, new BigDecimal("20000"), LocalDate.now());
        Investment inv3 = createInvestment(456L, TipoProduto.FUNDO_ACOES, new BigDecimal("15000"), LocalDate.now());

        investmentRepository.save(inv1);
        investmentRepository.save(inv2);
        investmentRepository.save(inv3);

        Long count = investmentRepository.countByClienteId(123L);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void shouldReturnZeroWhenNoInvestmentsForClient() {
        Investment inv = createInvestment(123L, TipoProduto.CDB, new BigDecimal("10000"), LocalDate.now());
        investmentRepository.save(inv);

        Long count = investmentRepository.countByClienteId(999L);

        assertThat(count).isEqualTo(0L);
    }

    @Test
    void shouldSumInvestmentValuesByClientId() {
        Investment inv1 = createInvestment(123L, TipoProduto.CDB, new BigDecimal("10000"), LocalDate.now());
        Investment inv2 = createInvestment(123L, TipoProduto.LCI, new BigDecimal("20000"), LocalDate.now());
        Investment inv3 = createInvestment(123L, TipoProduto.FUNDO_ACOES, new BigDecimal("15000"), LocalDate.now());
        Investment inv4 = createInvestment(456L, TipoProduto.CDB, new BigDecimal("5000"), LocalDate.now());

        investmentRepository.save(inv1);
        investmentRepository.save(inv2);
        investmentRepository.save(inv3);
        investmentRepository.save(inv4);

        BigDecimal total = investmentRepository.sumValorByClienteId(123L);

        assertThat(total).isEqualByComparingTo(new BigDecimal("45000"));
    }

    @Test
    void shouldReturnNullWhenNoInvestmentsToSum() {
        BigDecimal total = investmentRepository.sumValorByClienteId(999L);

        assertThat(total).isNull();
    }

    @Test
    void shouldGetAllClientVolumes() {
        Investment inv1 = createInvestment(123L, TipoProduto.CDB, new BigDecimal("10000"), LocalDate.now());
        Investment inv2 = createInvestment(123L, TipoProduto.LCI, new BigDecimal("20000"), LocalDate.now());
        Investment inv3 = createInvestment(456L, TipoProduto.FUNDO_ACOES, new BigDecimal("15000"), LocalDate.now());
        Investment inv4 = createInvestment(789L, TipoProduto.CDB, new BigDecimal("5000"), LocalDate.now());

        investmentRepository.save(inv1);
        investmentRepository.save(inv2);
        investmentRepository.save(inv3);
        investmentRepository.save(inv4);

        List<BigDecimal> volumes = investmentRepository.getAllCustomerAmounts();

        assertThat(volumes).hasSize(3);
        assertThat(volumes).contains(
                new BigDecimal("30000.00"),  // Customer 123
                new BigDecimal("15000.00"),  // Customer 456
                new BigDecimal("5000.00")    // Customer 789
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoInvestmentsForVolumes() {
        List<BigDecimal> volumes = investmentRepository.getAllCustomerAmounts();

        assertThat(volumes).isEmpty();
    }

    private Investment createInvestment(Long clienteId, TipoProduto tipo, BigDecimal valor, LocalDate data) {
        Investment investment = new Investment();
        investment.setClienteId(clienteId);
        investment.setTipo(tipo);
        investment.setValor(valor);
        investment.setRentabilidade(new BigDecimal("0.10"));
        investment.setData(data);
        return investment;
    }
}
