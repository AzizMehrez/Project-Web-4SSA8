package tn.shoptyn.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        Long shopId,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
