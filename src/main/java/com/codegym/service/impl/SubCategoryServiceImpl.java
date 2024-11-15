package com.codegym.service.impl;

import com.codegym.entity.Subcategory;
import com.codegym.repository.SubCategoryRepository;
import com.codegym.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Override
    public List<Subcategory> findAll() {
        return subCategoryRepository.findAll();
    }

    @Override
    public Subcategory findById(Long id) {
        return subCategoryRepository.findById(id).orElse(null);
    }

    @Override
    public Subcategory save(Subcategory subcategory) {
        return subCategoryRepository.save(subcategory);
    }

    @Override
    public void deleteById(Long id) {
        subCategoryRepository.deleteById(id);
    }
}
