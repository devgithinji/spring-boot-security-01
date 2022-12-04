package com.densoft.springbootbasicauth.repository;

import com.densoft.springbootbasicauth.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
