package app.domain.manager.dto.response;

import java.util.UUID;

import app.domain.store.model.entity.Store;
import app.domain.user.model.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreDetailResponse {
	private UUID storeId;
	private String storeName;
	private String description;
	private String address;
	private String phoneNumber;
	private long minOrderAmount;
	private String regionName;
	private String categoryName;
	private double averageRating;

	private Long ownerId;
	private String ownerEmail;
	private String ownerName;
	private String ownerRealName;
	private String ownerPhone;

	public GetStoreDetailResponse() {
	}

	public GetStoreDetailResponse(UUID storeId, String storeName, String description, String address, String phoneNumber, long minOrderAmount, String regionName, String categoryName, double averageRating, Long ownerId, String ownerEmail, String ownerName, String ownerRealName, String ownerPhone) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.description = description;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.minOrderAmount = minOrderAmount;
		this.regionName = regionName;
		this.categoryName = categoryName;
		this.averageRating = averageRating;
		this.ownerId = ownerId;
		this.ownerEmail = ownerEmail;
		this.ownerName = ownerName;
		this.ownerRealName = ownerRealName;
		this.ownerPhone = ownerPhone;
	}

	public static GetStoreDetailResponse from(Store store, double avgRating) {
		User user = store.getUser();

		return GetStoreDetailResponse.builder()
			.storeId(store.getStoreId())
			.storeName(store.getStoreName())
			.description(store.getDescription())
			.address(store.getAddress())
			.phoneNumber(store.getPhoneNumber())
			.minOrderAmount(store.getMinOrderAmount())
			.regionName(store.getRegion().getRegionName())
			.categoryName(store.getCategory().getCategoryName())
			.averageRating(avgRating)
			.ownerId(user.getUserId())
			.ownerEmail(user.getEmail())
			.ownerName(user.getUsername())
			.ownerRealName(user.getNickname())
			.ownerPhone(user.getPhoneNumber())
			.build();
	}
}