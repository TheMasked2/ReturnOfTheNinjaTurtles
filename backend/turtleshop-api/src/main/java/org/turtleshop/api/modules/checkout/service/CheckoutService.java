package org.turtleshop.api.modules.checkout.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.cart.enums.CartStatus;
import org.turtleshop.api.modules.cart.model.Cart;
import org.turtleshop.api.modules.cart.model.CartItem;
import org.turtleshop.api.modules.cart.repository.CartAccess;
import org.turtleshop.api.modules.cart.repository.CartItemAccess;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.repository.InventoryAccess;
import org.turtleshop.api.modules.order.enums.OrderStatus;
import org.turtleshop.api.modules.order.model.Order;
import org.turtleshop.api.modules.order.model.OrderItem;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.order.repository.OrderItemAccess;
import org.turtleshop.api.modules.order.service.OrderLiveSyncService;
import org.turtleshop.api.modules.product.model.ProductModel;
import org.turtleshop.api.modules.product.repository.ProductAccess;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;
import org.turtleshop.api.modules.shipment.model.Shipment;
import org.turtleshop.api.modules.shipment.repository.ShipmentAccess;
import org.turtleshop.api.modules.shipment.repository.ShipmentStatusLogAccess;
import org.turtleshop.api.modules.transaction.model.PaymentMethodModel;
import org.turtleshop.api.modules.transaction.model.TransactionModel;
import org.turtleshop.api.modules.transaction.repository.PaymentMethodAccess;
import org.turtleshop.api.modules.transaction.repository.TransactionAccess;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartAccess cartAccess;
    private final CartItemAccess cartItemAccess;
    private final OrderAccess orderAccess;
    private final OrderItemAccess orderItemAccess;
    private final ProductAccess productAccess;
    private final InventoryAccess inventoryAccess;
    private final ShipmentAccess shipmentAccess;
    private final ShipmentStatusLogAccess shipmentStatusLogAccess;
    private final PaymentMethodAccess paymentMethodAccess;
    private final TransactionAccess transactionAccess;
    private final OrderLiveSyncService orderLiveSyncService;

    @Transactional
    public PlaceOrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {
        Cart cart = cartAccess.getActiveCartByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No active cart exists for this customer"));

        List<CartItem> cartItems = cartItemAccess.getAllCartItems(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cart is empty");
        }

        PaymentMethodModel paymentMethod = getPaymentMethodByName(request.getPaymentMethod());
        BigDecimal totalAmount = calculateTotalAmountAndValidateInventory(cartItems);

        int orderId = orderAccess.createOrder(
                customerId,
                OrderStatus.AWAITING_PAYMENT,
                totalAmount
        );

        for (CartItem cartItem : cartItems) {
            orderItemAccess.addOrderItemToOrder(
                    orderId,
                    cartItem.getProductId(),
                    cartItem.getQuantity()
            );
        }

        updateInventoryAfterOrder(cartItems);

        int shipmentId = shipmentAccess.createShipment(
                orderId,
                request.getShippingMethod(),
                request.getShippingAddress()
        );

        shipmentStatusLogAccess.createShipmentStatusLog(
                shipmentId,
                ShipmentStatus.AWAITING_PAYMENT
        );

        TransactionModel transaction = TransactionModel.builder()
                .orderId(orderId)
                .paymentMethodId(paymentMethod.getPaymentMethodId())
                .amount(totalAmount)
                .status("PENDING")
                .build();

        int transactionId = transactionAccess.insert(transaction);

        cartAccess.updateCartStatus(cart.getCartId(), CartStatus.CONVERTED);

        return PlaceOrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.AWAITING_PAYMENT.name())
                .shipmentId(shipmentId)
                .transactionId(transactionId)
                .transactionStatus("PENDING")
                .totalAmount(totalAmount)
                .build();
    }

        @Transactional
    public void confirmOrderPayment(int orderId, int transactionId) {
        Order order = orderAccess.getOrderById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Order does not exist"
                ));

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only orders awaiting payment can be confirmed"
            );
        }

        TransactionModel transaction = transactionAccess.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Transaction does not exist"
                ));

        if (transaction.getOrderId() != orderId) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Transaction does not belong to this order"
            );
        }

        transactionAccess.update(
                transactionId,
                transaction.getAmount(),
                "SUCCESS",
                transaction.getPaymentMethodId()
        );

        orderAccess.updateOrderStatus(orderId, OrderStatus.CONFIRMED);

        UUID customerId = order.getCustomerId();
        List<OrderItem> orderItems = orderItemAccess.getAllOrderItems(orderId);

        for (OrderItem item : orderItems) {
            ProductModel product = productAccess.findAllByIds(List.of(item.getProductId()))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Product does not exist for order item"
                    ));
            orderLiveSyncService.syncOrderToGraph(
                    orderId,
                    customerId.toString(),
                    item.getProductId(),
                    product.getProductName(),
                    item.getQuantity()
            );
        }

        Shipment shipment = shipmentAccess.getShipmentByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No shipment found for this order"
                ));

        shipmentStatusLogAccess.createShipmentStatusLog(
                shipment.getShipmentId(),
                ShipmentStatus.DELIVERED
        );
    }

    private BigDecimal calculateTotalAmountAndValidateInventory(List<CartItem> cartItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            ProductModel product = productAccess.findAllByIds(List.of(cartItem.getProductId()))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Product does not exist"));

            InventoryModel inventory = inventoryAccess.findByProductId(cartItem.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Inventory does not exist for product"));

            if (inventory.getQuantityAvailable() < cartItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Not enough inventory for product id: " + cartItem.getProductId());
            }

            BigDecimal itemTotal = product.getBasePrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);
        }

        return totalAmount;
    }

    private void updateInventoryAfterOrder(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            InventoryModel inventory = inventoryAccess.findByProductId(cartItem.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Inventory does not exist for product"));

            int newQuantityAvailable = inventory.getQuantityAvailable() - cartItem.getQuantity();
            int newQuantityReserved = inventory.getQuantityReserved() + cartItem.getQuantity();

            inventoryAccess.updateQuantitiesByProductId(
                    cartItem.getProductId(),
                    newQuantityAvailable,
                    newQuantityReserved
            );
        }
    }

    private PaymentMethodModel getPaymentMethodByName(String paymentMethodName) {
        if (paymentMethodName == null || paymentMethodName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment method is required");
        }

        return paymentMethodAccess.findAll()
                .stream()
                .filter(paymentMethod ->
                        paymentMethod.getProvider().equalsIgnoreCase(paymentMethodName)
                                || paymentMethod.getType().equalsIgnoreCase(paymentMethodName)
                )
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Payment method does not exist"));
    }
}