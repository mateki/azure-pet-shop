package com.chtrembl.petstore.pet;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.TableOutput;
import com.microsoft.azure.storage.table.TableServiceEntity;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderReservations {
    @FunctionName("OrderReservations")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
            @TableOutput(name = "reservationsTable", tableName = "reservations", connection = "AzureWebJobsStorage") OutputBinding<Reservation> outputReservation,
            final ExecutionContext context) {

        // Pobierz parametr sessionid z żądania HTTP POST
        String sessionId = request.getQueryParameters().get("sessionid");
        if (StringUtils.isBlank(sessionId)) {
            request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("sessionid is mandatory").build();
        }
        String orders = request.getBody();
        if (StringUtils.isBlank(orders)) {
            request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("post body is mandatory").build();
        }

        // Utwórz obiekt reprezentujący rezerwację
        Reservation reservation = new Reservation(sessionId, orders);

        // Zapisz rezerwację do kontenera Table Storage
        outputReservation.setValue(reservation);

        // Zwróć odpowiedź HTTP 200 OK
        return request.createResponseBuilder(HttpStatus.OK).body("Reservation saved successfully.").build();
    }

    // Klasa reprezentująca rezerwację
    public class Reservation extends TableServiceEntity {

        String orders;

        public Reservation(String sessionId, String orders) {
            this.partitionKey = sessionId;
            this.rowKey = sessionId;
            this.orders = orders;
        }

        public Reservation() {
        }
    }
}