package app.domain.manager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.user.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 유저 상세 정보 응답 DTO")
public record GetCustomerDetailResponse(
	Long userId,
	String email,
	String userName,
	String name,
	String nickName,
	String phoneNumber,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<GetCustomerAddressListResponse> address
) {
	public static GetCustomerDetailResponse from(
		User user,
		List<GetCustomerAddressListResponse> addressList
	) {
		return new GetCustomerDetailResponse(
			user.getUserId(),
			user.getEmail(),
			user.getUsername(),
			user.getRealName(),
			user.getNickname(),
			user.getPhoneNumber(),
			user.getCreatedAt(),
			user.getUpdatedAt(),
			addressList
		);
	}
}