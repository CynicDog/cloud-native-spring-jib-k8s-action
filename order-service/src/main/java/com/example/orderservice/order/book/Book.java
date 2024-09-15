package com.example.orderservice.order.book;

public record Book(
        String isbn,
        String title,
        String author,
        Double price
){}
