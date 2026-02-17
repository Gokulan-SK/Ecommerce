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
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,

    // Order Events
    ORDER_SUCCESS,
    ORDER_FAILED
}