package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaymentView {

    private Long orderId;
    private BigDecimal totalAmount;
    private List<ItemView> items;

    public PaymentView(Long orderId, BigDecimal totalAmount, List<ItemView> items) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<ItemView> getItems() {
        return items;
    }

    public static class ItemView {
        private String productName;
        private int quantity;
        private BigDecimal subtotal;

        public ItemView(String productName, int quantity, BigDecimal subtotal) {
            this.productName = productName;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }
}