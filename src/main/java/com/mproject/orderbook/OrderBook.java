package com.mproject.orderbook;

import java.util.TreeMap;

public interface OrderBook {
    void processOrder(OrderCommand order);
    void addOrder(Order order);
    void modifyOrder(Order order);
    void deleteOrder(Order order);
    long getBestBid();
    long getBestAsk();
}
