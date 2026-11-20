package tn.shoptyn.order.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CATALOG")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    record ProductDto(
            Long id,
            String name,
            Long shopId,
            BigDecimal price,
            int stockQuantity,
            boolean active
    ) {
    }
}
