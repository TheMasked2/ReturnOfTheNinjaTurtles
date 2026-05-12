package org.turtleshop.api.modules.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.transaction.dto.PaymentMethodCreateRequest;
import org.turtleshop.api.modules.transaction.model.PaymentMethodModel;
import org.turtleshop.api.modules.transaction.repository.PaymentMethodAccess;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodAccess PaymentMethodAccess;

    public List<PaymentMethodModel> listPaymentMethods() {
        return PaymentMethodAccess.findAll();
    }

    public PaymentMethodModel getPaymentMethodById(int paymentMethodId) {
        return PaymentMethodAccess.findById(paymentMethodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment method not found: " + paymentMethodId));
    }

    public int createPaymentMethod(PaymentMethodCreateRequest request) {
        PaymentMethodModel paymentMethod = PaymentMethodModel.builder()
                .provider(request.getProvider())
                .type(request.getType())
                .build();
        return PaymentMethodAccess.insert(paymentMethod);
    }
}