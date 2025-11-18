package com.portfolio.api.repository;

import com.portfolio.api.model.entity.InvestmentDataCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvestmentDataCacheRepository extends JpaRepository<InvestmentDataCache, String> {

    @Query("SELECT c FROM InvestmentDataCache c WHERE c.cpf = :cpf AND c.expiresAt > :now")
    Optional<InvestmentDataCache> findValidCacheByCpf(@Param("cpf") String cpf, @Param("now") LocalDateTime now);
}
