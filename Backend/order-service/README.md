# Order Service (MS2)

Spring Boot microservice for **carts and orders** in ShopyTn.

- **Port:** `8083`
- **Eureka name:** `ORDER`
- **Database:** `order_db` (MySQL)
- **Gateway routes:** `/api/cart/**`, `/api/orders/**`

## Main endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/cart` | Get current user cart |
| POST | `/api/cart/items` | Add item |
| PATCH | `/api/cart/items/{productId}` | Update quantity |
| DELETE | `/api/cart/items/{productId}` | Remove item |
| POST | `/api/orders` | Create order from cart |
| GET | `/api/orders/me` | List my orders |
| GET | `/api/orders/{id}` | Order detail |
| PATCH | `/api/orders/{id}/cancel` | Cancel unpaid order |
| GET | `/api/orders/vendor/me` | Vendor received orders |

## Events

- Publishes: `ORDER_CREATED` (`order.created`), `ORDER_CANCELLED` (`order.cancelled`)
- Consumes: `PAYMENT_COMPLETED`, `PAYMENT_FAILED`, `DELIVERY_UPDATED`

## Run locally

```bash
# Ensure MySQL order_db + RabbitMQ + Eureka are available
./mvnw spring-boot:run
```
