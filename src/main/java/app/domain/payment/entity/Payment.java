package app.domain.payment.entity;

import java.util.UUID;

import app.domain.order.entity.Orders;
import app.domain.payment.entity.enums.PaymentStatus;
import app.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	@JoinColumn(name = "orders_id", nullable = false, unique = true)
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