package com.mproject.orderbook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.apachecommons.CommonsLog;

import java.util.Collections;
import java.util.TreeMap;

@CommonsLog
@AllArgsConstructor
@ToString
@Getter
public class OrderBookImpl implements OrderBook {

    private final String symbol;

    private final TreeMap<Long, OrderBatch> bids;
    private final TreeMap<Long, OrderBatch> asks;

    public OrderBookImpl(String symbol) {
        this.symbol = symbol;
        this.bids = new TreeMap<>(Collections.reverseOrder());
        this.asks = new TreeMap<>();
    }

    public void processOrder(OrderCommand orderCommand) {
        Order order = Order.builder()
                .id(orderCommand.getId())
                .timestamp(orderCommand.getTimestamp())
                .price(orderCommand.getPrice())
                .side(orderCommand.getSide())
                .type(orderCommand.getType())
                .quantity(orderCommand.getQuantity())
                .venue(orderCommand.getVenue())
                .build();

        switch (orderCommand.getCommand())
        {
            case NEW -> addOrder(order);
            case AMEND -> modifyOrder(order);
            case CANCEL -> deleteOrder(order);
            default -> throw new IllegalArgumentException("Not supported command " + orderCommand.getCommand());
        }
    }

    @Override
    public void addOrder(Order order) {
        var side = getOrdeSide(order.getSide());
        var orderBatch = side.computeIfAbsent(order.getPrice(), OrderBatch::new);
        orderBatch.getOrders().put(order.getId(), order);
    }

    @Override
    public void modifyOrder(Order order) {
        var side = getOrdeSide(order.getSide());
        var price = order.getPrice();

        var orderBatch = side.get(price);
        if (orderBatch == null) {
            throw new IllegalArgumentException("Couldn't modify order. Order with id = " + order.getId() + " was not found by price = " + order.getPrice());
        }

        var modifiedOrder = orderBatch.getOrders().get(order.getId());

        if (modifiedOrder == null) {
            throw new IllegalArgumentException("Couldn't modify order. Order was not found. id = " + order.getId());
        } else if (modifiedOrder.getType() == OrderType.MARKET) {
            throw new IllegalArgumentException("Couldn't modify order. Try to update Market order. id = " + order.getId());
        } else if (!validateBeforeUpdate(modifiedOrder, order)) {
            throw new IllegalArgumentException("Couldn't modify order. Forbidden attributes are changed. id = " + order.getId());
        }

        modifiedOrder.setQuantity(order.getQuantity());
    }

    @Override
    public void deleteOrder(Order order) {
        var side = getOrdeSide(order.getSide());
        long price = order.getPrice();
        long id = order.getId();

        var orderBatch = side.get(price);
        if (orderBatch == null) {
            return;
        }

        orderBatch.getOrders().remove(id);

        if (orderBatch.getOrders().isEmpty()) {
            side.remove(price);
        }
    }

    @Override
    public long getBestBid() {
        return !bids.isEmpty() ? bids.firstKey() : 0;
    }

    @Override
    public long getBestAsk() {
        return !asks.isEmpty() ? asks.firstKey() : 0;
    }

    private TreeMap<Long, OrderBatch> getOrdeSide(OrderSide side) {
        return side == OrderSide.BID ? bids : asks;
    }

    private boolean validateBeforeUpdate(Order firstOrder, Order secondOrder) {
        if (firstOrder == secondOrder) return true;
        if (firstOrder == null || secondOrder == null) return false;

        if (firstOrder.getId() != secondOrder.getId()) return false;
        if (firstOrder.getTimestamp() != secondOrder.getTimestamp()) return false;
        if (firstOrder.getPrice() != secondOrder.getPrice()) return false;
        if (firstOrder.getType() != secondOrder.getType()) return false;
        if (firstOrder.getSide() != secondOrder.getSide()) return false;
        return firstOrder.getVenue() != null ? firstOrder.getVenue().equals(secondOrder.getVenue()) : secondOrder.getVenue() == null;
    }
}
