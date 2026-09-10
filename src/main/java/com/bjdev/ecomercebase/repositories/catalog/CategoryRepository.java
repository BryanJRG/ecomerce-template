package com.bjdev.ecomercebase.repositories.catalog;

import com.bjdev.ecomercebase.models.catalog.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
