package app.domain.customer.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import app.domain.order.model.entity.Orders;
import app.domain.order.model.entity.enums.OrderChannel;
import app.domain.order.model.entity.enums.OrderStatus;
import app.domain.order.model.entity.enums.PaymentMethod;
import app.domain.order.model.entity.enums.ReceiptMethod;

public record CustomerOrderResponse(
    UUID ordersId,
    String storeName,
    Long totalPrice,
    String deliveryAddress,
    PaymentMethod paymentMethod,
    OrderChannel orderChannel,
    ReceiptMethod receiptMethod,
    OrderStatus orderStatus,
    boolean isRefundable,
    String orderHistory,
    String requestMessage,
    LocalDateTime createdAt
) {
    public static CustomerOrderResponse of(Orders orders) {
        return new CustomerOrderResponse(
            orders.getOrdersId(),
            orders.getStore().getStoreName(),
            orders.getTotalPrice(),
            orders.getDeliveryAddress(),
            orders.getPaymentMethod(),
            orders.getOrderChannel(),
            orders.getReceiptMethod(),
            orders.getOrderStatus(),
            orders.isRefundable(),
            orders.getOrderHistory(),
            orders.getRequestMessage(),
            orders.getCreatedAt()
        );
    }
}
