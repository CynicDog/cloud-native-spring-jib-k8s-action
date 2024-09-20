package com.example.orderservice.order.event;

public record OrderAcceptedMessage(
        Long orderId
) { }
