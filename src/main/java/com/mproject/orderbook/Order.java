package com.mproject.orderbook;

import lombok.*;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class Order {
    private final long id;
    private final long timestamp;
    private final OrderType type;
    private final OrderSide side;
    private final long price;
    @Setter
    private long quantity;
    private final String venue;
}
