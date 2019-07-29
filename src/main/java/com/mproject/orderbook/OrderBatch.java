package com.mproject.orderbook;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.LinkedHashMap;

@Value
class OrderBatch {
    private final long price;
    private final LinkedHashMap<Long, Order> orders;

    OrderBatch(long price) {
        this.price = price;
        this.orders = new LinkedHashMap<>();
    }
}
