package app.domain.menu.model.dto.request;

public record MenuCreateRequest(
	String name,
	String desc,
	Long price
) {

}
