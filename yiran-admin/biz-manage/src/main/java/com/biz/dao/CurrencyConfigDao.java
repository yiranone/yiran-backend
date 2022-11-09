package com.biz.dao;

import com.biz.entity.CurrencyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyConfigDao extends JpaRepository<CurrencyConfig, Long>, JpaSpecificationExecutor<CurrencyConfig> {

    CurrencyConfig findByChannelIdAndCurrency(Long channelId, String currency);

    List<CurrencyConfig> findAllByChannelIdOrderBySortNoAsc(Long channelId);

    @Modifying
    @Query(value = "update currency_config t set t.is_delete = 1 where t.id in ?1", nativeQuery = true)
    void softDeleteByIdIn(List<Long> idList);

    List<CurrencyConfig> findAllByCurrency(String currency);
}
