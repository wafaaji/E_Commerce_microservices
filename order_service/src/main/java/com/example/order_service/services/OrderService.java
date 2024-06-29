package com.example.order_service.services;

import com.example.order_service.entities.Auth;
import com.example.order_service.entities.Order;
import com.example.order_service.entities.OrderProduct;
import com.example.order_service.entities.Product;
import com.example.order_service.repositories.OrderRepository;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate; // For direct calls
    @Autowired
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate loadBalancedRestTemplate; // For load balanced calls
    @Autowired
    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    @HystrixCommand(fallbackMethod = "createOrderFallback")
    public Order createOrder(Order order) {
        double totalCost = 0.0;
        try {
            // Use the non-load balanced RestTemplate for actual API calls
            String ownerUrl = "http://localhost:8002/authService/api/auth/" + order.getOwnerId();
            ResponseEntity<Auth> ownerResponse = restTemplate.getForEntity(ownerUrl, Auth.class);

            String customerUrl = "http://localhost:8002/authService/api/auth/" + order.getCustomerId();
            ResponseEntity<Auth> customerResponse = restTemplate.getForEntity(customerUrl, Auth.class);

            if (ownerResponse.getStatusCode().is2xxSuccessful() && ownerResponse.getBody() != null
                    && customerResponse.getStatusCode().is2xxSuccessful() && customerResponse.getBody() != null) {

                for (OrderProduct orderProduct : order.getOrderProducts()) {
                    // Use the load balanced RestTemplate for Ribbon managed calls
                    String productUrl = "http://order-service/productService/api/products/" + orderProduct.getProductId();
                    ResponseEntity<Product> productResponse = loadBalancedRestTemplate.getForEntity(productUrl, Product.class);

                    if (productResponse.getStatusCode().is2xxSuccessful() && productResponse.getBody() != null) {
                        double productCost = productResponse.getBody().getPrice() * orderProduct.getQuantity();
                        totalCost += productCost;
                        orderProduct.setOrder(order); // Set the order in each orderProduct
                    } else {
                        throw new RuntimeException("Product not found: " + orderProduct.getProductId());
                    }
                }

                order.setCost(totalCost);

                if (ownerResponse.getBody().getAuthType().equals("user") && customerResponse.getBody().getAuthType().equals("user")) {
                    order.setType("u-u");
                } else if (ownerResponse.getBody().getAuthType().equals("user") && customerResponse.getBody().getAuthType().equals("company")) {
                    order.setType("u-c");
                } else if (ownerResponse.getBody().getAuthType().equals("company") && customerResponse.getBody().getAuthType().equals("company")) {
                    order.setType("c-c");
                } else if (ownerResponse.getBody().getAuthType().equals("company") && customerResponse.getBody().getAuthType().equals("user")) {
                    order.setType("c-u");
                } else {
                    throw new RuntimeException("Invalid auth types");
                }

                return orderRepository.save(order);
            } else {
                throw new RuntimeException("Owner or customer not found in auth-service");
            }
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            throw e; // Ensure the exception is propagated to Hystrix
        }
    }

    public Order createOrderFallback(Order order) {
        // Fallback logic for createOrder method
        return new Order(); // Return a default Order object or null
    }

    @HystrixCommand(fallbackMethod = "updateOrderFallback")
    @Transactional
    public Order updateOrder(Long orderId, Order updatedOrderDetails) {
        // Fetch the existing order from the database using the orderId
        Optional<Order> existingOrderOptional = orderRepository.findById(orderId);
        if (!existingOrderOptional.isPresent()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        Order existingOrder = existingOrderOptional.get();

        // Initialize the orderProducts collection
        Hibernate.initialize(existingOrder.getOrderProducts());

        // Remove existing OrderProduct entries
        existingOrder.getOrderProducts().clear();

        double totalCost = 0.0;

        for (OrderProduct updatedOrderProduct : updatedOrderDetails.getOrderProducts()) {
            String productUrl = "http://order-service/productService/api/products/" + updatedOrderProduct.getProductId();
            ResponseEntity<Product> productResponse = loadBalancedRestTemplate.getForEntity(productUrl, Product.class);

            if (productResponse.getStatusCode().is2xxSuccessful() && productResponse.getBody() != null) {
                double productCost = productResponse.getBody().getPrice() * updatedOrderProduct.getQuantity();
                totalCost += productCost;
                updatedOrderProduct.setOrder(existingOrder); // Set the order in each orderProduct
                existingOrder.getOrderProducts().add(updatedOrderProduct); // Add updated OrderProduct to the order
            } else {
                throw new RuntimeException("Product not found: " + updatedOrderProduct.getProductId());
            }
        }

        existingOrder.setCost(totalCost);
        existingOrder.setOwnerId(updatedOrderDetails.getOwnerId());
        existingOrder.setCustomerId(updatedOrderDetails.getCustomerId());
        existingOrder.setType(updatedOrderDetails.getType());
        existingOrder.setCreatedAt(updatedOrderDetails.getCreatedAt());

        // Save the updated order back to the database
        return orderRepository.save(existingOrder);
    }



    public Order updateOrderFallback(Long orderId, Order updatedOrderDetails) {
        // Fallback logic for updateOrder method
        return new Order(); // Return a default Order object or null
    }


    public boolean deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            return false;
        }
        orderRepository.deleteById(id);
        return true;
    }

    public List<Order> getOrdersByType(String type) {
        return orderRepository.findByType("u-c");
    }
}
