package app.domain.manager.dto.response;

import java.util.UUID;

import app.domain.store.model.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 리스트 응답 DTO")
public record GetStoreListResponse(
	UUID storeId,
	String storeName,
	String address,
	long minOrderAmount,
	double averageRating
) {
	public static GetStoreListResponse from(Store store, double averageRating) {
		return new GetStoreListResponse(
			store.getStoreId(),
			store.getStoreName(),
			store.getAddress(),
			store.getMinOrderAmount(),
			averageRating
		);
	}
}
