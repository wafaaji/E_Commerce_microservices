package com.example.product_service.services;

import com.example.product_service.entities.Auth;
import com.example.product_service.entities.Product;
import com.example.product_service.repositories.ProductRepository;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    public ProductService(ProductRepository productRepository, RestTemplate restTemplate) {
        this.productRepository = productRepository;
        this.restTemplate = restTemplate;
    }
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByOwner(Long ownerId) {
        // Fetch Auth entity from Auth service
        String authServiceUrl = "http://product-service/authService/api/auth/" + ownerId;
        ResponseEntity<Auth> response = restTemplate.getForEntity(authServiceUrl, Auth.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // Fetch product by ownerId if Auth entity is found
            return productRepository.findByOwnerId(ownerId);
        } else {
            return Optional.empty();
        }
    }

    @HystrixCommand(fallbackMethod = "createProductFallback")
    public Product createProduct(Product product) {
        // Assume AuthService URL is something like http://localhost:8002/api/auth
        System.out.println("we start create Product service");
        String url = "http://product-service/authService/api/auth/" + product.getOwnerId();
        System.out.println("we put the url");

        ResponseEntity<Auth> response = restTemplate.getForEntity(url, Auth.class);
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return productRepository.save(product);
            } catch (Exception e) {
                System.err.println("Error saving product: " + e.getMessage());
                throw e; // Rethrow the exception to handle it further up the call stack
            }
        } else {
            System.err.println("Error saving ownerId: ");
            throw new RuntimeException("Owner not found in auth-service");
        }
    }

    public Product createProductFallback(Product order) {
        // Fallback logic for createProduct method
        return new Product(); // Return a default Order object or null
    }

    public Product updateProduct(Long id, Product product) {

        // Fetch the existing order from the database using the orderId
        Optional<Product> existingProductOptional = productRepository.findById(id);
        if (!existingProductOptional.isPresent()) {
            throw new RuntimeException("Product not found with ID: " + id);
        }
        Product existingProduct = existingProductOptional.get();

        if(!product.getName().isEmpty()){
            existingProduct.setName(product.getName());
        }
        if(!product.getDescription().isEmpty()){
            existingProduct.setDescription(product.getDescription());
        }
        if(product.getPrice()!= null && product.getPrice().isNaN()){
            existingProduct.setPrice(product.getPrice());
        }

        return productRepository.save(existingProduct);
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }
}
