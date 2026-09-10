package com.bjdev.ecomercebase.mappers;

import com.bjdev.ecomercebase.dto.request.ShippingAddressUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ShippingAddressResponse;
import com.bjdev.ecomercebase.models.address.ShippingAddress;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    void updateAddressFromRequest(ShippingAddressUpdateRequest request, @MappingTarget ShippingAddress address);

    ShippingAddressResponse toResponse(ShippingAddress address);
}
