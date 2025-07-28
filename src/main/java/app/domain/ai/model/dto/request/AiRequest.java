package app.domain.ai.model.dto.request;

import app.domain.ai.model.entity.enums.ReqType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConditionalNotNull
public class AiRequest {

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String storeName;

    private String menuName;

    @NotNull(message = "요청 타입은 필수입니다.")
    private ReqType reqType;

    @NotBlank(message = "프롬프트 텍스트는 필수입니다.")
    private String promptText;
}