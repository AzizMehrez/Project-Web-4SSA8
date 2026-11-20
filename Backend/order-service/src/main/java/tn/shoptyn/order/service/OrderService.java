package tn.shoptyn.order.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.shoptyn.order.client.ProductClient;
import tn.shoptyn.order.client.UserClient;
import tn.shoptyn.order.domain.Cart;
import tn.shoptyn.order.domain.CartItem;
import tn.shoptyn.order.domain.Order;
import tn.shoptyn.order.domain.OrderItem;
import tn.shoptyn.order.dto.CreateOrderRequest;
import tn.shoptyn.order.dto.OrderItemResponse;
import tn.shoptyn.order.dto.OrderResponse;
import tn.shoptyn.order.messaging.OrderEventPublisher;
import tn.shoptyn.order.repository.CartRepository;
import tn.shoptyn.order.repository.OrderRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final OrderEventPublisher eventPublisher;

    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        String shippingAddress = resolveShippingAddress(userId, request.shippingAddress());

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);

        for (CartItem cartItem : cart.getItems()) {
            ProductClient.ProductDto product = requireAvailableProduct(cartItem.getProductId(), cartItem.getQuantity());
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setShopId(product.shopId());
            item.setUnitPrice(product.price());
            item.setQuantity(cartItem.getQuantity());
            order.getItems().add(item);
        }

        order.setTotalAmount(order.calculateTotal());
        Order saved = orderRepository.save(order);

        cart.clear();
        cartRepository.save(cart);

        eventPublisher.publishOrderCreated(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, String userId, boolean admin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!admin && !order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view this order");
        }
        return toResponse(order);
    }

    public OrderResponse cancelOrder(Long id, String userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to cancel this order");
        }
        if (!order.canBeCancelled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already paid or cannot be cancelled");
        }
        order.cancel();
        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getVendorOrders(Long shopId) {
        return orderRepository.findOrdersForShop(shopId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String resolveShippingAddress(String userId, String requestedAddress) {
        if (requestedAddress != null && !requestedAddress.isBlank()) {
            return requestedAddress;
        }
        try {
            UserClient.UserDto user = userClient.getUser(userId);
            if (user != null && user.defaultAddress() != null && !user.defaultAddress().isBlank()) {
                return user.defaultAddress();
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service unavailable", ex);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping address is required");
    }

    private ProductClient.ProductDto requireAvailableProduct(Long productId, int quantity) {
        ProductClient.ProductDto product;
        try {
            product = productClient.getProduct(productId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog service unavailable", ex);
        }
        if (product == null || !product.active()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product unavailable");
        }
        if (product.stockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }
        return product;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getShopId(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.lineTotal()
                ))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                items
        );
    }
}
