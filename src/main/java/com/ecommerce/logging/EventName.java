package com.ecommerce.logging;

public enum EventName {

    // Authentication Events
    USER_LOGIN,
    USER_LOGOUT,

    // Product / Catalog Events
    PRODUCT_VIEW,

    // Search Events
    SEARCH_START,
    SEARCH_RESULT,

    // Cart Events
    CART_VIEW,
    CART_ADD,
    CART_REMOVE,
    CART_UPDATE,
    ADD_TO_CART,
    REMOVE_FROM_CART,

    // Checkout Events
    CHECKOUT_INITIATED,

    // Payment Events
    PAYMENT_INITIATED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    ORDER_MARKED_PAID,
    PAYMENT_LOCK_TIMEOUT,

    // Order Events
    ORDER_CREATED,
    ORDER_ITEM_CREATED,
    STOCK_DEDUCTED,
    CART_CHECKED_OUT,
    ORDER_SUCCESS,
    ORDER_FAILED
}
