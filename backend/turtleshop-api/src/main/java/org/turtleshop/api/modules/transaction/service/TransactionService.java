package org.turtleshop.api.modules.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.transaction.model.TransactionModel;
import org.turtleshop.api.modules.transaction.repository.TransactionAccess;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionAccess TransactionAccess;

    public List<TransactionModel> listTransactions(int page, int size) {
        return TransactionAccess.findAll(page * size, size);
    }

    public TransactionModel getTransactionById(int transactionId) {
        return TransactionAccess.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Transaction not found: " + transactionId));
    }

    public List<TransactionModel> getTransactionsByOrderId(int orderId) {
        return TransactionAccess.findByOrderId(orderId);
    }

    public List<TransactionModel> getTransactionsByPaymentMethod(int paymentMethodId) {
        return TransactionAccess.findByPaymentMethodId(paymentMethodId);
    }

    public List<TransactionModel> getTransactionsByStatus(String status) {
        return TransactionAccess.findByStatus(status);
    }

    public int createTransaction(TransactionModel transaction) {
        validateAmount(transaction.getAmount());
        return TransactionAccess.insert(transaction);
    }

    public void updateTransaction(int transactionId, BigDecimal amount, String status, Integer paymentMethodId) {
        validateAmount(amount);
        getTransactionById(transactionId);
        TransactionAccess.update(transactionId, amount, status, paymentMethodId);
    }

    public void deleteTransaction(int transactionId) {
        getTransactionById(transactionId);
        TransactionAccess.deleteById(transactionId);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount must be zero or positive");
        }
    }
}