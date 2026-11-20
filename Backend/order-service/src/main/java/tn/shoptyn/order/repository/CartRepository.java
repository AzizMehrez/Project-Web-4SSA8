package tn.shoptyn.order.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.shoptyn.order.domain.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(String userId);
}
