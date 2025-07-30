package app.domain.owner.model.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record StoreApproveRequest(
	@NotNull UUID userId,
	@NotNull UUID regionId,
	@NotNull String address,
	@NotNull String storeName,
	String desc,
	String phoneNumber,
	@NotNull Long minOrderAmount
) {
}
