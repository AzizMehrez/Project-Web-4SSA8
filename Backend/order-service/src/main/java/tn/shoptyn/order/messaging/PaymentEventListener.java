package tn.shoptyn.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.shoptyn.order.domain.Order;
import tn.shoptyn.order.domain.OrderStatus;
import tn.shoptyn.order.repository.OrderRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = "${shoptyn.rabbitmq.queues.payment-completed:order.payment-completed}")
    @Transactional
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.confirm();
                log.info("Order {} confirmed after payment", order.getOrderNumber());
            }
        });
    }

    @RabbitListener(queues = "${shoptyn.rabbitmq.queues.payment-failed:order.payment-failed}")
    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.cancel();
                log.info("Order {} cancelled after payment failure", order.getOrderNumber());
            }
        });
    }

    @RabbitListener(queues = "${shoptyn.rabbitmq.queues.delivery-updated:order.delivery-updated}")
    @Transactional
    public void onDeliveryUpdated(DeliveryUpdatedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        switch (event.status()) {
            case "PICKED_UP", "IN_TRANSIT" -> order.setStatus(OrderStatus.SHIPPED);
            case "DELIVERED" -> order.setStatus(OrderStatus.DELIVERED);
            default -> log.debug("Ignoring delivery status {} for order {}", event.status(), event.orderId());
        }
    }

    public record PaymentCompletedEvent(String eventId, Long orderId) {
    }

    public record PaymentFailedEvent(String eventId, Long orderId) {
    }

    public record DeliveryUpdatedEvent(String eventId, Long orderId, String status) {
    }
}
