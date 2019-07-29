package com.mproject.orderbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class OrderBookImplTest {

    private OrderBookImpl orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBookImpl("BTC");
    }

    @Test
    void processOrder() {
        {
            OrderCommand addCommand = OrderCommand.builder()
                    .command(OrderUpdateCommand.NEW)
                    .id(1)
                    .price(100)
                    .quantity(10)
                    .side(OrderSide.BID)
                    .timestamp(1564355155)
                    .type(OrderType.LIMIT)
                    .venue("trader")
                    .build();

            orderBook.processOrder(addCommand);

            Order expectedOrder = Order.builder()
                    .id(1)
                    .price(100)
                    .quantity(10)
                    .side(OrderSide.BID)
                    .timestamp(1564355155)
                    .type(OrderType.LIMIT)
                    .venue("trader")
                    .build();

            assertEquals(expectedOrder, orderBook.getBids().get(100L).getOrders().get(1L));
        }

        {
            OrderCommand modifyCommand = OrderCommand.builder()
                    .command(OrderUpdateCommand.NEW)
                    .id(1)
                    .price(100)
                    .quantity(999)
                    .side(OrderSide.BID)
                    .timestamp(1564355155)
                    .type(OrderType.LIMIT)
                    .venue("trader")
                    .build();

            orderBook.processOrder(modifyCommand);

            Order expectedOrder = Order.builder()
                    .id(1)
                    .price(100)
                    .quantity(999)
                    .side(OrderSide.BID)
                    .timestamp(1564355155)
                    .type(OrderType.LIMIT)
                    .venue("trader")
                    .build();

            assertEquals(999, orderBook.getBids().get(100L).getOrders().get(1L).getQuantity());
            assertEquals(expectedOrder, orderBook.getBids().get(100L).getOrders().get(1L));
        }

        {
            OrderCommand cancelCommand = OrderCommand.builder()
                    .command(OrderUpdateCommand.CANCEL)
                    .id(1)
                    .side(OrderSide.BID)
                    .price(100)
                    .build();

            orderBook.processOrder(cancelCommand);

            assertTrue(orderBook.getBids().isEmpty());
        }
    }

    @Test
    void addOrder() {
        populateOrders();

        assertEquals(3, orderBook.getBids().size());
        assertEquals(3, orderBook.getAsks().size());

        assertEquals(103, orderBook.getBids().firstKey());
        assertEquals(101, orderBook.getBids().lastKey());
        assertEquals(111, orderBook.getAsks().firstKey());
        assertEquals(113, orderBook.getAsks().lastKey());

        assertEquals(1, orderBook.getBids().get(101L).getOrders().size());
        assertEquals(1, orderBook.getAsks().get(112L).getOrders().size());
        assertEquals(2, orderBook.getBids().get(103L).getOrders().size());
        assertEquals(103, orderBook.getBids().get(103L).getOrders().get(3L).getPrice());
        assertEquals(103, orderBook.getBids().get(103L).getOrders().get(4L).getPrice());
    }

    @Test
    void modifyOrder() {
        populateOrders();

        orderBook.modifyOrder(Order.builder().id(6).price(112).quantity(500).side(OrderSide.ASK)
                .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build());

        assertEquals(500, orderBook.getAsks().get(112L).getOrders().get(6L).getQuantity());
    }

    @Test
    void modifyOrderMarket1Error() {
        populateOrders();

        String errorMessage = assertThrows(IllegalArgumentException.class,
                () -> orderBook.modifyOrder(Order.builder().id(3).price(103).quantity(30).side(OrderSide.BID)
                        .timestamp(1564355155).type(OrderType.MARKET).venue("trader").build())
        ).getMessage();

        assertEquals("Couldn't modify order. Try to update Market order. id = 3", errorMessage);
    }

    @Test
    void modifyOrderNotValidError() {
        populateOrders();

        String errorMessage = assertThrows(IllegalArgumentException.class,
                () -> orderBook.modifyOrder(Order.builder().id(6).price(112000).quantity(500).side(OrderSide.ASK)
                        .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build())).getMessage();

        assertEquals("Couldn't modify order. Order with id = 6 was not found by price = 112000", errorMessage);
    }

    @Test
    void deleteOrder() {
        {
            populateOrders();

            orderBook.deleteOrder(Order.builder().id(3).price(103).quantity(30).side(OrderSide.BID)
                    .timestamp(1564355155).type(OrderType.MARKET).venue("trader").build());

            assertEquals(3, orderBook.getBids().size());
            assertEquals(3, orderBook.getAsks().size());

            assertEquals(1, orderBook.getBids().get(103L).getOrders().size());
            assertNull(orderBook.getBids().get(103L).getOrders().get(3L));
            assertEquals(103, orderBook.getBids().get(103L).getOrders().get(4L).getPrice());
        }

        {
            orderBook.deleteOrder(Order.builder().id(5).price(111).quantity(10).side(OrderSide.ASK)
                    .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build());

            assertEquals(3, orderBook.getBids().size());
            assertEquals(2, orderBook.getAsks().size());

            assertNull(orderBook.getAsks().get(111L));
        }
    }

    @Test
    void getBestBid() {
        assertEquals(0, orderBook.getBestBid());
        populateOrders();
        assertEquals(103, orderBook.getBestBid());
    }

    @Test
    void getBestAsk() {
        assertEquals(0, orderBook.getBestAsk());
        populateOrders();
        assertEquals(111, orderBook.getBestAsk());
    }

    private void populateOrders() {
        orderBook.addOrder(Order.builder().id(1).price(101).quantity(10).side(OrderSide.BID)
                .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build());
        orderBook.addOrder(Order.builder().id(2).price(102).quantity(20).side(OrderSide.BID)
                .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build());
        orderBook.addOrder(Order.builder().id(3).price(103).quantity(30).side(OrderSide.BID)
                .timestamp(1564355155).type(OrderType.MARKET).venue("trader").build());
        orderBook.addOrder(Order.builder().id(4).price(103).quantity(40).side(OrderSide.BID)
                .timestamp(1564355155).type(OrderType.LIMIT).venue("trader2").build());

        orderBook.addOrder(Order.builder().id(5).price(111).quantity(10).side(OrderSide.ASK)
                .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build());
        orderBook.addOrder(Order.builder().id(6).price(112).quantity(20).side(OrderSide.ASK)
                .timestamp(1564355155).type(OrderType.LIMIT).venue("trader").build());
        orderBook.addOrder(Order.builder().id(7).price(113).quantity(30).side(OrderSide.ASK)
                .timestamp(1564355155).type(OrderType.MARKET).venue("trader").build());
    }
}