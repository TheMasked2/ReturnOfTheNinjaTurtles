package org.turtleshop.api.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.cart.dto.AddCartItemRequest;
import org.turtleshop.api.modules.cart.dto.CartItemResponse;
import org.turtleshop.api.modules.cart.dto.CartResponse;
import org.turtleshop.api.modules.cart.enums.CartStatus;
import org.turtleshop.api.modules.cart.model.Cart;
import org.turtleshop.api.modules.cart.model.CartItem;
import org.turtleshop.api.modules.cart.repository.CartAccess;
import org.turtleshop.api.modules.cart.repository.CartItemAccess;
import org.turtleshop.api.modules.cart.service.CartService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @Mock
    private CartAccess cartAccess;

    @Mock
    private CartItemAccess cartItemAccess;

    @InjectMocks
    private CartService cartService;

    @Test
    void createCart_whenCustomerAlreadyHasActiveCart_shouldThrowConflict() {
        UUID customerId = UUID.randomUUID();
        when(cartAccess.getActiveCartByCustomerId(customerId))
                .thenReturn(Optional.of(Cart.builder().cartId(1).customerId(customerId).status(CartStatus.ACTIVE).build()));

        assertThatThrownBy(() -> cartService.createCart(customerId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Customer already has an active cart");

        verify(cartAccess, never()).insertCart(customerId);
    }

    @Test
    void createCart_whenNoActiveCartExists_shouldInsertAndReturnEmptyCartResponse() {
        UUID customerId = UUID.randomUUID();
        Cart createdCart = Cart.builder()
                .cartId(15)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(cartAccess.getActiveCartByCustomerId(customerId)).thenReturn(Optional.empty());
        when(cartAccess.insertCart(customerId)).thenReturn(15);
        when(cartAccess.getCartById(15)).thenReturn(Optional.of(createdCart));
        when(cartItemAccess.getAllCartItems(15)).thenReturn(List.of());

        CartResponse response = cartService.createCart(customerId);

        assertThat(response.getCartId()).isEqualTo(15);
        assertThat(response.getCustomerId()).isEqualTo(customerId);
        assertThat(response.getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void addItemToCart_whenActiveCartExists_shouldInsertItemAndReturnMappedResponse() {
        UUID customerId = UUID.randomUUID();
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(7);
        request.setQuantity(3);
        Cart cart = Cart.builder().cartId(4).customerId(customerId).status(CartStatus.ACTIVE).build();
        CartItem cartItem = CartItem.builder().cartItemId(55).cartId(4).productId(7).quantity(3).build();

        when(cartAccess.getActiveCartByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(cartItemAccess.insertCartItem(4, 7, 3)).thenReturn(55);
        when(cartItemAccess.getCartItemById(55)).thenReturn(Optional.of(cartItem));

        CartItemResponse response = cartService.addItemToCart(customerId, request);

        assertThat(response.getCartItemId()).isEqualTo(55);
        assertThat(response.getProductId()).isEqualTo(7);
        assertThat(response.getQuantity()).isEqualTo(3);
    }

    @Test
    void changeQuantityOfCartItem_whenItemExists_shouldUpdateQuantity() {
        when(cartItemAccess.getCartItemById(12))
                .thenReturn(Optional.of(CartItem.builder().cartItemId(12).quantity(1).build()));

        cartService.changeQuantityOfCartItem(12, 5);

        verify(cartItemAccess).updateCartItemQuantity(12, 5);
    }
}
