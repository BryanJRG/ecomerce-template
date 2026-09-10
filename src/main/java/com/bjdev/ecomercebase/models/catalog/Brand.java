package com.bjdev.ecomercebase.models.catalog;

import jakarta.persistence.*;
import lombok.*;

/** Never hard-deleted — use {@link #active} to remove a brand from the catalog. */
@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
