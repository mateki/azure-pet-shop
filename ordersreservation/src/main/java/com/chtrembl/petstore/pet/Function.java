package com.chtrembl.petstore.pet;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        // Get the name parameter from the request
        String name = request.getBody().orElse("");

        // Get the Blob Storage connection string and container name from environment variables
        String storageConnectionString = System.getenv("AzureWebJobsStorage");
        String containerName = "reservations";

        try {
            // Create a CloudBlobClient object using the connection string
            CloudBlobClient blobClient = CloudStorageAccount.parse(storageConnectionString).createCloudBlobClient();

            // Get a reference to the container
            CloudBlobContainer container = blobClient.getContainerReference(containerName);

            // Create a new blob with a random name
            String blobName = java.util.UUID.randomUUID().toString();
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);

            // Upload the name parameter to the blob
            blob.uploadText(name);

            // Return a success response
            return request.createResponseBuilder(HttpStatus.OK).body("Reservation saved: " + name).build();

        } catch (Exception e) {
            // Return an error response
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving reservation: " + e.getMessage()).build();
        }
    }
}
