package tn.shoptyn.order.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
