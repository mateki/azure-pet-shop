package com.chtrembl.petstore.order.api;

import com.chtrembl.petstore.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Repository
public class FunctionOrderRepository implements ItemReservationRepository{
    static final Logger log = LoggerFactory.getLogger(FunctionOrderRepository.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${petstore.service.reservations.url:}")
    private String petStoreReservationsURL;

    @Override
    public void put(Order order) {
        String endpoint = "/api/reservation";
        String params = "?sessionid=" + order.getId();
        String url = petStoreReservationsURL+endpoint + params;
        log.info("reservation for " + url);
        try {
            restTemplate.put(url, order);
            log.info("reservation sucessfully saved");
        } catch (HttpStatusCodeException ex) {
            log.warn("reservation failed" + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
        }
    }

    @Override
    public Optional<Order> get(String id) {
        return Optional.empty();
    }
}
