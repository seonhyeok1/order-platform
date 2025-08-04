package app.domain.manager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.user.model.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerDetailResponse {

	private Long userId;
	private String email;
	private String userName;
	private String name;
	private String nickName;
	private String phoneNumber;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<GetCustomerAddressListResponse> address;

	public static GetCustomerDetailResponse from(User user, List<GetCustomerAddressListResponse> addressList) {
		return GetCustomerDetailResponse.builder()
			.userId(user.getUserId())
			.email(user.getEmail())
			.userName(user.getUsername())
			.name(user.getRealName())
			.nickName(user.getNickname())
			.phoneNumber(user.getPhoneNumber())
			.createdAt(user.getCreatedAt())
			.updatedAt(user.getUpdatedAt())
			.address(addressList)
			.build();
	}
}