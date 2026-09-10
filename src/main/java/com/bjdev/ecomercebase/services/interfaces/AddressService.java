package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.ShippingAddressCreateRequest;
import com.bjdev.ecomercebase.dto.request.ShippingAddressUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ShippingAddressResponse;

import java.util.List;

public interface AddressService {

    ShippingAddressResponse createAddress(Long clientId, ShippingAddressCreateRequest request);

    /** Scoped by ownership — see ShippingAddressRepository.findByIdAndClientId. */
    ShippingAddressResponse updateAddress(Long clientId, Long addressId, ShippingAddressUpdateRequest request);

    /** Hard-delete: addresses aren't accounting-relevant on their own — an Order that used one keeps its own FK, which the database refuses to let this break (ON DELETE RESTRICT). */
    void deleteAddress(Long clientId, Long addressId);

    List<ShippingAddressResponse> listAddresses(Long clientId);
}
