package app.domain.store.entity;

import app.domain.menu.entity.Category;
import app.domain.user.entity.User;
import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "p_store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id", nullable = false)
    private Category categoryId;

    @Column(nullable = false, length = 100)
    private String storeName;

    @Column()
    private String desc;

    @Column(nullable = false)
    private String address;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private int minOrderAmount;

    @Column(nullable = false, length = 20)
    private String storeAcceptStatus;
}