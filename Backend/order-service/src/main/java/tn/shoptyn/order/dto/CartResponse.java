package tn.shoptyn.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        String userId,
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
}
