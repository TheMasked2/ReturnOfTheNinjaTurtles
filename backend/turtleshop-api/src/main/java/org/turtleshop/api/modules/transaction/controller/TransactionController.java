package org.turtleshop.api.modules.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.transaction.dto.TransactionCreateRequest;
import org.turtleshop.api.modules.transaction.dto.TransactionResponse;
import org.turtleshop.api.modules.transaction.dto.TransactionUpdateRequest;
import org.turtleshop.api.modules.transaction.model.TransactionModel;
import org.turtleshop.api.modules.transaction.service.TransactionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.listTransactions(page, size).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable int transactionId) {
        return ResponseEntity.ok(toTransactionResponse(transactionService.getTransactionById(transactionId)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByOrderId(@PathVariable int orderId) {
        return ResponseEntity.ok(transactionService.getTransactionsByOrderId(orderId).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/payment-method/{paymentMethodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPaymentMethod(
            @PathVariable int paymentMethodId) {
        return ResponseEntity.ok(transactionService.getTransactionsByPaymentMethod(paymentMethodId).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transactionService.getTransactionsByStatus(status).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createTransaction(@RequestBody TransactionCreateRequest request) {
        TransactionModel transaction = TransactionModel.builder()
                .orderId(request.getOrderId())
                .paymentMethodId(request.getPaymentMethodId())
                .amount(request.getAmount())
                .status(request.getStatus())
                .build();
        transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTransaction(@PathVariable int transactionId,
                                                  @RequestBody TransactionUpdateRequest request) {
        transactionService.updateTransaction(transactionId, request.getAmount(),
                request.getStatus(), request.getPaymentMethodId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable int transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }

    private TransactionResponse toTransactionResponse(TransactionModel model) {
        return TransactionResponse.builder()
                .transactionId(model.getTransactionId())
                .orderId(model.getOrderId())
                .paymentMethodId(model.getPaymentMethodId())
                .amount(model.getAmount())
                .status(model.getStatus())
                .transactionDate(model.getTransactionDate())
                .build();
    }
}