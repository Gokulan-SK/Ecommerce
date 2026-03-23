package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.exception.EmptyCartCheckoutException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.logging.EventName;
import com.ecommerce.logging.StructuredLogger;
import com.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Order Service
 * Handles order creation and management
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checkout cart and create order
     * Atomically converts cart to order with concurrency-safe stock deduction
     * 
     * @param username the username
     * @return created order
     * @throws EmptyCartCheckoutException if cart is empty or doesn't exist
     * @throws InsufficientStockException if product stock is insufficient
     */
    @Transactional
    public Order checkout(String username) {
        try {
            // 1. Fetch user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EmptyCartCheckoutException("User not found: " + username));

            // 2. LOG CHECKOUT INITIATED (before any locks)
            logCheckoutInitiated(user);

            // 3. Fetch ACTIVE cart WITH PESSIMISTIC LOCK
            Cart cart = cartRepository.findActiveCartForUpdate(user.getId())
                    .orElseThrow(() -> new EmptyCartCheckoutException("No active cart found for user: " + username));

            // 4. IDEMPOTENCY: Double checkout impossible due to lock, but defense-in-depth
            if (cart.getStatus() == Cart.CartStatus.CHECKED_OUT) {
                throw new IllegalStateException("Cart already checked out");
            }

            // 5. Validate not empty
            if (cart.getItems().isEmpty()) {
                throw new EmptyCartCheckoutException("Cannot checkout with empty cart");
            }

            // 6. Create Order (BEFORE stock deduction for ID generation)
            BigDecimal totalAmount = calculateTotal(cart);
            Order order = Order.builder()
                    .user(user)
                    .totalAmount(totalAmount)
                    .status(OrderStatus.CREATED)
                    .build();
            order = orderRepository.save(order);

            // 7. Lock + Validate + Deduct stock ATOMICALLY (CRITICAL FIX)
            for (CartItem item : cart.getItems()) {
                // LOCK: Get fresh locked instance (NOT item.getProduct())
                Product lockedProduct = productRepository.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(
                                () -> new IllegalStateException("Product not found: " + item.getProduct().getId()));

                // VALIDATE: Check stock on locked instance
                if (lockedProduct.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            String.format("Insufficient stock for %s. Requested: %d, Available: %d",
                                    lockedProduct.getName(), item.getQuantity(), lockedProduct.getStock()));
                }

                // DEDUCT: Immediately deduct from locked instance
                lockedProduct.setStock(lockedProduct.getStock() - item.getQuantity());
                productRepository.save(lockedProduct);

                // Log stock deduction
                logStockDeduction(user, order, lockedProduct, item.getQuantity());

                // Create OrderItem with snapshot price
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(lockedProduct)
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice()) // Snapshot from cart
                        .build();
                order.addItem(orderItem);

                // Log item creation
                logOrderItemCreated(user, order, orderItem);
            }

            // 8. Save order with items
            orderRepository.save(order);

            // 9. Mark cart CHECKED_OUT
            cart.setStatus(Cart.CartStatus.CHECKED_OUT);
            cartRepository.save(cart);

            // 10. Log cart checkout
            logCartCheckedOut(user, cart, order);

            // 11. Log order creation
            logOrderCreated(user, order);

            return order;

        } catch (EmptyCartCheckoutException | InsufficientStockException e) {
            // Log order failure with anomaly flag
            StructuredLogger.logSystemEvent(
                    "Order checkout failed: " + e.getMessage(),
                    e,
                    "/checkout",
                    true // isAnomaly
            );
            throw e; // Rethrow to trigger transaction rollback
        } catch (Exception e) {
            // Log unexpected errors
            StructuredLogger.logSystemEvent(
                    "Order checkout failed with unexpected error",
                    e,
                    "/checkout",
                    true);
            throw e;
        }
    }

    /**
     * Log checkout initiation
     */
    private void logCheckoutInitiated(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        StructuredLogger.logBusinessEvent(EventName.CHECKOUT_INITIATED, data);
    }

    /**
     * Log stock deduction for a product
     */
    private void logStockDeduction(User user, Order order, Product product, int quantity) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("orderId", order.getId());
        data.put("productId", product.getId());
        data.put("quantity", quantity);
        data.put("remainingStock", product.getStock());
        StructuredLogger.logBusinessEvent(EventName.STOCK_DEDUCTED, data);
    }

    /**
     * Log order item creation
     */
    private void logOrderItemCreated(User user, Order order, OrderItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("orderId", order.getId());
        data.put("productId", item.getProduct().getId());
        data.put("quantity", item.getQuantity());
        data.put("unitPrice", item.getUnitPrice());
        StructuredLogger.logBusinessEvent(EventName.ORDER_ITEM_CREATED, data);
    }

    /**
     * Log cart checked out
     */
    private void logCartCheckedOut(User user, Cart cart, Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("cartId", cart.getId());
        data.put("orderId", order.getId());
        StructuredLogger.logBusinessEvent(EventName.CART_CHECKED_OUT, data);
    }

    /**
     * Log order creation
     */
    private void logOrderCreated(User user, Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("userId", user.getId());
        data.put("itemCount", order.getItems().size());
        data.put("totalAmount", order.getTotalAmount());
        data.put("status", order.getStatus().name());
        StructuredLogger.logBusinessEvent(EventName.ORDER_CREATED, data);
    }

    /**
     * Calculate total price from cart using snapshot prices
     * 
     * @param cart the cart
     * @return total amount
     */
    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get all orders for a user
     * 
     * @param username the username
     * @return list of orders
     */
    public List<Order> getUserOrders(String username) {
        return orderRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }
}
