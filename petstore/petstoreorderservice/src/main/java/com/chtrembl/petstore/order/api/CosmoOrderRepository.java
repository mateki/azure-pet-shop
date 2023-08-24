package com.chtrembl.petstore.order.api;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.chtrembl.petstore.order.model.Order;
import com.chtrembl.petstore.order.model.UserOrderMock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CosmoOrderRepository {
    @Value("${cosmo.account.host:https://mdjavacosmo.documents.azure.com:443/}")
    private String accountHost;

    @Value("${cosmo.account.key:zXQ23GlasovA5ALawr0XnSlwNbnd4FO9tvbWMYS6u2uaLc0RtVl2ExCjc4BVlxwMbATQtB5YYQZjACDbbXLbrA==}")
    private String accountKey;

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
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            close();
        }
        System.exit(0);
    }

    //  </Main>

    private void getStartedDemo() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + accountHost);

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
//        scaleContainer();

        //  Setup family items to create
        ArrayList<Order> familiesToCreate = new ArrayList<>();
        familiesToCreate.add(UserOrderMock.getOrder());


        createFamilies(familiesToCreate);

        System.out.println("Reading items.");
//        readItems(familiesToCreate);

        System.out.println("Querying items.");
//        queryItems();
    }

    private void createFamilies(List<Order> families) throws Exception {
        double totalRequestCharge = 0;
        for (Order family : families) {

            //  Create item using container that we created using sync client

            //  Using appropriate partition key improves the performance of database operations
            CosmosItemResponse item = container.createItem(family, new PartitionKey(family.getEmail()), new CosmosItemRequestOptions());

            //  Get request charge and other properties like latency, and diagnostics strings, etc.
            System.out.println(String.format("Created item with request charge of %.2f within" +
                            " duration %s",
                    item.getRequestCharge(), item.getDuration()));
            totalRequestCharge += item.getRequestCharge();
        }
        System.out.println(String.format("Created %d items with total request " +
                        "charge of %.2f",
                families.size(),
                totalRequestCharge));
    }

    private void readItems(ArrayList<Order> familiesToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        familiesToCreate.forEach(family -> {
            try {
                CosmosItemResponse<Order> item = container.readItem(family.getId(), new PartitionKey(family.getId()), Order.class);
                double requestCharge = item.getRequestCharge();
                Duration requestLatency = item.getDuration();
                System.out.println(String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                        item.getItem().getId(), requestCharge, requestLatency));
            } catch (CosmosException e) {
                e.printStackTrace();
                System.err.println(String.format("Read Item failed with %s", e));
            }
        });
    }

    private void queryItems() {
        // Set some common query options
        int preferredPageSize = 10;
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<Order> familiesPagedIterable = container.queryItems(
                "SELECT * FROM Family WHERE Family.partitionKey IN ('Andersen', 'Wakefield', 'Johnson')", queryOptions, Order.class);

        familiesPagedIterable.iterableByPage(preferredPageSize).forEach(cosmosItemPropertiesFeedResponse -> {
            System.out.println("Got a page of query result with " +
                    cosmosItemPropertiesFeedResponse.getResults().size() + " items(s)"
                    + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

            System.out.println("Item Ids " + cosmosItemPropertiesFeedResponse
                    .getResults()
                    .stream()
                    .map(Order::getId)
                    .collect(Collectors.toList()));
        });
    }

    private void createDatabaseIfNotExists() throws Exception {
        System.out.println("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() throws Exception {
        System.out.println("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/email");

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());

        System.out.println("Checking container " + container.getId() + " completed!\n");
    }
}
