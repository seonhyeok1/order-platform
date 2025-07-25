package app.domain.store.entity;

import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Region extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID regionId;

    @Column(unique = true, nullable = false)
    private Integer regionCode;

    @Column(nullable = false)
    private boolean isActive = false;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 20)
    private String sido; // 시도 (예: 서울특별시, 경기도 등)

    @Column(length = 30)
    private String sigungu; // 시군구 (예: 강남구, 수원시 등)

    @Column(length = 30)
    private String eupmyendong; // 읍면동 (예: 역삼동, 신길동 등)
}