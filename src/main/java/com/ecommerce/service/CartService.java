package com.ecommerce.service;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.CartItemNotFoundException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.logging.EventName;
import com.ecommerce.logging.StructuredLogger;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for Cart operations
 * Handles business logic for shopping cart management with transactional
 * integrity
 */
@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get or create an active cart for the user
     * 
     * @param username the username
     * @return the active cart
     */
    @Transactional
    public Cart getOrCreateCartForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Optional<Cart> existingCart = cartRepository.findByUserAndStatus(user, Cart.CartStatus.ACTIVE);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart
        Cart newCart = Cart.builder()
                .user(user)
                .status(Cart.CartStatus.ACTIVE)
                .build();

        return cartRepository.save(newCart);
    }

    /**
     * Add a product to the user's cart
     * 
     * @param username  the username
     * @param productId the product ID
     * @param quantity  the quantity to add
     * @return the updated cart
     */
    @Transactional
    public Cart addProductToCart(String username, Long productId, int quantity) {
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Validate stock availability
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new InsufficientStockException(product.getName(), quantity, 0);
        }

        if (quantity > product.getStock()) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }

        // Get or create cart
        Cart cart = getOrCreateCartForUser(username);

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Validate new quantity against stock
            if (newQuantity > product.getStock()) {
                throw new InsufficientStockException(product.getName(), newQuantity, product.getStock());
            }

            item.setQuantity(newQuantity);
        } else {
            // Create new cart item with price snapshot
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);

        // Log business event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("productId", productId);
        businessFields.put("productName", product.getName());
        businessFields.put("quantity", quantity);
        businessFields.put("cartSize", savedCart.getItems().size());
        businessFields.put("cartTotal", calculateCartTotal(savedCart));

        StructuredLogger.logBusinessEvent(EventName.CART_ADD, businessFields);

        return savedCart;
    }

    /**
     * Remove a product from the user's cart
     * 
     * @param username  the username
     * @param productId the product ID
     * @return the updated cart
     */
    @Transactional
    public Cart removeProductFromCart(String username, Long productId) {
        Cart cart = getOrCreateCartForUser(username);

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId, username));

        cart.getItems().remove(itemToRemove);
        Cart savedCart = cartRepository.save(cart);

        // Log business event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("productId", productId);
        businessFields.put("cartSize", savedCart.getItems().size());
        businessFields.put("cartTotal", calculateCartTotal(savedCart));

        StructuredLogger.logBusinessEvent(EventName.CART_REMOVE, businessFields);

        return savedCart;
    }

    /**
     * Update quantity of a product in the cart
     * 
     * @param username  the username
     * @param productId the product ID
     * @param quantity  the new quantity (0 = remove)
     * @return the updated cart
     */
    @Transactional
    public Cart updateQuantity(String username, Long productId, int quantity) {
        // If quantity is 0, remove item
        if (quantity <= 0) {
            return removeProductFromCart(username, productId);
        }

        Cart cart = getOrCreateCartForUser(username);

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId, username));

        // Validate stock
        Product product = item.getProduct();
        if (quantity > product.getStock()) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }

        item.setQuantity(quantity);
        Cart savedCart = cartRepository.save(cart);

        // Log business event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("productId", productId);
        businessFields.put("newQuantity", quantity);
        businessFields.put("cartSize", savedCart.getItems().size());
        businessFields.put("cartTotal", calculateCartTotal(savedCart));

        StructuredLogger.logBusinessEvent(EventName.CART_UPDATE, businessFields);

        return savedCart;
    }

    /**
     * Get the user's active cart
     * 
     * @param username the username
     * @return the active cart
     */
    public Cart getCart(String username) {
        Cart cart = getOrCreateCartForUser(username);

        // Log business event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("cartSize", cart.getItems().size());
        businessFields.put("cartTotal", calculateCartTotal(cart));

        StructuredLogger.logBusinessEvent(EventName.CART_VIEW, businessFields);

        return cart;
    }

    /**
     * Clear all items from the user's cart
     * 
     * @param username the username
     */
    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCartForUser(username);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Calculate total price of cart
     * 
     * @param cart the cart
     * @return the total price
     */
    private BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total item count in cart
     * Sums the quantity of all items
     * 
     * @param username the username
     * @return total quantity across all cart items
     */
    public int getCartItemCount(String username) {
        try {
            Cart cart = getOrCreateCartForUser(username);
            return cart.getItems().stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }
}
