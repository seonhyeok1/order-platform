package app.domain.ai.entity;

import app.domain.ai.entity.enums.AiRequestStatus;
import app.domain.ai.entity.enums.ReqType;
import app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "p_ai_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiRequest extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID aiRequestId;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private String menuName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReqType reqType; // 메뉴 설명, 가게 설명

    @Lob
    @Column(nullable = false)
    private String promptText;

    @Lob
    private String generatedContent;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiRequestStatus status;
}