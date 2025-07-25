package app.domain.review.entity;

import app.domain.order.entity.Orders;
import app.domain.user.entity.User;
import app.domain.store.entity.Store;
import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID reviewId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_order_id",nullable = false)
    private Orders Orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String context;
}