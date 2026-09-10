package com.bjdev.ecomercebase.models.address;

import com.bjdev.ecomercebase.models.client.Client;
import jakarta.persistence.*;
import lombok.*;

/**
 * Belongs to {@link Client} (not User) so a guest checkout can also persist the one-off address
 * it needs to ship an order to, without requiring a registered account.
 */
@Entity
@Table(name = "shipping_addresses", indexes = {
        @Index(name = "idx_shipping_address_client", columnList = "client_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
