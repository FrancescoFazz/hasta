package com.hasta.backend.product.controller;

import com.hasta.backend.product.service.ProductService;
import com.hasta.backend.product.model.CreateProductRequest;
import com.hasta.backend.product.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody @Valid CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Product>> getById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll(
        @RequestParam(required = false) Long sellerId,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Boolean available,
        @RequestParam(required = false) String keyword){

        if(sellerId != null){
            return ResponseEntity.ok(productService.getBySeller(sellerId));
        }
        if(category != null){
            return ResponseEntity.ok(productService.getByCategory(category));
        }
        if(available != null && available){
            return ResponseEntity.ok(productService.getAvailable());
        }
        if(keyword != null){
            return ResponseEntity.ok(productService.search(keyword));
        }
        return ResponseEntity.ok(productService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product updated){
        return ResponseEntity.ok(productService.update(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
