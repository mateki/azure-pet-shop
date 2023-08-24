package com.chtrembl.petstore.order.api;

import com.chtrembl.petstore.order.model.Order;

import java.util.Optional;

public interface ItemReservationRepository {
    void put(Order order);
    Optional<Order>get(String id);
}
