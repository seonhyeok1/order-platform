package app.domain.order.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.order.model.OrdersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundUpdateService {

    private final OrdersRepository ordersRepository;

    @Transactional
    public void updateRefundableStatus(UUID orderId) {
        try {
            int updatedRows = ordersRepository.updateRefundableStatus(orderId, false);
            if (updatedRows > 0) {
                log.info("주문 {} 환불 불가 상태로 변경 완료", orderId);
            } else {
                log.warn("주문 {}를 찾을 수 없어 환불 불가 처리를 건너뜁니다", orderId);
            }
        } catch (Exception e) {
            log.error("주문 {} 환불 불가 처리 실패", orderId, e);
        }
    }
}