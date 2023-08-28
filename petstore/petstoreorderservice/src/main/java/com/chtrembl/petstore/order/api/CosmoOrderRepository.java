package com.chtrembl.petstore.order.api;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.chtrembl.petstore.order.model.Order;
import com.chtrembl.petstore.order.model.UserOrderMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

@Repository
@Primary
public class CosmoOrderRepository implements ItemReservationRepository{
    @Value("${petstore.service.reservations.cosmo.url:https://mdjavacosmo.documents.azure.com:443/}")
    private String accountHost;

    @Value("${petstore.service.reservations.cosmo.key:zXQ23GlasovA5ALawr0XnSlwNbnd4FO9tvbWMYS6u2uaLc0RtVl2ExCjc4BVlxwMbATQtB5YYQZjACDbbXLbrA==}")
    private String accountKey;

    static final Logger log = LoggerFactory.getLogger(CosmoOrderRepository.class);

    private CosmosClient client;

    private final String databaseName = "Orders";
    private final String containerName = "reservations";

    private CosmosDatabase database;
    private CosmosContainer container;

    public void close() {
        client.close();
    }


    public void run() {

        try {
            getStartedDemo();
            log.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            log.info("Closing the client");
            close();
        }        
    }

    public void init() throws Exception {
        log.info("Using Azure Cosmos DB endpoint: " + accountHost);

        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add("West US");

        //  Create sync client
        client = new CosmosClientBuilder()
                .endpoint(accountHost)
                .key(accountKey)
                .preferredRegions(preferredRegions)
                .userAgentSuffix("CosmosDBJavaQuickstart")
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        createDatabaseIfNotExists();
        createContainerIfNotExists();
    }


    public void put(Order order) {
        try {
            init();
            putOrder(order);
            log.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            log.info("Closing the client");
            close();
        }
    }

    public Optional<Order> get(String id) {
        try {
            init();
            return Optional.of(getOrder(id));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            log.info("Closing the client");
            close();
            return Optional.empty();
        }
    }

    private void getStartedDemo() throws Exception {
        init();

        //  Setup family items to create
        Order orderOrg = UserOrderMock.getOrder();
        orderOrg.setStatus(Order.StatusEnum.PLACED);
        log.info("order org " + orderOrg);
        put(UserOrderMock.getOrder());

        log.info("order from db " + get(orderOrg.getId()));

        orderOrg.setStatus(Order.StatusEnum.APPROVED);
        put(orderOrg);

        log.info("order from db " + get(orderOrg.getId()));
    }

    private void putOrder(Order order) throws Exception {
        double totalRequestCharge = 0;
        //  Create item using container that we created using sync client

        //  Using appropriate partition key improves the performance of database operations
        CosmosItemResponse item = container.upsertItem(order, new PartitionKey(order.getId()), new CosmosItemRequestOptions());

        //  Get request charge and other properties like latency, and diagnostics strings, etc.
        log.info(String.format("Created item with request charge of %.2f within duration %s",
                item.getRequestCharge(), item.getDuration()));
        totalRequestCharge += item.getRequestCharge();

        log.info(String.format("Created items with total request charge of %.2f",
                totalRequestCharge));
    }

    private Order getOrder(String id) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        try {
            CosmosItemResponse<Order> item = container.readItem(id, new PartitionKey(id), Order.class);
            double requestCharge = item.getRequestCharge();
            Duration requestLatency = item.getDuration();
            log.info(String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                    item.getItem().getId(), requestCharge, requestLatency));
            return item.getItem();
        } catch (CosmosException e) {
            e.printStackTrace();
            System.err.println(String.format("Read Item failed with %s", e));
        }
        return null;
    }

    private void createDatabaseIfNotExists() throws Exception {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());
        log.debug("Checking database " + database.getId() + " completed!");
    }

    private void createContainerIfNotExists() throws Exception {
        CosmosContainerProperties containerProperties =new CosmosContainerProperties(containerName, "/id");
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());
        log.debug("Checking container " + container.getId() + " completed!");
    }
}
