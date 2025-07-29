package app.domain.ai.model.dto.request;

import app.domain.ai.model.entity.enums.ReqType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiRequest(
	@NotBlank(message = "가게 이름은 필수입니다.")
	String storeName,

	String menuName,

	@NotNull(message = "요청 타입은 필수입니다.")
	ReqType reqType,

	@NotBlank(message = "프롬프트 텍스트는 필수입니다.")
	String promptText
) {
	// 필요한 경우 레코드의 컴팩트 생성자를 정의할 수 있습니다.
}