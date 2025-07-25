package app.domain.payment.model.entity;

import java.util.UUID;

import app.domain.payment.model.entity.enums.PaymentStatus;
import app.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

	@Column(nullable = false, length = 50)
	private String pgCode;

	@Column(nullable = false)
	private int resultCode;

	@Column(nullable = false)
	private String tid;

	@Column(nullable = false, unique = true)
	private UUID ordersId;

	@Column(nullable = false)
	private String signature;

	@Column(nullable = false, length = 50)
	private String paymentMethod;

	@Column(nullable = false)
	private int amount;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;
}