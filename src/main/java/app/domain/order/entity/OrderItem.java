package app.domain.order.entity;

import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "p_b_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID OrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_order_id", nullable = false)
    private Orders orders;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;
}