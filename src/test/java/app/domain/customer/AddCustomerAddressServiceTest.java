package app.domain.customer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class AddCustomerAddressServiceTest {

	@InjectMocks
	private CustomerAddressService customerAddressService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserAddressRepository userAddressRepository;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.userId(1L)
			.username("tester")
			.email("testuser@example.com")
			.password("password123")
			.nickname("test_nickname")
			.realName("tester_name")
			.phoneNumber("01012345678")
			.userRole(UserRole.CUSTOMER)
			.build();

		// The userAddressRepository.save stubbing has been removed from here.
	}

	@Nested
	@DisplayName("주소 추가 성공 케이스")
	class SuccessCases {

		@Test
		@DisplayName("1. 정상 등록")
		void addUserAddress_NormalCase() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				testUser.getUserId(),
				"우리집",
				"서울시 강남구 테헤란로 212",
				"1501호",
				true
			);
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

			when(userAddressRepository.save(any(UserAddress.class)))
				.thenAnswer(invocation -> {
					UserAddress useraddress = invocation.getArgument(0);
					if (useraddress.getAddressId() == null) {
						return UserAddress.builder()
							.addressId(UUID.randomUUID())
							.user(useraddress.getUser())
							.alias(useraddress.getAlias())
							.address(useraddress.getAddress())
							.addressDetail(useraddress.getAddressDetail())
							.isDefault(useraddress.isDefault())
							.build();
					}
					return useraddress;
				});

			// When
			AddCustomerAddressResponse response = customerAddressService.addUserAddress(request);

			// Then
			assertNotNull(response);
			assertNotNull(response.address_id());

			// DB에 저장된 데이터 검증
			ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).save(captor.capture());
			UserAddress savedArg = captor.getValue();

			assertThat(savedArg.getUser().getUserId()).isEqualTo(testUser.getUserId());
			assertThat(savedArg.getAlias()).isEqualTo("우리집");
			assertThat(savedArg.getAddress()).isEqualTo("서울시 강남구 테헤란로 212");
			assertThat(savedArg.getAddressDetail()).isEqualTo("1501호");
			assertThat(savedArg.isDefault()).isTrue();
		}
	}

	@Nested
	@DisplayName("주소 추가 실패 케이스")
	class FailureCases {

		@Test
		@DisplayName("1. 존재하지 않는 사용자 ID로 요청 시")
		void addUserAddress_Fail_UserNotFound() {
			// Given
			long nonExistentUserId = 9999L;
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				nonExistentUserId,
				"우리집",
				"서울시 강남구 테헤란로 212",
				"1501호",
				false
			);
			when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

			// When & Then
			GeneralException ex = assertThrows(
				GeneralException.class,
				() -> customerAddressService.addUserAddress(request)
			);
			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);

			verify(userRepository, times(1)).findById(nonExistentUserId);
			verify(userAddressRepository, never()).save(any(UserAddress.class));
		}

		@Test
		@DisplayName("2. 필수 값인 user ID가 null일 경우")
		void addUserAddress_Fail_NullUserID() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				null,
				"집",
				"서울시 강남구 테헤란로 212",
				"101호",
				false
			);

			// When & Then
			assertThrows(IllegalArgumentException.class, () -> customerAddressService.addUserAddress(request));

			// This test now correctly has no unnecessary stubs.
			verify(userRepository, never()).findById(any());
			verify(userAddressRepository, never()).save(any(UserAddress.class));
		}

		@Test
		@DisplayName("3. 필수 값인 별명(alias)가 null일 경우")
		void addUserAddress_Fail_NullAlias() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				testUser.getUserId(),
				null,
				"서울시 강남구 테헤란로 212",
				"101호",
				false
			);

			// When & Then
			assertThrows(IllegalArgumentException.class, () -> customerAddressService.addUserAddress(request));
			verify(userRepository, never()).findById(any());
			verify(userAddressRepository, never()).save(any(UserAddress.class));
		}

		@Test
		@DisplayName("4. 필수 값인 주소(address)가 null일 경우")
		void addUserAddress_Fail_NullAddress() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				testUser.getUserId(),
				"집",
				null,
				"101호",
				false
			);

			// When & Then
			assertThrows(IllegalArgumentException.class, () -> customerAddressService.addUserAddress(request));
			verify(userRepository, never()).findById(any());
			verify(userAddressRepository, never()).save(any(UserAddress.class));
		}
	}

	@Nested
	@DisplayName("주소 추가 예외 케이스")
	class ExceptionCases {

		@Test
		@DisplayName("1. DB 저장 실패 시 _INTERNAL_SERVER_ERROR 예외 발생")
		void addUserAddress_Fail_DbSaveError() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				testUser.getUserId(),
				"우리집",
				"서울시",
				"101동",
				true
			);

			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

			when(userAddressRepository.save(any(UserAddress.class)))
				.thenThrow(new DataAccessResourceFailureException("Simulated DB error"));

			// When & Then
			GeneralException ex = assertThrows(
				GeneralException.class,
				() -> customerAddressService.addUserAddress(request)
			);

			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus._INTERNAL_SERVER_ERROR);

			// Verify interactions
			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).save(any(UserAddress.class));
		}

		@Test
		@DisplayName("2. UUID 생성 실패 시 _INTERNAL_SERVER_ERROR 예외 발생")
		void addUserAddress_Fail_UuidNotAssigned() {
			// Given
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.save(any(UserAddress.class)))
				.thenAnswer(inv -> {
					UserAddress ua = inv.getArgument(0);
					// addressId 를 의도적으로 null 로 반환
					return UserAddress.builder()
						.addressId(null)
						.user(ua.getUser()).alias(ua.getAlias())
						.address(ua.getAddress()).addressDetail(ua.getAddressDetail())
						.isDefault(ua.isDefault())
						.build();
				});

			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				testUser.getUserId(),
				"우리집",
				"서울시",
				"101동",
				true
			);

			// When & Then
			GeneralException ex = assertThrows(
				GeneralException.class,
				() -> customerAddressService.addUserAddress(request)
			);
			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus._INTERNAL_SERVER_ERROR);
			verify(userAddressRepository, times(1)).save(any(UserAddress.class));
		}
	}
}