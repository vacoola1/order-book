package com.mproject.orderbook;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderCommand {
    private final OrderUpdateCommand command;
    private final int id;
    private final long timestamp;
    private final OrderType type;
    private final OrderSide side;
    private final long price;
    private final int quantity;
    private final String venue;
}
