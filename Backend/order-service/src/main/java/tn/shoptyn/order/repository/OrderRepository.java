package tn.shoptyn.order.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.shoptyn.order.domain.Order;
import tn.shoptyn.order.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
            select distinct o from Order o
            join o.items i
            where i.shopId = :shopId
            order by o.createdAt desc
            """)
    List<Order> findOrdersForShop(@Param("shopId") Long shopId);
}
