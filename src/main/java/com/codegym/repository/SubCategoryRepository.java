package com.codegym.repository;

import com.codegym.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubCategoryRepository extends JpaRepository<Subcategory, Long>, JpaSpecificationExecutor<Subcategory> {}

