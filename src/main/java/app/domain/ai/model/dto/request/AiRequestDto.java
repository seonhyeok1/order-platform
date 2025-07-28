package app.domain.ai.model.dto.request;

import app.domain.ai.model.entity.enums.ReqType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRequestDto {

	private String storeName;
	private String menuName;
	private ReqType reqType;
	private String promptText;
}