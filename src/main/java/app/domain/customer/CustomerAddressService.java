package app.domain.customer;

import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.UserRepository;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import app.domain.customer.dto.request.AddCustomerAddressRequest;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

	private final UserAddressRepository userAddressRepository;
	private final UserRepository userRepository;

	public AddCustomerAddressResponse addUserAddress(AddCustomerAddressRequest request) {
		// --- START: Input Validation ---
		// A robust service should always validate its inputs first.
		if (request.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null.");
		}
		// StringUtils.hasText is excellent for checking for null, empty, and whitespace-only strings.
		if (!StringUtils.hasText(request.alias())) {
			throw new IllegalArgumentException("Address alias is required.");
		}
		if (!StringUtils.hasText(request.address())) {
			throw new IllegalArgumentException("Address is required.");
		}
		// --- END: Input Validation ---

		// 1. userId로 User 엔티티 조회
		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		// 2. UserAddress 생성 (User 포함)
		UserAddress address = UserAddress.builder()
			.user(user)
			.alias(request.alias())
			.address(request.address())
			.addressDetail(request.addressDetail())
			.isDefault(request.isDefault())
			.build();

		// 3. 저장 및 반환
		try {
			UserAddress savedAddress = userAddressRepository.save(address);
			if (savedAddress.getAddressId() == null) {
				// This indicates a failure in persistence or ID generation.
				throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
			}
			return new AddCustomerAddressResponse(savedAddress.getAddressId());
		} catch (DataAccessException e) {
			// Catch persistence-layer exceptions and wrap them in a service-layer exception.
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}