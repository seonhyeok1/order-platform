package app.domain.customer;

import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import app.domain.user.model.UserRepository;
import app.domain.user.model.UserAddressRepository;
import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;

import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

	private final UserAddressRepository userAddressRepository;
	private final UserRepository userRepository;

	@Transactional
	public AddCustomerAddressResponse addCustomerAddress(AddCustomerAddressRequest request) {
		// --- START: Input Validation ---
		if (request.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null.");
		}
		if (!StringUtils.hasText(request.alias())) {
			throw new IllegalArgumentException("Address alias is required.");
		}
		if (!StringUtils.hasText(request.address())) {
			throw new IllegalArgumentException("Address is required.");
		}
		// --- END: Input Validation ---

		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		// --- START: isDefault logic ---
		if (request.isDefault()) { // New address is intended to be default
			// Find existing default address for this user using repository
			userAddressRepository.findByUser_UserIdAndIsDefaultTrue(user.getUserId())
				.ifPresent(existingDefault -> {
					existingDefault.setDefault(false); // Unset existing default
					userAddressRepository.save(existingDefault); // Save the updated existing address
				});
		}
		// --- END: isDefault logic ---

		UserAddress address = UserAddress.builder()
			.user(user)
			.alias(request.alias())
			.address(request.address())
			.addressDetail(request.addressDetail())
			.isDefault(request.isDefault()) // Set isDefault based on request
			.build();

		try {
			UserAddress savedAddress = userAddressRepository.save(address);
			if (savedAddress.getAddressId() == null) {
				throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
			}
			return new AddCustomerAddressResponse(savedAddress.getAddressId());
		} catch (DataAccessException e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public List<GetCustomerAddressListResponse> getCustomerAddresses(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		return userAddressRepository.findAllByUserUserId(userId)
			.stream().map(GetCustomerAddressListResponse::from).toList();
	}


}