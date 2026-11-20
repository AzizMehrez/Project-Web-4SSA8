package tn.shoptyn.order.web;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.shoptyn.order.dto.CreateOrderRequest;
import tn.shoptyn.order.dto.OrderResponse;
import tn.shoptyn.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(userId, request);
    }

    @GetMapping("/me")
    public List<OrderResponse> myOrders(@RequestHeader("X-User-Id") String userId) {
        return orderService.getMyOrders(userId);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "CLIENT") String role
    ) {
        boolean admin = "ADMIN".equalsIgnoreCase(role);
        return orderService.getOrder(id, userId, admin);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId
    ) {
        return orderService.cancelOrder(id, userId);
    }

    @GetMapping("/vendor/me")
    public List<OrderResponse> vendorOrders(@RequestHeader("X-Shop-Id") Long shopId) {
        return orderService.getVendorOrders(shopId);
    }
}
