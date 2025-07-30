package app.domain.customer;

import app.domain.customer.model.UserAddressRepository;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.dto.response.AddUserAddressResponse;
import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.UserAddress;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import app.domain.customer.model.dto.request.AddUserAddressRequest;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public AddUserAddressResponse addUserAddress(AddUserAddressRequest request) {
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
        UserAddress savedAddress = userAddressRepository.save(address);
        return new AddUserAddressResponse(savedAddress.getAddressId());
    }
}