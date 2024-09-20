package com.example.orderservice.order.event;

public record OrderDispatchedMessage(
        Long orderId
) { }
