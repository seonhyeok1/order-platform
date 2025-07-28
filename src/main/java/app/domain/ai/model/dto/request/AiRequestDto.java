package app.domain.ai.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRequestDto {

	private String store_name;
	private String menu_name;
	private ReqType req_type;
	private String prompt_text;

	public enum ReqType {
		STORE_DESCRIPTION("가게 설명"),
		MENU_DESCRIPTION("메뉴 설명");

		private final String description;

		ReqType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
}
