package com.portfolio.api.mapper;

import com.portfolio.api.provider.dto.OFBInvestmentDto;
import com.portfolio.api.service.external.OFBInvestmentDataService.InvestmentData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for converting OFB investment DTOs to internal data model.
 * Handles all 5 investment types through unified DTO with convenience methods.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OFBInvestmentMapper {

    @Mapping(source = "type", target = "type")
    @Mapping(source = "productNameOrDefault", target = "issuerName")
    @Mapping(source = "amount", target = "investedAmount")
    @Mapping(source = "amount", target = "currentValue")
    @Mapping(target = "profitability", constant = "0.0")
    InvestmentData toInvestmentData(OFBInvestmentDto dto);

    List<InvestmentData> toInvestmentDataList(List<OFBInvestmentDto> dtos);
}
