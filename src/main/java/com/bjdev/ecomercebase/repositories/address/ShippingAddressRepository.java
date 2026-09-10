package com.bjdev.ecomercebase.repositories.address;

import com.bjdev.ecomercebase.models.address.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByClientId(Long clientId);

    /** Scoped by ownership so a wrong id and someone else's address return the exact same 404. */
    Optional<ShippingAddress> findByIdAndClientId(Long id, Long clientId);

    /** Enforces "at most one default address per client" before a new/updated one is marked default. */
    @Modifying
    @Query("UPDATE ShippingAddress a SET a.isDefault = false WHERE a.client.id = :clientId AND a.id <> :excludingId")
    void clearDefaultForOtherAddresses(@Param("clientId") Long clientId, @Param("excludingId") Long excludingId);
}
