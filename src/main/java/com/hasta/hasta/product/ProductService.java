package com.hasta.hasta.product;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hasta.hasta.user.User;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    public void addProduct(String name, String description, int quantity, String category, User seller){
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setQuantity(quantity);
        p.setDescription(description);
        p.setSeller(seller);
        productRepository.save(p);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getById(Long id){
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getAll(){
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> getBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Product> getByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailable() {
        return productRepository.findByQuantityGreaterThan(0);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    public Product update(Long id, Product updated) {
        Optional<Product> optional = getById(id);
        if(optional.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        Product existing = optional.get();
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setQuantity(updated.getQuantity());
        existing.setCategory(updated.getCategory());
        return productRepository.save(existing);
    }

    public Product updateQuantity(Long id, int quantity) {
        Optional<Product> optional = getById(id);
        if(optional.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        Product existing = optional.get();
        existing.setQuantity(existing.getQuantity() - quantity);
        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

}
