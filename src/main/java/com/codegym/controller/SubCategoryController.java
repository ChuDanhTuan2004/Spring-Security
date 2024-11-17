package com.codegym.controller;

import com.codegym.entity.Subcategory;
import com.codegym.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin("*")
@RestController
@RequestMapping("/api/subcategories")
public class SubCategoryController {

    @Autowired
    private SubCategoryService subcategoryService;

    // Create
    @PostMapping
    public ResponseEntity<Subcategory> createSubcategory(@RequestBody Subcategory subcategory) {
        Subcategory createdSubcategory = subcategoryService.save(subcategory);
        return new ResponseEntity<>(createdSubcategory, HttpStatus.CREATED);
    }

    // Read (Get all)
    @GetMapping
    public ResponseEntity<List<Subcategory>> getAllSubcategories() {
        List<Subcategory> subcategories = subcategoryService.findAll();
        return new ResponseEntity<>(subcategories, HttpStatus.OK);
    }

    // Read (Get by ID)
    @GetMapping("/{id}")
    public ResponseEntity<Subcategory> getSubcategoryById(@PathVariable Long id) {
        Subcategory subcategory = subcategoryService.findById(id);
        if (subcategory != null) {
            return new ResponseEntity<>(subcategory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Subcategory> updateSubcategory(@PathVariable Long id, @RequestBody Subcategory subcategory) {
        Subcategory existingSubcategory = subcategoryService.findById(id);
        if (existingSubcategory != null) {
            subcategory.setSubcategoryId(id);  // Ensure the ID is set correctly
            Subcategory updatedSubcategory = subcategoryService.save(subcategory);
            return new ResponseEntity<>(updatedSubcategory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Long id) {
        Subcategory existingSubcategory = subcategoryService.findById(id);
        if (existingSubcategory != null) {
            subcategoryService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}