package org.turtleshop.api.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.cart.enums.CartStatus;
import org.turtleshop.api.modules.cart.model.Cart;
import org.turtleshop.api.modules.cart.model.CartItem;
import org.turtleshop.api.modules.cart.repository.CartAccess;
import org.turtleshop.api.modules.cart.repository.CartItemAccess;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.checkout.service.CheckoutService;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.repository.InventoryAccess;
import org.turtleshop.api.modules.order.enums.OrderStatus;
import org.turtleshop.api.modules.order.model.Order;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.order.repository.OrderItemAccess;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceUnitTest {

    @Mock
    private CartAccess cartAccess;
    @Mock
    private CartItemAccess cartItemAccess;
    @Mock
    private OrderAccess orderAccess;
    @Mock
    private OrderItemAccess orderItemAccess;
    @Mock
    private ProductAccess productAccess;
    @Mock
    private InventoryAccess inventoryAccess;
    @Mock
    private ShipmentAccess shipmentAccess;
    @Mock
    private ShipmentStatusLogAccess shipmentStatusLogAccess;
    @Mock
    private PaymentMethodAccess paymentMethodAccess;
    @Mock
    private TransactionAccess transactionAccess;

    @InjectMocks
    private CheckoutService checkoutService;

    @Test
    void placeOrder_whenCartIsEmpty_shouldThrowConflict() {
        UUID customerId = UUID.randomUUID();
        when(cartAccess.getActiveCartByCustomerId(customerId))
                .thenReturn(Optional.of(Cart.builder().cartId(1).customerId(customerId).status(CartStatus.ACTIVE).build()));
        when(cartItemAccess.getAllCartItems(1)).thenReturn(List.of());

        assertThatThrownBy(() -> checkoutService.placeOrder(customerId, validRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void placeOrder_whenEverythingIsValid_shouldCreateOrderItemsShipmentTransactionAndConvertCart() {
        UUID customerId = UUID.randomUUID();
        Cart cart = Cart.builder().cartId(1).customerId(customerId).status(CartStatus.ACTIVE).build();
        CartItem cartItem = CartItem.builder().cartItemId(10).cartId(1).productId(7).quantity(2).build();
        ProductModel product = new ProductModel();
        product.setProductId(7);
        product.setBasePrice(new BigDecimal("12.50"));
        PaymentMethodModel paymentMethod = PaymentMethodModel.builder()
                .paymentMethodId(3)
                .provider("Visa")
                .type("Credit Card")
                .build();

        when(cartAccess.getActiveCartByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(cartItemAccess.getAllCartItems(1)).thenReturn(List.of(cartItem));
        when(paymentMethodAccess.findAll()).thenReturn(List.of(paymentMethod));
        when(productAccess.findById(7)).thenReturn(Optional.of(product));
        when(inventoryAccess.findByProductId(7))
                .thenReturn(Optional.of(InventoryModel.builder().productId(7).quantityAvailable(5).quantityReserved(1).build()));
        when(orderAccess.createOrder(customerId, OrderStatus.AWAITING_PAYMENT, new BigDecimal("25.00"))).thenReturn(100);
        when(shipmentAccess.createShipment(100, "PostNL", "Rotterdam")).thenReturn(200);
        when(transactionAccess.insert(any(TransactionModel.class))).thenReturn(300);

        PlaceOrderResponse response = checkoutService.placeOrder(customerId, validRequest());

        verify(orderItemAccess).addOrderItemToOrder(100, 7, 2);
        verify(inventoryAccess).updateQuantitiesByProductId(7, 3, 3);
        verify(shipmentStatusLogAccess).createShipmentStatusLog(200, ShipmentStatus.AWAITING_PAYMENT);
        verify(cartAccess).updateCartStatus(1, CartStatus.CONVERTED);
        assertThat(response.getOrderId()).isEqualTo(100);
        assertThat(response.getShipmentId()).isEqualTo(200);
        assertThat(response.getTransactionId()).isEqualTo(300);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("25.00");
        assertThat(response.getOrderStatus()).isEqualTo("AWAITING_PAYMENT");
    }

    @Test
    void confirmOrderPayment_whenOrderIsAwaitingPayment_shouldMarkTransactionSuccessAndOrderConfirmed() {
        when(orderAccess.getOrderById(100)).thenReturn(Optional.of(
                Order.builder().orderId(100).status(OrderStatus.AWAITING_PAYMENT).build()
        ));
        when(transactionAccess.findById(300)).thenReturn(Optional.of(
                TransactionModel.builder()
                        .transactionId(300)
                        .orderId(100)
                        .paymentMethodId(3)
                        .amount(new BigDecimal("25.00"))
                        .status("PENDING")
                        .build()
        ));
        when(shipmentAccess.getShipmentByOrderId(100)).thenReturn(Optional.of(
                Shipment.builder().shipmentId(200).orderId(100).build()
        ));

        checkoutService.confirmOrderPayment(100, 300);

        verify(transactionAccess).update(300, new BigDecimal("25.00"), "SUCCESS", 3);
        verify(orderAccess).updateOrderStatus(100, OrderStatus.CONFIRMED);
        verify(shipmentStatusLogAccess).createShipmentStatusLog(200, ShipmentStatus.READY_TO_SHIP);
    }

    @Test
    void confirmOrderPayment_whenTransactionBelongsToDifferentOrder_shouldThrowConflict() {
        when(orderAccess.getOrderById(100)).thenReturn(Optional.of(
                Order.builder().orderId(100).status(OrderStatus.AWAITING_PAYMENT).build()
        ));
        when(transactionAccess.findById(300)).thenReturn(Optional.of(
                TransactionModel.builder().transactionId(300).orderId(999).amount(BigDecimal.TEN).paymentMethodId(1).build()
        ));

        assertThatThrownBy(() -> checkoutService.confirmOrderPayment(100, 300))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transaction does not belong to this order");
    }

    private PlaceOrderRequest validRequest() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Rotterdam");
        request.setPaymentMethod("Visa");
        return request;
    }
}
