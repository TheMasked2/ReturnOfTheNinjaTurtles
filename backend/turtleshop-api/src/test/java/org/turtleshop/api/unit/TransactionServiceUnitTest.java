package org.turtleshop.api.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.transaction.repository.TransactionAccess;
import org.turtleshop.api.modules.transaction.service.TransactionService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private TransactionAccess transactionAccess;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getTransactionById_whenTransactionDoesNotExist_shouldThrowNotFound() {
        when(transactionAccess.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transaction not found");
    }
}