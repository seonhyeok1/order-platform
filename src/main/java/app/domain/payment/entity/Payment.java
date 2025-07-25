package app.domain.payment.entity;

import app.domain.order.entity.Orders;
import app.domain.payment.entity.enums.PaymentStatus;
import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID paymentId;

    @Column(nullable = false)
    private String pgCode;

    @Column(nullable = false)
    private Integer resultCode;

    @Column(nullable = false)
    private String tid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_order_id", nullable = false, unique = true)
    private Orders orders;

    @Column(nullable = false)
    private String signature;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}