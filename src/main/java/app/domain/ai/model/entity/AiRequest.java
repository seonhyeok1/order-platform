package app.domain.ai.model.entity;

import java.util.UUID;

import app.domain.ai.model.entity.enums.AiRequestStatus;
import app.domain.ai.model.entity.enums.ReqType;
import app.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	public void updateGeneratedContent(String generatedContent, AiRequestStatus status) {
		this.generatedContent = generatedContent;
		this.status = status;
	}
}
