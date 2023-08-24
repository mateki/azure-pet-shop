package com.chtrembl.petstore.order.api;

import com.chtrembl.petstore.order.model.Order;

public interface ItemReservationRepository {
    void put(String key, Order order);
}
