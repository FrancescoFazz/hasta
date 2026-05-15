package com.hasta.backend.product;

import com.hasta.backend.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.hasta.backend.user.User;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(UserRepository userRepository,
                          ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Product addProduct(CreateProductRequest request){

        User seller = userRepository.getReferenceById(request.getSellerId());

        Product p = new Product();
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setQuantity(request.getQuantity());
        p.setSeller(seller);
        productRepository.save(p);
        return p;
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
