package app.domain.menu.dto.request;

public record MenuCreateRequest(
	String name,
	String desc,
	Long price
) {

}
