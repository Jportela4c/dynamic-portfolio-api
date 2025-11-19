package com.portfolio.api.mapper;

import com.portfolio.api.provider.dto.OFBInvestmentDto;
import com.portfolio.api.service.external.OFBInvestmentDataService.InvestmentData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OFBInvestmentMapper {

    @Mapping(source = "productType", target = "type")
    @Mapping(source = "productName", target = "issuerName")
    @Mapping(source = "amount", target = "investedAmount")
    InvestmentData toInvestmentData(OFBInvestmentDto dto);

    List<InvestmentData> toInvestmentDataList(List<OFBInvestmentDto> dtos);
}
