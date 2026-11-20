package tn.shoptyn.order.messaging;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.shoptyn.order.domain.Order;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${shoptyn.rabbitmq.exchange}")
    private String exchange;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getItems().stream()
                        .map(item -> new OrderLine(
                                item.getProductId(),
                                item.getProductName(),
                                item.getShopId(),
                                item.getUnitPrice(),
                                item.getQuantity()
                        ))
                        .toList()
        );
        rabbitTemplate.convertAndSend(exchange, "order.created", event);
    }

    public void publishOrderCancelled(Order order) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                order.getOrderNumber(),
                order.getUserId()
        );
        rabbitTemplate.convertAndSend(exchange, "order.cancelled", event);
    }

    public record OrderLine(
            Long productId,
            String productName,
            Long shopId,
            BigDecimal unitPrice,
            int quantity
    ) {
    }

    public record OrderCreatedEvent(
            String eventId,
            Long orderId,
            String orderNumber,
            String userId,
            BigDecimal totalAmount,
            String shippingAddress,
            List<OrderLine> items
    ) {
    }

    public record OrderCancelledEvent(
            String eventId,
            Long orderId,
            String orderNumber,
            String userId
    ) {
    }
}
