package tn.shoptyn.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.shoptyn.order.client.ProductClient;
import tn.shoptyn.order.domain.Cart;
import tn.shoptyn.order.domain.CartItem;
import tn.shoptyn.order.dto.AddCartItemRequest;
import tn.shoptyn.order.dto.CartItemResponse;
import tn.shoptyn.order.dto.CartResponse;
import tn.shoptyn.order.dto.UpdateCartItemRequest;
import tn.shoptyn.order.repository.CartRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    public CartResponse addItem(String userId, AddCartItemRequest request) {
        ProductClient.ProductDto product = requireAvailableProduct(request.productId(), request.quantity());
        Cart cart = getOrCreateCart(userId);
        cart.addItem(product.id(), request.quantity());
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse updateItem(String userId, Long productId, UpdateCartItemRequest request) {
        requireAvailableProduct(productId, request.quantity());
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        item.setQuantity(request.quantity());
        return toResponse(cartRepository.save(cart));
    }

    public void removeItem(String userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        });
    }

    private ProductClient.ProductDto requireAvailableProduct(Long productId, int quantity) {
        ProductClient.ProductDto product;
        try {
            product = productClient.getProduct(productId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog service unavailable", ex);
        }
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        if (!product.active()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is inactive");
        }
        if (product.stockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }
        return product;
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            ProductClient.ProductDto product = productClient.getProduct(item.getProductId());
            BigDecimal line = product.price().multiply(BigDecimal.valueOf(item.getQuantity()));
            items.add(new CartItemResponse(
                    product.id(),
                    product.name(),
                    item.getQuantity(),
                    product.price(),
                    line
            ));
            total = total.add(line);
        }
        return new CartResponse(cart.getId(), cart.getUserId(), items, total);
    }
}
