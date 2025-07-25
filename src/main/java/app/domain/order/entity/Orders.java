package app.domain.order.entity;

import app.domain.order.entity.enums.OrderChannel;
import app.domain.order.entity.enums.OrderStatus;
import app.domain.order.entity.enums.PaymentMethod;
import app.domain.order.entity.enums.ReceiptMethod;
import app.domain.store.entity.Store;
import app.domain.user.entity.User;
import app.global.entity.BaseEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "p_b_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID OrdersId;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // nullable (오프라인 주문 고려)

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private String deliveryAddress; // 오프라인,포장은 "없음"

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderChannel OrderChannel;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReceiptMethod receiptMethod;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus OrderStatus;

    @Lob
    private String requestMessage;
}