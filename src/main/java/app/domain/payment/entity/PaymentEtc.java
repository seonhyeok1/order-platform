package app.domain.payment.entity;

import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_payment_etc")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentEtc extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID paymentEtcId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Lob
    @Column(nullable = false)
    private String paymentResponse; // JSON 문자열
}