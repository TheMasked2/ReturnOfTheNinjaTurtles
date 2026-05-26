package org.turtleshop.api.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.inventory.dto.InventoryAdjustmentRequest;
import org.turtleshop.api.modules.shipment.dto.CreateShipmentStatusLogRequest;
import org.turtleshop.api.modules.shipment.dto.UpdateShipmentLogNotesRequest;
import org.turtleshop.api.modules.shipment.dto.UpdateShipmentMethodRequest;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Sql(
        scripts = {
                "/db/testdata/clean-test-data.sql",
                "/db/testdata/checkout-flow-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CheckoutOrderShipmentE2ETest extends IntegrationTestBase {

    private static final UUID LEONARDO_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void checkoutEndpoint_shouldCreateOrderShipmentTransactionAndConvertCart() throws Exception {
        PlaceOrderResponse response = placeOrderForLeonardo();

        mockMvc.perform(get("/api/orders/{orderId}", response.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("ORDER_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(response.getOrderId()))
                .andExpect(jsonPath("$.customerId").value(LEONARDO_CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(49.98));

        mockMvc.perform(get("/api/orders/customer/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("ORDER_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(response.getOrderId()));

        mockMvc.perform(get("/api/orders/{orderId}/items", response.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("ORDER_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].quantity").value(2));

        mockMvc.perform(get("/api/shipments/{shipmentId}", response.getShipmentId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(response.getShipmentId()))
                .andExpect(jsonPath("$.orderId").value(response.getOrderId()))
                .andExpect(jsonPath("$.shipmentMethod").value("PostNL"));

        mockMvc.perform(get("/api/shipments/order/{orderId}", response.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(response.getShipmentId()));

        mockMvc.perform(get("/api/shipments/{shipmentId}/logs", response.getShipmentId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AWAITING_PAYMENT"));

        mockMvc.perform(get("/api/cart/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_READ_ALL")
                        )))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelOrderEndpoint_shouldCancelAwaitingPaymentOrder() throws Exception {
        PlaceOrderResponse response = placeOrderForLeonardo();

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", response.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("ORDER_UPDATE_ALL")
                        )))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/{orderId}", response.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("ORDER_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shipmentUpdateEndpoints_shouldAddStatusLogAndUpdateShipmentDetails() throws Exception {
        PlaceOrderResponse response = placeOrderForLeonardo();

        CreateShipmentStatusLogRequest createLogRequest = new CreateShipmentStatusLogRequest();
        createLogRequest.setStatus(ShipmentStatus.IN_TRANSIT);
        createLogRequest.setNotes("Moving through the carrier network");

        String createdLogJson = mockMvc.perform(post("/api/shipments/{shipmentId}/logs", response.getShipmentId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createLogRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(response.getShipmentId()))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> createdLog = objectMapper.readValue(createdLogJson, new TypeReference<>() {});
        Integer logId = (Integer) createdLog.get("logId");

        UpdateShipmentMethodRequest updateMethodRequest = new UpdateShipmentMethodRequest();
        updateMethodRequest.setShipmentMethod("DHL");

        mockMvc.perform(patch("/api/shipments/{shipmentId}/method", response.getShipmentId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateMethodRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/shipments/{shipmentId}", response.getShipmentId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentMethod").value("DHL"));

        UpdateShipmentLogNotesRequest updateNotesRequest = new UpdateShipmentLogNotesRequest();
        updateNotesRequest.setNotes("Left warehouse");

        mockMvc.perform(patch("/api/shipments/logs/{logId}/notes", logId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateNotesRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/shipments/logs/{logId}", logId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("SHIPMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Left warehouse"));
    }

    @Test
    void checkoutEndpoint_whenInventoryIsTooLow_shouldReturnConflict() throws Exception {
        InventoryAdjustmentRequest reserveRequest = new InventoryAdjustmentRequest();
        reserveRequest.setQuantity(19);

        mockMvc.perform(post("/api/inventory/product/{productId}/reserve", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isNoContent());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Roffa");
        request.setPaymentMethod("Visa");

        mockMvc.perform(post("/api/checkout/customer/{customerId}/place-order", LEONARDO_CUSTOMER_ID)
                        .with(user("leonardo@example.com").authorities(
                                new SimpleGrantedAuthority("ORDER_CREATE_OWN")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    private PlaceOrderResponse placeOrderForLeonardo() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Roffa");
        request.setPaymentMethod("Visa");

        String placeOrderJson = mockMvc.perform(post("/api/checkout/customer/{customerId}/place-order", LEONARDO_CUSTOMER_ID)
                        .with(user("leonardo@example.com").authorities(
                                new SimpleGrantedAuthority("ORDER_CREATE_OWN")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("AWAITING_PAYMENT"))
                .andExpect(jsonPath("$.transactionStatus").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(49.98))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(placeOrderJson, PlaceOrderResponse.class);
    }
}
