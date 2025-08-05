package app.domain.customer.dto.response;

import java.util.UUID;

import app.domain.store.model.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerStoreDetailResponse {

	private UUID storeId;
	private String storeName;
	private String description;
	private String address;
	private String phoneNumber;
	private long minOrderAmount;
	private String categoryName;
	private double averageRating;

	public GetCustomerStoreDetailResponse() {
	}

	public GetCustomerStoreDetailResponse(UUID storeId, String storeName, String description, String address, String phoneNumber, long minOrderAmount, String categoryName, double averageRating) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.description = description;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.minOrderAmount = minOrderAmount;
		this.categoryName = categoryName;
		this.averageRating = averageRating;
	}

	public static GetCustomerStoreDetailResponse from(Store store, double avgRating) {
		return GetCustomerStoreDetailResponse.builder()
			.storeId(store.getStoreId())
			.storeName(store.getStoreName())
			.description(store.getDescription())
			.address(store.getAddress())
			.phoneNumber(store.getPhoneNumber())
			.minOrderAmount(store.getMinOrderAmount())
			.categoryName(store.getCategory().getCategoryName())
			.averageRating(avgRating)
			.build();
	}
}