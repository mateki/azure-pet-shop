package com.chtrembl.petstore.pet;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

/**
 * Azure Functions with Service Bus Trigger.
 */
public class BusReservation {
    /**
     * This function will be invoked when a new message is received at the Service Bus Queue.
     */
    @FunctionName("orderServiceBusReservation")
    public void run(
            @ServiceBusQueueTrigger(name = "message", queueName = "orders", connection = "ServiceBusConnection") String message,
            @BindingName("SessionId") String sessionId,
            final ExecutionContext context
    ) {
        context.getLogger().info("Java Service Bus Queue trigger function executed.");
        context.getLogger().info(message);

        HttpStatus response = dbReservation(context, message,sessionId);
        for (int i = 0; i < 3; i++) {
            if (HttpStatus.OK == response) {
                return;
            } else {
                response = dbReservation(context,  message,sessionId);
            }
        }
        reservationFallback(context, message);
    }

    private HttpStatus dbReservation(final ExecutionContext context, String message, String sessionId) {
        context.getLogger().info("DB save attempt");

        String storageConnectionString = System.getenv("AzureWebJobsStorage");
        String containerName = "reservations";

        try {
            CloudBlobClient blobClient = CloudStorageAccount.parse(storageConnectionString).createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(containerName);
            container.createIfNotExists();

            String blobName = (StringUtils.isBlank(sessionId) ? java.util.UUID.randomUUID().toString() : sessionId) + ".json";
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            blob.uploadText(message);


        } catch (Exception e) {
            context.getLogger().info("DB save failure");
            return HttpStatus.FAILED_DEPENDENCY;
        }
        return HttpStatus.OK;
    }
    private void reservationFallback(final ExecutionContext context, String message) {
        try {

            String mailerUrl = System.getenv("MailSender");
            context.getLogger().info("calling fallback mail sender "+mailerUrl);
            URL url = new URL(mailerUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(message.getBytes());
            os.flush();
            os.close();

            String responseMessage = connection.getResponseMessage();
            if(connection.getResponseCode()==200){
                context.getLogger().info("fallback ok");
            }{
                context.getLogger().info("fallback failure "+responseMessage);
            }
        } catch (Exception e) {
            context.getLogger().info("fallback failure "+e);
        }
    }
}
