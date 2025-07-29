package app.domain.customer;

import app.domain.customer.model.UserAddressRepository;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.UserAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import app.domain.customer.model.dto.request.AddUserAddressRequest;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public UUID addAddress(Long userId, AddUserAddressRequest request) {
        // 1. userId로 User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        // 2. UserAddress 생성 (User 포함)
        UserAddress address = UserAddress.builder()
                .addressId(UUID.randomUUID())
                .user(user)
                .alias(request.alias())
                .address(request.address())
                .addressDetail(request.addressDetail())
                .isDefault(request.isDefault())
                .build();

        // 3. 저장 및 반환
        return userAddressRepository.save(address).getAddressId();
    }
}