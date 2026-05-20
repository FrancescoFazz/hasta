package com.hasta.backend.product.service;

import com.hasta.backend.exception.ApplicationException;
import com.hasta.backend.exception.enums.ProductException;
import com.hasta.backend.exception.enums.UserException;
import com.hasta.backend.product.model.CreateProductRequest;
import com.hasta.backend.product.model.Product;
import com.hasta.backend.product.repository.ProductRepository;
import com.hasta.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.hasta.backend.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Product addProduct(CreateProductRequest request){

        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new ApplicationException(UserException.NOT_FOUND));

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

    @Transactional
    public Product update(Long id, Product updated) {
        Optional<Product> optional = getById(id);
        if(optional.isEmpty()) {
            throw new ApplicationException(ProductException.ALREADY_EXISTS);
        }
        Product existing = optional.get();
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setQuantity(updated.getQuantity());
        existing.setCategory(updated.getCategory());
        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

}
