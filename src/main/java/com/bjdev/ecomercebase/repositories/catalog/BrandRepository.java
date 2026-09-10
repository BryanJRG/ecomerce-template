package com.bjdev.ecomercebase.repositories.catalog;

import com.bjdev.ecomercebase.models.catalog.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
