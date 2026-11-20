package tn.shoptyn.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import tn.shoptyn.order.domain.OrderStatus;

public record OrderResponse(
        Long id,
        String orderNumber,
        String userId,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
