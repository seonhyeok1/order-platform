package app.domain.payment.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.payment.model.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}