package app.domain.manager.dto.response;

import java.util.UUID;

import app.domain.store.model.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 상세 응답 DTO")
public record GetStoreDetailResponse(
	UUID storeId,
	String storeName,
	String description,
	String address,
	String phoneNumber,
	long minOrderAmount,
	String regionName,
	String categoryName,
	double averageRating
) {
	public static GetStoreDetailResponse from(Store store, double avgRating) {
		return new GetStoreDetailResponse(
			store.getStoreId(),
			store.getStoreName(),
			store.getDescription(),
			store.getAddress(),
			store.getPhoneNumber(),
			store.getMinOrderAmount(),
			store.getRegion().getRegionName(),
			store.getCategory().getCategoryName(),
			avgRating
		);
	}
}
