package app.domain.store.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.domain.order.model.entity.Orders;
import app.domain.order.model.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOrderListResponse {

    private UUID storeId;
    private List<StoreOrderDetail> orderList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreOrderDetail {
        private UUID orderId;
        private String customerName; // Assuming customer name can be retrieved from Order or User
        private Long totalPrice;
        private OrderStatus orderStatus;
        private LocalDateTime orderedAt;

        public static StoreOrderDetail from(Orders order) {
            return StoreOrderDetail.builder()
                .orderId(order.getOrdersId())
                .customerName(order.getUser().getUsername()) // Assuming Orders has a User and User has a username
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .orderedAt(order.getCreatedAt())
                .build();
        }
    }
}
