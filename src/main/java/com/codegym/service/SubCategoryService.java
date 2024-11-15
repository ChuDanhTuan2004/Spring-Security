package com.codegym.service;

import com.codegym.entity.Subcategory;

import java.util.List;

public interface SubCategoryService {
    List<Subcategory> findAll();
    Subcategory findById(Long id);
    Subcategory save(Subcategory subcategory);
    void deleteById(Long id);
}
