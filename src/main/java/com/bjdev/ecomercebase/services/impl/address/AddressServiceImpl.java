package com.bjdev.ecomercebase.services.impl.address;

import com.bjdev.ecomercebase.dto.request.ShippingAddressCreateRequest;
import com.bjdev.ecomercebase.dto.request.ShippingAddressUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ShippingAddressResponse;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.mappers.ShippingAddressMapper;
import com.bjdev.ecomercebase.models.address.ShippingAddress;
import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.repositories.address.ShippingAddressRepository;
import com.bjdev.ecomercebase.repositories.client.ClientRepository;
import com.bjdev.ecomercebase.services.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final ClientRepository clientRepository;
    private final ShippingAddressMapper addressMapper;

    @Override
    @Transactional
    public ShippingAddressResponse createAddress(Long clientId, ShippingAddressCreateRequest request) {
        Client client = clientRepository.findById(clientId).orElseThrow(NotFoundException::shippingAddress);

        ShippingAddress address = ShippingAddress.builder()
                .client(client)
                .label(request.label())
                .recipientName(request.recipientName())
                .phone(request.phone())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .department(request.department())
                .isDefault(request.isDefault())
                .build();
        address = shippingAddressRepository.save(address);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            shippingAddressRepository.clearDefaultForOtherAddresses(clientId, address.getId());
        }

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public ShippingAddressResponse updateAddress(Long clientId, Long addressId, ShippingAddressUpdateRequest request) {
        ShippingAddress address = getByIdOrThrow(clientId, addressId);
        addressMapper.updateAddressFromRequest(request, address);
        address = shippingAddressRepository.save(address);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            shippingAddressRepository.clearDefaultForOtherAddresses(clientId, address.getId());
        }

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long clientId, Long addressId) {
        ShippingAddress address = getByIdOrThrow(clientId, addressId);
        shippingAddressRepository.delete(address);
    }

    @Override
    public List<ShippingAddressResponse> listAddresses(Long clientId) {
        return shippingAddressRepository.findByClientId(clientId).stream().map(addressMapper::toResponse).toList();
    }

    private ShippingAddress getByIdOrThrow(Long clientId, Long addressId) {
        return shippingAddressRepository.findByIdAndClientId(addressId, clientId)
                .orElseThrow(NotFoundException::shippingAddress);
    }
}
